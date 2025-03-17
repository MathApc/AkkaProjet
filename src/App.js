import { Routes, Route } from "react-router-dom";
import { MarketDataProvider } from "./context/MarketDataContext";
import Home from "./pages/Home";
import Login from "./pages/Login";
import Dashboard from "./pages/Dashboard";
import Navbar from "./components/Navbar";

function App() {
  return (
    <MarketDataProvider>
      <Navbar />
      <Routes>
        <Route path="/" element={<Home />} />
        <Route path="/login" element={<Login />} />
        <Route path="/dashboard" element={<Dashboard />} />
      </Routes>
    </MarketDataProvider>
  );
}

export default App;
