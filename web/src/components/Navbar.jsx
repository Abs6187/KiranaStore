import React from 'react';
import { Store, Pin, Settings, Plus, Search, Glasses, Eye } from 'lucide-react';
import { speakText } from '../services/voiceService';

export default function Navbar({ 
  searchQuery, 
  setSearchQuery, 
  onOpenAddModal, 
  onOpenSettings,
  onToggleSimpleMode,
  totalCount,
  pinnedCount,
  simpleMode = false
}) {
  const handleToggleSimpleMode = () => {
    if (onToggleSimpleMode) {
      const nextState = !simpleMode;
      onToggleSimpleMode();
      speakText(nextState ? "Simple elder mode chalu ho gaya hai" : "Simple mode band ho gaya hai");
    }
  };

  return (
    <header className="sticky top-0 z-30 glass-nav px-4 sm:px-8 py-3.5 flex items-center justify-between gap-4">
      {/* Brand Title */}
      <div className="flex items-center gap-3">
        <div className={`rounded-xl bg-gradient-to-tr from-emerald-500 to-teal-400 flex items-center justify-center shadow-lg shadow-emerald-500/20 ${simpleMode ? 'w-12 h-12' : 'w-10 h-10'}`}>
          <Store className={`text-slate-950 font-bold ${simpleMode ? 'w-6 h-6' : 'w-5 h-5'}`} />
        </div>
        <div>
          <h1 className={`font-bold tracking-tight bg-gradient-to-r from-white via-slate-100 to-slate-400 bg-clip-text text-transparent ${simpleMode ? 'text-xl sm:text-2xl' : 'text-lg sm:text-xl'}`}>
            KiranaStore
          </h1>
          {!simpleMode && (
            <div className="flex items-center gap-2 text-xs text-slate-400 font-medium">
              <span className="flex items-center gap-1 text-emerald-400">
                <span className="w-1.5 h-1.5 rounded-full bg-emerald-400 animate-pulse"></span> Live Firestore
              </span>
              <span>•</span>
              <span className="flex items-center gap-1">
                <Pin className="w-3 h-3 text-amber-400 fill-amber-400" /> {pinnedCount} Pinned
              </span>
            </div>
          )}
        </div>
      </div>

      {/* Search Bar */}
      <div className="flex-1 max-w-md hidden sm:block relative">
        <Search className={`text-slate-400 absolute left-3.5 top-1/2 -translate-y-1/2 ${simpleMode ? 'w-5 h-5' : 'w-4 h-4'}`} />
        <input 
          type="text"
          placeholder="Search products by name or category..."
          value={searchQuery}
          onChange={(e) => setSearchQuery(e.target.value)}
          className={`w-full bg-slate-900/80 border border-slate-800 rounded-xl pl-10 pr-4 text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500/50 focus:ring-2 focus:ring-emerald-500/20 transition-all ${simpleMode ? 'py-3 text-base' : 'py-2 text-sm'}`}
        />
      </div>

      {/* Action Buttons */}
      <div className="flex items-center gap-2">
        {/* Simple Mode Always-Visible Floating Toggle */}
        <button
          onClick={handleToggleSimpleMode}
          className={`flex items-center gap-1.5 rounded-xl border transition-all active:scale-95 ${
            simpleMode
              ? 'bg-amber-500/20 border-amber-500/50 text-amber-300 font-bold px-3 py-2 text-xs shadow-md shadow-amber-500/10'
              : 'bg-slate-900 border-slate-800 text-slate-300 hover:bg-slate-800 hover:text-white px-3 py-2 text-xs font-semibold'
          }`}
          title="Toggle Simple / Elder Mode (Large text & voice alerts)"
        >
          <Glasses className={`w-4 h-4 ${simpleMode ? 'text-amber-400' : 'text-slate-400'}`} />
          <span className="hidden sm:inline">{simpleMode ? 'Simple Mode ON' : 'Simple Mode'}</span>
        </button>

        <button
          onClick={onOpenAddModal}
          className={`flex items-center gap-2 rounded-xl bg-emerald-500 hover:bg-emerald-400 active:scale-95 text-slate-950 font-semibold shadow-lg shadow-emerald-500/25 transition-all ${simpleMode ? 'px-5 py-3 text-base' : 'px-3.5 py-2 text-sm'}`}
        >
          <Plus className={`stroke-[3] ${simpleMode ? 'w-5 h-5' : 'w-4 h-4'}`} />
          <span className="hidden xs:inline">Add Item</span>
        </button>

        <button
          onClick={onOpenSettings}
          className={`rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-slate-300 hover:text-white active:scale-95 transition-all ${simpleMode ? 'p-3.5' : 'p-2.5'}`}
          title="Settings & Diagnostics"
        >
          <Settings className={`${simpleMode ? 'w-5 h-5' : 'w-4 h-4'}`} />
        </button>
      </div>
    </header>
  );
}
