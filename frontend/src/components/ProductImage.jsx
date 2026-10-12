import { useState } from 'react';

const TINTS = ['#cfe3ea', '#f3dcc3', '#d9e6cf', '#e5d6ea', '#f1d3d3', '#d6dcf0'];

/** Shows the product photo, or a tinted tile with the initial if there is no image / it fails to load. */
export default function ProductImage({ product, className = '' }) {
  const [failed, setFailed] = useState(false);
  if (product.imageUrl && !failed) {
    return (
      <img className={`pimg ${className}`} src={product.imageUrl} alt={product.name}
           loading="lazy" onError={() => setFailed(true)} />
    );
  }
  return (
    <div className={`pimg tile ${className}`} style={{ background: TINTS[(product.id || 0) % TINTS.length] }}
         role="img" aria-label={product.name}>
      {product.name?.[0]}
    </div>
  );
}
