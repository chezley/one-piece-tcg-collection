import AsyncStorage from '@react-native-async-storage/async-storage';
import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useMemo,
  useRef,
  useState,
} from 'react';

import { Condition } from '../data/catalog';

const STORAGE_KEY = 'onepiece-tcg.collection.v1';

export interface OwnedEntry {
  quantity: number;
  condition: Condition;
  dateAdded: string;
}

/** Owned cards keyed by card id, e.g. `{ "OP01-001": { quantity: 2, ... } }`. */
export type Collection = Record<string, OwnedEntry>;

interface CollectionContextValue {
  collection: Collection;
  loading: boolean;
  addCard: (cardId: string, quantity?: number, condition?: Condition) => void;
  setQuantity: (cardId: string, quantity: number) => void;
  setCondition: (cardId: string, condition: Condition) => void;
  removeCard: (cardId: string) => void;
  reset: () => void;
}

const CollectionContext = createContext<CollectionContextValue | null>(null);

export function CollectionProvider({ children }: { children: React.ReactNode }) {
  const [collection, setCollection] = useState<Collection>({});
  const [loading, setLoading] = useState(true);
  // Skip the write-back for the very first render, which still holds the
  // empty default and would clobber whatever is on disk.
  const hydrated = useRef(false);

  useEffect(() => {
    let cancelled = false;

    AsyncStorage.getItem(STORAGE_KEY)
      .then((stored) => {
        if (cancelled) return;
        if (stored) {
          setCollection(JSON.parse(stored) as Collection);
        }
      })
      .catch((error) => {
        console.warn('Could not read the saved collection', error);
      })
      .finally(() => {
        if (cancelled) return;
        hydrated.current = true;
        setLoading(false);
      });

    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!hydrated.current) return;
    AsyncStorage.setItem(STORAGE_KEY, JSON.stringify(collection)).catch((error) => {
      console.warn('Could not save the collection', error);
    });
  }, [collection]);

  const addCard = useCallback(
    (cardId: string, quantity = 1, condition: Condition = 'Near Mint') => {
      setCollection((current) => {
        const existing = current[cardId];
        if (existing) {
          return {
            ...current,
            [cardId]: { ...existing, quantity: existing.quantity + quantity },
          };
        }
        return {
          ...current,
          [cardId]: { quantity, condition, dateAdded: new Date().toISOString() },
        };
      });
    },
    []
  );

  const setQuantity = useCallback((cardId: string, quantity: number) => {
    setCollection((current) => {
      if (quantity <= 0) {
        const { [cardId]: _removed, ...rest } = current;
        return rest;
      }
      const existing = current[cardId];
      return {
        ...current,
        [cardId]: existing
          ? { ...existing, quantity }
          : { quantity, condition: 'Near Mint', dateAdded: new Date().toISOString() },
      };
    });
  }, []);

  const setCondition = useCallback((cardId: string, condition: Condition) => {
    setCollection((current) => {
      const existing = current[cardId];
      if (!existing) return current;
      return { ...current, [cardId]: { ...existing, condition } };
    });
  }, []);

  const removeCard = useCallback((cardId: string) => {
    setCollection((current) => {
      const { [cardId]: _removed, ...rest } = current;
      return rest;
    });
  }, []);

  const reset = useCallback(() => setCollection({}), []);

  const value = useMemo(
    () => ({ collection, loading, addCard, setQuantity, setCondition, removeCard, reset }),
    [collection, loading, addCard, setQuantity, setCondition, removeCard, reset]
  );

  return <CollectionContext.Provider value={value}>{children}</CollectionContext.Provider>;
}

export function useCollection(): CollectionContextValue {
  const context = useContext(CollectionContext);
  if (!context) {
    throw new Error('useCollection must be used inside a CollectionProvider');
  }
  return context;
}
