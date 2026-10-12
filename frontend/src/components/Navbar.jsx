import { Link, NavLink, useNavigate } from 'react-router-dom';
import { useAuth } from '../AuthContext.jsx';
import { useCart } from '../CartContext.jsx';

export default function Navbar() {
  const { user, isAdmin, logout } = useAuth();
  const { cart } = useCart();
  const navigate = useNavigate();

  return (
    <header className="nav">
      <div className="nav-inner">
        <Link to="/" className="brand"><span className="brand-dot" />shopsphere</Link>
        <nav className="nav-links">
          <NavLink to="/products">Products</NavLink>
          {user && <NavLink to="/orders">Orders</NavLink>}
          {isAdmin && <NavLink to="/admin">Admin</NavLink>}
        </nav>
        <div className="nav-actions">
          {user && (
            <NavLink to="/cart" className="cart-link">
              Cart{cart.itemCount > 0 && <span className="badge">{cart.itemCount}</span>}
            </NavLink>
          )}
          {user ? (
            <>
              <span className="who">{user.name}</span>
              <button className="btn ghost" onClick={() => { logout(); navigate('/'); }}>Log out</button>
            </>
          ) : (
            <Link to="/login" className="btn">Log in</Link>
          )}
        </div>
      </div>
    </header>
  );
}
