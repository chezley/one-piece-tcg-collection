import { Image } from 'expo-image';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { Card } from '../data/catalog';
import { rarityColor, useTheme } from '../theme';

interface Props {
  card: Card;
  width: number;
  ownedQuantity: number;
  onPress: () => void;
}

/** Card image in the browse grid, with a rarity badge and owned-count pill. */
export function CardTile({ card, width, ownedQuantity, onPress }: Props) {
  const theme = useTheme();
  const height = width * 1.4;

  return (
    <Pressable
      onPress={onPress}
      style={({ pressed }) => [{ width, opacity: pressed ? 0.7 : 1 }]}
      accessibilityRole="button"
      accessibilityLabel={`${card.name}, ${card.cardNumber}${
        ownedQuantity > 0 ? `, ${ownedQuantity} owned` : ''
      }`}
    >
      <View
        style={[
          styles.imageFrame,
          {
            height,
            backgroundColor: theme.surfaceAlt,
            borderColor: ownedQuantity > 0 ? theme.accent : theme.border,
            borderWidth: ownedQuantity > 0 ? 2 : StyleSheet.hairlineWidth,
          },
        ]}
      >
        {card.imageURL ? (
          <Image
            source={card.imageURL}
            style={styles.image}
            contentFit="cover"
            transition={150}
            cachePolicy="disk"
          />
        ) : (
          <View style={styles.placeholder}>
            <Text style={{ color: theme.textMuted, fontSize: 11 }}>No image</Text>
          </View>
        )}

        <View style={[styles.rarity, { backgroundColor: rarityColor(card.rarity, theme) }]}>
          <Text style={styles.rarityText}>{card.rarity}</Text>
        </View>

        {ownedQuantity > 0 && (
          <View style={[styles.owned, { backgroundColor: theme.accent }]}>
            <Text style={styles.ownedText}>{ownedQuantity}</Text>
          </View>
        )}
      </View>

      <Text numberOfLines={1} style={[styles.name, { color: theme.text }]}>
        {card.name}
      </Text>
      <Text style={[styles.number, { color: theme.textMuted }]}>{card.cardNumber}</Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  imageFrame: {
    borderRadius: 10,
    overflow: 'hidden',
  },
  image: {
    width: '100%',
    height: '100%',
  },
  placeholder: {
    flex: 1,
    alignItems: 'center',
    justifyContent: 'center',
  },
  rarity: {
    position: 'absolute',
    top: 4,
    left: 4,
    paddingHorizontal: 5,
    paddingVertical: 1,
    borderRadius: 4,
  },
  rarityText: {
    color: '#FFFFFF',
    fontSize: 9,
    fontWeight: '700',
  },
  owned: {
    position: 'absolute',
    bottom: 4,
    right: 4,
    minWidth: 20,
    height: 20,
    borderRadius: 10,
    alignItems: 'center',
    justifyContent: 'center',
    paddingHorizontal: 5,
  },
  ownedText: {
    color: '#FFFFFF',
    fontSize: 11,
    fontWeight: '700',
  },
  name: {
    marginTop: 5,
    fontSize: 12,
    fontWeight: '600',
  },
  number: {
    fontSize: 10,
    marginTop: 1,
  },
});
