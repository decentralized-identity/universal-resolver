import unittest

from entry_disable_policy import (
    evaluate_entries,
    evaluate_identifier,
    transition_state,
)


class EntryDisablePolicyTest(unittest.TestCase):

    def test_no_response_is_unresponsive(self):
        result = evaluate_identifier(did_lint_result("no response"), "did:example:123")

        self.assertFalse(result.responding)
        self.assertEqual(result.reason, "no response")

    def test_non_compliant_is_responding(self):
        result = evaluate_identifier(did_lint_result("non-compliant"), "did:example:123")

        self.assertTrue(result.responding)
        self.assertEqual(result.reason, "responding")

    def test_partially_compliant_is_responding(self):
        result = evaluate_identifier(did_lint_result("partially compliant"), "did:example:123")

        self.assertTrue(result.responding)
        self.assertEqual(result.reason, "responding")

    def test_missing_identifier_result_is_unresponsive(self):
        result = evaluate_identifier({
            "did:example": {
                "status": "compliant",
                "identifiers": [],
            }
        }, "did:example:123")

        self.assertFalse(result.responding)
        self.assertEqual(result.reason, "missing identifier result")

    def test_maps_application_config_entries_by_own_test_identifiers(self):
        entries = evaluate_entries(config_with_entries(), {
            "did:example": {
                "status": "no response",
                "identifiers": [
                    {"did:example:one": {"error": "no response"}},
                    {"did:example:two": {"error": "no response"}},
                ],
            },
            "did:web": {
                "status": "compliant",
                "identifiers": [
                    {"did:web:example.com": {"valid": True}},
                ],
            },
        })

        self.assertEqual([entry["id"] for entry in entries], ["entry-a", "entry-b", "entry-overlap"])
        self.assertTrue(entries[0]["unresponsive"])
        self.assertFalse(entries[1]["unresponsive"])
        self.assertFalse(entries[2]["unresponsive"])

    def test_third_consecutive_failure_disables_entry(self):
        entries = [{"id": "entry-a", "unresponsive": True, "identifierResults": [], "testIdentifiers": []}]
        previous_state = {
            "entries": {
                "entry-a": {
                    "consecutiveFailures": 2,
                    "disabled": False,
                }
            }
        }

        state, report = transition_state(entries, previous_state, threshold=3, generated_at="2026-01-01T00:00:00+00:00")

        self.assertEqual(state["entries"]["entry-a"]["consecutiveFailures"], 3)
        self.assertTrue(state["entries"]["entry-a"]["disabled"])
        self.assertEqual(report["disabledEntries"], ["entry-a"])

    def test_first_and_second_failure_increment_without_disabling(self):
        entries = [{"id": "entry-a", "unresponsive": True, "identifierResults": [], "testIdentifiers": []}]

        state, report = transition_state(entries, {}, threshold=3, generated_at="2026-01-01T00:00:00+00:00")
        self.assertEqual(state["entries"]["entry-a"]["consecutiveFailures"], 1)
        self.assertFalse(state["entries"]["entry-a"]["disabled"])
        self.assertEqual(report["disabledEntries"], [])

        state, report = transition_state(entries, state, threshold=3, generated_at="2026-01-08T00:00:00+00:00")
        self.assertEqual(state["entries"]["entry-a"]["consecutiveFailures"], 2)
        self.assertFalse(state["entries"]["entry-a"]["disabled"])
        self.assertEqual(report["disabledEntries"], [])

    def test_pass_resets_counter_and_re_enables(self):
        entries = [{"id": "entry-a", "unresponsive": False, "identifierResults": [], "testIdentifiers": []}]
        previous_state = {
            "entries": {
                "entry-a": {
                    "consecutiveFailures": 3,
                    "disabled": True,
                }
            }
        }

        state, report = transition_state(entries, previous_state, threshold=3, generated_at="2026-01-15T00:00:00+00:00")

        self.assertEqual(state["entries"]["entry-a"]["consecutiveFailures"], 0)
        self.assertFalse(state["entries"]["entry-a"]["disabled"])
        self.assertEqual(report["disabledEntries"], [])

    def test_overlap_entry_requires_all_own_identifiers_unresponsive(self):
        config = {
            "uniresolver": {
                "drivers": [
                    {
                        "id": "entry-overlap",
                        "testIdentifiers": [
                            "did:key:z6Mkresponding",
                            "did:tz:tz1missing",
                        ],
                    }
                ]
            }
        }
        did_lint = {
            "did:key": {
                "status": "compliant",
                "identifiers": [
                    {"did:key:z6Mkresponding": {"valid": True}},
                ],
            },
            "did:tz": {
                "status": "no response",
                "identifiers": [],
            },
        }

        entries = evaluate_entries(config, did_lint)

        self.assertFalse(entries[0]["unresponsive"])


def did_lint_result(status):
    return {
        "did:example": {
            "status": status,
            "identifiers": [
                {"did:example:123": {"valid": status != "no response", "error": "no response" if status == "no response" else None}},
            ],
        }
    }


def config_with_entries():
    return {
        "uniresolver": {
            "drivers": [
                {
                    "id": "entry-a",
                    "testIdentifiers": [
                        "did:example:one",
                        "did:example:two",
                    ],
                },
                {
                    "id": "entry-b",
                    "testIdentifiers": [
                        "did:web:example.com",
                    ],
                },
                {
                    "id": "entry-overlap",
                    "testIdentifiers": [
                        "did:example:one",
                        "did:web:example.com",
                    ],
                },
            ]
        }
    }


if __name__ == "__main__":
    unittest.main()
