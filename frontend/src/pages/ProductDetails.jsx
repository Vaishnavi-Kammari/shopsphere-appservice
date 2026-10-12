import { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useParams } from 'react-router-dom';
import { api, errorMessage, money } from '../api.js';
import { useAuth } from '../AuthContext.jsx';
import { useCart } from '../CartContext.jsx';
import ProductImage from '../components/ProductImage.jsx';

export default function ProductDetails() {
  const { id } = useParams();
  const { user } = useAuth();
  const { addItem } = useCart();
  const navigate = useNavigate();
  const location = useLocation();
  const [product, setProduct] = useState(null);
  const [quantity, setQuantity] = useState(1);
  const [message, setMessage] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get(`/products/${id}`).then((r) => setProduct(r.data)).catch((e) => setError(errorMessage(e)));
  }, [id]);

  if (error) return <p className="alert error">{error}</p>;
  if (!product) return <p className="muted">Loading...</p>;

  const add = async () => {
    if (!user) return navigate('/login', { state: { from: location.pathname } });
    try {
      await addItem(product.id, quantity);
      setMessage({ type: 'ok', text: `Added ${quantity} to your cart.` });
    } catch (e) {
      setMessage({ type: 'error', text: errorMessage(e) });
    }
  };

  return (
    <>
      <Link to="/products" className="muted">&larr; All products</Link>
      <div className="detail">
        <ProductImage product={product} className="detail-img" />
        <div>
          <span className="muted">{product.categoryName}</span>
          <h1>{product.name}</h1>
          <p className="price">{money(product.price)}</p>
          <p>{product.description}</p>
          <p className={product.stock === 0 ? 'out-text' : 'muted'}>
            {product.stock === 0 ? 'Out of stock' : `${product.stock} in stock`}
          </p>
          {product.stock > 0 && (
            <div className="row">
              <input type="number" min="1" max={product.stock} value={quantity} className="qty"
                     onChange={(e) => setQuantity(Math.max(1, Math.min(product.stock, Number(e.target.value) || 1)))} />
              <button className="btn" onClick={add}>Add to cart</button>
            </div>
          )}
          {message && <p className={`alert ${message.type}`}>{message.text} {message.type === 'ok' && <Link to="/cart">View cart</Link>}</p>}
        </div>
      </div>
    </>
  );
}
