import { Ionicons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { useMemo, useState } from 'react';
import { FlatList, Pressable, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { CardDetailSheet } from '../components/CardDetailSheet';
import { QuantityStepper } from '../components/QuantityStepper';
import { CARDS_BY_ID, Card } from '../data/catalog';
import { useCollection } from '../storage/collection';
import { rarityColor, useTheme } from '../theme';

export function CollectionScreen() {
  const theme = useTheme();
  const insets = useSafeAreaInsets();
  const { collection, setQuantity, removeCard } = useCollection();
  const [selected, setSelected] = useState<Card | null>(null);

  const rows = useMemo(
    () =>
      Object.entries(collection)
        .map(([cardId, entry]) => ({ card: CARDS_BY_ID[cardId], entry }))
        .filter((row) => row.card !== undefined)
        .sort((a, b) => a.card.cardNumber.localeCompare(b.card.cardNumber)),
    [collection]
  );

  const totalCopies = rows.reduce((sum, row) => sum + row.entry.quantity, 0);

  return (
    <View style={{ flex: 1, backgroundColor: theme.background, paddingTop: insets.top }}>
      <View style={styles.headerBlock}>
        <Text style={[styles.title, { color: theme.text }]}>Collection</Text>
        <Text style={{ color: theme.textMuted, fontSize: 13 }}>
          {rows.length} unique · {totalCopies} {totalCopies === 1 ? 'copy' : 'copies'}
        </Text>
      </View>

      <FlatList
        data={rows}
        keyExtractor={(row) => row.card.id}
        contentContainerStyle={{
          paddingHorizontal: 16,
          paddingBottom: insets.bottom + 24,
          gap: 10,
        }}
        renderItem={({ item }) => (
          <View
            style={[styles.row, { backgroundColor: theme.surface, borderColor: theme.border }]}
          >
            <Pressable onPress={() => setSelected(item.card)} style={styles.rowMain}>
              {item.card.imageURL && (
                <Image
                  source={item.card.imageURL}
                  style={[styles.thumb, { backgroundColor: theme.surfaceAlt }]}
                  contentFit="cover"
                  transition={150}
                  cachePolicy="disk"
                />
              )}
              <View style={styles.rowText}>
                <Text numberOfLines={1} style={{ color: theme.text, fontSize: 15, fontWeight: '700' }}>
                  {item.card.name}
                </Text>
                <Text style={{ color: theme.textMuted, fontSize: 12, marginTop: 2 }}>
                  {item.card.cardNumber} · {item.card.type}
                </Text>
                <View style={styles.tagRow}>
                  <View
                    style={[styles.tag, { backgroundColor: rarityColor(item.card.rarity, theme) }]}
                  >
                    <Text style={styles.tagText}>{item.card.rarity}</Text>
                  </View>
                  <Text style={{ color: theme.textMuted, fontSize: 11 }}>
                    {item.entry.condition}
                  </Text>
                </View>
              </View>
            </Pressable>

            <View style={styles.rowActions}>
              <QuantityStepper
                size="small"
                quantity={item.entry.quantity}
                onChange={(next) => setQuantity(item.card.id, next)}
              />
              <Pressable
                onPress={() => removeCard(item.card.id)}
                hitSlop={8}
                accessibilityLabel={`Remove ${item.card.name}`}
              >
                <Ionicons name="trash-outline" size={18} color={theme.danger} />
              </Pressable>
            </View>
          </View>
        )}
        ListEmptyComponent={
          <View style={styles.emptyWrap}>
            <Ionicons name="albums-outline" size={44} color={theme.textMuted} />
            <Text style={[styles.emptyTitle, { color: theme.text }]}>No cards yet</Text>
            <Text style={[styles.emptyBody, { color: theme.textMuted }]}>
              Head to Browse, tap a card, and add the copies you own.
            </Text>
          </View>
        }
      />

      <CardDetailSheet card={selected} onClose={() => setSelected(null)} />
    </View>
  );
}

const styles = StyleSheet.create({
  headerBlock: {
    paddingHorizontal: 16,
    paddingTop: 8,
    paddingBottom: 14,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    letterSpacing: -0.5,
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    padding: 10,
    gap: 10,
  },
  rowMain: {
    flex: 1,
    flexDirection: 'row',
    alignItems: 'center',
    gap: 10,
  },
  thumb: {
    width: 46,
    height: 64,
    borderRadius: 6,
  },
  rowText: {
    flex: 1,
  },
  tagRow: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 6,
    marginTop: 5,
  },
  tag: {
    paddingHorizontal: 6,
    paddingVertical: 2,
    borderRadius: 4,
  },
  tagText: {
    color: '#FFFFFF',
    fontSize: 9,
    fontWeight: '700',
  },
  rowActions: {
    alignItems: 'center',
    gap: 8,
  },
  emptyWrap: {
    alignItems: 'center',
    marginTop: 70,
    gap: 8,
    paddingHorizontal: 40,
  },
  emptyTitle: {
    fontSize: 17,
    fontWeight: '700',
  },
  emptyBody: {
    fontSize: 13,
    textAlign: 'center',
    lineHeight: 19,
  },
});
