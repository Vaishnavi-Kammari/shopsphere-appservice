import { useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import { errorMessage } from '../api.js';
import { useAuth } from '../AuthContext.jsx';

export default function Login() {
  const { login, register } = useAuth();
  const navigate = useNavigate();
  const location = useLocation();
  const [mode, setMode] = useState('login');
  const [form, setForm] = useState({ name: '', email: '', password: '' });
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();
    setError('');
    setBusy(true);
    try {
      const user = mode === 'login'
        ? await login(form.email, form.password)
        : await register(form.name, form.email, form.password);
      const from = location.state?.from;
      navigate(from || (user.role === 'ADMIN' ? '/admin' : '/'), { replace: true });
    } catch (err) {
      setError(errorMessage(err));
    } finally {
      setBusy(false);
    }
  };

  return (
    <form className="auth" onSubmit={submit}>
      <div className="tabs">
        <button type="button" className={mode === 'login' ? 'on' : ''} onClick={() => setMode('login')}>Log in</button>
        <button type="button" className={mode === 'register' ? 'on' : ''} onClick={() => setMode('register')}>Create account</button>
      </div>
      {error && <p className="alert error">{error}</p>}
      {mode === 'register' && (
        <label>Name<input required value={form.name} onChange={set('name')} autoComplete="name" /></label>
      )}
      <label>Email<input required type="email" value={form.email} onChange={set('email')} autoComplete="email" /></label>
      <label>Password
        <input required type="password" minLength={mode === 'register' ? 8 : undefined} value={form.password}
               onChange={set('password')} autoComplete={mode === 'login' ? 'current-password' : 'new-password'} />
      </label>
      {mode === 'register' && <p className="muted small">At least 8 characters.</p>}
      <button className="btn block" disabled={busy}>{busy ? 'Please wait...' : mode === 'login' ? 'Log in' : 'Create account'}</button>
      {import.meta.env.DEV && (
        <div className="hint">
          <strong>Development accounts</strong>
          <button type="button" onClick={() => setForm({ ...form, email: 'admin@shopsphere.local', password: 'Admin@123' })}>Fill admin</button>
          <button type="button" onClick={() => setForm({ ...form, email: 'customer@shopsphere.local', password: 'Customer@123' })}>Fill customer</button>
        </div>
      )}
    </form>
  );
}
