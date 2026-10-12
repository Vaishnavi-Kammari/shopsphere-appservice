import { useEffect, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import { api, errorMessage } from '../api.js';
import ProductCard from '../components/ProductCard.jsx';

export default function Products() {
  const [params, setParams] = useSearchParams();
  const search = params.get('search') || '';
  const categoryId = params.get('categoryId') || '';
  const [text, setText] = useState(search);
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => { api.get('/categories').then((r) => setCategories(r.data)).catch(() => {}); }, []);
  useEffect(() => setText(search), [search]);

  useEffect(() => {
    setLoading(true);
    api.get('/products', { params: { search: search || undefined, categoryId: categoryId || undefined } })
      .then((r) => { setProducts(r.data); setError(''); })
      .catch((e) => setError(errorMessage(e)))
      .finally(() => setLoading(false));
  }, [search, categoryId]);

  const update = (next) => {
    const p = new URLSearchParams(params);
    Object.entries(next).forEach(([k, v]) => (v ? p.set(k, v) : p.delete(k)));
    setParams(p);
  };

  return (
    <>
      <h1>Products</h1>
      <form className="toolbar" onSubmit={(e) => { e.preventDefault(); update({ search: text.trim() }); }}>
        <input value={text} onChange={(e) => setText(e.target.value)} placeholder="Search products" />
        <select value={categoryId} onChange={(e) => update({ categoryId: e.target.value })} aria-label="Category">
          <option value="">All categories</option>
          {categories.map((c) => <option key={c.id} value={c.id}>{c.name}</option>)}
        </select>
        <button className="btn">Search</button>
      </form>
      {error && <p className="alert error">{error}</p>}
      {loading ? <p className="muted">Loading...</p>
        : products.length === 0 ? <p className="empty">No products match your search.</p>
        : <div className="grid">{products.map((p) => <ProductCard key={p.id} product={p} />)}</div>}
    </>
  );
}
