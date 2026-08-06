import React, { useState } from 'react';
import { Mic, MicOff, Sparkles, Volume2, CheckCircle2, AlertCircle } from 'lucide-react';
import { startListening, stopListening, speakText, isSpeechSupported } from '../services/voiceService';
import { processVoiceCommand } from '../services/aiService';
import { updateSalesPrice, addProduct } from '../services/productService';

export default function VoiceFab({ products }) {
  const [isListening, setIsListening] = useState(false);
  const [transcript, setTranscript] = useState('');
  const [aiStatus, setAiStatus] = useState(null); // { type: 'success'|'error'|'thinking', message: string }

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
    setAiStatus(null);

    startListening({
      onStart: () => setIsListening(true),
      onResult: async (text) => {
        setIsListening(false);
        setTranscript(text);
        setAiStatus({ type: 'thinking', message: `Parsing: "${text}" with Gemini 2.0 Flash...` });

        // Process through AI
        const aiResult = await processVoiceCommand(text, products);
        
        if (aiResult.action === 'update_price' && aiResult.targetProduct && aiResult.price) {
          await updateSalesPrice(
            aiResult.targetProduct.id, 
            aiResult.targetProduct.name, 
            aiResult.targetProduct.salesPrice, 
            aiResult.price, 
            "Voice AI Command"
          );
          const feedback = aiResult.replyText || `${aiResult.targetProduct.name} price updated to ₹${aiResult.price}`;
          setAiStatus({ type: 'success', message: feedback });
          speakText(feedback);
        } else if (aiResult.action === 'add_product' && aiResult.productName && aiResult.price) {
          await addProduct({
            name: aiResult.productName,
            salesPrice: aiResult.price,
            unit: aiResult.unit || 'kg',
            category: 'General'
          });
          const feedback = aiResult.replyText || `Added ${aiResult.productName} at ₹${aiResult.price}`;
          setAiStatus({ type: 'success', message: feedback });
          speakText(feedback);
        } else if (aiResult.action === 'query_price' && aiResult.targetProduct) {
          const feedback = `${aiResult.targetProduct.name} price is ₹${aiResult.targetProduct.salesPrice} per ${aiResult.targetProduct.unit || 'kg'}`;
          setAiStatus({ type: 'success', message: feedback });
          speakText(feedback);
        } else {
          const fallbackMsg = aiResult.replyText || "Samajh nahi aaya. Please say again e.g. 'Mustard oil 175 rupees karo'";
          setAiStatus({ type: 'error', message: fallbackMsg });
          speakText(fallbackMsg);
        }
      },
      onError: (err) => {
        setIsListening(false);
        setAiStatus({ type: 'error', message: `Voice error: ${err}` });
      },
      onEnd: () => setIsListening(false)
    });
  };

  return (
    <div className="fixed bottom-6 right-6 z-40 flex flex-col items-end gap-3 pointer-events-none">
      {/* Voice Status Toast Banner */}
      {(isListening || transcript || aiStatus) && (
        <div className="pointer-events-auto max-w-xs sm:max-w-sm rounded-2xl glass-panel p-3.5 border border-emerald-500/30 shadow-2xl animate-in slide-in-from-bottom-5 duration-200">
          <div className="flex items-start gap-2.5">
            <div className="p-1.5 rounded-lg bg-emerald-500/10 text-emerald-400 shrink-0">
              {aiStatus?.type === 'success' ? (
                <CheckCircle2 className="w-4 h-4 text-emerald-400" />
              ) : aiStatus?.type === 'error' ? (
                <AlertCircle className="w-4 h-4 text-rose-400" />
              ) : (
                <Sparkles className="w-4 h-4 text-emerald-400 animate-spin" />
              )}
            </div>
            <div className="text-xs">
              <span className="font-bold text-slate-200 block mb-0.5">
                {isListening ? '🎤 Listening (hi-IN / en-IN)...' : 'Gemini AI Response'}
              </span>
              <p className="text-slate-300">
                {isListening ? 'Say: "Mustard oil 175 rupees karo"' : aiStatus?.message || transcript}
              </p>
            </div>
          </div>
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
    </div>
  );
}
