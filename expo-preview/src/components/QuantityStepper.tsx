import { Ionicons } from '@expo/vector-icons';
import { Pressable, StyleSheet, Text, View } from 'react-native';

import { useTheme } from '../theme';

interface Props {
  quantity: number;
  onChange: (quantity: number) => void;
  size?: 'small' | 'large';
}

export function QuantityStepper({ quantity, onChange, size = 'large' }: Props) {
  const theme = useTheme();
  const button = size === 'large' ? 40 : 30;
  const icon = size === 'large' ? 22 : 16;

  return (
    <View style={[styles.row, { backgroundColor: theme.surfaceAlt, borderColor: theme.border }]}>
      <Pressable
        onPress={() => onChange(quantity - 1)}
        disabled={quantity <= 0}
        hitSlop={6}
        accessibilityRole="button"
        accessibilityLabel="Decrease quantity"
        style={[styles.button, { width: button, height: button, opacity: quantity <= 0 ? 0.3 : 1 }]}
      >
        <Ionicons name="remove" size={icon} color={theme.text} />
      </Pressable>

      <Text
        style={[
          styles.count,
          { color: theme.text, fontSize: size === 'large' ? 18 : 15, minWidth: button },
        ]}
      >
        {quantity}
      </Text>

      <Pressable
        onPress={() => onChange(quantity + 1)}
        hitSlop={6}
        accessibilityRole="button"
        accessibilityLabel="Increase quantity"
        style={[styles.button, { width: button, height: button }]}
      >
        <Ionicons name="add" size={icon} color={theme.text} />
      </Pressable>
    </View>
  );
}

const styles = StyleSheet.create({
  row: {
    flexDirection: 'row',
    alignItems: 'center',
    borderRadius: 999,
    borderWidth: StyleSheet.hairlineWidth,
    overflow: 'hidden',
  },
  button: {
    alignItems: 'center',
    justifyContent: 'center',
  },
  count: {
    textAlign: 'center',
    fontWeight: '700',
    fontVariant: ['tabular-nums'],
  },
});
