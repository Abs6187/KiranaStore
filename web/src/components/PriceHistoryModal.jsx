import React from 'react';
import { X, History, TrendingUp, TrendingDown, Clock, ShieldCheck } from 'lucide-react';

export default function PriceHistoryModal({ product, historyLogs, isOpen, onClose }) {
  if (!isOpen) return null;

  const filteredLogs = product 
    ? historyLogs.filter(log => String(log.productId) === String(product.id))
    : historyLogs;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-lg rounded-2xl glass-panel p-6 border border-slate-800 shadow-2xl relative flex flex-col max-h-[85vh]">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-indigo-500/10 text-indigo-400">
              <History className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-100">
                {product ? `${product.name} - Price Audit Log` : 'All Price Change Audit Logs'}
              </h2>
              <p className="text-xs text-slate-400">
                Immutable audit trail recorded in Firestore
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Audit List */}
        <div className="flex-1 overflow-y-auto py-4 space-y-3 pr-1">
          {filteredLogs.length === 0 ? (
            <div className="text-center py-10 text-slate-500">
              <ShieldCheck className="w-8 h-8 mx-auto mb-2 opacity-50 text-indigo-400" />
              <p className="text-sm font-medium">No price changes recorded yet</p>
            </div>
          ) : (
            filteredLogs.map((log) => {
              const isIncrease = log.newPrice > log.oldPrice;
              const diff = (log.newPrice - log.oldPrice).toFixed(2);

              return (
                <div 
                  key={log.id} 
                  className="p-3.5 rounded-xl bg-slate-900/90 border border-slate-800/80 flex items-center justify-between gap-3"
                >
                  <div className="flex items-center gap-3">
                    <div className={`p-2 rounded-lg ${isIncrease ? 'bg-rose-500/10 text-rose-400' : 'bg-emerald-500/10 text-emerald-400'}`}>
                      {isIncrease ? <TrendingUp className="w-4 h-4" /> : <TrendingDown className="w-4 h-4" />}
                    </div>
                    <div>
                      <h4 className="text-sm font-bold text-slate-200">
                        {log.productName}
                      </h4>
                      <div className="flex items-center gap-2 text-xs text-slate-400">
                        <Clock className="w-3 h-3 text-slate-500" />
                        {log.changedAt ? new Date(log.changedAt).toLocaleString('en-IN') : 'Just now'}
                        <span>•</span>
                        <span className="text-slate-400 italic">{log.changeReason || 'Update'}</span>
                      </div>
                    </div>
                  </div>

                  <div className="text-right">
                    <div className="text-sm font-bold text-slate-100">
                      ₹{log.newPrice}
                    </div>
                    <div className="text-xs text-slate-500 line-through">
                      ₹{log.oldPrice}
                    </div>
                  </div>
                </div>
              );
            })
          )}
        </div>

        {/* Footer */}
        <div className="pt-3 border-t border-slate-800 text-center">
          <button
            onClick={onClose}
            className="w-full py-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white text-sm font-medium"
          >
            Close History
          </button>
        </div>
      </div>
    </div>
  );
}
