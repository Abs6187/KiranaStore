import { 
  collection, 
  doc, 
  onSnapshot, 
  addDoc, 
  updateDoc, 
  deleteDoc, 
  serverTimestamp, 
  query, 
  orderBy 
} from "firebase/firestore";
import { db } from "../firebase";
import { addPriceHistory } from "./priceHistoryService";

const PRODUCTS_COLLECTION = "products";

/**
 * Subscribe to real-time updates of all products from Firestore
 */
export const subscribeProducts = (callback) => {
  const q = query(collection(db, PRODUCTS_COLLECTION), orderBy("createdAt", "desc"));
  return onSnapshot(q, (snapshot) => {
    const products = snapshot.docs.map((docSnap) => {
      const data = docSnap.data();
      return {
        id: docSnap.id,
        ...data,
        // Fallback for timestamps if missing
        createdAt: data.createdAt?.toDate ? data.createdAt.toDate() : new Date(),
        lastUpdated: data.lastUpdated?.toDate ? data.lastUpdated.toDate() : new Date()
      };
    });
    callback(products);
  }, (error) => {
    console.error("Error fetching products from Firestore:", error);
  });
};

/**
 * Add a new product to Firestore
 */
export const addProduct = async ({ name, salesPrice, purchasePrice = 0, unit = "kg", category = "General", isPinned = false }) => {
  const newProd = {
    name: name.trim(),
    salesPrice: parseFloat(salesPrice) || 0,
    purchasePrice: parseFloat(purchasePrice) || 0,
    unit: unit.trim() || "kg",
    category: category.trim() || "General",
    isPinned: Boolean(isPinned),
    createdAt: serverTimestamp(),
    lastUpdated: serverTimestamp()
  };

  const docRef = await addDoc(collection(db, PRODUCTS_COLLECTION), newProd);
  
  // Record initial price history entry
  await addPriceHistory({
    productId: docRef.id,
    productName: newProd.name,
    oldPrice: 0,
    newPrice: newProd.salesPrice,
    changeReason: "Initial Creation"
  });

  return docRef.id;
};

/**
 * Update the selling price of a product & log history
 */
export const updateSalesPrice = async (productId, productName, oldPrice, newPrice, changeReason = "Manual Edit") => {
  const pId = String(productId);
  const parsedNewPrice = parseFloat(newPrice);
  if (isNaN(parsedNewPrice)) return;

  const productRef = doc(db, PRODUCTS_COLLECTION, pId);
  await updateDoc(productRef, {
    salesPrice: parsedNewPrice,
    lastUpdated: serverTimestamp()
  });

  await addPriceHistory({
    productId: pId,
    productName,
    oldPrice: parseFloat(oldPrice) || 0,
    newPrice: parsedNewPrice,
    changeReason
  });
};

/**
 * Toggle pin status for a product
 */
export const togglePinProduct = async (productId, currentIsPinned) => {
  const productRef = doc(db, PRODUCTS_COLLECTION, String(productId));
  await updateDoc(productRef, {
    isPinned: !currentIsPinned
  });
};

/**
 * Delete a product
 */
export const deleteProduct = async (productId) => {
  const productRef = doc(db, PRODUCTS_COLLECTION, String(productId));
  await deleteDoc(productRef);
};
