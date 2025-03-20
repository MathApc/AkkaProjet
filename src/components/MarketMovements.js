import { useContext, useEffect } from "react";
import { MarketDataContext } from "../context/MarketDataContext";
import "./MarketMovements.css";

const MarketMovements = () => {
  const { marketData, setMarketData, loading, setLoading } = useContext(MarketDataContext);

  useEffect(() => {
    if (!marketData || marketData.length === 0) {
      setLoading(true);
    }
  }, [marketData, setLoading]);

  return (
    <div className="market-movements-container">
      {loading ? (
        <p className="loading-text">Chargement des données...</p>
      ) : marketData && marketData.length > 0 ? (
        <ul className="market-list">
          {marketData.map((asset) => {
            let displayValue;

            if (asset.asset.type === "crypto") {
              displayValue = `$${parseFloat(asset.data[asset.data.length - 1][4]).toFixed(2)}`; // Dernier prix de clôture
            } else if (asset.asset.type === "forex") {
              const forexRates = Object.values(asset.data);
              displayValue = forexRates.length > 0 ? `${parseFloat(forexRates[0].rate).toFixed(4)} USD` : "Données indisponibles";
            } else if (asset.asset.type === "stock") {
              const stockPrices = asset.data.results;
              displayValue = stockPrices.length > 0 ? `$${parseFloat(stockPrices[stockPrices.length - 1].c).toFixed(2)}` : "Données indisponibles";
            } else {
              displayValue = "Données indisponibles";
            }

            return (
              <li key={asset.asset.symbol} className="market-item">
                {asset.asset.name} ({asset.asset.symbol}) : <strong>{displayValue}</strong>
              </li>
            );
          })}
        </ul>
      ) : (
        <p className="no-data-text">Aucune donnée disponible.</p>
      )}
    </div>
  );
};

export default MarketMovements;
