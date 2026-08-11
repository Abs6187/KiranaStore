import React, { useState } from 'react';
import { Pin, History, Edit2, Check, X, Trash2, TrendingUp, Tag, Plus, Minus, MoreHorizontal } from 'lucide-react';
import { updateSalesPrice, togglePinProduct, deleteProduct } from '../services/productService';
import { announce, speakText } from '../services/voiceService';

export default function ProductCard({ product, onOpenHistory, simpleMode = false }) {
  const [isEditing, setIsEditing] = useState(false);
  const [newPrice, setNewPrice] = useState(product.salesPrice);
  const [isUpdating, setIsUpdating] = useState(false);
  const [showMore, setShowMore] = useState(false);

  const handleSavePrice = async () => {
    const val = parseFloat(newPrice);
    if (isNaN(val) || val === product.salesPrice) {
      setIsEditing(false);
      return;
    }
    if (val <= (product.purchasePrice || 0) && product.purchasePrice > 0) {
      speakText("Chetavni: Selling price khareed daam se kam hai! Loss hoga.");
    }
    setIsUpdating(true);
    await updateSalesPrice(product.id, product.name, product.salesPrice, val, "Manual Web Edit");
    speakText(`${product.name} ka price ₹${val} ho gaya`);
    setIsUpdating(false);
    setIsEditing(false);
  };

  const adjustPrice = async (delta) => {
    const current = parseFloat(product.salesPrice) || 0;
    const next = Math.max(0, Math.round((current + delta) * 100) / 100);
    if (next === current) return;
    if (next <= (product.purchasePrice || 0) && product.purchasePrice > 0) {
      speakText("Chetavni: Selling price khareed daam se kam hai!");
    }
    setIsUpdating(true);
    await updateSalesPrice(product.id, product.name, current, next, "Stepper Button");
    speakText(`${product.name} ka price ₹${next} ho gaya`);
    setIsUpdating(false);
  };

  const handlePin = async () => {
    await togglePinProduct(product.id, product.isPinned);
    speakText(product.isPinned ? `${product.name} top se unpin ho gaya` : `${product.name} top par pin ho gaya`);
  };

  const handleDelete = async () => {
    if (window.confirm(`Are you sure you want to delete ${product.name}?`)) {
      await deleteProduct(product.id);
      speakText(`${product.name} catalogue se hata diya gaya hai`);
    }
  };

  const margin = (product.salesPrice - (product.purchasePrice || 0)).toFixed(2);

  return (
    <div className={`relative rounded-2xl glass-panel border flex flex-col justify-between transition-all ${
      product.isPinned ? 'border-amber-500/40 bg-slate-900/90' : 'border-slate-800/80'
    } ${simpleMode ? 'p-5 space-y-3' : 'p-4 card-elevation'}`}>
      
      <div>
        {/* Top Bar: Category & Actions */}
        <div className="flex items-center justify-between mb-3">
          <span className={`inline-flex items-center gap-1 font-semibold px-2.5 py-1 rounded-md bg-slate-800/80 text-slate-300 border border-slate-700/50 ${simpleMode ? 'text-xs' : 'text-[11px]'}`}>
            <Tag className={`text-slate-400 ${simpleMode ? 'w-4 h-4' : 'w-3 h-3'}`} />
            {product.category || 'General'}
          </span>
          
          {simpleMode ? (
            <button
              onClick={() => setShowMore((v) => !v)}
              className="p-2 rounded-lg text-slate-400 hover:text-slate-200 hover:bg-slate-800 transition-all"
              title="More actions"
            >
              <MoreHorizontal className="w-5 h-5" />
            </button>
          ) : (
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
          )}
        </div>

        {/* Expanded More Actions (Simple Mode) */}
        {simpleMode && showMore && (
          <div className="flex items-center justify-end gap-2 mb-3 pb-3 border-b border-slate-800/80">
            <button
              onClick={handlePin}
              className={`flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold transition-all ${product.isPinned ? 'text-amber-400 bg-amber-400/10' : 'text-slate-400 hover:text-slate-200 hover:bg-slate-800'}`}
            >
              <Pin className={`w-3.5 h-3.5 ${product.isPinned ? 'fill-amber-400' : ''}`} />
              {product.isPinned ? 'Unpin' : 'Pin'}
            </button>
            <button
              onClick={() => onOpenHistory(product)}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold text-slate-400 hover:text-indigo-400 hover:bg-indigo-500/10 transition-all"
            >
              <History className="w-3.5 h-3.5" /> History
            </button>
            <button
              onClick={handleDelete}
              className="flex items-center gap-1.5 px-3 py-1.5 rounded-lg text-xs font-semibold text-rose-400 hover:bg-rose-500/10 transition-all"
            >
              <Trash2 className="w-3.5 h-3.5" /> Delete
            </button>
          </div>
        )}

        {/* Product Name */}
        <h3 className={`font-extrabold text-slate-100 mb-1 leading-snug break-words ${simpleMode ? 'text-xl sm:text-2xl' : 'text-base'}`}>
          {product.name}
        </h3>
        <p className={`text-slate-400 mb-3 ${simpleMode ? 'text-sm font-semibold' : 'text-xs'}`}>
          Unit: <span className="text-slate-200 font-bold">{product.unit || 'kg'}</span>
        </p>
      </div>

      {/* Price Block */}
      {simpleMode ? (
        /* SIMPLE MODE CLEAN STACKED PRICE & STEPPERS */
        <div className="pt-3 border-t border-slate-800/80 space-y-3">
          <div className="flex items-baseline justify-between gap-2">
            <div>
              <span className="text-[11px] uppercase tracking-wider font-bold text-slate-400 block mb-0.5">
                Selling Price
              </span>
              <span className="text-3xl sm:text-4xl font-black text-emerald-400 tracking-tight truncate block max-w-[200px]" title={`₹${product.salesPrice}`}>
                ₹{product.salesPrice}
              </span>
            </div>

            {product.purchasePrice > 0 && (
              <div className="text-right">
                <span className="text-xs text-slate-400 block font-medium">Buy: ₹{product.purchasePrice}</span>
                <span className="inline-flex items-center font-bold text-teal-400 text-xs">
                  <TrendingUp className="w-3.5 h-3.5 mr-0.5" /> +₹{margin}
                </span>
              </div>
            )}
          </div>

          {/* Quick Stepper Buttons Grid */}
          <div className="grid grid-cols-4 gap-1.5 pt-1">
            <button
              onClick={() => adjustPrice(-10)}
              disabled={isUpdating}
              className="py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:bg-slate-800 active:bg-slate-700 font-black text-xs sm:text-sm flex items-center justify-center gap-0.5 transition-all"
              title="Decrease price by ₹10"
            >
              <Minus className="w-3.5 h-3.5 stroke-[3]" />10
            </button>
            <button
              onClick={() => adjustPrice(-5)}
              disabled={isUpdating}
              className="py-2.5 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:bg-slate-800 active:bg-slate-700 font-black text-xs sm:text-sm flex items-center justify-center gap-0.5 transition-all"
              title="Decrease price by ₹5"
            >
              <Minus className="w-3.5 h-3.5 stroke-[3]" />5
            </button>
            <button
              onClick={() => adjustPrice(5)}
              disabled={isUpdating}
              className="py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 active:scale-95 text-slate-950 font-black text-xs sm:text-sm flex items-center justify-center gap-0.5 shadow-md shadow-emerald-500/20 transition-all"
              title="Increase price by ₹5"
            >
              <Plus className="w-3.5 h-3.5 stroke-[3]" />5
            </button>
            <button
              onClick={() => adjustPrice(10)}
              disabled={isUpdating}
              className="py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 active:scale-95 text-slate-950 font-black text-xs sm:text-sm flex items-center justify-center gap-0.5 shadow-md shadow-emerald-500/20 transition-all"
              title="Increase price by ₹10"
            >
              <Plus className="w-3.5 h-3.5 stroke-[3]" />10
            </button>
          </div>
        </div>
      ) : (
        /* STANDARD MODE COMPACT FLEX PRICE DISPLAY */
        <div className="pt-3 border-t border-slate-800/80 flex items-end justify-between">
          <div>
            <span className="uppercase tracking-wider font-semibold text-slate-400 block mb-0.5 text-[10px]">
              Selling Price
            </span>
            {isEditing ? (
              <div className="flex flex-col gap-1">
                <div className="flex items-center gap-1.5">
                  <span className="text-emerald-400 font-bold text-lg">₹</span>
                  <input
                    type="number"
                    step="0.01"
                    value={newPrice}
                    onChange={(e) => setNewPrice(e.target.value)}
                    autoFocus
                    className={`w-20 bg-slate-950 border rounded-lg px-2 py-1 text-base font-bold focus:outline-none ${
                      Number(newPrice) > 0 && product.purchasePrice > 0 && Number(newPrice) <= product.purchasePrice
                        ? 'border-amber-500 text-amber-400'
                        : 'border-emerald-500 text-white'
                    }`}
                  />
                  <button
                    onClick={handleSavePrice}
                    disabled={isUpdating}
                    className="p-1 rounded-md bg-emerald-500 text-slate-950 hover:bg-emerald-400"
                    title="Save Price"
                  >
                    <Check className="w-4 h-4 stroke-[3]" />
                  </button>
                  <button
                    onClick={() => setIsEditing(false)}
                    className="p-1 rounded-md bg-slate-800 text-slate-400 hover:text-white"
                    title="Cancel"
                  >
                    <X className="w-4 h-4" />
                  </button>
                </div>
                {Number(newPrice) > 0 && product.purchasePrice > 0 && Number(newPrice) <= product.purchasePrice && (
                  <div className="text-[10px] font-bold text-amber-400 bg-amber-500/10 px-1.5 py-0.5 rounded border border-amber-500/20">
                    ⚠️ Khareed daam (₹{product.purchasePrice}) se kam hai! Loss hoga.
                  </div>
                )}
              </div>
            ) : (
              <div className="flex items-baseline gap-1 group/price cursor-pointer" onClick={() => setIsEditing(true)}>
                <span className="text-2xl font-black text-emerald-400 tracking-tight truncate max-w-[140px] block" title={`₹${product.salesPrice}`}>
                  ₹{product.salesPrice}
                </span>
                <Edit2 className="w-3.5 h-3.5 text-slate-500 opacity-0 group-hover/price:opacity-100 transition-opacity shrink-0" />
              </div>
            )}
          </div>

          {/* Purchase Price & Margin */}
          {product.purchasePrice > 0 && (
            <div className="text-right shrink-0">
              <span className="text-slate-500 block text-[10px]">Buy: ₹{product.purchasePrice}</span>
              <span className="inline-flex items-center font-semibold text-teal-400 text-xs">
                <TrendingUp className="mr-0.5 w-3 h-3" /> +₹{margin}
              </span>
            </div>
          )}
        </div>
      )}
    </div>
  );
}
