#!/usr/bin/env python3
import argparse
import datetime
import json
from dataclasses import dataclass
from pathlib import Path
from typing import Any

import yaml


STATE_VERSION = 1
FAILURE_STATUS = "no response"
RESPONDING_STATUSES = {"compliant", "partially compliant", "non-compliant"}


@dataclass(frozen=True)
class IdentifierEvaluation:
    did: str
    method: str | None
    method_status: str | None
    responding: bool
    reason: str

    def to_dict(self) -> dict[str, Any]:
        return {
            "did": self.did,
            "method": self.method,
            "methodStatus": self.method_status,
            "responding": self.responding,
            "reason": self.reason,
        }


def load_yaml(path: str | Path) -> dict[str, Any]:
    with open(path, encoding="utf-8") as file:
        return yaml.safe_load(file)


def load_json(path: str | Path) -> dict[str, Any]:
    path = Path(path)
    if not path.exists():
        return {}

    with open(path, encoding="utf-8") as file:
        return json.load(file)


def write_json(path: str | Path, value: dict[str, Any]) -> None:
    with open(path, "w", encoding="utf-8") as file:
        json.dump(value, file, indent=2, sort_keys=True)
        file.write("\n")


def extract_method(did: str) -> str | None:
    parts = did.split(":")
    if len(parts) < 3 or parts[0] != "did":
        return None
    return parts[1]


def normalize_status(status: Any) -> str | None:
    if status is None:
        return None
    return str(status).strip().lower()


def find_identifier_payload(method_result: dict[str, Any], did: str) -> dict[str, Any] | None:
    for identifier_result in method_result.get("identifiers", []):
        if not isinstance(identifier_result, dict):
            continue
        if did in identifier_result:
            value = identifier_result[did]
            return value if isinstance(value, dict) else {}
    return None


def evaluate_identifier(did_lint_result: dict[str, Any], did: str) -> IdentifierEvaluation:
    method = extract_method(did)
    if method is None:
        return IdentifierEvaluation(did, None, None, False, "invalid did")

    method_key = f"did:{method}"
    method_result = did_lint_result.get(method_key)
    if not isinstance(method_result, dict):
        return IdentifierEvaluation(did, method, None, False, "missing method result")

    method_status = normalize_status(method_result.get("status"))
    identifier_payload = find_identifier_payload(method_result, did)
    if identifier_payload is None:
        return IdentifierEvaluation(did, method, method_status, False, "missing identifier result")

    identifier_error = normalize_status(identifier_payload.get("error"))
    if method_status == FAILURE_STATUS or identifier_error == FAILURE_STATUS:
        return IdentifierEvaluation(did, method, method_status, False, FAILURE_STATUS)

    if method_status in RESPONDING_STATUSES:
        return IdentifierEvaluation(did, method, method_status, True, "responding")

    # Unknown DIDLint statuses should not trigger auto-disable by default.
    return IdentifierEvaluation(did, method, method_status, True, "unknown status treated as responding")


def driver_entries(config: dict[str, Any]) -> list[dict[str, Any]]:
    return config.get("uniresolver", {}).get("drivers", [])


def evaluate_entries(config: dict[str, Any], did_lint_result: dict[str, Any]) -> list[dict[str, Any]]:
    evaluations = []

    for driver in driver_entries(config):
        entry_id = driver.get("id")
        test_identifiers = [
            did for did in driver.get("testIdentifiers", [])
            if isinstance(did, str) and did.startswith("did:")
        ]

        identifier_results = [evaluate_identifier(did_lint_result, did) for did in test_identifiers]
        all_unresponsive = bool(identifier_results) and all(not result.responding for result in identifier_results)

        evaluations.append({
            "id": entry_id,
            "testIdentifiers": test_identifiers,
            "identifierResults": [result.to_dict() for result in identifier_results],
            "unresponsive": all_unresponsive,
        })

    return evaluations


def previous_entry_state(previous_state: dict[str, Any], entry_id: str) -> dict[str, Any]:
    entries = previous_state.get("entries", {})
    state = entries.get(entry_id, {})
    return state if isinstance(state, dict) else {}


def transition_state(
    entry_evaluations: list[dict[str, Any]],
    previous_state: dict[str, Any],
    threshold: int,
    generated_at: str,
) -> tuple[dict[str, Any], dict[str, Any]]:
    state_entries: dict[str, dict[str, Any]] = {}
    report_entries: list[dict[str, Any]] = []
    disabled_entries: list[str] = []

    for entry in entry_evaluations:
        entry_id = entry["id"]
        previous = previous_entry_state(previous_state, entry_id)
        previous_failures = int(previous.get("consecutiveFailures", 0))
        previous_disabled = bool(previous.get("disabled", False))

        if entry["unresponsive"]:
            consecutive_failures = previous_failures + 1
            disabled = consecutive_failures >= threshold
        else:
            consecutive_failures = 0
            disabled = False

        if disabled:
            disabled_entries.append(entry_id)

        state_entries[entry_id] = {
            "consecutiveFailures": consecutive_failures,
            "disabled": disabled,
        }

        report_entry = {
            **entry,
            "previousConsecutiveFailures": previous_failures,
            "previousDisabled": previous_disabled,
            "consecutiveFailures": consecutive_failures,
            "disabled": disabled,
        }
        report_entries.append(report_entry)

    state = {
        "version": STATE_VERSION,
        "updatedAt": generated_at,
        "entries": state_entries,
    }
    report = {
        "version": STATE_VERSION,
        "generatedAt": generated_at,
        "threshold": threshold,
        "disabledEntries": disabled_entries,
        "entries": report_entries,
    }

    return state, report


def disabled_entries_text(disabled_entries: list[str]) -> str:
    return ",".join(disabled_entries)


def utc_now() -> str:
    return datetime.datetime.now(datetime.timezone.utc).replace(microsecond=0).isoformat()


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(description="Compute report-only disabled resolver entries from DIDLint results.")
    parser.add_argument("--config", required=True, help="Path to uni-resolver-web application.yml.")
    parser.add_argument("--did-lint-result", required=True, help="Path to DIDLint result.json.")
    parser.add_argument("--state", required=False, help="Path to previous entry disable state JSON.")
    parser.add_argument("--state-out", required=True, help="Path to write next state JSON.")
    parser.add_argument("--report-out", required=True, help="Path to write report JSON.")
    parser.add_argument("--disabled-out", required=True, help="Path to write comma-separated disabled entry ids.")
    parser.add_argument("--threshold", type=int, default=3, help="Consecutive unresponsive runs before disable.")
    parser.add_argument("--generated-at", default=None, help="Override generated timestamp for deterministic tests.")
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    if args.threshold < 1:
        raise ValueError("--threshold must be greater than 0")

    config = load_yaml(args.config)
    did_lint_result = load_json(args.did_lint_result)
    previous_state = load_json(args.state) if args.state else {}
    generated_at = args.generated_at or utc_now()

    entry_evaluations = evaluate_entries(config, did_lint_result)
    state, report = transition_state(entry_evaluations, previous_state, args.threshold, generated_at)

    write_json(args.state_out, state)
    write_json(args.report_out, report)
    with open(args.disabled_out, "w", encoding="utf-8") as file:
        file.write(disabled_entries_text(report["disabledEntries"]))
        file.write("\n")

    print(f"Evaluated {len(report['entries'])} resolver entries.")
    print(f"Report-only disabled entries after this run: {disabled_entries_text(report['disabledEntries']) or '(none)'}")


if __name__ == "__main__":
    main()
