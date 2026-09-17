import { useColorScheme } from 'react-native';

export interface Theme {
  dark: boolean;
  background: string;
  surface: string;
  surfaceAlt: string;
  border: string;
  text: string;
  textMuted: string;
  accent: string;
  accentText: string;
  gold: string;
  danger: string;
}

const light: Theme = {
  dark: false,
  background: '#F5F2EC',
  surface: '#FFFFFF',
  surfaceAlt: '#EDE7DD',
  border: '#DED6C8',
  text: '#1B1A17',
  textMuted: '#6E675C',
  accent: '#C8102E',
  accentText: '#FFFFFF',
  gold: '#B07A17',
  danger: '#B3261E',
};

const dark: Theme = {
  dark: true,
  background: '#141210',
  surface: '#1F1C19',
  surfaceAlt: '#2A2622',
  border: '#3A3530',
  text: '#F4F1EC',
  textMuted: '#A69C8F',
  accent: '#E8384F',
  accentText: '#FFFFFF',
  gold: '#E0AC4A',
  danger: '#F2635A',
};

export function useTheme(): Theme {
  return useColorScheme() === 'dark' ? dark : light;
}

/** Accent colour per rarity, used for the badge on each card tile. */
export function rarityColor(rarity: string, theme: Theme): string {
  switch (rarity) {
    case 'SEC':
      return '#8E44AD';
    case 'SR':
      return theme.gold;
    case 'R':
      return '#2E77B8';
    case 'L':
      return theme.accent;
    case 'UC':
      return '#3F8F62';
    default:
      return theme.textMuted;
  }
}
