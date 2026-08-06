import React from 'react';
import { Store, Pin, Settings, Plus, Search, Sparkles } from 'lucide-react';

export default function Navbar({ 
  searchQuery, 
  setSearchQuery, 
  onOpenAddModal, 
  onOpenSettings,
  totalCount,
  pinnedCount 
}) {
  return (
    <header className="sticky top-0 z-30 glass-nav px-4 sm:px-8 py-3.5 flex items-center justify-between gap-4">
      {/* Brand Title */}
      <div className="flex items-center gap-3">
        <div className="w-10 h-10 rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20">
          <Store className="w-5 h-5 text-slate-950 font-bold" />
        </div>
        <div>
          <h1 className="text-lg sm:text-xl font-bold tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent">
            KiranaStore
          </h1>
          <div className="flex items-center gap-2 text-xs text-slate-400 font-medium">
            <span className="flex items-center gap-1 text-emerald-400">
              <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Live Firestore
            </span>
            <span>•</span>
            <span className="flex items-center gap-1">
              <Pin className="w-3 h-3 text-amber-400 fill-amber-400" /> {pinnedCount} Pinned
            </span>
          </div>
        </div>
      </div>

      {/* Search Bar */}
      <div className="flex-1 max-w-md hidden sm:block relative">
        <Search className="w-4 h-4 text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2" />
        <input 
          type="text"
          placeholder="Search products by name or category..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className="w-full bg-slate-900/80 border border-slate-800 rounded-xl pl-10 pr-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20 transition-all"
        />
      </div>

      {/* Action Buttons */}
      <div className="flex items-center gap-2">
        <button
          onClick={onOpenAddModal}
          className="flex items-center gap-2 px-3.5 py-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 active:scale-95 text-slate-950 font-semibold text-sm shadow-lg shadow-emerald-500/25 transition-all"
        >
          <Plus className="w-4 h-4 stroke-[3]" />
          <span className="hidden xs:inline">Add Item</span>
        </button>

        <button
          onClick={onOpenSettings}
          className="p-2.5 rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-300 hover:text-white active:scale-95 transition-all"
          title="Settings & Diagnostics"
        >
          <Settings className="w-4 h-4" />
        </button>
      </div>
    </header>
  );
}
