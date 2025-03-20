import { useEffect, useState } from "react";
import { useNavigate } from "react-router-dom";
import { MarketDataProvider } from "../context/MarketDataContext";
import AssetDistributionChart from "../components/AssetDistributionChart";
import FilteredMarketChart from "../components/FilteredMarketChart";
import MarketMovements from "../components/MarketMovements";
import PortfolioMetrics from "../components/PortfolioMetrics";
import { Link } from "react-router-dom";

const API_URL = "http://localhost:8080"; 

function getUserIdFromToken() {
  const token = localStorage.getItem("token");
  if (!token) return null;

  try {
    const payload = JSON.parse(atob(token.split(".")[1])); 
    console.log("Payload du token :", payload);
    return payload.userId || payload.id || null;
  } catch (error) {
    console.error("Erreur lors du décodage du token :", error);
    return null;
  }
}

function Dashboard() {
  const navigate = useNavigate();
  const [assetAllocation, setAssetAllocation] = useState({});
  const [userId, setUserId] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    if (!token) {
      navigate("/login");
      return;
    }

    const id = getUserIdFromToken();
    if (!id) {
      console.error("Aucun userId trouvé.");
      navigate("/login");
      return;
    }

    setUserId(id);

    const fetchPortfolio = async () => {
      try {
        const response = await fetch(`${API_URL}/portfolio?userId=${id}`, {
          headers: { "Authorization": `Bearer ${token}` }
        });

        const data = await response.json();
        if (response.ok) {
          setAssetAllocation(data.assets);
        }
      } catch (err) {
        console.error("Erreur lors du chargement du portefeuille :", err);
      }
    };

    fetchPortfolio();
  }, [navigate]);


  return (
    <div>
      <AssetDistributionChart assetAllocation={assetAllocation} />
      <PortfolioMetrics userId={userId} />
      <FilteredMarketChart />

      <MarketMovements />

      <Link to="/ajouter-actif">
        <button
          style={{
            
            backgroundColor: "#007bff",
            color: "white",
            border: "none",
            padding: "12px 20px",
            borderRadius: "5px",
            cursor: "pointer",
            fontSize: "1rem",
            fontWeight: "bold",
            marginTop: "20px",
            marginLeft: "70%",
            marginBottom: "30px",
            display: "block",
            textAlign: "center",
            width: "fit-content",
            transition: "background 0.3s",
            textDecorationColor: "#007bff",
            
          }}
          onMouseOver={(e) => (e.target.style.backgroundColor = "#0056b3")}
          onMouseOut={(e) => (e.target.style.backgroundColor = "#007bff")}
        >
          Ajouter/Modifier un actif
        </button>
      </Link>
 
      <MarketDataProvider />
    </div>
  );
}

export default Dashboard;
