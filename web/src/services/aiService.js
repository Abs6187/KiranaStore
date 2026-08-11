/**
 * Kirana AI Agent for Web PWA
 * Powered by Gemini 2.0 Flash REST API / Backend logic
 */

/**
 * Calculate Levenshtein distance between two strings
 */
export const levenshteinDistance = (a, b) => {
  const str1 = a.toLowerCase().trim();
  const str2 = b.toLowerCase().trim();
  const track = Array(str2.length + 1).fill(null).map(() =>
    Array(str1.length + 1).fill(null));
  for (let i = 0; i <= str1.length; i += 1) track[0][i] = i;
  for (let j = 0; j <= str2.length; j += 1) track[j][0] = j;
  for (let j = 1; j <= str2.length; j += 1) {
    for (let i = 1; i <= str1.length; i += 1) {
      const indicator = str1[i - 1] === str2[j - 1] ? 0 : 1;
      track[j][i] = Math.min(
        track[j][i - 1] + 1, // deletion
        track[j - 1][i] + 1, // insertion
        track[j - 1][i - 1] + indicator, // substitution
      );
    }
  }
  return track[str2.length][str1.length];
};

/**
 * Fuzzy match spoken query against list of existing products
 */
export const findBestProductMatch = (spokenName, products) => {
  if (!spokenName || !products || products.length === 0) return null;
  const target = spokenName.toLowerCase().trim();

  // 1. Direct substring match
  const exactSub = products.find(p => p.name.toLowerCase().includes(target) || target.includes(p.name.toLowerCase()));
  if (exactSub) return exactSub;

  // 2. Levenshtein min distance
  let minDistance = Infinity;
  let bestMatch = null;

  for (const product of products) {
    const dist = levenshteinDistance(target, product.name);
    if (dist < minDistance && dist <= 4) {
      minDistance = dist;
      bestMatch = product;
    }
  }

  return bestMatch;
};

const SYSTEM_PROMPT = `
You are KiranaAI, a smart inventory assistant for an Indian grocery (Kirana) store owner.
The user speaks in Hindi, Hinglish, or English commands such as:
- "Mustard oil 175 rupees karo" -> action: update_price, productName: "Mustard Oil", price: 175
- "Basmati rice 280 rupees add karo" -> action: add_product, productName: "Basmati Rice", price: 280, unit: "kg"
- "Atta ka price kitna hai?" -> action: query_price, productName: "Atta"
- "Change mustard oil to 175 rupees" -> action: update_price, productName: "Sugar", price: 45

Return strictly valid JSON only (no markdown, no code fences):
{
  "action": "update_price" | "add_product" | "query_price" | "unknown",
  "productName": "extracted product name",
  "price": number or null,
  "unit": "kg" | "litre" | "packet" | "piece" or null,
  "replyText": "Hindi/English confirmation spoken feedback text"
}
`;

/**
 * Process voice text prompt through Gemini 2.0 Flash
 */
export const processVoiceCommand = async (userTranscript, existingProducts = []) => {
  const apiKey = import.meta.env.VITE_GEMINI_API_KEY || "";
  if (!apiKey) {
    return {
      action: "unknown",
      replyText: "Gemini API key missing in environment configuration."
    };
  }

  try {
    const catalogList = existingProducts.map(p => `${p.name} (₹${p.salesPrice}/${p.unit || 'unit'})`).join(", ");

    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [
          {
            role: "user",
            parts: [{ text: `${SYSTEM_PROMPT}\n\nCurrent Catalogue: [${catalogList}]\n\nUser Voice Transcript: "${userTranscript}"` }]
          }
        ]
      })
    });

    if (!response.ok) {
      throw new Error(`Gemini API Error: ${response.status}`);
    }

    const data = await response.json();
    let rawText = data.candidates?.[0]?.content?.parts?.[0]?.text || "{}";
    
    // Strip markdown code fences if present
    rawText = rawText.replace(/```json/g, "").replace(/```/g, "").trim();
    
    const parsed = JSON.parse(rawText);

    // If updating or querying, resolve best matching product in catalog
    if (parsed.productName && (parsed.action === "update_price" || parsed.action === "query_price")) {
      const match = findBestProductMatch(parsed.productName, existingProducts);
      if (match) {
        parsed.targetProduct = match;
      }
    }

    return parsed;

  } catch (err) {
    console.error("AI command parsing failed:", err);
    return {
      action: "unknown",
      replyText: "Samajh nahi aaya. Kripya dubara bole."
    };
  }
};

/**
 * Fetch AI item suggestions & spelling corrections using Gemini 2.0 Flash
 */
export const getAIProductSuggestions = async (queryText) => {
  if (!queryText || queryText.trim().length < 2) return [];

  const apiKey = import.meta.env.VITE_GEMINI_API_KEY || "";
  if (!apiKey) return [];

  const PROMPT = `
You are a smart Kirana store inventory assistant in India.
The user is typing a product name: "${queryText.trim()}". It might contain typos, misspellings, phonetics (Hindi/English), or incomplete brand names (e.g., "att" -> "Aashirvaad Atta", "musard" -> "Mustard Oil", "tata s" -> "Tata Salt").

Provide up to 4 corrected, standard Indian Kirana store item name suggestions.
Return strictly valid JSON array only (no markdown, no code blocks):
[
  { "name": "Corrected Product Name", "unit": "kg|litre|packet|piece|gm", "category": "General|Oils & Ghee|Grains & Pulses|Spices & Salt|Snacks & Tea" }
]
`;

  try {
    const response = await fetch(`https://generativelanguage.googleapis.com/v1beta/models/gemini-2.5-flash:generateContent?key=${apiKey}`, {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify({
        contents: [{ role: "user", parts: [{ text: PROMPT }] }]
      })
    });

    if (!response.ok) return [];

    const data = await response.json();
    let rawText = data.candidates?.[0]?.content?.parts?.[0]?.text || "[]";
    rawText = rawText.replace(/```json/g, "").replace(/```/g, "").trim();

    const parsed = JSON.parse(rawText);
    return Array.isArray(parsed) ? parsed : [];
  } catch (err) {
    console.warn("AI suggestion fetch failed:", err);
    return [];
  }
};
