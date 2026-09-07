#!/usr/bin/env python3
"""Validates the bundled card catalog (OnePieceTCG/OnePieceTCG/Catalog/*.json).

Standalone script (no Xcode/Swift toolchain required) so catalog data can be
checked in CI or locally before it ships. See ../README.md for usage.

For every discovered set file, checks:
  1. the card count matches the documented official count in OFFICIAL_SET_COUNTS
  2. every card has a non-empty name, rarity, and cardNumber
  3. there are no duplicate (setCode, cardNumber) pairs across the whole catalog

Exits 0 and prints a summary if everything passes; exits 1 and prints every
offending set/card if anything fails (never a bare assertion).
"""
import json
import sys
from pathlib import Path

# Official per-set card counts, from each set's published card list (see #11).
# A set discovered on disk but missing here fails loudly rather than being
# silently skipped -- add its count here when bundling a new set.
OFFICIAL_SET_COUNTS = {
    "OP01": 121,
}

CATALOG_DIR = Path(__file__).resolve().parent.parent / "OnePieceTCG" / "Catalog"


def discover_dataset_files(catalog_dir: Path) -> list[Path]:
    """Mirrors CatalogLoader.discoverDatasetResourceNames: every *.json file
    directly in the catalog directory, sorted for a deterministic order."""
    return sorted(catalog_dir.glob("*.json"))


def validate(catalog_dir: Path) -> list[str]:
    """Returns a list of human-readable problem descriptions; empty means the
    catalog is clean."""
    problems: list[str] = []
    seen_card_keys: dict[tuple[str, str], str] = {}  # (setCode, cardNumber) -> source file
    total_cards = 0

    dataset_files = discover_dataset_files(catalog_dir)
    if not dataset_files:
        return [f"no catalog set files found under {catalog_dir}"]

    for path in dataset_files:
        try:
            data = json.loads(path.read_text())
        except (OSError, json.JSONDecodeError) as error:
            problems.append(f"{path.name}: could not read/parse JSON ({error})")
            continue

        set_info = data.get("set", {})
        set_code = set_info.get("code")
        cards = data.get("cards", [])

        if not set_code:
            problems.append(f"{path.name}: missing set.code")
            continue

        if set_code not in OFFICIAL_SET_COUNTS:
            problems.append(
                f"{path.name} (set {set_code}): no documented official count in "
                "OFFICIAL_SET_COUNTS -- add one rather than skipping this set"
            )
        elif len(cards) != OFFICIAL_SET_COUNTS[set_code]:
            problems.append(
                f"{path.name} (set {set_code}): expected "
                f"{OFFICIAL_SET_COUNTS[set_code]} cards, found {len(cards)}"
            )

        for card in cards:
            total_cards += 1
            card_number = card.get("cardNumber")
            name = card.get("name")
            rarity = card.get("rarity")
            card_label = card_number or card.get("id") or "<unknown card>"

            if not name or not str(name).strip():
                problems.append(f"{path.name} (set {set_code}): card '{card_label}' has an empty name")
            if not rarity or not str(rarity).strip():
                problems.append(f"{path.name} (set {set_code}): card '{card_label}' has an empty rarity")
            if not card_number or not str(card_number).strip():
                problems.append(f"{path.name} (set {set_code}): card '{card_label}' has an empty cardNumber")
                continue  # can't dedupe-check without a cardNumber

            key = (set_code, card_number)
            if key in seen_card_keys:
                problems.append(
                    f"{path.name} (set {set_code}): duplicate cardNumber '{card_number}' "
                    f"(first seen in {seen_card_keys[key]})"
                )
            else:
                seen_card_keys[key] = path.name

    if not problems:
        print(
            f"Catalog validation passed: {len(dataset_files)} set(s), "
            f"{total_cards} card(s), no issues found."
        )
    return problems


def main() -> int:
    catalog_dir = CATALOG_DIR
    if len(sys.argv) > 1:
        catalog_dir = Path(sys.argv[1])

    problems = validate(catalog_dir)
    if problems:
        print(f"Catalog validation FAILED ({len(problems)} issue(s)):", file=sys.stderr)
        for problem in problems:
            print(f"  - {problem}", file=sys.stderr)
        return 1
    return 0


if __name__ == "__main__":
    sys.exit(main())
