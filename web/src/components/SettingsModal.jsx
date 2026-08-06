import React, { useState } from 'react';
import { X, Key, Check, Wifi, Sparkles, Shield, Cpu, ExternalLink } from 'lucide-react';

export default function SettingsModal({ isOpen, onClose }) {
  const [apiKey, setApiKey] = useState(() => localStorage.getItem("GEMINI_API_KEY") || (import.meta.env.VITE_GEMINI_API_KEY || ""));
  const [testResult, setTestResult] = useState(null);
  const [isTesting, setIsTesting] = useState(false);

  if (!isOpen) return null;

  const handleSaveKey = () => {
    localStorage.setItem("GEMINI_API_KEY", apiKey.trim());
    alert("Gemini API key saved to local storage!");
  };

  const handleTestConnection = async () => {
    if (!apiKey.trim()) {
      setTestResult({ success: false, message: "Please enter a Gemini API Key first." });
      return;
    }
    setIsTesting(true);
    setTestResult(null);
    try {
      const res = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=${apiKey.trim()}`, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify({
          contents: [{ parts: [{ text: "Respond with OK" }] }]
        })
      });
      if (res.ok) {
        setTestResult({ success: true, message: "Gemini 2.0 Flash API Connection OK ✅" });
      } else {
        setTestResult({ success: false, message: `API Error: HTTP ${res.status}` });
      }
    } catch (e) {
      setTestResult({ success: false, message: `Network error: ${e.message}` });
    } finally {
      setIsTesting(false);
    }
  };

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center p-4 bg-slate-950/80 backdrop-blur-sm animate-in fade-in duration-200">
      <div className="w-full max-w-md rounded-2xl glass-panel p-6 border border-slate-800 shadow-2xl relative">
        {/* Header */}
        <div className="flex items-center justify-between pb-4 border-b border-slate-800 mb-4">
          <div className="flex items-center gap-2.5">
            <div className="p-2 rounded-xl bg-purple-500/10 text-purple-400">
              <Cpu className="w-5 h-5" />
            </div>
            <div>
              <h2 className="text-lg font-bold text-slate-100">Settings & Diagnostics</h2>
              <p className="text-xs text-slate-400">KiranaStore Web PWA Settings</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="p-1.5 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-all"
          >
            <X className="w-5 h-5" />
          </button>
        </div>

        {/* Form Body */}
        <div className="space-y-4">
          {/* Gemini API Key */}
          <div>
            <label className="block text-xs font-semibold text-slate-300 uppercase tracking-wider mb-1.5">
              Gemini API Key (Developer Free-Tier)
            </label>
            <div className="relative">
              <input
                type="password"
                value={apiKey}
                onChange={(e) => setApiKey(e.target.value)}
                placeholder="AIzaSy..."
                className="w-full bg-slate-900 border border-slate-800 rounded-xl pl-3.5 pr-20 py-2.5 text-sm text-slate-100 focus:outline-none focus:border-purple-500"
              />
              <button
                onClick={handleSaveKey}
                className="absolute right-1.5 top-1/2 -translate-y-1/2 px-3 py-1 rounded-lg bg-purple-600 hover:bg-purple-500 text-white text-xs font-bold"
              >
                Save
              </button>
            </div>
          </div>

          <button
            onClick={handleTestConnection}
            disabled={isTesting}
            className="w-full py-2 rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-purple-400 font-semibold text-xs flex items-center justify-center gap-2"
          >
            <Sparkles className="w-4 h-4" />
            {isTesting ? "Testing Gemini API..." : "🔍 Test Gemini 2.0 Flash Connection"}
          </button>

          {testResult && (
            <div className={`p-3 rounded-xl text-xs font-semibold ${testResult.success ? 'bg-emerald-500/10 text-emerald-400 border border-emerald-500/20' : 'bg-rose-500/10 text-rose-400 border border-rose-500/20'}`}>
              {testResult.message}
            </div>
          )}

          {/* Diagnostics Info */}
          <div className="pt-3 border-t border-slate-800 space-y-2 text-xs">
            <div className="flex items-center justify-between py-1.5 px-3 rounded-xl bg-slate-900/60 border border-slate-800/60">
              <span className="text-slate-400 flex items-center gap-2">
                <Wifi className="w-3.5 h-3.5 text-emerald-400" /> Firestore Project
              </span>
              <span className="font-mono text-slate-200 font-medium">kirana-store-abs6187</span>
            </div>
            <div className="flex items-center justify-between py-1.5 px-3 rounded-xl bg-slate-900/60 border border-slate-800/60">
              <span className="text-slate-400 flex items-center gap-2">
                <Shield className="w-3.5 h-3.5 text-indigo-400" /> PWA Service Worker
              </span>
              <span className="font-mono text-emerald-400 font-medium">Active (Workbox)</span>
            </div>
          </div>
        </div>

        <div className="pt-4 mt-4 border-t border-slate-800 text-center">
          <button
            onClick={onClose}
            className="w-full py-2 rounded-xl bg-slate-900 border border-slate-800 text-slate-300 hover:text-white text-sm font-medium"
          >
            Done
          </button>
        </div>
      </div>
    </div>
  );
}
