import { Routes, Route } from "react-router-dom";
import { MarketDataProvider } from "./context/MarketDataContext";
import { AuthProvider } from "./context/AuthContext";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Navbar from "./components/Navbar";
import styles from "./App.module.css"; // Import du CSS Module
import AjouterActif from "./pages/AjouterActif";
import PrivateRoute from "./PrivateRoute";

function App() {
  return (
    <MarketDataProvider>
      <AuthProvider> 
      <>
        <Navbar />
        <Routes>
          <Route path="/" element={<Home />} />
          <Route path="/dashboard" element={<PrivateRoute />}>
            <Route path="" element={<Dashboard />} />
          </Route>
          <Route path="/login" element={<Login />} />
          <Route path="/ajouter-actif" element={<AjouterActif />} />
        </Routes>
      </>
      </AuthProvider> 
    </MarketDataProvider>
  );
}

export default App;