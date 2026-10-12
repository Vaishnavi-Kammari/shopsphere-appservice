import { useEffect, useState } from 'react';
import { useLocation } from 'react-router-dom';
import { api, errorMessage, money } from '../api.js';

export function StatusBadge({ status }) {
  return <span className={`status ${status.toLowerCase()}`}>{status}</span>;
}

export function OrderItems({ items }) {
  return (
    <table className="table compact">
      <tbody>
        {items.map((i, idx) => (
          <tr key={idx}>
            <td>{i.productName}</td>
            <td>{i.quantity} x {money(i.unitPrice)}</td>
            <td className="right">{money(i.lineTotal)}</td>
          </tr>
        ))}
      </tbody>
    </table>
  );
}

export default function Orders() {
  const location = useLocation();
  const [orders, setOrders] = useState(null);
  const [error, setError] = useState('');

  useEffect(() => {
    api.get('/orders').then((r) => setOrders(r.data)).catch((e) => setError(errorMessage(e)));
  }, []);

  return (
    <>
      <h1>Your orders</h1>
      {location.state?.placed && <p className="alert ok">Order placed. Thank you!</p>}
      {error && <p className="alert error">{error}</p>}
      {orders === null ? <p className="muted">Loading...</p>
        : orders.length === 0 ? <p className="empty">You have not placed any orders yet.</p>
        : orders.map((o) => (
          <section className="order" key={o.id}>
            <header>
              <div><strong>Order #{o.id}</strong> <span className="muted small">{new Date(o.createdAt).toLocaleString()}</span></div>
              <StatusBadge status={o.status} />
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
