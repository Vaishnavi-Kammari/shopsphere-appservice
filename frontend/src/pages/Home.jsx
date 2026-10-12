import { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, errorMessage } from '../api.js';
import ProductCard from '../components/ProductCard.jsx';

export default function Home() {
  const navigate = useNavigate();
  const [query, setQuery] = useState('');
  const [products, setProducts] = useState([]);
  const [categories, setCategories] = useState([]);
  const [error, setError] = useState('');

  useEffect(() => {
    Promise.all([api.get('/products'), api.get('/categories')])
      .then(([p, c]) => { setProducts(p.data.slice(0, 8)); setCategories(c.data); })
      .catch((e) => setError(errorMessage(e)));
  }, []);

  const submit = (e) => {
    e.preventDefault();
    navigate(`/products?search=${encodeURIComponent(query)}`);
  };

  return (
    <>
      <section className="hero">
        <h1>Everything you need, one sphere away.</h1>
        <p>Browse the catalogue, fill your cart and check out in a minute.</p>
        <form className="search" onSubmit={submit}>
          <input value={query} onChange={(e) => setQuery(e.target.value)} placeholder="Search headphones, books, backpacks..." />
          <button className="btn">Search</button>
        </form>
        <div className="chips">
          {categories.map((c) => (
            <Link key={c.id} to={`/products?categoryId=${c.id}`} className="chip">{c.name}</Link>
          ))}
        </div>
      </section>
      <h2 className="section-title">Featured products</h2>
      {error && <p className="alert error">{error}</p>}
      <div className="grid">{products.map((p) => <ProductCard key={p.id} product={p} />)}</div>
    </>
  );
}
