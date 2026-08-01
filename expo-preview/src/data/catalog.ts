import rawOP01 from './OP01.json';

/** Physical condition of an owned copy. Mirrors `CardCondition` in the iOS app. */
export const CONDITIONS = [
  'Near Mint',
  'Lightly Played',
  'Moderately Played',
  'Heavily Played',
  'Damaged',
] as const;

export type Condition = (typeof CONDITIONS)[number];

export interface Card {
  id: string;
  name: string;
  cardNumber: string;
  rarity: string;
  cost: number | null;
  power: number | null;
  attribute: string | null;
  type: string;
  imageURL: string | null;
  setCode: string;
  setName: string;
}

interface RawSet {
  set: { code: string; name: string };
  cards: Array<{
    id: string;
    name: string;
    cardNumber: string;
    rarity: string;
    cost?: number | null;
    power?: number | null;
    attribute?: string | null;
    type?: string | null;
    imageURL?: string | null;
  }>;
}

function flatten(raw: RawSet): Card[] {
  return raw.cards.map((card) => ({
    id: card.id,
    name: card.name,
    cardNumber: card.cardNumber,
    rarity: card.rarity,
    cost: card.cost ?? null,
    power: card.power ?? null,
    attribute: card.attribute ?? null,
    type: card.type ?? 'Unknown',
    imageURL: card.imageURL ?? null,
    setCode: raw.set.code,
    setName: raw.set.name,
  }));
}

const RAW_SETS: RawSet[] = [rawOP01 as RawSet];

export const CARDS: Card[] = RAW_SETS.flatMap(flatten);

export const CARDS_BY_ID: Record<string, Card> = Object.fromEntries(
  CARDS.map((card) => [card.id, card])
);

export const SETS = RAW_SETS.map((raw) => ({
  code: raw.set.code,
  name: raw.set.name,
  cardCount: raw.cards.length,
}));

/** Card types present in the catalog, in the order the game presents them. */
const TYPE_ORDER = ['Leader', 'Character', 'Event', 'Stage'];

export const CARD_TYPES = Array.from(new Set(CARDS.map((card) => card.type))).sort(
  (a, b) => TYPE_ORDER.indexOf(a) - TYPE_ORDER.indexOf(b)
);

/** Rarities present in the catalog, ordered common → secret rare. */
const RARITY_ORDER = ['C', 'UC', 'R', 'SR', 'SEC', 'L'];

export const RARITIES = Array.from(new Set(CARDS.map((card) => card.rarity))).sort(
  (a, b) => RARITY_ORDER.indexOf(a) - RARITY_ORDER.indexOf(b)
);

export const RARITY_LABELS: Record<string, string> = {
  C: 'Common',
  UC: 'Uncommon',
  R: 'Rare',
  SR: 'Super Rare',
  SEC: 'Secret Rare',
  L: 'Leader',
};

export function searchCards(
  cards: Card[],
  query: string,
  type: string | null,
  rarity: string | null
): Card[] {
  const needle = query.trim().toLowerCase();

  return cards.filter((card) => {
    if (type && card.type !== type) return false;
    if (rarity && card.rarity !== rarity) return false;
    if (!needle) return true;
    return (
      card.name.toLowerCase().includes(needle) ||
      card.cardNumber.toLowerCase().includes(needle) ||
      (card.attribute ?? '').toLowerCase().includes(needle)
    );
  });
}
