import { useEffect, useState, useContext } from "react";
import { Line } from "react-chartjs-2";
import { MarketDataContext } from "../context/MarketDataContext";
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

  const POLYGON_API_KEY = "ipie3FkT8KPYK6C644eEZQsAPXhepwp6"; // clé API Polygon.io

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
                console.log(`Données API Forex pour ${asset.symbol}:`, data);
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

        let firstValidResponse = validResponses.find((r) => r.data);
        let labels = [];

        if (firstValidResponse && firstValidResponse.data) {
          if (selectedPeriod === "1d") {
            labels = firstValidResponse.data.map((entry) => {
              const date = new Date(entry[0]);
              return date.toLocaleTimeString("fr-FR", { hour: "2-digit", minute: "2-digit" });
            });
          } else {
            const relativeLabels = {
              "7d": ["il y a 1 semaine", "il y a 3 jours", "aujourd'hui"],
              "30d": ["il y a 1 mois", "il y a 3 semaines", "il y a 2 semaines", "cette semaine"],
              "180d": ["il y a 6 mois", "il y a 4 mois", "il y a 2 mois", "actuellement"],
              "365d": ["il y a 1 an", "il y a 9 mois", "il y a 6 mois", "il y a 3 mois", "actuellement"],
            };
            labels = relativeLabels[selectedPeriod] || [];
          }
        }

        console.log("Labels générés :", labels);

        const datasets = validResponses
          .map(({ asset, data }) => {
            if (!selectedCategories.includes(asset.type)) return null;

            let values = [];
            if (asset.type === "crypto") {
              values = data.map((entry) => parseFloat(entry[4])); // Prix de clôture
            } else if (asset.type === "forex") {
              values = Object.values(data).slice(0, periodOptions[selectedPeriod].days).map((entry) => parseFloat(entry.rate));
            } else if (asset.type === "stock") {
              values = data.results.map((entry) => entry.c); // Prix de clôture sur Polygon.io
            }

            return {
              label: asset.name,
              data: values.slice(-labels.length), // Adapter les valeurs aux labels
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
    <div>
      <h2>Évolution des Actifs</h2>

      {/* Sélecteur de période */}
      <label htmlFor="period">Période : </label>
      <select id="period" value={selectedPeriod} onChange={(e) => setSelectedPeriod(e.target.value)}>
        {Object.entries(periodOptions).map(([key, value]) => (
          <option key={key} value={key}>
            {value.label}
          </option>
        ))}
      </select>

      {/* Boutons de filtre */}
      <div>
        <button onClick={() => setSelectedCategories(["crypto", "forex", "stock"])}>Tout afficher</button>
        <button onClick={() => setSelectedCategories(["crypto"])}>Cryptos</button>
        <button onClick={() => setSelectedCategories(["forex"])}>Forex</button>
        <button onClick={() => setSelectedCategories(["stock"])}>Actions</button>
      </div>

      {loading || !chartData.labels.length ? <p>Chargement des données...</p> : <Line data={chartData} />}
    </div>
  );
};

export default FilteredMarketChart;
