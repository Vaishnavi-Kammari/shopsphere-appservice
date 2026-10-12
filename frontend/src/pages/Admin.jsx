import { useCallback, useEffect, useState } from 'react';
import { api, errorMessage, money } from '../api.js';
import { OrderItems, StatusBadge } from './Orders.jsx';

const STATUSES = ['PENDING', 'PROCESSING', 'SHIPPED', 'DELIVERED', 'CANCELLED'];
const BLANK = { name: '', description: '', price: '', stock: '', imageUrl: '', categoryId: '' };

function ProductsTab() {
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [form, setForm] = useState(BLANK);
  const [editingId, setEditingId] = useState(null);
  const [msg, setMsg] = useState(null);

  const load = useCallback(async () => {
    try {
      const [p, c] = await Promise.all([api.get('/products'), api.get('/categories')]);
      setProducts(p.data);
      setCategories(c.data);
    } catch (e) {
      setMsg({ type: 'error', text: errorMessage(e) });
    }
  }, []);
  useEffect(() => { load(); }, [load]);

  const set = (field) => (e) => setForm({ ...form, [field]: e.target.value });

  const edit = (p) => {
    setEditingId(p.id);
    setForm({ name: p.name, description: p.description || '', price: p.price, stock: p.stock,
              imageUrl: p.imageUrl || '', categoryId: p.categoryId || '' });
    window.scrollTo({ top: 0, behavior: 'smooth' });
  };

  const reset = () => { setEditingId(null); setForm(BLANK); };

  const submit = async (e) => {
    e.preventDefault();
    const body = { ...form, price: Number(form.price), stock: Number(form.stock), categoryId: Number(form.categoryId) };
    try {
      if (editingId) await api.put(`/admin/products/${editingId}`, body);
      else await api.post('/admin/products', body);
      setMsg({ type: 'ok', text: editingId ? 'Product updated.' : 'Product created.' });
      reset();
      load();
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  const remove = async (p) => {
    if (!window.confirm(`Delete "${p.name}"? It will also be removed from customers' carts.`)) return;
    try {
      await api.delete(`/admin/products/${p.id}`);
      setMsg({ type: 'ok', text: 'Product deleted.' });
      if (editingId === p.id) reset();
      load();
    } catch (err) {
      setMsg({ type: 'error', text: errorMessage(err) });
    }
  };

  return (
    <>
      {msg && <p className={`alert ${msg.type}`}>{msg.text}</p>}
      <form className="panel form-grid" onSubmit={submit}>
        <h2>{editingId ? `Edit product #${editingId}` : 'Add product'}</h2>
        <label>Name<input required maxLength={150} value={form.name} onChange={set('name')} /></label>
        <label>Category
          <select required value={form.categoryId} onChange={set('categoryId')}>
            <option value="">Select...</option>
            {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
          </select>
        </label>
        <label>Price (USD)<input required type="number" min="0.01" step="0.01" value={form.price} onChange={set('price')} /></label>
        <label>Stock<input required type="number" min="0" step="1" value={form.stock} onChange={set('stock')} /></label>
        <label className="wide">Image URL (optional)<input maxLength={1000} value={form.imageUrl} onChange={set('imageUrl')} /></label>
        <label className="wide">Description<textarea rows={3} maxLength={2000} value={form.description} onChange={set('description')} /></label>
        <div className="row wide">
          <button className="btn">{editingId ? 'Save changes' : 'Add product'}</button>
          {editingId && <button type="button" className="btn ghost" onClick={reset}>Cancel</button>}
        </div>
      </form>

      <div className="scroll">
        <table className="table">
          <thead><tr><th>ID</th><th>Name</th><th>Category</th><th className="right">Price</th><th className="right">Stock</th><th /></tr></thead>
          <tbody>
            {products.map((p) => (
              <tr key={p.id}>
                <td>{p.id}</td>
                <td>{p.name}</td>
                <td>{p.categoryName}</td>
                <td className="right">{money(p.price)}</td>
                <td className="right">{p.stock}</td>
                <td className="right nowrap">
                  <button className="btn ghost" onClick={() => edit(p)}>Edit</button>{' '}
                  <button className="btn danger" onClick={() => remove(p)}>Delete</button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </>
  );
}

function OrdersTab() {
  const [orders, setOrders] = useState(null);
  const [msg, setMsg] = useState(null);

  useEffect(() => {
    api.get('/admin/orders').then((r) => setOrders(r.data)).catch((e) => setMsg({ type: 'error', text: errorMessage(e) }));
  }, []);

  const changeStatus = async (order, status) => {
    try {
      const { data } = await api.put(`/admin/orders/${order.id}/status`, { status });
      setOrders((list) => list.map((o) => (o.id === data.id ? data : o)));
      setMsg({ type: 'ok', text: `Order #${order.id} is now ${status}.` });
    } catch (e) {
      setMsg({ type: 'error', text: errorMessage(e) });
    }
  };

  return (
    <>
      {msg && <p className={`alert ${msg.type}`}>{msg.text}</p>}
      {orders === null ? <p className="muted">Loading...</p>
        : orders.length === 0 ? <p className="empty">No orders yet.</p>
        : orders.map((o) => (
          <section className="order" key={o.id}>
            <header>
              <div>
                <strong>Order #{o.id}</strong>{' '}
                <span className="muted small">{o.customerName} ({o.customerEmail}) - {new Date(o.createdAt).toLocaleString()}</span>
              </div>
              <div className="row">
                <StatusBadge status={o.status} />
                <select value={o.status} aria-label={`Status of order ${o.id}`}
                        onChange={(e) => changeStatus(o, e.target.value)}>
                  {STATUSES.map((s) => <option key={s}>{s}</option>)}
                </select>
              </div>
            </header>
            <OrderItems items={o.items} />
            <footer>
              <span className="muted small">Ship to: {o.shippingAddress}</span>
              <strong>{money(o.total)}</strong>
            </footer>
          </section>
        ))}
    </>
  );
}

export default function Admin() {
  const [tab, setTab] = useState('products');
  return (
    <>
      <h1>Admin</h1>
      <div className="tabs left">
        <button className={tab === 'products' ? 'on' : ''} onClick={() => setTab('products')}>Products</button>
        <button className={tab === 'orders' ? 'on' : ''} onClick={() => setTab('orders')}>Orders</button>
      </div>
      {tab === 'products' ? <ProductsTab /> : <OrdersTab />}
    </>
  );
}
