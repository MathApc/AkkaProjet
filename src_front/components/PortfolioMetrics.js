import { useEffect, useState } from "react";
import "./PortfolioMetrics.css";

const API_URL = "http://localhost:8080";

const PortfolioMetrics = () => {
  const [metrics, setMetrics] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);

  useEffect(() => {
    const token = localStorage.getItem("token");
    const userId = localStorage.getItem("userId");

    if (!userId) {
      setError("Utilisateur non authentifié");
      return;
    }

    const fetchMetrics = async () => {
      try {
        const response = await fetch(`${API_URL}/financial/metrics?userId=${userId}`, {
          headers: { Authorization: `Bearer ${token}` },
        });

        if (!response.ok) {
          throw new Error(`Erreur HTTP: ${response.status}`);
        }

        const data = await response.json();
        setMetrics(data);
      } catch (err) {
        setError("Erreur lors du chargement des métriques");
      } finally {
        setLoading(false);
      }
    };

    fetchMetrics();
  }, []);

  return (
    <div className="portfolio-metrics-container">
      <h2 className="chart-title">Métriques du Portefeuille</h2>
      {loading ? (
        <p>Chargement des métriques...</p>
      ) : error ? (
        <p className="error-text">{error}</p>
      ) : (
        <div className="metrics-list">
          <p><strong>NAV :</strong> {metrics.NAV?.toFixed(2)} $</p>
          <p><strong>Market Trend :</strong> {metrics.marketTrend?.toFixed(2)}</p>
        </div>
      )}
    </div>
  );
};

export default PortfolioMetrics;
