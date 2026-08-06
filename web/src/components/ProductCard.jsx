import React, { useState } from 'react';
import { Pin, History, Edit2, Check, X, Trash2, TrendingUp, Tag } from 'lucide-react';
import { updateSalesPrice, togglePinProduct, deleteProduct } from '../services/productService';

export default function ProductCard({ product, onOpenHistory }) {
  const [isEditing, setIsEditing] = useState(false);
  const [newPrice, setNewPrice] = useState(product.salesPrice);
  const [isUpdating, setIsUpdating] = useState(false);

  const handleSavePrice = async () => {
    const val = parseFloat(newPrice);
    if (isNaN(val) || val === product.salesPrice) {
      setIsEditing(false);
      return;
    }
    setIsUpdating(true);
    await updateSalesPrice(product.id, product.name, product.salesPrice, val, "Manual Web Edit");
    setIsUpdating(false);
    setIsEditing(false);
  };

  const handlePin = async () => {
    await togglePinProduct(product.id, product.isPinned);
  };

  const handleDelete = async () => {
    if (window.confirm(`Are you sure you want to delete ${product.name}?`)) {
      await deleteProduct(product.id);
    }
  };

  const margin = (product.salesPrice - (product.purchasePrice || 0)).toFixed(2);

  return (
    <div className={`relative rounded-2xl glass-panel p-4 card-elevation border ${product.isPinned ? 'border-amber-500/40 bg-slate-900/90' : 'border-slate-800/80'} flex flex-col justify-between group`}>
      {/* Top Bar: Category & Pin */}
      <div className="flex items-center justify-between mb-3">
        <span className="inline-flex items-center gap-1 text-[11px] font-medium px-2.5 py-1 rounded-md bg-slate-800/80 text-slate-300 border border-slate-700/50">
          <Tag className="w-3 h-3 text-slate-400" />
          {product.category || 'General'}
        </span>
        <div className="flex items-center gap-1">
          <button
            onClick={handlePin}
            className={`p-1.5 rounded-lg transition-all ${product.isPinned ? 'text-amber-400 bg-amber-400/10' : 'text-slate-500 hover:text-slate-300'}`}
            title={product.isPinned ? "Unpin product" : "Pin product to top"}
          >
            <Pin className={`w-4 h-4 ${product.isPinned ? 'fill-amber-400' : ''}`} />
          </button>
          <button
            onClick={() => onOpenHistory(product)}
            className="p-1.5 rounded-lg text-slate-400 hover:text-indigo-400 hover:bg-indigo-500/10 transition-all"
            title="View Price History Log"
          >
            <History className="w-4 h-4" />
          </button>
          <button
            onClick={handleDelete}
            className="p-1.5 rounded-lg text-slate-500 hover:text-rose-400 hover:bg-rose-500/10 transition-all opacity-80 group-hover:opacity-100"
            title="Delete Product"
          >
            <Trash2 className="w-4 h-4" />
          </button>
        </div>
      </div>

      {/* Product Name */}
      <h3 className="text-base font-bold text-slate-100 mb-1 line-clamp-1">
        {product.name}
      </h3>
      <p className="text-xs text-slate-400 mb-4">
        Unit: <span className="text-slate-300 font-medium">{product.unit || 'kg'}</span>
      </p>

      {/* Price Display & Inline Editor */}
      <div className="pt-3 border-t border-slate-800/80 flex items-end justify-between">
        <div>
          <span className="text-[10px] uppercase tracking-wider font-semibold text-slate-400 block mb-0.5">
            Selling Price
          </span>
          {isEditing ? (
            <div className="flex items-center gap-1.5">
              <span className="text-emerald-400 font-bold text-lg">₹</span>
              <input
                type="number"
                step="0.01"
                value={newPrice}
                onChange={(e) => setNewPrice(e.target.value)}
                autoFocus
                className="w-20 bg-slate-950 border border-emerald-500 rounded-lg px-2 py-1 text-base font-bold text-white focus:outline-none"
              />
              <button
                onClick={handleSavePrice}
                disabled={isUpdating}
                className="p-1 rounded-md bg-emerald-500 text-slate-950 hover:bg-emerald-400"
              >
                <Check className="w-4 h-4 stroke-[3]" />
              </button>
              <button
                onClick={() => setIsEditing(false)}
                className="p-1 rounded-md bg-slate-800 text-slate-400 hover:text-white"
              >
                <X className="w-4 h-4" />
              </button>
            </div>
          ) : (
            <div className="flex items-baseline gap-1 group/price cursor-pointer" onClick={() => setIsEditing(true)}>
              <span className="text-2xl font-black text-emerald-400 tracking-tight">
                ₹{product.salesPrice}
              </span>
              <Edit2 className="w-3.5 h-3.5 text-slate-500 opacity-0 group-hover/price:opacity-100 transition-opacity" />
            </div>
          )}
        </div>

        {/* Purchase Price & Margin */}
        {product.purchasePrice > 0 && (
          <div className="text-right">
            <span className="text-[10px] text-slate-500 block">Buy: ₹{product.purchasePrice}</span>
            <span className="inline-flex items-center text-xs font-semibold text-teal-400">
              <TrendingUp className="w-3 h-3 mr-0.5" /> +₹{margin}
            </span>
          </div>
        )}
      </div>
    </div>
  );
}
