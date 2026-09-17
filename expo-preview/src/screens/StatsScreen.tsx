import { useMemo } from 'react';
import { ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import {
  CARDS,
  CARDS_BY_ID,
  CARD_TYPES,
  RARITIES,
  RARITY_LABELS,
  SETS,
} from '../data/catalog';
import { useCollection } from '../storage/collection';
import { rarityColor, useTheme } from '../theme';

export function StatsScreen() {
  const theme = useTheme();
  const insets = useSafeAreaInsets();
  const { collection } = useCollection();

  const stats = useMemo(() => {
    const owned = Object.entries(collection)
      .map(([cardId, entry]) => ({ card: CARDS_BY_ID[cardId], entry }))
      .filter((row) => row.card !== undefined);

    const totalCopies = owned.reduce((sum, row) => sum + row.entry.quantity, 0);

    const byRarity = RARITIES.map((rarity) => {
      const total = CARDS.filter((card) => card.rarity === rarity).length;
      const collected = owned.filter((row) => row.card.rarity === rarity).length;
      return { key: rarity, label: RARITY_LABELS[rarity] ?? rarity, collected, total };
    });

    const byType = CARD_TYPES.map((type) => {
      const total = CARDS.filter((card) => card.type === type).length;
      const collected = owned.filter((row) => row.card.type === type).length;
      return { key: type, label: type, collected, total };
    });

    return { uniqueOwned: owned.length, totalCopies, byRarity, byType };
  }, [collection]);

  const completion = CARDS.length === 0 ? 0 : stats.uniqueOwned / CARDS.length;

  return (
    <ScrollView
      style={{ flex: 1, backgroundColor: theme.background }}
      contentContainerStyle={{
        paddingTop: insets.top + 8,
        paddingBottom: insets.bottom + 32,
        paddingHorizontal: 16,
        gap: 16,
      }}
    >
      <Text style={[styles.title, { color: theme.text }]}>Stats</Text>

      <View style={styles.tileRow}>
        <Tile label="Unique cards" value={String(stats.uniqueOwned)} />
        <Tile label="Total copies" value={String(stats.totalCopies)} />
        <Tile label="Completion" value={`${Math.round(completion * 100)}%`} />
      </View>

      <View style={[styles.card, { backgroundColor: theme.surface, borderColor: theme.border }]}>
        <Text style={[styles.cardTitle, { color: theme.text }]}>Set completion</Text>
        {SETS.map((set) => {
          const collected = CARDS.filter(
            (card) => card.setCode === set.code && collection[card.id]
          ).length;
          return (
            <ProgressRow
              key={set.code}
              label={`${set.code} · ${set.name}`}
              collected={collected}
              total={set.cardCount}
              color={theme.accent}
            />
          );
        })}
      </View>

      <View style={[styles.card, { backgroundColor: theme.surface, borderColor: theme.border }]}>
        <Text style={[styles.cardTitle, { color: theme.text }]}>By rarity</Text>
        {stats.byRarity.map((row) => (
          <ProgressRow
            key={row.key}
            label={row.label}
            collected={row.collected}
            total={row.total}
            color={rarityColor(row.key, theme)}
          />
        ))}
      </View>

      <View style={[styles.card, { backgroundColor: theme.surface, borderColor: theme.border }]}>
        <Text style={[styles.cardTitle, { color: theme.text }]}>By card type</Text>
        {stats.byType.map((row) => (
          <ProgressRow
            key={row.key}
            label={row.label}
            collected={row.collected}
            total={row.total}
            color={theme.gold}
          />
        ))}
      </View>
    </ScrollView>
  );
}

function Tile({ label, value }: { label: string; value: string }) {
  const theme = useTheme();
  return (
    <View style={[styles.tile, { backgroundColor: theme.surface, borderColor: theme.border }]}>
      <Text style={{ color: theme.text, fontSize: 24, fontWeight: '800' }}>{value}</Text>
      <Text style={{ color: theme.textMuted, fontSize: 11, marginTop: 2 }}>{label}</Text>
    </View>
  );
}

function ProgressRow({
  label,
  collected,
  total,
  color,
}: {
  label: string;
  collected: number;
  total: number;
  color: string;
}) {
  const theme = useTheme();
  const fraction = total === 0 ? 0 : collected / total;

  return (
    <View style={styles.progressRow}>
      <View style={styles.progressLabels}>
        <Text numberOfLines={1} style={{ color: theme.text, fontSize: 13, flex: 1 }}>
          {label}
        </Text>
        <Text style={{ color: theme.textMuted, fontSize: 12, fontVariant: ['tabular-nums'] }}>
          {collected}/{total}
        </Text>
      </View>
      <View style={[styles.track, { backgroundColor: theme.surfaceAlt }]}>
        <View
          style={[styles.fill, { backgroundColor: color, width: `${Math.round(fraction * 100)}%` }]}
        />
      </View>
    </View>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 32,
    fontWeight: '800',
    letterSpacing: -0.5,
  },
  tileRow: {
    flexDirection: 'row',
    gap: 10,
  },
  tile: {
    flex: 1,
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    paddingVertical: 14,
    paddingHorizontal: 12,
  },
  card: {
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    padding: 14,
    gap: 12,
  },
  cardTitle: {
    fontSize: 15,
    fontWeight: '700',
  },
  progressRow: {
    gap: 6,
  },
  progressLabels: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
  },
  track: {
    height: 7,
    borderRadius: 999,
    overflow: 'hidden',
  },
  fill: {
    height: '100%',
    borderRadius: 999,
  },
});
