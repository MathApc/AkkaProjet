import { useEffect } from "react";
import { useNavigate } from "react-router-dom";
import { MarketDataProvider } from "../context/MarketDataContext";
import AssetDistributionChart from "../components/AssetDistributionChart";
import PerformanceChart from "../components/PerformanceChart";
import FilteredMarketChart from "../components/FilteredMarketChart";
import MarketMovements from "../components/MarketMovements";
import { Link } from "react-router-dom";

const SESSION_TIMEOUT = 15 * 60 * 1000; // 15 minutes

function Dashboard() {
  const navigate = useNavigate();

  useEffect(() => {
    const checkSession = () => {
      const expiry = localStorage.getItem("expiry");

      if (!expiry || new Date().getTime() > expiry) {
        handleLogout();
      } else {
        // Rafraîchir la session (étendre le temps d'expiration à chaque action)
        const newExpiry = new Date().getTime() + SESSION_TIMEOUT;
        localStorage.setItem("expiry", newExpiry);
      }
    };

    // Vérification immédiate
    checkSession();

    // Vérification automatique toutes les minutes
    const interval = setInterval(checkSession, 60000);

    return () => clearInterval(interval); // Nettoyage de l'intervalle
  }, []);

  const handleLogout = () => {
    localStorage.removeItem("user");
    localStorage.removeItem("expiry");
    navigate("/login");
  };

  return (
    <div>
      <h1>Dashboard</h1>
      <button onClick={handleLogout}>Se déconnecter</button>
      <AssetDistributionChart />
      <PerformanceChart />
      <FilteredMarketChart />
      <MarketMovements />
      <Link to="/ajouter-actif">
        <button>Ajouter/Modifier un actif</button>
      </Link>
      <MarketDataProvider />
    </div>
    
  );
}

export default Dashboard;
