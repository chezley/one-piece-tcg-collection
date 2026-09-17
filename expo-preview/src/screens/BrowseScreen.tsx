import { Ionicons } from '@expo/vector-icons';
import { useMemo, useState } from 'react';
import {
  FlatList,
  Pressable,
  ScrollView,
  StyleSheet,
  Text,
  TextInput,
  View,
  useWindowDimensions,
} from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { CardDetailSheet } from '../components/CardDetailSheet';
import { CardTile } from '../components/CardTile';
import { CARDS, CARD_TYPES, Card, RARITIES, searchCards } from '../data/catalog';
import { useCollection } from '../storage/collection';
import { useTheme } from '../theme';

const GAP = 10;
const H_PADDING = 16;

export function BrowseScreen() {
  const theme = useTheme();
  const insets = useSafeAreaInsets();
  const { width } = useWindowDimensions();
  const { collection } = useCollection();

  const [query, setQuery] = useState('');
  const [type, setType] = useState<string | null>(null);
  const [rarity, setRarity] = useState<string | null>(null);
  const [selected, setSelected] = useState<Card | null>(null);

  // A Pro Max in portrait fits three columns comfortably; wider (landscape,
  // iPad) picks up more so tiles never stretch out of proportion.
  const columns = Math.max(3, Math.floor((width - H_PADDING * 2) / 130));
  const tileWidth = (width - H_PADDING * 2 - GAP * (columns - 1)) / columns;

  const results = useMemo(
    () => searchCards(CARDS, query, type, rarity),
    [query, type, rarity]
  );

  return (
    <View style={{ flex: 1, backgroundColor: theme.background, paddingTop: insets.top }}>
      <View style={styles.headerBlock}>
        <Text style={[styles.title, { color: theme.text }]}>Browse</Text>
        <Text style={{ color: theme.textMuted, fontSize: 13 }}>
          {results.length} of {CARDS.length} cards
        </Text>
      </View>

      <View
        style={[
          styles.search,
          { backgroundColor: theme.surface, borderColor: theme.border },
        ]}
      >
        <Ionicons name="search" size={18} color={theme.textMuted} />
        <TextInput
          value={query}
          onChangeText={setQuery}
          placeholder="Name, number, or attribute"
          placeholderTextColor={theme.textMuted}
          autoCorrect={false}
          autoCapitalize="none"
          clearButtonMode="while-editing"
          style={[styles.searchInput, { color: theme.text }]}
        />
      </View>

      <ScrollView
        horizontal
        showsHorizontalScrollIndicator={false}
        contentContainerStyle={styles.filterRow}
      >
        <FilterChip label="All" active={!type && !rarity} onPress={() => { setType(null); setRarity(null); }} />
        {CARD_TYPES.map((value) => (
          <FilterChip
            key={value}
            label={value}
            active={type === value}
            onPress={() => setType(type === value ? null : value)}
          />
        ))}
        {RARITIES.map((value) => (
          <FilterChip
            key={value}
            label={value}
            active={rarity === value}
            onPress={() => setRarity(rarity === value ? null : value)}
          />
        ))}
      </ScrollView>

      <FlatList
        data={results}
        key={columns}
        numColumns={columns}
        keyExtractor={(card) => card.id}
        columnWrapperStyle={{ gap: GAP }}
        contentContainerStyle={{
          paddingHorizontal: H_PADDING,
          paddingBottom: insets.bottom + 24,
          gap: GAP + 4,
        }}
        keyboardDismissMode="on-drag"
        renderItem={({ item }) => (
          <CardTile
            card={item}
            width={tileWidth}
            ownedQuantity={collection[item.id]?.quantity ?? 0}
            onPress={() => setSelected(item)}
          />
        )}
        ListEmptyComponent={
          <Text style={[styles.empty, { color: theme.textMuted }]}>
            No cards match that search.
          </Text>
        }
      />

      <CardDetailSheet card={selected} onClose={() => setSelected(null)} />
    </View>
  );
}

function FilterChip({
  label,
  active,
  onPress,
}: {
  label: string;
  active: boolean;
  onPress: () => void;
}) {
  const theme = useTheme();
  return (
    <Pressable
      onPress={onPress}
      style={[
        styles.chip,
        {
          backgroundColor: active ? theme.accent : theme.surface,
          borderColor: active ? theme.accent : theme.border,
        },
      ]}
    >
      <Text
        style={{
          color: active ? theme.accentText : theme.text,
          fontSize: 13,
          fontWeight: active ? '700' : '500',
        }}
      >
        {label}
      </Text>
    </Pressable>
  );
}

const styles = StyleSheet.create({
  headerBlock: {
    paddingHorizontal: H_PADDING,
    paddingTop: 8,
    paddingBottom: 10,
  },
  title: {
    fontSize: 32,
    fontWeight: '800',
    letterSpacing: -0.5,
  },
  search: {
    flexDirection: 'row',
    alignItems: 'center',
    gap: 8,
    marginHorizontal: H_PADDING,
    paddingHorizontal: 12,
    height: 42,
    borderRadius: 10,
    borderWidth: StyleSheet.hairlineWidth,
  },
  searchInput: {
    flex: 1,
    fontSize: 16,
    height: '100%',
  },
  filterRow: {
    gap: 8,
    paddingHorizontal: H_PADDING,
    paddingVertical: 12,
  },
  chip: {
    paddingHorizontal: 14,
    paddingVertical: 7,
    borderRadius: 999,
    borderWidth: StyleSheet.hairlineWidth,
  },
  empty: {
    textAlign: 'center',
    marginTop: 40,
    fontSize: 14,
  },
});
