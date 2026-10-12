import { Link } from 'react-router-dom';
import { money } from '../api.js';
import ProductImage from './ProductImage.jsx';

export default function ProductCard({ product }) {
  return (
    <Link to={`/products/${product.id}`} className="card">
      <ProductImage product={product} />
      <div className="card-body">
        <span className="muted small">{product.categoryName}</span>
        <h3>{product.name}</h3>
        <div className="card-foot">
          <strong>{money(product.price)}</strong>
          {product.stock === 0 ? <span className="tag out">Out of stock</span>
            : product.stock <= 10 ? <span className="tag low">Only {product.stock} left</span> : null}
        </div>
      </div>
    </Link>
  );
}
