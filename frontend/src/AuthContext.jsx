import { createContext, useCallback, useContext, useEffect, useState } from 'react';
import { api, TOKEN_KEY } from './api.js';

const AuthContext = createContext(null);
const USER_KEY = 'shopsphere.user';

function readUser() {
  try {
    return JSON.parse(localStorage.getItem(USER_KEY));
  } catch {
    return null;
  }
}

export function AuthProvider({ children }) {
  const [user, setUser] = useState(() => (localStorage.getItem(TOKEN_KEY) ? readUser() : null));

  const store = (data) => {
    localStorage.setItem(TOKEN_KEY, data.token);
    localStorage.setItem(USER_KEY, JSON.stringify(data.user));
    setUser(data.user);
    return data.user;
  };

  const logout = useCallback(() => {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_KEY);
    setUser(null);
  }, []);

  useEffect(() => {
    window.addEventListener('auth:expired', logout);
    return () => window.removeEventListener('auth:expired', logout);
  }, [logout]);

  const login = async (email, password) => store((await api.post('/auth/login', { email, password })).data);
  const register = async (name, email, password) =>
    store((await api.post('/auth/register', { name, email, password })).data);

  return (
    <AuthContext.Provider value={{ user, isAdmin: user?.role === 'ADMIN', login, register, logout }}>
      {children}
    </AuthContext.Provider>
  );
}

export const useAuth = () => useContext(AuthContext);
