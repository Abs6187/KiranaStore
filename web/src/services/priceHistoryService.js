import { 
  collection, 
  addDoc, 
  onSnapshot 
} from "firebase/firestore";
import { db } from "../firebase";

const PRICE_HISTORY_COLLECTION = "price_history";

/**
 * Add a price change record to immutable price_history collection
 */
export const addPriceHistory = async ({ productId, productName, oldPrice, newPrice, changeReason = "Manual" }) => {
  try {
    await addDoc(collection(db, PRICE_HISTORY_COLLECTION), {
      productId: String(productId),
      productName: productName || "Unknown Product",
      oldPrice: Number(oldPrice) || 0,
      newPrice: Number(newPrice) || 0,
      changeReason,
      changedAt: new Date()
    });
  } catch (err) {
    console.error("Error adding price history:", err);
  }
};

/**
 * Real-time subscription to price history logs
 */
export const subscribePriceHistory = (callback) => {
  const colRef = collection(db, PRICE_HISTORY_COLLECTION);

  return onSnapshot(colRef, (snapshot) => {
    const history = snapshot.docs.map((docSnap) => {
      const data = docSnap.data();
      return {
        id: docSnap.id,
        ...data,
        changedAt: data.changedAt?.toDate ? data.changedAt.toDate() : (data.changedAt ? new Date(data.changedAt) : new Date())
      };
    });

    history.sort((a, b) => b.changedAt - a.changedAt);
    callback(history);
  }, (err) => {
    console.error("Error loading price history:", err);
    callback([]);
  });
};
