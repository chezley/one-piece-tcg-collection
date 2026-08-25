#!/usr/bin/env python3
"""Data-integrity checks for the bundled card catalog (Catalog/*.json).

Run before shipping catalog changes (adding/editing a set file, or adding a
new set once #11/#13 bundle more than OP01). Also usable as a pre-commit or
CI step -- exits 0 when the catalog is clean, 1 when it finds a problem, with
a message naming the offending set/card rather than a generic assertion.

Usage:
    python3 OnePieceTCG/Scripts/validate_catalog.py [--catalog-dir PATH]
"""

import argparse
import json
import sys
from pathlib import Path

# Official released card count per set, used to catch a card silently
# missing or duplicated during data entry/scraping. Sourced from the
# official set list (https://en.onepiece-cardgame.com/cardlist/) at the time
# each set's JSON was authored. When a new set is bundled (#11/#13), add its
# code and documented count here -- an undocumented set fails validation
# loudly instead of skipping the count check silently.
OFFICIAL_SET_COUNTS = {
    "OP01": 121,  # Romance Dawn
}

REQUIRED_NON_EMPTY_FIELDS = ("name", "rarity", "cardNumber")

DEFAULT_CATALOG_DIR = (
    Path(__file__).resolve().parent.parent / "OnePieceTCG" / "Catalog"
)


def load_dataset(path):
    with path.open(encoding="utf-8") as f:
        return json.load(f)


def validate_catalog(catalog_dir):
    """Returns a list of human-readable error strings; empty means clean."""
    errors = []

    set_files = sorted(catalog_dir.glob("*.json"))
    if not set_files:
        return [f"No catalog set files found in {catalog_dir}"]

    seen_card_numbers = {}  # (setCode, cardNumber) -> file it was first seen in

    for path in set_files:
        try:
            dataset = load_dataset(path)
        except (json.JSONDecodeError, OSError) as e:
            errors.append(f"{path.name}: failed to parse as JSON ({e})")
            continue

        set_info = dataset.get("set", {})
        set_code = set_info.get("code")
        if not set_code:
            errors.append(f"{path.name}: missing \"set.code\"")
            continue

        cards = dataset.get("cards", [])

        expected_count = OFFICIAL_SET_COUNTS.get(set_code)
        if expected_count is None:
            errors.append(
                f"{path.name}: set '{set_code}' has no documented official "
                f"count in OFFICIAL_SET_COUNTS -- add one so this check can "
                f"catch missing/duplicated cards for it"
            )
        elif len(cards) != expected_count:
            errors.append(
                f"{path.name}: set '{set_code}' has {len(cards)} card(s), "
                f"expected {expected_count} per the documented official count"
            )

        for index, card in enumerate(cards):
            card_number = card.get("cardNumber")
            card_label = card_number or f"cards[{index}]"

            for field in REQUIRED_NON_EMPTY_FIELDS:
                value = card.get(field)
                if not isinstance(value, str) or not value.strip():
                    errors.append(
                        f"{path.name}: {set_code} {card_label}: "
                        f"\"{field}\" is missing or empty"
                    )

            if card_number:
                key = (set_code, card_number)
                if key in seen_card_numbers:
                    errors.append(
                        f"{path.name}: {set_code} {card_number}: duplicate "
                        f"(setCode, cardNumber) pair, also seen in "
                        f"{seen_card_numbers[key]}"
                    )
                else:
                    seen_card_numbers[key] = path.name

    return errors


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument(
        "--catalog-dir",
        type=Path,
        default=DEFAULT_CATALOG_DIR,
        help="Directory containing catalog-set *.json files "
        "(default: OnePieceTCG/OnePieceTCG/Catalog next to this script)",
    )
    args = parser.parse_args()

    if not args.catalog_dir.is_dir():
        print(f"error: catalog directory not found: {args.catalog_dir}", file=sys.stderr)
        return 1

    errors = validate_catalog(args.catalog_dir)

    if errors:
        print(f"Catalog validation FAILED ({len(errors)} issue(s)):")
        for error in errors:
            print(f"  - {error}")
        return 1

    total_cards = sum(
        len(load_dataset(p).get("cards", [])) for p in sorted(args.catalog_dir.glob("*.json"))
    )
    print(
        f"Catalog validation passed: {len(list(args.catalog_dir.glob('*.json')))} "
        f"set(s), {total_cards} card(s), no issues found."
    )
    return 0


if __name__ == "__main__":
    sys.exit(main())
