import React, { useState, useEffect, useRef } from 'react';
import { X, Check, Sparkles, Mic, AlertTriangle, Edit3, Loader2, Package } from 'lucide-react';
import { addProduct, updateSalesPrice } from '../services/productService';
import { speakText } from '../services/voiceService';
import { getAIProductSuggestions } from '../services/aiService';

export default function VoiceConfirmModal({ isOpen, onClose, parsedData, simpleMode = false }) {
  const [name, setName] = useState('');
  const [salesPrice, setSalesPrice] = useState('');
  const [purchasePrice, setPurchasePrice] = useState('');
  const [unit, setUnit] = useState('kg');
  const [category, setCategory] = useState('General');
  const [suggestions, setSuggestions] = useState([]);
  const [isLoadingSuggestions, setIsLoadingSuggestions] = useState(false);
  const [isSubmitting, setIsSubmitting] = useState(false);
  const nameInputRef = useRef(null);

  useEffect(() => {
    if (parsedData) {
      setName(parsedData.productName || parsedData.targetProduct?.name || '');
      setSalesPrice(parsedData.price || parsedData.targetProduct?.salesPrice || '');
      setPurchasePrice(parsedData.purchasePrice || parsedData.targetProduct?.purchasePrice || '');
      setUnit(parsedData.unit || parsedData.targetProduct?.unit || 'kg');
      setCategory(parsedData.category || parsedData.targetProduct?.category || 'General');
    }
  }, [parsedData]);

  useEffect(() => {
    if (!name.trim() || name.trim().length < 2) {
      setSuggestions([]);
      return;
    }

    const timer = setTimeout(async () => {
      setIsLoadingSuggestions(true);
      const items = await getAIProductSuggestions(name);
      setSuggestions(items);
      setIsLoadingSuggestions(false);
    }, 350);

    return () => clearTimeout(timer);
  }, [name]);

  if (!isOpen || !parsedData) return null;

  const selectSuggestion = (item) => {
    setName(item.name);
    if (item.unit) setUnit(item.unit);
    if (item.category) setCategory(item.category);
    setSuggestions([]);
    setTimeout(() => {
      if (nameInputRef.current) {
        nameInputRef.current.focus();
        nameInputRef.current.setSelectionRange(item.name.length, item.name.length);
      }
    }, 50);
  };

  const handleConfirm = async (e) => {
    e.preventDefault();
    if (!name.trim()) {
      speakText("Kripya product ka naam dhaley.");
      return;
    }
    if (!salesPrice) {
      speakText("Kripya selling price dhaley.");
      return;
    }

    // Voice warning when Selling Price <= Purchase Price
    if (Number(salesPrice) > 0 && Number(purchasePrice) > 0 && Number(salesPrice) <= Number(purchasePrice)) {
      speakText("Selling price khareed daam se kam hai! Loss hoga.");
    }

    setIsSubmitting(true);
    try {
      if (parsedData.action === 'update_price' && parsedData.targetProduct) {
        await updateSalesPrice(
          parsedData.targetProduct.id,
          name.trim(),
          parsedData.targetProduct.salesPrice,
          parseFloat(salesPrice),
          "Voice Command (Confirmed)"
        );
        speakText(`${name.trim()} ka naya price ₹${salesPrice} confirm ho gaya`);
      } else {
        await addProduct({
          name: name.trim(),
          salesPrice: parseFloat(salesPrice),
          purchasePrice: purchasePrice ? parseFloat(purchasePrice) : 0,
          unit,
          category
        });
        speakText(`${name.trim()} ₹${salesPrice} mein catalogue mein save ho gaya`);
      }
      onClose();
    } catch (err) {
      console.error("Failed to commit voice command:", err);
    } finally {
      setIsSubmitting(false);
    }
  };

  const isLossWarning = Number(salesPrice) > 0 && Number(purchasePrice) > 0 && Number(salesPrice) <= Number(purchasePrice);

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-md animate-in fade-in duration-200">
      <div className="w-full max-w-md rounded-2xl glass-panel p-6 border border-emerald-500/40 shadow-2xl relative bg-slate-900/95">
        {/* Header */}
        <div className="flex items-center justify-between pb-3 mb-3 border-b border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-emerald-500/20 text-emerald-400">
              <Sparkles className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-base font-bold text-slate-100">Confirm Voice Input</h2>
              <p className="text-[11px] text-slate-400">Review & edit before saving to database</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Raw Voice Transcript Box */}
        <div className="p-3 rounded-xl bg-slate-950 border border-slate-800 mb-4 flex items-start gap-2.5">
          <Mic className="w-4 h-4 text-emerald-400 shrink-0 mt-0.5" />
          <div>
            <span className="text-[10px] font-bold text-slate-400 uppercase tracking-wider block mb-0.5">
              Heard Voice Command:
            </span>
            <p className="text-xs font-semibold text-emerald-300 italic">
              "{parsedData.rawTranscript || parsedData.replyText}"
            </p>
          </div>
        </div>

        {/* Confirmation Form */}
        <form onSubmit={handleConfirm} className="space-y-4">
          {/* Product Name & AI Spell Suggestions */}
          <div>
            <div className="flex items-center justify-between mb-1.5">
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Product Name (Edit/Correct) *
              </label>
              {isLoadingSuggestions && (
                <span className="flex items-center gap-1 text-[11px] text-emerald-400 font-medium">
                  <Loader2 className="w-3 h-3 animate-spin" /> Checking spelling...
                </span>
              )}
            </div>
            <div className="relative">
              <input
                ref={nameInputRef}
                type="text"
                required
                value={name}
                onChange={(e) => setName(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm font-bold text-slate-100 focus:outline-none focus:border-emerald-500"
              />
            </div>

            {/* AI Suggestions Chips */}
            {suggestions.length > 0 && (
              <div className="mt-2 p-2.5 rounded-xl bg-slate-950 border border-emerald-500/30 space-y-1.5">
                <div className="flex items-center gap-1.5 text-[11px] font-bold text-emerald-400 uppercase tracking-wider">
                  <Sparkles className="w-3.5 h-3.5" />
                  Gemini AI Corrected Spelling Suggestions (Click to auto-fill):
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {suggestions.map((item, index) => (
                    <button
                      key={index}
                      type="button"
                      onMouseDown={(e) => {
                        e.preventDefault();
                        selectSuggestion(item);
                      }}
                      onClick={() => selectSuggestion(item)}
                      className="px-2.5 py-1 rounded-lg bg-emerald-500/10 hover:bg-emerald-500/20 border border-emerald-500/30 text-xs font-semibold text-emerald-300 flex items-center gap-1 transition-all active:scale-95 cursor-pointer"
                    >
                      <span>{item.name}</span>
                      <span className="text-[10px] text-slate-400">({item.unit || 'unit'})</span>
                    </button>
                  ))}
                </div>
              </div>
            )}
          </div>

          {/* Pricing Fields */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Selling Price (₹) *
              </label>
              <input
                type="number"
                step="0.01"
                required
                value={salesPrice}
                onChange={(e) => setSalesPrice(e.target.value)}
                className={`w-full bg-slate-950 border rounded-xl px-3.5 py-2.5 text-sm font-bold focus:outline-none ${
                  isLossWarning ? 'border-amber-500 text-amber-400' : 'border-slate-800 text-emerald-400 focus:border-emerald-500'
                }`}
              />
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Purchase Price (₹)
              </label>
              <input
                type="number"
                step="0.01"
                placeholder="150"
                value={purchasePrice}
                onChange={(e) => setPurchasePrice(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-300 focus:outline-none focus:border-emerald-500"
              />
            </div>
          </div>

          {/* Loss Warning Banner */}
          {isLossWarning && (
            <div className="p-3 rounded-xl bg-amber-500/10 border border-amber-500/30 text-amber-300 text-xs space-y-1">
              <div className="flex items-center gap-1.5 font-bold text-amber-400">
                <AlertTriangle className="w-4 h-4" />
                <span>Chetaavani (चेतावनी):</span>
              </div>
              <p className="text-[11px] leading-relaxed text-amber-200">
                Selling price (₹{salesPrice}) khareed daam (₹{purchasePrice}) se kam hai! <strong className="text-amber-400">Loss: ₹{(Number(purchasePrice) - Number(salesPrice)).toFixed(2)}</strong>
              </p>
            </div>
          )}

          {/* Unit & Category */}
          <div className="grid grid-cols-2 gap-3">
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Unit
              </label>
              <select
                value={unit}
                onChange={(e) => setUnit(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
              >
                <option value="kg">kg</option>
                <option value="litre">litre</option>
                <option value="packet">packet</option>
                <option value="piece">piece</option>
                <option value="gm">gm</option>
              </select>
            </div>
            <div>
              <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
                Category
              </label>
              <select
                value={category}
                onChange={(e) => setCategory(e.target.value)}
                className="w-full bg-slate-950 border border-slate-800 rounded-xl px-3.5 py-2.5 text-sm text-slate-200 focus:outline-none focus:border-emerald-500"
              >
                <option value="General">General</option>
                <option value="Oils & Ghee">Oils & Ghee</option>
                <option value="Grains & Pulses">Grains & Pulses</option>
                <option value="Spices & Salt">Spices & Salt</option>
                <option value="Snacks & Tea">Snacks & Tea</option>
              </select>
            </div>
          </div>

          {/* Actions */}
          <div className="pt-3 flex items-center justify-end gap-2">
            <button
              type="button"
              onClick={onClose}
              className="px-4 py-2.5 rounded-xl bg-slate-950 border border-slate-800 text-slate-400 hover:text-white text-xs font-bold transition-all"
            >
              Cancel
            </button>
            <button
              type="submit"
              disabled={isSubmitting}
              className="flex items-center gap-2 px-5 py-2.5 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 font-extrabold text-xs shadow-lg shadow-emerald-500/20 active:scale-95 transition-all"
            >
              <Check className="w-4 h-4 stroke-[3]" />
              {isSubmitting ? 'Saving to Database...' : 'Confirm & Save to DB'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
}
