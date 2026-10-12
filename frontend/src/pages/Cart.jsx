import { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { api, errorMessage, money } from '../api.js';
import { useCart } from '../CartContext.jsx';
import ProductImage from '../components/ProductImage.jsx';

export default function Cart() {
  const { cart, setQuantity, removeItem, refresh } = useCart();
  const navigate = useNavigate();
  const [address, setAddress] = useState('');
  const [error, setError] = useState('');
  const [busy, setBusy] = useState(false);

  const guard = async (fn) => {
    setError('');
    try { await fn(); } catch (e) { setError(errorMessage(e)); }
  };

  const checkout = (e) => {
    e.preventDefault();
    guard(async () => {
      setBusy(true);
      try {
        await api.post('/orders', { shippingAddress: address });
        await refresh();
        navigate('/orders', { state: { placed: true } });
      } finally {
        setBusy(false);
      }
    });
  };

  if (cart.items.length === 0) {
    return (
      <>
        <h1>Your cart</h1>
        <p className="empty">Your cart is empty. <Link to="/products">Browse products</Link></p>
      </>
    );
  }

  return (
    <>
      <h1>Your cart</h1>
      {error && <p className="alert error">{error}</p>}
      <div className="cart-layout">
        <div className="cart-items">
          {cart.items.map((item) => (
            <div className="cart-row" key={item.productId}>
              <ProductImage product={{ id: item.productId, name: item.name, imageUrl: item.imageUrl }} className="thumb" />
              <div className="grow">
                <Link to={`/products/${item.productId}`}><strong>{item.name}</strong></Link>
                <div className="muted small">{money(item.unitPrice)} each - {item.stock} in stock</div>
              </div>
              <div className="stepper">
                <button aria-label="Decrease" disabled={item.quantity <= 1}
                        onClick={() => guard(() => setQuantity(item.productId, item.quantity - 1))}>-</button>
                <span>{item.quantity}</span>
                <button aria-label="Increase" disabled={item.quantity >= item.stock}
                        onClick={() => guard(() => setQuantity(item.productId, item.quantity + 1))}>+</button>
              </div>
              <strong className="line">{money(item.lineTotal)}</strong>
              <button className="btn ghost" onClick={() => guard(() => removeItem(item.productId))}>Remove</button>
            </div>
          ))}
        </div>
        <form className="summary" onSubmit={checkout}>
          <h2>Checkout</h2>
          <div className="sum-line"><span>Items</span><span>{cart.itemCount}</span></div>
          <div className="sum-line total"><span>Total</span><span>{money(cart.total)}</span></div>
          <label>Shipping address
            <textarea required maxLength={500} rows={3} value={address} onChange={(e) => setAddress(e.target.value)}
                      placeholder="Name, street, city, postal code" />
          </label>
          <button className="btn block" disabled={busy}>{busy ? 'Placing order...' : 'Place order'}</button>
          <p className="muted small">Demo only: no payment is taken.</p>
        </form>
      </div>
    </>
  );
}
