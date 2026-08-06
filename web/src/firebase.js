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

const firebaseConfig = {
  apiKey: "AIzaSyC06YbrZ3FMApz-Wwqj4UlNhO-jyOG58Bs",
  authDomain: "kirana-store-abs6187.firebaseapp.com",
  projectId: "kirana-store-abs6187",
  storageBucket: "kirana-store-abs6187.firebasestorage.app",
  messagingSenderId: "958842510285",
  appId: "1:958842510285:web:kirana-store-pwa"
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
