#!/bin/sh
# The checked files come from untrusted pull requests. Start the check with a minimal
# environment so runner tokens (e.g. ACTIONS_RUNTIME_TOKEN) are not reachable from
# docker compose or /proc/1/environ.
exec env -i \
  PATH="$PATH" \
  HOME=/tmp \
  GITHUB_OUTPUT="$GITHUB_OUTPUT" \
  GITHUB_STEP_SUMMARY="$GITHUB_STEP_SUMMARY" \
  python /pr-check/pr_check.py "$@"
