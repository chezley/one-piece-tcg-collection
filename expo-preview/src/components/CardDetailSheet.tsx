import { Ionicons } from '@expo/vector-icons';
import { Image } from 'expo-image';
import { Modal, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { Card, CONDITIONS, Condition, RARITY_LABELS } from '../data/catalog';
import { useCollection } from '../storage/collection';
import { rarityColor, useTheme } from '../theme';
import { QuantityStepper } from './QuantityStepper';

interface Props {
  card: Card | null;
  onClose: () => void;
}

/** Full-card view with the controls for owning copies of it. */
export function CardDetailSheet({ card, onClose }: Props) {
  const theme = useTheme();
  const insets = useSafeAreaInsets();
  const { collection, setQuantity, setCondition } = useCollection();

  const entry = card ? collection[card.id] : undefined;
  const quantity = entry?.quantity ?? 0;

  return (
    <Modal
      visible={card !== null}
      animationType="slide"
      presentationStyle="pageSheet"
      onRequestClose={onClose}
    >
      <View style={{ flex: 1, backgroundColor: theme.background }}>
        <View style={[styles.header, { borderBottomColor: theme.border }]}>
          <Text numberOfLines={1} style={[styles.headerTitle, { color: theme.text }]}>
            {card?.name ?? ''}
          </Text>
          <Pressable onPress={onClose} hitSlop={10} accessibilityLabel="Close">
            <Ionicons name="close-circle" size={28} color={theme.textMuted} />
          </Pressable>
        </View>

        {card && (
          <ScrollView
            contentContainerStyle={[
              styles.content,
              { paddingBottom: insets.bottom + 32 },
            ]}
          >
            {card.imageURL && (
              <Image
                source={card.imageURL}
                style={[styles.image, { backgroundColor: theme.surfaceAlt }]}
                contentFit="contain"
                transition={200}
                cachePolicy="disk"
              />
            )}

            <View style={styles.badgeRow}>
              <Badge
                label={RARITY_LABELS[card.rarity] ?? card.rarity}
                color={rarityColor(card.rarity, theme)}
              />
              <Badge label={card.type} color={theme.textMuted} />
              <Badge label={`${card.setCode} · ${card.setName}`} color={theme.textMuted} />
            </View>

            <View style={[styles.statsCard, { backgroundColor: theme.surface, borderColor: theme.border }]}>
              <Stat label="Card no." value={card.cardNumber} />
              <Divider />
              <Stat label="Cost" value={card.cost != null ? String(card.cost) : '—'} />
              <Divider />
              <Stat label="Power" value={card.power != null ? card.power.toLocaleString() : '—'} />
              <Divider />
              <Stat label="Attribute" value={card.attribute ?? '—'} />
            </View>

            <Text style={[styles.sectionTitle, { color: theme.textMuted }]}>COPIES OWNED</Text>
            <View
              style={[styles.ownRow, { backgroundColor: theme.surface, borderColor: theme.border }]}
            >
              <Text style={{ color: theme.text, fontSize: 16, fontWeight: '600' }}>
                {quantity === 0 ? 'Not in collection' : `${quantity} in collection`}
              </Text>
              <QuantityStepper
                quantity={quantity}
                onChange={(next) => setQuantity(card.id, next)}
              />
            </View>

            {quantity > 0 && (
              <>
                <Text style={[styles.sectionTitle, { color: theme.textMuted }]}>CONDITION</Text>
                <View style={styles.conditionWrap}>
                  {CONDITIONS.map((condition) => {
                    const selected = (entry?.condition ?? 'Near Mint') === condition;
                    return (
                      <Pressable
                        key={condition}
                        onPress={() => setCondition(card.id, condition as Condition)}
                        style={[
                          styles.conditionChip,
                          {
                            backgroundColor: selected ? theme.accent : theme.surface,
                            borderColor: selected ? theme.accent : theme.border,
                          },
                        ]}
                      >
                        <Text
                          style={{
                            color: selected ? theme.accentText : theme.text,
                            fontSize: 13,
                            fontWeight: selected ? '700' : '500',
                          }}
                        >
                          {condition}
                        </Text>
                      </Pressable>
                    );
                  })}
                </View>
              </>
            )}
          </ScrollView>
        )}
      </View>
    </Modal>
  );
}

function Badge({ label, color }: { label: string; color: string }) {
  return (
    <View style={[styles.badge, { backgroundColor: color }]}>
      <Text style={styles.badgeText}>{label}</Text>
    </View>
  );
}

function Stat({ label, value }: { label: string; value: string }) {
  const theme = useTheme();
  return (
    <View style={styles.stat}>
      <Text style={{ color: theme.textMuted, fontSize: 11 }}>{label}</Text>
      <Text style={{ color: theme.text, fontSize: 15, fontWeight: '700', marginTop: 2 }}>
        {value}
      </Text>
    </View>
  );
}

function Divider() {
  const theme = useTheme();
  return <View style={{ width: StyleSheet.hairlineWidth, backgroundColor: theme.border }} />;
}

const styles = StyleSheet.create({
  header: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    paddingHorizontal: 16,
    paddingVertical: 14,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  headerTitle: {
    flex: 1,
    fontSize: 18,
    fontWeight: '700',
  },
  content: {
    padding: 16,
    gap: 14,
  },
  image: {
    width: '100%',
    aspectRatio: 0.7,
    borderRadius: 14,
  },
  badgeRow: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 6,
  },
  badge: {
    paddingHorizontal: 8,
    paddingVertical: 4,
    borderRadius: 6,
  },
  badgeText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
  statsCard: {
    flexDirection: 'row',
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    overflow: 'hidden',
  },
  stat: {
    flex: 1,
    paddingVertical: 12,
    paddingHorizontal: 10,
  },
  sectionTitle: {
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 0.8,
    marginTop: 4,
  },
  ownRow: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    padding: 14,
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    gap: 12,
  },
  conditionWrap: {
    flexDirection: 'row',
    flexWrap: 'wrap',
    gap: 8,
  },
  conditionChip: {
    paddingHorizontal: 12,
    paddingVertical: 8,
    borderRadius: 999,
    borderWidth: StyleSheet.hairlineWidth,
  },
});
