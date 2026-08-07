import React, { useState } from 'react';
import { X, Wifi, Sparkles, Shield, Cpu, CheckCircle } from 'lucide-react';
import { processVoiceCommand } from '../services/aiService';

export default function SettingsModal({ isOpen, onClose }) {
  const [testResult, setTestResult] = useState(null);
  const [isTesting, setIsTesting] = useState(false);

  if (!isOpen) return null;

  const handleTestConnection = async () => {
    setIsTesting(true);
    setTestResult(null);
    try {
      const res = await processVoiceCommand("Mustard oil 175 rupees karo", []);
      if (res && res.action) {
        setTestResult({ success: true, message: "Gemini 2.0 Flash AI Backend Connection Active ✅" });
      } else {
        setTestResult({ success: false, message: "AI Backend response returned unexpected format." });
      }
    } catch (e) {
      setTestResult({ success: false, message: `Backend error: ${e.message}` });
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
              <p className="text-xs text-slate-400">KiranaStore Web PWA Status</p>
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
          {/* Backend AI Badge */}
          <div className="p-3.5 rounded-xl bg-slate-900/80 border border-slate-800 flex items-center justify-between">
            <div>
              <div className="text-xs font-semibold text-slate-300 uppercase tracking-wider">
                Voice AI Engine
              </div>
              <div className="text-sm font-bold text-purple-400 flex items-center gap-1.5 mt-0.5">
                <CheckCircle className="w-4 h-4 text-emerald-400" />
                Gemini 2.0 Flash (Backend logic)
              </div>
            </div>
            <span className="text-[10px] px-2 py-0.5 rounded-full bg-emerald-500/10 text-emerald-400 border border-emerald-500/20 font-mono">
              Configured
            </span>
          </div>

          <button
            onClick={handleTestConnection}
            disabled={isTesting}
            className="w-full py-2.5 rounded-xl bg-slate-900 border border-slate-800 hover:bg-slate-800 text-purple-400 font-semibold text-xs flex items-center justify-center gap-2 transition-all"
          >
            <Sparkles className="w-4 h-4" />
            {isTesting ? "Testing Gemini AI..." : "🔍 Test Gemini 2.0 Flash Connection"}
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
