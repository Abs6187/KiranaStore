import React, { useState, useEffect } from 'react';
import { Smartphone, Download, CheckCircle2, ChevronLeft, ChevronRight, ShieldCheck } from 'lucide-react';

export default function PwaInstallWidget({ simpleMode = false }) {
  const [deferredPrompt, setDeferredPrompt] = useState(null);
  const [isInstalled, setIsInstalled] = useState(false);
  const [isExpanded, setIsExpanded] = useState(false);
  const [installSuccess, setInstallSuccess] = useState(false);

  useEffect(() => {
    // Check if running as standalone PWA
    const checkStandalone = () => {
      const isStandalone = window.matchMedia('(display-mode: standalone)').matches || window.navigator.standalone === true;
      setIsInstalled(isStandalone);
    };

    checkStandalone();
    window.addEventListener('resize', checkStandalone);

    // Listen for browser PWA install prompt
    const handleBeforeInstallPrompt = (e) => {
      e.preventDefault();
      setDeferredPrompt(e);
      // Auto expand once if prompt is ready and not installed
      setIsExpanded(true);
    };

    const handleAppInstalled = () => {
      setIsInstalled(true);
      setDeferredPrompt(null);
      setInstallSuccess(true);
      setTimeout(() => setIsExpanded(false), 3000);
    };

    window.addEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
    window.addEventListener('appinstalled', handleAppInstalled);

    return () => {
      window.removeEventListener('resize', checkStandalone);
      window.removeEventListener('beforeinstallprompt', handleBeforeInstallPrompt);
      window.removeEventListener('appinstalled', handleAppInstalled);
    };
  }, []);

  const handleInstallClick = async () => {
    if (!deferredPrompt) {
      alert("PWA Installation: To install, open Chrome/Edge menu (⋮) and select 'Install Kirana Store PWA' or 'Add to Home Screen'.");
      return;
    }

    deferredPrompt.prompt();
    const { outcome } = await deferredPrompt.userChoice;
    if (outcome === 'accepted') {
      setInstallSuccess(true);
      setIsInstalled(true);
    }
    setDeferredPrompt(null);
  };

  return (
    <div className={`fixed left-4 bottom-6 z-40 transition-all duration-300 pointer-events-none ${simpleMode ? 'scale-105' : ''}`}>
      {isExpanded ? (
        /* Expanded Floating Card */
        <div className="pointer-events-auto w-72 rounded-2xl glass-panel p-4 border border-emerald-500/30 shadow-2xl shadow-emerald-950/50 animate-in slide-in-from-left duration-300 relative bg-slate-900/95 backdrop-blur-md">
          {/* Header */}
          <div className="flex items-center justify-between pb-2 mb-2 border-b border-slate-800">
            <div className="flex items-center gap-2">
              <div className="p-1.5 rounded-lg bg-emerald-500/20 text-emerald-400">
                <Smartphone className="w-4 h-4" />
              </div>
              <span className="text-xs font-bold text-slate-200">Kirana Store PWA</span>
            </div>
            <button
              onClick={() => setIsExpanded(false)}
              className="p-1 rounded-lg text-slate-400 hover:text-white hover:bg-slate-800 transition-all"
              title="Collapse PWA Menu"
            >
              <ChevronLeft className="w-4 h-4" />
            </button>
          </div>

          {/* Body Info */}
          <div className="space-y-2 mb-3">
            <p className="text-xs text-slate-300 leading-relaxed">
              {isInstalled
                ? 'App is installed and running with offline Firestore sync.'
                : 'Install as a fast native desktop/mobile web app.'}
            </p>

            <div className="flex items-center gap-1.5 text-[11px] text-emerald-400 font-medium">
              <ShieldCheck className="w-3.5 h-3.5" />
              <span>Offline Ready • Fast Loading</span>
            </div>
          </div>

          {/* Action Button */}
          {isInstalled || installSuccess ? (
            <div className="flex items-center gap-2 py-2 px-3 rounded-xl bg-emerald-500/10 border border-emerald-500/30 text-emerald-400 text-xs font-bold">
              <CheckCircle2 className="w-4 h-4" />
              <span>Installed & Ready</span>
            </div>
          ) : (
            <button
              onClick={handleInstallClick}
              className="w-full flex items-center justify-center gap-2 py-2 px-3 rounded-xl bg-emerald-500 hover:bg-emerald-400 text-slate-950 text-xs font-bold shadow-md shadow-emerald-500/20 transition-all active:scale-95"
            >
              <Download className="w-4 h-4" />
              <span>Install PWA App</span>
            </button>
          )}
        </div>
      ) : (
        /* Collapsed Floating Icon Button */
        <button
          onClick={() => setIsExpanded(true)}
          className="pointer-events-auto flex items-center gap-2.5 px-3.5 py-2.5 rounded-2xl bg-slate-900/90 hover:bg-slate-800 border border-slate-700/80 text-emerald-400 shadow-xl shadow-slate-950/60 hover:scale-105 active:scale-95 transition-all group"
          title="Open PWA App Details"
        >
          <div className="relative">
            <Smartphone className="w-5 h-5 text-emerald-400 group-hover:rotate-12 transition-transform" />
            {!isInstalled && (
              <span className="absolute -top-1 -right-1 w-2.5 h-2.5 rounded-full bg-emerald-400 animate-ping" />
            )}
          </div>
          <span className="text-xs font-bold text-slate-200 hidden sm:inline">PWA App</span>
          <ChevronRight className="w-3.5 h-3.5 text-slate-400 group-hover:translate-x-0.5 transition-transform" />
        </button>
      )}
    </div>
  );
}
