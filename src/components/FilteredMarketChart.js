import { useEffect, useState, useContext } from "react";
import { Line } from "react-chartjs-2";
import { MarketDataContext } from "../context/MarketDataContext";
import "./FilteredMarketChart.css";
import {
  Chart as ChartJS,
  CategoryScale,
  LinearScale,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const FilteredMarketChart = () => {
  const { setMarketData, setLoading, loading } = useContext(MarketDataContext);
  const [chartData, setChartData] = useState({ labels: [], datasets: [] });
  const [selectedCategories, setSelectedCategories] = useState(["crypto", "forex", "stock"]);
  const [selectedPeriod, setSelectedPeriod] = useState("1d");

  const POLYGON_API_KEY = ""//"ipie3FkT8KPYK6C644eEZQsAPXhepwp6"; // clé API Polygon.io

  const assets = [
    { type: "crypto", symbol: "BTCUSDT", name: "Bitcoin", color: "#FF6384" },
    { type: "crypto", symbol: "ETHUSDT", name: "Ethereum", color: "#36A2EB" },
    { type: "forex", symbol: "EURUSD", name: "EUR/USD", color: "#4BC0C0" },
    { type: "forex", symbol: "GBPUSD", name: "GBP/USD", color: "#9966FF" },
    { type: "stock", symbol: "AAPL", name: "Apple", color: "#FFCE56" },
    { type: "stock", symbol: "TSLA", name: "Tesla", color: "#FF9F40" },
  ];

  const periodOptions = {
    "1d": { label: "24h", days: 1, binanceInterval: "1h", polygonMultiplier: 1, polygonTimespan: "hour" },
    "7d": { label: "1 semaine", days: 7, binanceInterval: "4h", polygonMultiplier: 1, polygonTimespan: "day" },
    "30d": { label: "1 mois", days: 30, binanceInterval: "12h", polygonMultiplier: 1, polygonTimespan: "day" },
    "180d": { label: "6 mois", days: 180, binanceInterval: "1d", polygonMultiplier: 7, polygonTimespan: "day" },
    "365d": { label: "1 an", days: 365, binanceInterval: "1w", polygonMultiplier: 30, polygonTimespan: "day" },
  };

  useEffect(() => {
    const fetchMarketData = async () => {
      try {
        setLoading(true);

        const responses = await Promise.all(
          assets.map(async (asset) => {
            let url = "";
            let data = null;

            try {
              if (asset.type === "crypto") {
                url = `https://api.binance.com/api/v3/klines?symbol=${asset.symbol}&interval=${periodOptions[selectedPeriod].binanceInterval}&limit=${selectedPeriod === "1d" ? 24 : periodOptions[selectedPeriod].days}`;
                const response = await fetch(url);
                if (!response.ok) throw new Error(`Erreur Binance ${response.status}`);
                data = await response.json();
              } else if (asset.type === "forex") {
                url = `https://www.floatrates.com/daily/${asset.symbol.substring(0, 3).toLowerCase()}.json`;
                const response = await fetch(url);
                if (!response.ok) throw new Error(`Erreur Forex ${response.status}`);
                data = await response.json();
              } else if (asset.type === "stock") {
                const endDate = new Date().toISOString().split("T")[0];
                const startDate = new Date();
                startDate.setDate(startDate.getDate() - periodOptions[selectedPeriod].days);
                const formattedStartDate = startDate.toISOString().split("T")[0];

                url = `https://api.polygon.io/v2/aggs/ticker/${asset.symbol}/range/${periodOptions[selectedPeriod].polygonMultiplier}/${periodOptions[selectedPeriod].polygonTimespan}/${formattedStartDate}/${endDate}?apiKey=${POLYGON_API_KEY}`;

                const response = await fetch(url);
                if (!response.ok) throw new Error(`Erreur Polygon.io ${response.status}`);
                data = await response.json();
              }

              return { asset, data };
            } catch (error) {
              console.error(`Erreur API pour ${asset.name} (${asset.type}) :`, error);
              return null;
            }
          })
        );

        const validResponses = responses.filter((r) => r && r.data);
        if (validResponses.length === 0) {
          console.error("Erreur : Aucune donnée exploitable trouvée.");
          setLoading(false);
          return;
        }

        let labels = validResponses[0].data.map((entry, index) => `Point ${index + 1}`);

        const datasets = validResponses
          .map(({ asset, data }) => {
            if (!selectedCategories.includes(asset.type)) return null;

            let values = [];
            if (asset.type === "crypto") {
              values = data.map((entry) => parseFloat(entry[4]));
            } else if (asset.type === "forex") {
              values = Object.values(data).slice(0, periodOptions[selectedPeriod].days).map((entry) => parseFloat(entry.rate));
            } else if (asset.type === "stock") {
              values = data.results.map((entry) => entry.c);
            }

            return {
              label: asset.name,
              data: values.slice(-labels.length),
              borderColor: asset.color,
              backgroundColor: asset.color + "33",
              tension: 0.3,
            };
          })
          .filter((dataset) => dataset !== null);

        setChartData({ labels, datasets });
        setMarketData(validResponses);
        setLoading(false);
      } catch (error) {
        console.error("Erreur API :", error);
        setLoading(false);
      }
    };

    fetchMarketData();
  }, [selectedCategories, selectedPeriod]);

  return (
    <div className="filtered-market-container">
      <h2 className="chart-title">Évolution du marché</h2>

      <label htmlFor="period" className="label-select">Période : </label>
      <select id="period" value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)} className="dropdown">
        {Object.entries(periodOptions).map(([key, value]) => (
          <option key={key} value={key}>{value.label}</option>
        ))}
      </select>

      <div className="button-group">
        <button className="filter-button" onClick={() => setSelectedCategories(["crypto", "forex", "stock"])}>Tout afficher</button>
        <button className="filter-button" onClick={() => setSelectedCategories(["crypto"])}>Cryptos</button>
        <button className="filter-button" onClick={() => setSelectedCategories(["forex"])}>Forex</button>
        <button className="filter-button" onClick={() => setSelectedCategories(["stock"])}>Actions</button>
      </div>

      {loading || !chartData.labels.length ? <p>Chargement des données...</p> : <Line data={chartData} />}
    </div>
  );
};

export default FilteredMarketChart;
