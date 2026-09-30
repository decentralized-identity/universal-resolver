#!/usr/bin/env python3
"""Check the Universal Resolver configuration files of a pull request.

Checked files (relative to --path, or downloaded from GitHub with --pr):
  - uni-resolver-web/src/main/resources/application.yml
  - docker-compose.yml
  - .env
  - README.md

The files are only parsed, never executed. Images referenced in docker-compose.yml
are resolved against their registries anonymously to make sure they can be pulled
without a login. The result is written as a Markdown
report and exposed as GitHub Action outputs (`result`, `errors`, `warnings`,
`report`) when running inside GitHub Actions.
"""

import argparse
import json
import os
import re
import socket
import subprocess
import sys
import tempfile
import urllib.error
import urllib.parse
import urllib.request
import uuid
from concurrent.futures import ThreadPoolExecutor
from pathlib import Path
from typing import Any

import yaml

APPLICATION_YML = "uni-resolver-web/src/main/resources/application.yml"
DOCKER_COMPOSE = "docker-compose.yml"
DOT_ENV = ".env"
README = "README.md"
FILES = [APPLICATION_YML, DOCKER_COMPOSE, DOT_ENV, README]

# Keys bound by uniresolver.web.config.DriverConfigs.DriverConfig.
# Spring ignores unknown keys silently, so a typo simply disables the setting.
DRIVER_KEYS = {
    "pattern",
    "url",
    "propertiesEndpoint",
    "supportsOptions",
    "supportsDereference",
    "acceptHeaderValue",
    "acceptHeaderValueDereference",
    "testIdentifiers",
    "traits",
}
DRIVER_STRING_KEYS = DRIVER_KEYS - {"testIdentifiers", "traits"}

# ${VAR} or ${VAR:default} as used in application.yml driver URLs
SPRING_PLACEHOLDER = re.compile(r"^\$\{([A-Za-z0-9_.\-]+)(?::(.*))?\}$")
# ${VAR}, ${VAR:-default}, ${VAR-default}, ${VAR:?err}, ... in docker-compose.yml ($$ escapes)
COMPOSE_VARIABLE = re.compile(r"(?<!\$)\$\{([A-Za-z_][A-Za-z0-9_]*)")
DOTENV_LINE = re.compile(r"^(?:export\s+)?([A-Za-z_][A-Za-z0-9_.\-]*)\s*=(.*)$")
COMPOSE_LOG_LINE = re.compile(r'level=(\w+)\s+msg="(.*)"$')
# docker compose warnings that are not reported
IGNORED_COMPOSE_WARNINGS = [
    re.compile(r"the attribute `version` is obsolete"),
]

REGISTRY_TIMEOUT = 20
MANIFEST_ACCEPT = ", ".join([
    "application/vnd.oci.image.index.v1+json",
    "application/vnd.oci.image.manifest.v1+json",
    "application/vnd.docker.distribution.manifest.list.v2+json",
    "application/vnd.docker.distribution.manifest.v2+json",
])
USER_AGENT = "universal-resolver-pr-check"
DEFAULT_REPOSITORY = "decentralized-identity/universal-resolver"


class Report:
    def __init__(self, subject: str | None = None):
        self.subject = subject
        self.results: dict[str, dict[str, list[str]]] = {name: {"errors": [], "warnings": []} for name in FILES}

    def error(self, file: str, message: str) -> None:
        self.results[file]["errors"].append(message)

    def warning(self, file: str, message: str) -> None:
        self.results[file]["warnings"].append(message)

    @property
    def error_count(self) -> int:
        return sum(len(r["errors"]) for r in self.results.values())

    @property
    def warning_count(self) -> int:
        return sum(len(r["warnings"]) for r in self.results.values())

    def markdown(self) -> str:
        lines = []
        if self.error_count:
            lines.append(f"### ❌ PR check failed: {self.error_count} error(s), {self.warning_count} warning(s)")
        elif self.warning_count:
            lines.append(f"### ✅ PR check passed with {self.warning_count} warning(s)")
        else:
            lines.append("### ✅ PR check passed")
        if self.subject:
            lines += ["", self.subject]
        lines += ["", "| File | Status |", "|------|--------|"]
        for name, result in self.results.items():
            if result["errors"]:
                status = f"❌ {len(result['errors'])} error(s)"
            elif result["warnings"]:
                status = f"⚠️ {len(result['warnings'])} warning(s)"
            else:
                status = "✅ OK"
            lines.append(f"| `{name}` | {status} |")
        for name, result in self.results.items():
            if not (result["errors"] or result["warnings"]):
                continue
            lines += ["", f"#### `{name}`", ""]
            lines += [f"- ❌ {msg}" for msg in result["errors"]]
            lines += [f"- ⚠️ {msg}" for msg in result["warnings"]]
        return "\n".join(lines) + "\n"


class UniqueKeyLoader(yaml.SafeLoader):
    """SafeLoader that rejects duplicate mapping keys.

    PyYAML silently keeps the last value, while Spring (SnakeYAML) refuses to
    start and docker compose refuses to load the file.
    """

    def construct_mapping(self, node, deep=False):
        seen = {}
        for key_node, _ in node.value:
            if key_node.tag == "tag:yaml.org,2002:merge":
                continue
            key = self.construct_object(key_node, deep=deep)
            if key in seen:
                raise yaml.constructor.ConstructorError(
                    "while constructing a mapping", node.start_mark,
                    f"found duplicate key '{key}' (first defined on line {seen[key] + 1})",
                    key_node.start_mark)
            seen[key] = key_node.start_mark.line
        return super().construct_mapping(node, deep=deep)


def describe_yaml_error(e: yaml.YAMLError) -> str:
    mark = getattr(e, "problem_mark", None)
    problem = getattr(e, "problem", None) or str(e)
    location = f"line {mark.line + 1}, column {mark.column + 1}: " if mark else ""
    return f"Invalid YAML at {location}{problem}"


def read_text(report: Report, root: Path, name: str) -> str | None:
    path = root / name
    if not path.is_file():
        report.error(name, "File not found")
        return None
    try:
        return path.read_text(encoding="utf-8")
    except UnicodeDecodeError as e:
        report.error(name, f"File is not valid UTF-8: {e}")
        return None


def load_yaml(report: Report, name: str, text: str | None) -> Any:
    if text is None:
        return None
    if "\t" in text:
        for number, line in enumerate(text.splitlines(), 1):
            if re.match(r"^ *\t", line):
                report.error(name, f"Line {number}: tab character used for indentation")
                return None
    try:
        return yaml.load(text, Loader=UniqueKeyLoader)
    except yaml.YAMLError as e:
        report.error(name, describe_yaml_error(e))
        return None


def check_application_yml(report: Report, config: Any) -> list[dict]:
    name = APPLICATION_YML
    if config is None:
        return []
    if not isinstance(config, dict):
        report.error(name, "Top level must be a mapping")
        return []
    drivers = (config.get("uniresolver") or {}).get("drivers") if isinstance(config.get("uniresolver"), dict) else None
    if not isinstance(drivers, list) or not drivers:
        report.error(name, "`uniresolver.drivers` must be a non-empty list")
        return []

    seen_patterns: dict[str, int] = {}
    valid_drivers = []
    for index, driver in enumerate(drivers):
        if not isinstance(driver, dict):
            report.error(name, f"Driver #{index + 1} must be a mapping, found `{type(driver).__name__}`")
            continue
        label = f"Driver #{index + 1}"
        pattern = driver.get("pattern")
        if isinstance(pattern, str):
            label += f" (`{pattern}`)"

        for key in ("pattern", "url"):
            value = driver.get(key)
            if not isinstance(value, str) or not value.strip():
                report.error(name, f"{label}: required key `{key}` is missing or empty")
        for key in sorted(set(driver) - DRIVER_KEYS):
            report.warning(name, f"{label}: unknown key `{key}` is ignored (typo? allowed: {', '.join(sorted(DRIVER_KEYS))})")
        for key in sorted(DRIVER_STRING_KEYS & set(driver)):
            value = driver[key]
            if value is not None and not isinstance(value, (str, bool, int)):
                report.error(name, f"{label}: `{key}` must be a scalar value, found `{type(value).__name__}`")

        test_identifiers = driver.get("testIdentifiers")
        if test_identifiers is not None and (
                not isinstance(test_identifiers, list) or not all(isinstance(t, str) for t in test_identifiers)):
            report.error(name, f"{label}: `testIdentifiers` must be a list of strings")
            test_identifiers = None
        if "traits" in driver and driver["traits"] is not None and not isinstance(driver["traits"], dict):
            report.error(name, f"{label}: `traits` must be a mapping")

        if not isinstance(pattern, str):
            continue
        if pattern in seen_patterns:
            report.error(name, f"{label}: duplicate pattern, already used by driver #{seen_patterns[pattern] + 1}")
        seen_patterns.setdefault(pattern, index)
        try:
            regex = re.compile(pattern)
        except re.error as e:
            # Python and Java regex dialects differ slightly, so don't fail hard
            report.warning(name, f"{label}: pattern could not be compiled ({e}); please verify it is a valid Java regex")
            regex = None
        if regex and test_identifiers:
            for identifier in test_identifiers:
                if not regex.search(identifier):
                    report.warning(name, f"{label}: test identifier `{identifier}` does not match the pattern")
        if isinstance(driver.get("url"), str):
            valid_drivers.append(driver)
    return valid_drivers


def run_compose_config(report: Report, root: Path) -> dict | None:
    """Validate with `docker compose config` and return the resolved config, or None."""
    name = DOCKER_COMPOSE
    command = ["docker-compose", "--project-directory", str(root), "-f", str(root / DOCKER_COMPOSE)]
    if (root / DOT_ENV).is_file():
        command += ["--env-file", str(root / DOT_ENV)]
    command += ["config", "--format", "json"]
    try:
        result = subprocess.run(command, capture_output=True, text=True, timeout=120,
                                env={"PATH": os.environ.get("PATH", ""), "HOME": "/tmp"})
    except FileNotFoundError:
        report.warning(name, "`docker-compose` binary not found, skipped compose validation")
        return None
    except subprocess.TimeoutExpired:
        report.error(name, "`docker compose config` timed out")
        return None

    for line in (result.stderr or "").splitlines():
        line = line.strip()
        if not line:
            continue
        match = COMPOSE_LOG_LINE.search(line)
        level, message = (match.group(1), match.group(2)) if match else ("error", line)
        message = message.replace(str(root) + "/", "")
        if level in ("warning", "warn", "info", "debug"):
            if not any(ignored.search(message) for ignored in IGNORED_COMPOSE_WARNINGS):
                report.warning(name, f"docker compose: {message}")
        else:
            report.error(name, f"docker compose: {message}")
    if result.returncode != 0:
        if not report.results[name]["errors"]:
            report.error(name, f"`docker compose config` failed with exit code {result.returncode}")
        return None
    try:
        return json.loads(result.stdout)
    except json.JSONDecodeError:
        return None


def check_docker_compose(report: Report, root: Path, compose: Any, env_keys: set[str] | None) -> dict | None:
    name = DOCKER_COMPOSE
    if compose is None:
        return None
    if not isinstance(compose, dict) or not isinstance(compose.get("services"), dict) or not compose["services"]:
        report.error(name, "`services` must be a non-empty mapping")
        return None
    resolved = run_compose_config(report, root)

    for service_name, service in compose["services"].items():
        if not isinstance(service, dict):
            report.error(name, f"Service `{service_name}` must be a mapping")
        elif not service.get("image") and not service.get("build"):
            report.error(name, f"Service `{service_name}` has neither `image` nor `build`")

    # Prefer the image names resolved by docker compose (variables interpolated)
    services = (resolved or {}).get("services") or compose["services"]
    images: dict[str, list[str]] = {}
    for service_name, service in services.items():
        image = service.get("image") if isinstance(service, dict) else None
        if isinstance(image, str) and image.strip() and "$" not in image:
            images.setdefault(image.strip(), []).append(service_name)
    check_images(report, images)

    if env_keys is not None:
        text = (root / DOCKER_COMPOSE).read_text(encoding="utf-8")
        for variable in sorted(set(COMPOSE_VARIABLE.findall(text)) - env_keys):
            report.warning(name, f"Variable `${{{variable}}}` is not defined in `{DOT_ENV}`")
    return compose


def parse_image_reference(image: str) -> tuple[str, str, str]:
    """Split an image reference into (registry host, repository, tag or digest)."""
    name, digest = image.split("@", 1) if "@" in image else (image, None)
    tag = None
    last = name.rsplit("/", 1)[-1]
    if ":" in last:
        name, tag = name.rsplit(":", 1)
    first, _, rest = name.partition("/")
    if rest and ("." in first or ":" in first or first == "localhost"):
        registry, repository = first, rest
    else:
        registry, repository = "docker.io", name
    if registry in ("docker.io", "index.docker.io"):
        registry = "registry-1.docker.io"
        if "/" not in repository:
            repository = f"library/{repository}"
    return registry, repository, digest or tag or "latest"


def parse_auth_challenge(header: str | None) -> tuple[str, dict[str, str]]:
    scheme, _, params = (header or "").partition(" ")
    return scheme.lower(), dict(re.findall(r'(\w+)="([^"]*)"', params))


def registry_request(url: str, method: str = "HEAD", token: str | None = None,
                     accept: str = MANIFEST_ACCEPT) -> tuple[int, Any, bytes]:
    headers = {"User-Agent": USER_AGENT, "Accept": accept}
    if token:
        headers["Authorization"] = f"Bearer {token}"
    request = urllib.request.Request(url, method=method, headers=headers)
    try:
        with urllib.request.urlopen(request, timeout=REGISTRY_TIMEOUT) as response:
            return response.status, response.headers, response.read() if method == "GET" else b""
    except urllib.error.HTTPError as e:
        return e.code, e.headers, b""


def check_image(image: str) -> tuple[str, str] | None:
    """Resolve the image manifest without credentials. Returns (level, message) or None if pullable."""
    registry, repository, reference = parse_image_reference(image)
    try:
        manifest_url = f"https://{registry}/v2/{repository}/manifests/{reference}"
        status, headers, _ = registry_request(manifest_url)
        if status == 405:  # registries without HEAD support
            status, headers, _ = registry_request(manifest_url, method="GET")

        if status == 401:
            scheme, challenge = parse_auth_challenge(headers.get("WWW-Authenticate"))
            if scheme != "bearer" or "realm" not in challenge:
                return "error", f"requires a login at `{registry}`"
            query = {"scope": f"repository:{repository}:pull"}
            if "service" in challenge:
                query["service"] = challenge["service"]
            token_status, _, body = registry_request(
                f"{challenge['realm']}?{urllib.parse.urlencode(query)}", method="GET", accept="application/json")
            if token_status != 200:
                return "error", f"does not exist or requires a login at `{registry}`"
            data = json.loads(body or b"{}")
            token = data.get("token") or data.get("access_token")
            status, _, _ = registry_request(manifest_url, token=token)

        if status == 200:
            return None
        if status in (401, 403):
            return "error", "does not exist or requires a login"
        if status == 404:
            return "error", f"not found (tag or digest `{reference}` does not exist)"
        if status == 429 or status >= 500:
            return "warning", f"could not be verified, `{registry}` returned HTTP {status}"
        return "error", f"could not be resolved, `{registry}` returned HTTP {status}"
    except (urllib.error.URLError, TimeoutError, OSError, ValueError) as e:
        reason = getattr(e, "reason", e)
        if isinstance(reason, socket.gaierror):
            return "error", f"registry host `{registry}` not found"
        # Timeouts and connection problems may be temporary
        return "warning", f"could not be verified ({reason})"


def check_images(report: Report, images: dict[str, list[str]]) -> None:
    """images: {image reference: [service names]}"""
    with ThreadPoolExecutor(max_workers=8) as executor:
        results = dict(zip(images, executor.map(check_image, images)))
    for image, result in sorted(results.items()):
        if result is None:
            continue
        level, message = result
        services = ", ".join(f"`{s}`" for s in images[image])
        text = f"Image `{image}` (service {services}) {message}"
        if level == "error":
            report.error(DOCKER_COMPOSE, text)
        else:
            report.warning(DOCKER_COMPOSE, text)


def check_dot_env(report: Report, text: str | None) -> set[str] | None:
    name = DOT_ENV
    if text is None:
        return None
    keys: dict[str, int] = {}
    for number, raw in enumerate(text.splitlines(), 1):
        line = raw.strip()
        if not line or line.startswith("#"):
            continue
        match = DOTENV_LINE.match(line)
        if not match:
            report.error(name, f"Line {number}: expected `KEY=value`, found `{line[:80]}`")
            continue
        key, value = match.group(1), match.group(2).strip()
        if raw != raw.lstrip():
            report.warning(name, f"Line {number}: leading whitespace before `{key}`")
        if value[:1] in ("'", '"') and (len(value) < 2 or not value.endswith(value[0])):
            report.error(name, f"Line {number}: unterminated quoted value for `{key}`")
        if key in keys:
            report.warning(name, f"Line {number}: `{key}` is already defined on line {keys[key]}, the last value wins")
        keys.setdefault(key, number)
    return set(keys)


def check_readme(report: Report, text: str | None) -> None:
    name = README
    if text is None:
        return None
    lines = text.splitlines()
    try:
        start = next(i for i, line in enumerate(lines) if re.match(r"^##\s+Drivers\s*$", line))
    except StopIteration:
        report.error(name, "Section `## Drivers` not found")
        return None

    table = []
    for index in range(start + 1, len(lines)):
        line = lines[index]
        if line.startswith("#"):
            break
        if line.lstrip().startswith("|"):
            table.append((index + 1, line))
        elif table and line.strip():
            report.error(name, f"Line {index + 1}: driver table interrupted by a non-table line")
            break
        elif table:
            break
    if len(table) < 3:
        report.error(name, "Driver table in section `## Drivers` not found or empty")
        return None

    def cells(line: str) -> list[str]:
        # Split on unescaped pipes; the leading pipe is required, the trailing one optional (like GitHub)
        line = line.strip()
        if line.endswith("|") and not line.endswith("\\|"):
            line = line[:-1]
        return re.split(r"(?<!\\)\|", line)[1:]

    header_number, header = table[0]
    columns = len(cells(header))
    separator_number, separator = table[1]
    if not all(re.fullmatch(r"\s*:?-+:?\s*", c) for c in cells(separator)) or len(cells(separator)) != columns:
        report.error(name, f"Line {separator_number}: invalid table separator row")
    for number, line in table[2:]:
        if len(cells(line)) != columns:
            report.error(name, f"Line {number}: table row has {len(cells(line))} columns, expected {columns}")


def check_cross_references(report: Report, drivers: list[dict], compose: dict | None) -> None:
    services = compose.get("services", {}) if compose else {}

    for driver in drivers:
        label = f"`{driver.get('pattern')}`"
        placeholder = SPRING_PLACEHOLDER.match(driver["url"].strip())
        if not placeholder or not services:
            continue
        default = placeholder.group(2) or ""
        host = re.match(r"^https?://([^:/]+)", default)
        if host and "." not in host.group(1) and host.group(1) not in ("localhost",) and host.group(1) not in services:
            report.warning(DOCKER_COMPOSE, f"Service `{host.group(1)}` referenced by driver {label} in `{APPLICATION_YML}` is not defined")


def set_outputs(report: Report, markdown: str) -> None:
    output_file = os.environ.get("GITHUB_OUTPUT")
    if output_file:
        delimiter = f"EOF_{uuid.uuid4().hex}"
        with open(output_file, "a", encoding="utf-8") as f:
            f.write(f"result={'failure' if report.error_count else 'success'}\n")
            f.write(f"errors={report.error_count}\n")
            f.write(f"warnings={report.warning_count}\n")
            f.write(f"report<<{delimiter}\n{markdown}{delimiter}\n")
    summary_file = os.environ.get("GITHUB_STEP_SUMMARY")
    if summary_file:
        with open(summary_file, "a", encoding="utf-8") as f:
            f.write(markdown)


def download_pr_files(repository: str, pr_number: int, target: Path) -> str:
    """Download the checked files of a pull request's head commit into target. Returns a description of the PR."""
    request = urllib.request.Request(
        f"https://api.github.com/repos/{repository}/pulls/{pr_number}",
        headers={"User-Agent": USER_AGENT, "Accept": "application/vnd.github+json"})
    with urllib.request.urlopen(request, timeout=REGISTRY_TIMEOUT) as response:
        pr = json.load(response)
    sha = pr["head"]["sha"]
    for name in FILES:
        # PR commits (also from forks) are served by the base repository
        url = f"https://raw.githubusercontent.com/{repository}/{sha}/{urllib.parse.quote(name)}"
        try:
            with urllib.request.urlopen(urllib.request.Request(url, headers={"User-Agent": USER_AGENT}),
                                        timeout=REGISTRY_TIMEOUT) as response:
                content = response.read()
        except urllib.error.HTTPError as e:
            if e.code == 404:
                continue  # reported as "File not found"
            raise
        (target / name).parent.mkdir(parents=True, exist_ok=True)
        (target / name).write_bytes(content)
    return f"PR [#{pr['number']}]({pr['html_url']}) \"{pr['title']}\" at commit `{sha[:8]}`"


def main() -> int:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    source = parser.add_mutually_exclusive_group()
    source.add_argument("--path", default=".", help="Repository root containing the files to check (default: .)")
    source.add_argument("--pr", type=int, metavar="NUMBER",
                        help="Download the files of this pull request from GitHub and check them")
    parser.add_argument("--repository", default=DEFAULT_REPOSITORY,
                        help=f"GitHub repository of the pull request (default: {DEFAULT_REPOSITORY})")
    parser.add_argument("--report", help="Optional file to write the Markdown report to")
    args = parser.parse_args()

    if args.pr is None:
        return run_checks(Path(args.path).resolve(), args.report)
    with tempfile.TemporaryDirectory(prefix="pr-check-") as directory:
        try:
            subject = download_pr_files(args.repository, args.pr, Path(directory))
        except (urllib.error.URLError, OSError, KeyError, ValueError) as e:
            print(f"Could not download PR #{args.pr} from {args.repository}: {e}", file=sys.stderr)
            return 2
        return run_checks(Path(directory), args.report, subject)


def run_checks(root: Path, report_file: str | None, subject: str | None = None) -> int:
    report = Report(subject)
    config = load_yaml(report, APPLICATION_YML, read_text(report, root, APPLICATION_YML))
    drivers = check_application_yml(report, config)
    env_keys = check_dot_env(report, read_text(report, root, DOT_ENV))
    compose = check_docker_compose(report, root, load_yaml(report, DOCKER_COMPOSE, read_text(report, root, DOCKER_COMPOSE)), env_keys)
    check_readme(report, read_text(report, root, README))
    check_cross_references(report, drivers, compose)

    markdown = report.markdown()
    print(markdown)
    if report_file:
        Path(report_file).write_text(markdown, encoding="utf-8")
    set_outputs(report, markdown)
    # Exit code stays 0 so the workflow can publish the report; use the `result` output to fail.
    return 0


if __name__ == "__main__":
    sys.exit(main())
