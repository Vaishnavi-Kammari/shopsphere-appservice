import axios from 'axios';

const TOKEN_KEY = 'shopsphere.token';

export const api = axios.create({
  baseURL: import.meta.env.VITE_API_BASE_URL || 'http://localhost:8080/api',
});

api.interceptors.request.use((config) => {
  const token = localStorage.getItem(TOKEN_KEY);
  if (token) config.headers.Authorization = `Bearer ${token}`;
  return config;
});

api.interceptors.response.use(
  (response) => response,
  (error) => {
    // An expired/invalid token on a protected call: drop the session so the UI returns to "logged out".
    const url = error.config?.url || '';
    if (error.response?.status === 401 && localStorage.getItem(TOKEN_KEY) && !url.startsWith('/auth/login')) {
      window.dispatchEvent(new Event('auth:expired'));
    }
    return Promise.reject(error);
  }
);

export const errorMessage = (error) =>
  error.response?.data?.message ||
  (error.request ? 'Cannot reach the server. Is the backend running?' : error.message) ||
  'Something went wrong';

export const money = (value) =>
  new Intl.NumberFormat('en-US', { style: 'currency', currency: 'USD' }).format(Number(value || 0));

export { TOKEN_KEY };
