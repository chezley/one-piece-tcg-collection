import { Ionicons } from '@expo/vector-icons';
import { createBottomTabNavigator } from '@react-navigation/bottom-tabs';
import { DarkTheme, DefaultTheme, NavigationContainer } from '@react-navigation/native';
import { StatusBar } from 'expo-status-bar';
import { SafeAreaProvider } from 'react-native-safe-area-context';

import { BrowseScreen } from './src/screens/BrowseScreen';
import { CollectionScreen } from './src/screens/CollectionScreen';
import { SettingsScreen } from './src/screens/SettingsScreen';
import { StatsScreen } from './src/screens/StatsScreen';
import { CollectionProvider } from './src/storage/collection';
import { useTheme } from './src/theme';

const Tab = createBottomTabNavigator();

const ICONS: Record<string, string> = {
  Browse: 'grid',
  Collection: 'albums',
  Stats: 'stats-chart',
  Settings: 'settings',
};

export default function App() {
  const theme = useTheme();
  const base = theme.dark ? DarkTheme : DefaultTheme;

  const navigationTheme = {
    ...base,
    colors: {
      ...base.colors,
      primary: theme.accent,
      background: theme.background,
      card: theme.surface,
      text: theme.text,
      border: theme.border,
    },
  };

  return (
    <SafeAreaProvider>
      <CollectionProvider>
        <NavigationContainer theme={navigationTheme}>
          <StatusBar style={theme.dark ? 'light' : 'dark'} />
          <Tab.Navigator
            screenOptions={({ route }) => ({
              headerShown: false,
              tabBarActiveTintColor: theme.accent,
              tabBarInactiveTintColor: theme.textMuted,
              tabBarIcon: ({ color, size, focused }) => {
                const name = ICONS[route.name] ?? 'ellipse';
                return (
                  <Ionicons
                    name={
                      (focused ? name : `${name}-outline`) as keyof typeof Ionicons.glyphMap
                    }
                    size={size}
                    color={color}
                  />
                );
              },
            })}
          >
            <Tab.Screen name="Browse" component={BrowseScreen} />
            <Tab.Screen name="Collection" component={CollectionScreen} />
            <Tab.Screen name="Stats" component={StatsScreen} />
            <Tab.Screen name="Settings" component={SettingsScreen} />
          </Tab.Navigator>
        </NavigationContainer>
      </CollectionProvider>
    </SafeAreaProvider>
  );
}
