import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api } from './api.js';
import { useAuth } from './AuthContext.jsx';

const CartContext = createContext(null);
const EMPTY = { items: [], total: 0, itemCount: 0 };

export function CartProvider({ children }) {
  const { user } = useAuth();
  const [cart, setCart] = useState(EMPTY);

  const refresh = useCallback(async () => {
    if (!user) {
      setCart(EMPTY);
      return;
    }
    try {
      setCart((await api.get('/cart')).data);
    } catch {
      setCart(EMPTY);
    }
  }, [user]);

  useEffect(() => {
    refresh();
  }, [refresh]);

  const run = async (request) => {
    const { data } = await request;
    setCart(data);
    return data;
  };

  const value = {
    cart,
    refresh,
    addItem: (productId, quantity = 1) => run(api.post('/cart/items', { productId, quantity })),
    setQuantity: (productId, quantity) => run(api.put(`/cart/items/${productId}`, { quantity })),
    removeItem: (productId) => run(api.delete(`/cart/items/${productId}`)),
  };
  return <CartContext.Provider value={value}>{children}</CartContext.Provider>;
}

export const useCart = () => useContext(CartContext);
