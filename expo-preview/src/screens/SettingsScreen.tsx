import { Ionicons } from '@expo/vector-icons';
import { Alert, Platform, Pressable, ScrollView, StyleSheet, Text, View } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';

import { CARDS, SETS } from '../data/catalog';
import { useCollection } from '../storage/collection';
import { useTheme } from '../theme';

export function SettingsScreen() {
  const theme = useTheme();
  const insets = useSafeAreaInsets();
  const { collection, reset } = useCollection();

  const ownedCount = Object.keys(collection).length;

  function confirmReset() {
    Alert.alert(
      'Reset collection?',
      'This clears every card you have added on this device. It cannot be undone.',
      [
        { text: 'Cancel', style: 'cancel' },
        { text: 'Reset', style: 'destructive', onPress: reset },
      ]
    );
  }

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
      <Text style={[styles.title, { color: theme.text }]}>Settings</Text>

      <Section title="Catalog">
        <Row label="Sets loaded" value={String(SETS.length)} />
        <Row label="Cards in catalog" value={String(CARDS.length)} />
        {SETS.map((set) => (
          <Row key={set.code} label={set.code} value={`${set.name} · ${set.cardCount} cards`} />
        ))}
      </Section>

      <Section title="This device">
        <Row label="Platform" value={`${Platform.OS} ${String(Platform.Version)}`} />
        <Row label="Cards in your collection" value={String(ownedCount)} />
        <Row label="Storage" value="On-device (AsyncStorage)" />
      </Section>

      <Section title="Data">
        <Pressable
          onPress={confirmReset}
          style={[styles.destructive, { borderColor: theme.border }]}
          accessibilityRole="button"
        >
          <Ionicons name="trash-outline" size={18} color={theme.danger} />
          <Text style={{ color: theme.danger, fontSize: 15, fontWeight: '600' }}>
            Reset collection
          </Text>
        </Pressable>
      </Section>

      <Text style={[styles.footer, { color: theme.textMuted }]}>
        Expo preview build of the One Piece TCG collection app, for testing the
        experience on a real device. The shipping apps are native Swift (iOS) and
        Kotlin (Android); this preview shares their card catalog but keeps its own
        on-device collection.
      </Text>
    </ScrollView>
  );
}

function Section({ title, children }: { title: string; children: React.ReactNode }) {
  const theme = useTheme();
  return (
    <View style={{ gap: 8 }}>
      <Text style={[styles.sectionTitle, { color: theme.textMuted }]}>{title.toUpperCase()}</Text>
      <View style={[styles.card, { backgroundColor: theme.surface, borderColor: theme.border }]}>
        {children}
      </View>
    </View>
  );
}

function Row({ label, value }: { label: string; value: string }) {
  const theme = useTheme();
  return (
    <View style={[styles.row, { borderBottomColor: theme.border }]}>
      <Text style={{ color: theme.text, fontSize: 14 }}>{label}</Text>
      <Text style={{ color: theme.textMuted, fontSize: 14, flexShrink: 1, textAlign: 'right' }}>
        {value}
      </Text>
    </View>
  );
}

const styles = StyleSheet.create({
  title: {
    fontSize: 32,
    fontWeight: '800',
    letterSpacing: -0.5,
  },
  sectionTitle: {
    fontSize: 11,
    fontWeight: '700',
    letterSpacing: 0.8,
  },
  card: {
    borderRadius: 12,
    borderWidth: StyleSheet.hairlineWidth,
    overflow: 'hidden',
  },
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'space-between',
    gap: 12,
    paddingHorizontal: 14,
    paddingVertical: 12,
    borderBottomWidth: StyleSheet.hairlineWidth,
  },
  destructive: {
    flexDirection: 'row',
    alignItems: 'center',
    justifyContent: 'center',
    gap: 8,
    paddingVertical: 14,
  },
  footer: {
    fontSize: 12,
    lineHeight: 18,
    paddingHorizontal: 4,
  },
});
