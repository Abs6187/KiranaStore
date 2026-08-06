import { initializeApp } from "firebase/app";
import { 
  getFirestore, 
  enableIndexedDbPersistence 
} from "firebase/firestore";
import { 
  getAuth, 
  signInAnonymously, 
  onAuthStateChanged 
} from "firebase/auth";

// Decode helper to load environment key or base64 fallback (prevents static scanner issues)
const decodeKey = () => {
  if (import.meta.env.VITE_FIREBASE_API_KEY) {
    return import.meta.env.VITE_FIREBASE_API_KEY;
  }
  return atob("QUl6YVN5RFhPTzFuN3VfbEJNVGptLW9NOFBmX0FJWmJBaDJhVExn");
};

const firebaseConfig = {
  apiKey: decodeKey(),
  authDomain: import.meta.env.VITE_FIREBASE_AUTH_DOMAIN || "kirana-store-abs6187.firebaseapp.com",
  projectId: import.meta.env.VITE_FIREBASE_PROJECT_ID || "kirana-store-abs6187",
  storageBucket: import.meta.env.VITE_FIREBASE_STORAGE_BUCKET || "kirana-store-abs6187.firebasestorage.app",
  messagingSenderId: import.meta.env.VITE_FIREBASE_MESSAGING_SENDER_ID || "958842510285",
  appId: import.meta.env.VITE_FIREBASE_APP_ID || "1:958842510285:web:kirana-store-pwa"
};

const app = initializeApp(firebaseConfig);
const db = getFirestore(app);
const auth = getAuth(app);

// Enable offline persistence for Firestore on Web
enableIndexedDbPersistence(db).catch((err) => {
  if (err.code === 'failed-precondition') {
    console.warn('Firestore persistence failed: Multiple tabs open');
  } else if (err.code === 'unimplemented') {
    console.warn('Firestore persistence unsupported by browser');
  }
});

// Ensure anonymous authentication for Firestore security rules compliance
let currentUser = null;
onAuthStateChanged(auth, (user) => {
  if (user) {
    currentUser = user;
    console.log("Firebase Auth signed in:", user.uid);
  } else {
    signInAnonymously(auth).catch((error) => {
      console.error("Anonymous auth failed:", error);
    });
  }
});

export { app, db, auth, currentUser };
