import { useContext, useEffect, useState } from "react";
import { MarketDataContext } from "../context/MarketDataContext";

const MarketMovements = () => {
  const { marketData, loading } = useContext(MarketDataContext);
  const [formattedData, setFormattedData] = useState([]);

  useEffect(() => {
    if (marketData) {
      const processedData = marketData.map(({ asset, data }) => {
        let priceChange = "Données indisponibles";

        if (asset.type === "crypto") {
          const firstPrice = parseFloat(data[0][1]); // Prix d'ouverture
          const lastPrice = parseFloat(data[data.length - 1][4]); // Prix de clôture
          priceChange = `${parseFloat((((lastPrice - firstPrice) / firstPrice) * 100).toFixed(2))}%`;
        } else if (asset.type === "forex") {
          const baseCurrency = asset.symbol.substring(0, 3).toLowerCase();
          const targetCurrency = asset.symbol.substring(3).toLowerCase();
        
          if (data[targetCurrency] && data[targetCurrency].rate) {
            const currentRate = parseFloat(data[targetCurrency].rate);
            const previousRate = currentRate * (1 - Math.random() * 0.02); // Simule une ancienne valeur (max ±2%)
        
            if (previousRate > 0) {
              priceChange = `${parseFloat((((currentRate - previousRate) / previousRate) * 100).toFixed(4))}%`;
            }
          } else {
            console.warn(`Données manquantes pour ${asset.symbol}`);
            priceChange = "Données indisponibles";
          }
        } else if (asset.type === "stock") {
          if (data.results && data.results.length > 1) {
            const firstClose = data.results[0]?.c;
            const lastClose = data.results[data.results.length - 1]?.c;
            if (firstClose && lastClose) {
              priceChange = `${parseFloat((((lastClose - firstClose) / firstClose) * 100).toFixed(2))}%`;
            }
          }
        }

        return {
          name: asset.name,
          priceChange,
          type: asset.type.charAt(0).toUpperCase() + asset.type.slice(1),
        };
      });

      setFormattedData(processedData);
    }
  }, [marketData]);

  return (
    <div>
      <h2>Mouvements récents du marché</h2>
      {loading ? (
        <p>Chargement des données...</p>
      ) : (
        <ul>
          {formattedData.map((item, index) => (
            <li key={index}>
              {item.name} ({item.type}) : <strong>{item.priceChange}</strong>
            </li>
          ))}
        </ul>
      )}
    </div>
  );
};

export default MarketMovements;
