import React, { useState, useEffect, useRef } from 'react';
import { Mic, MicOff, Sparkles, Send, CheckCircle2, AlertCircle, X } from 'lucide-react';
import { startListening, stopListening, speakText, isSpeechSupported } from '../services/voiceService';
import { processVoiceCommand } from '../services/aiService';
import VoiceConfirmModal from './VoiceConfirmModal';

export default function VoiceFab({ products }) {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [commandText, setCommandText] = useState('');
  const [aiStatus, setAiStatus] = useState(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [confirmModalData, setConfirmModalData] = useState(null);
  const inputRef = useRef(null);

  // Keep a live reference to products so async callbacks use the latest catalog
  const productsRef = useRef(products);
  useEffect(() => {
    productsRef.current = products;
  }, [products]);

  const clearStatus = () => {
    setAiStatus(null);
    setTranscript('');
  };

  const executeCommand = async (text) => {
    const trimmed = text.trim();
    if (!trimmed || isProcessing) return;

    setIsProcessing(true);
    setTranscript(trimmed);
    setCommandText('');
    setAiStatus({ type: 'thinking', message: `Parsing: "${trimmed}" with Gemini Flash...` });

    try {
      const aiResult = await processVoiceCommand(trimmed, productsRef.current);

      if ((aiResult.action === 'update_price' && aiResult.targetProduct && aiResult.price) ||
          (aiResult.action === 'add_product' && aiResult.productName && aiResult.price)) {
        
        // NO direct DB write! Open Voice Confirmation & Spell-Check Modal!
        setConfirmModalData({
          ...aiResult,
          rawTranscript: trimmed
        });
        setAiStatus({ type: 'success', message: `Parsed: "${aiResult.productName || aiResult.targetProduct?.name}". Confirm details to save.` });
        speakText("Details check karein aur confirm button dabayein.");
      } else if (aiResult.action === 'query_price' && aiResult.targetProduct) {
        const feedback = aiResult.replyText || `${aiResult.targetProduct.name} ka price ₹${aiResult.targetProduct.salesPrice} per ${aiResult.targetProduct.unit || 'kg'} hai`;
        setAiStatus({ type: 'success', message: feedback });
        speakText(feedback);
      } else {
        const fallbackMsg = aiResult.replyText || "Samajh nahi aaya. Please say again e.g. 'Mustard oil 175 rupees karo'";
        setAiStatus({ type: 'error', message: fallbackMsg });
        speakText(fallbackMsg);
      }
    } catch (err) {
      const errorMsg = `Error: ${err.message || "Something went wrong"}`;
      setAiStatus({ type: 'error', message: errorMsg });
    } finally {
      setIsProcessing(false);
    }
  };

  const handleMicClick = () => {
    if (isListening) {
      stopListening();
      setIsListening(false);
      return;
    }

    if (!isSpeechSupported()) {
      alert("Voice recognition is not supported in this browser. Please use Chrome or Edge.");
      return;
    }

    setTranscript('');
    setCommandText('');
    setAiStatus(null);

    startListening({
      onStart: () => setIsListening(true),
      onResult: (text) => {
        setIsListening(false);
        executeCommand(text);
      },
      onError: (err) => {
        setIsListening(false);
        setAiStatus({ type: 'error', message: `Voice error: ${err}` });
      },
      onEnd: () => setIsListening(false)
    });
  };

  const handleSendText = (e) => {
    e.preventDefault();
    executeCommand(commandText);
  };

  // Cleanup recognition if the component unmounts while listening
  useEffect(() => {
    return () => stopListening();
  }, []);

  const showPanel = isListening || isProcessing || transcript || aiStatus || commandText;
  const isThinking = isProcessing || aiStatus?.type === 'thinking';

  return (
    <div className="fixed bottom-6 right-6 z-40 flex flex-col items-end gap-3 pointer-events-none">
      {/* Voice Status / Command Input Panel */}
      {showPanel && (
        <div className="pointer-events-auto w-72 sm:w-80 rounded-2xl glass-panel p-3.5 border border-emerald-500/30 shadow-2xl animate-in slide-in-from-bottom-5 duration-200">
          {/* Status Header */}
          {aiStatus && (
            <div className="flex items-start gap-2.5 mb-2">
              <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 shrink-0">
                {aiStatus.type === 'success' ? (
                  <CheckCircle2 className="w-4 h-4 text-emerald-400" />
                ) : aiStatus.type === 'error' ? (
                  <AlertCircle className="w-4 h-4 text-rose-400" />
                ) : (
                  <Sparkles className="w-4 h-4 text-emerald-400 animate-spin" />
                )}
              </div>
              <div className="flex-1 min-w-0">
                <span className="font-bold text-slate-200 block mb-0.5 text-xs">
                  {isThinking ? 'Gemini AI Thinking' : isListening ? 'Listening' : 'Gemini AI Response'}
                </span>
                <p className="text-slate-300 text-xs break-words">
                  {aiStatus.message}
                </p>
              </div>
              <button
                onClick={clearStatus}
                className="p-1 rounded-md text-slate-500 hover:text-slate-300 hover:bg-slate-800 shrink-0"
                title="Clear"
              >
                <X className="w-3.5 h-3.5" />
              </button>
            </div>
          )}

          {/* Listening Hint */}
          {isListening && !aiStatus && (
            <div className="flex items-start gap-2.5">
              <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 shrink-0">
                <Mic className="w-4 h-4 text-emerald-400" />
              </div>
              <div>
                <span className="font-bold text-slate-200 block mb-0.5 text-xs">Listening (hi-IN / en-IN)</span>
                <p className="text-slate-300 text-xs">Say: &quot;Mustard oil 175 rupees karo&quot;</p>
              </div>
            </div>
          )}

          {/* Typed Command Input */}
          {!isListening && !isThinking && (
            <form onSubmit={handleSendText} className="flex items-center gap-2 mt-2">
              <input
                ref={inputRef}
                type="text"
                value={commandText}
                onChange={(e) => setCommandText(e.target.value)}
                placeholder='Type command, e.g. "Mustard oil 175"'
                disabled={isProcessing}
                className="flex-1 min-w-0 bg-slate-950 border border-slate-800 rounded-xl px-3 py-2 text-xs text-slate-200 placeholder-slate-500 focus:outline-none focus:border-emerald-500"
              />
              <button
                type="submit"
                disabled={!commandText.trim() || isProcessing}
                className="p-2 rounded-xl bg-emerald-500 text-slate-950 hover:bg-emerald-400 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
                title="Send command"
              >
                <Send className="w-4 h-4" />
              </button>
            </form>
          )}
        </div>
      )}

      {/* Floating Mic Button */}
      <button
        onClick={handleMicClick}
        className={`pointer-events-auto w-14 h-14 rounded-2xl flex items-center justify-center shadow-2xl transition-all active:scale-95 ${
          isListening
            ? 'bg-rose-500 text-white listening-pulse'
            : 'bg-emerald-500 hover:bg-emerald-400 text-slate-950 shadow-emerald-500/30 hover:scale-105'
        }`}
        title="Voice AI Command (Hindi / Hinglish / English)"
      >
        {isListening ? (
          <MicOff className="w-6 h-6 stroke-[2.5]" />
        ) : (
          <Mic className="w-6 h-6 stroke-[2.5]" />
        )}
      </button>
      {/* Voice Confirmation & Spell-Check Modal */}
      <VoiceConfirmModal
        isOpen={Boolean(confirmModalData)}
        onClose={() => setConfirmModalData(null)}
        parsedData={confirmModalData}
      />
    </div>
  );
}
