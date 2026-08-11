import React, { useState, useEffect, useMemo } from 'react';
import Navbar from './components/Navbar';
import ProductCard from './components/ProductCard';
import AddProductModal from './components/AddProductModal';
import PriceHistoryModal from './components/PriceHistoryModal';
import VoiceFab from './components/VoiceFab';
import PwaInstallWidget from './components/PwaInstallWidget';
import SettingsModal from './components/SettingsModal';
import { subscribeProducts } from './services/productService';
import { subscribePriceHistory } from './services/priceHistoryService';
import { Store, Tag, Plus, Sparkles, History, Layers } from 'lucide-react';

export default function App() {
  const [products, setProducts] = useState([]);
  const [priceHistory, setPriceHistory] = useState([]);
  const [searchQuery, setSearchQuery] = useState('');
  const [selectedCategory, setSelectedCategory] = useState('All');
  
  // Modals state
  const [isAddModalOpen, setIsAddModalOpen] = useState(false);
  const [isSettingsOpen, setIsSettingsOpen] = useState(false);
  const [historyModalProduct, setHistoryModalProduct] = useState(null);
  const [isAllHistoryOpen, setIsAllHistoryOpen] = useState(false);
  const [isLoading, setIsLoading] = useState(true);

  // Simple / Elder Mode preference
  const [simpleMode, setSimpleMode] = useState(() => {
    try {
      return localStorage.getItem('kirana-simple-mode') === 'true';
    } catch {
      return false;
    }
  });

  useEffect(() => {
    try {
      localStorage.setItem('kirana-simple-mode', String(simpleMode));
    } catch {
      // ignore storage errors (private browsing)
    }
  }, [simpleMode]);

  useEffect(() => {
    // Realtime Firestore Subscription for Products
    const unsubscribeProducts = subscribeProducts((prods) => {
      setProducts(prods);
      setIsLoading(false);
    });

    // Realtime Firestore Subscription for Price Audit Log
    const unsubscribeHistory = subscribePriceHistory((logs) => {
      setPriceHistory(logs);
    });

    return () => {
      unsubscribeProducts();
      unsubscribeHistory();
    };
  }, []);

  // Categories list
  const categories = useMemo(() => {
    const set = new Set(products.map(p => p.category || 'General'));
    return ['All', ...Array.from(set)];
  }, [products]);

  // Filtered & Sorted Products
  const filteredProducts = useMemo(() => {
    return products
      .filter(p => {
        const matchesSearch = p.name.toLowerCase().includes(searchQuery.toLowerCase()) ||
          (p.category && p.category.toLowerCase().includes(searchQuery.toLowerCase()));
        const matchesCat = selectedCategory === 'All' || p.category === selectedCategory;
        return matchesSearch && matchesCat;
      })
      .sort((a, b) => {
        // Pinned products first
        if (a.isPinned && !b.isPinned) return -1;
        if (!a.isPinned && b.isPinned) return 1;
        return 0;
      });
  }, [products, searchQuery, selectedCategory]);

  const pinnedCount = products.filter(p => p.isPinned).length;

  return (
    <div className={`min-h-screen bg-slate-950 text-slate-100 flex flex-col font-sans ${simpleMode ? 'simple-mode' : ''}`}>
      {/* Navbar Header */}
      <Navbar 
        searchQuery={searchQuery}
        setSearchQuery={setSearchQuery}
        onOpenAddModal={() => setIsAddModalOpen(true)}
        onOpenSettings={() => setIsSettingsOpen(true)}
        onToggleSimpleMode={() => setSimpleMode((v) => !v)}
        totalCount={products.length}
        pinnedCount={pinnedCount}
        simpleMode={simpleMode}
      />

      {/* Main Container */}
      <main className="flex-1 max-w-7xl w-full mx-auto px-4 sm:px-8 py-6 flex flex-col gap-6">
        
        {/* Top Control Bar: Mobile Search & Category Pills */}
        <div className="flex flex-col sm:flex-row items-stretch sm:items-center justify-between gap-4">
          
          {/* Category Filter Pills */}
          <div className="flex items-center gap-2 overflow-x-auto pb-1 scrollbar-none">
            {categories.map((cat) => (
              <button
                key={cat}
                onClick={() => setSelectedCategory(cat)}
                className={`px-3.5 py-1.5 rounded-xl text-xs font-semibold whitespace-nowrap transition-all border ${
                  selectedCategory === cat
                    ? 'bg-emerald-500 text-slate-950 border-emerald-400 shadow-md shadow-emerald-500/20'
                    : 'bg-slate-900 text-slate-400 border-slate-800 hover:text-slate-200 hover:bg-slate-800'
                }`}
              >
                {cat}
              </button>
            ))}
          </div>

          {/* Audit Log Button & Mobile Search */}
          <div className="flex items-center gap-2">
            <button
              onClick={() => setIsAllHistoryOpen(true)}
              className="flex items-center gap-2 px-3.5 py-1.5 rounded-xl bg-slate-900 border border-slate-800 text-indigo-400 hover:text-indigo-300 hover:bg-indigo-500/10 text-xs font-semibold transition-all"
            >
              <History className="w-4 h-4" />
              <span>Price Audit Log ({priceHistory.length})</span>
            </button>
          </div>
        </div>

        {/* Mobile Search Bar */}
        <div className="sm:hidden">
          <input 
            type="text"
            placeholder="Search products by name..."
            value={searchQuery}
            onChange={(e) => setSearchQuery(e.target.value)}
            className="w-full bg-slate-900 border border-slate-800 rounded-xl px-4 py-2 text-sm text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
          />
        </div>

        {/* Product Grid */}
        {isLoading ? (
          <div className="flex-1 flex flex-col items-center justify-center py-20 text-slate-500">
            <Sparkles className="w-8 h-8 animate-spin text-emerald-400 mb-3" />
            <p className="text-sm font-medium">Connecting to Firestore Catalogue...</p>
          </div>
        ) : filteredProducts.length === 0 ? (
          <div className="flex-1 flex flex-col items-center justify-center py-20 text-center border-2 border-dashed border-slate-800 rounded-3xl p-8">
            <div className="w-12 h-12 rounded-2xl bg-slate-900 flex items-center justify-center text-slate-500 mb-3">
              <Layers className="w-6 h-6" />
            </div>
            <h3 className="text-base font-bold text-slate-300 mb-1">No products found</h3>
            <p className="text-xs text-slate-500 max-w-sm mb-4">
              {searchQuery ? `No items matching "${searchQuery}"` : "Your inventory catalogue is empty. Add your first item or use Voice AI!"}
            </p>
            <button
              onClick={() => setIsAddModalOpen(true)}
              className="flex items-center gap-2 px-4 py-2 rounded-xl bg-emerald-500 text-slate-950 font-bold text-sm"
            >
              <Plus className="w-4 h-4 stroke-[3]" /> Add First Item
            </button>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 lg:grid-cols-4 gap-4">
            {filteredProducts.map((product) => (
              <ProductCard
                key={product.id}
                product={product}
                onOpenHistory={(p) => setHistoryModalProduct(p)}
                simpleMode={simpleMode}
              />
            ))}
          </div>
        )}
      </main>

      {/* Floating Left PWA Install Widget */}
      <PwaInstallWidget simpleMode={simpleMode} />

      {/* Floating Action Button for Voice AI Commands */}
      <VoiceFab products={products} simpleMode={simpleMode} />

      {/* Add Product Modal */}
      <AddProductModal 
        isOpen={isAddModalOpen}
        onClose={() => setIsAddModalOpen(false)}
        simpleMode={simpleMode}
      />

      {/* Single Product Price Audit Modal */}
      {historyModalProduct && (
        <PriceHistoryModal
          product={historyModalProduct}
          historyLogs={priceHistory}
          isOpen={Boolean(historyModalProduct)}
          onClose={() => setHistoryModalProduct(null)}
        />
      )}

      {/* All Products Price Audit Modal */}
      {isAllHistoryOpen && (
        <PriceHistoryModal
          product={null}
          historyLogs={priceHistory}
          isOpen={isAllHistoryOpen}
          onClose={() => setIsAllHistoryOpen(false)}
        />
      )}

      {/* Settings Modal */}
      <SettingsModal 
        isOpen={isSettingsOpen}
        onClose={() => setIsSettingsOpen(false)}
        simpleMode={simpleMode}
        onToggleSimpleMode={() => setSimpleMode((v) => !v)}
      />
    </div>
  );
}
