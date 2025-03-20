import { useEffect, useState } from "react";
import { Line } from "react-chartjs-2";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from "chart.js";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const MarketTrendChart = () => {
  const [chartData, setChartData] = useState(null);
  const [loading, setLoading] = useState(true);

  const API_KEY = "YOUR_ALPHA_VANTAGE_API_KEY"; //                          CE CODE N EST PAS UTILISER :!!!!!!!!!!!!!!!!!!!!!!
  // Liste des cryptos, forex et actions
  const assets = [
    { type: "crypto", symbol: "BTCUSDT", name: "Bitcoin", color: "#FF6384" },
    { type: "crypto", symbol: "ETHUSDT", name: "Ethereum", color: "#36A2EB" },
    { type: "forex", symbol: "EURUSD", name: "EUR/USD", color: "#4BC0C0" },
    { type: "forex", symbol: "GBPUSD", name: "GBP/USD", color: "#9966FF" },
    { type: "stock", symbol: "AAPL", name: "Apple", color: "#FFCE56" },
    { type: "stock", symbol: "TSLA", name: "Tesla", color: "#FF9F40" },
  ];

  useEffect(() => {
    const fetchMarketData = async () => {
      try {
        const responses = await Promise.all(
          assets.map(async (asset) => {
            let url = "";

            if (asset.type === "crypto") {
              url = `https://api.binance.com/api/v3/klines?symbol=${asset.symbol}&interval=1h&limit=24`;
            } else if (asset.type === "forex") {
              url = `https://www.alphavantage.co/query?function=FX_INTRADAY&from_symbol=${asset.symbol.substring(0,3)}&to_symbol=${asset.symbol.substring(3)}&interval=60min&apikey=${API_KEY}`;
            } else if (asset.type === "stock") {
              url = `https://www.alphavantage.co/query?function=TIME_SERIES_INTRADAY&symbol=${asset.symbol}&interval=60min&apikey=${API_KEY}`;
            }

            const response = await fetch(url);
            const data = await response.json();
            return { asset, data };
          })
        );

        console.log("Données API :", responses);

        // Labels horaires basés sur la première réponse reçue
        const labels = responses[0].data.map((entry, index) => `H-${index}`);

        const datasets = responses.map(({ asset, data }) => {
          let values;
          
          if (asset.type === "crypto") {
            values = data.map((entry) => parseFloat(entry[4])); // Prix de clôture
          } else if (asset.type === "forex") {
            values = Object.values(data[`Time Series FX (60min)`] || {}).map((item) => parseFloat(item["4. close"]));
          } else if (asset.type === "stock") {
            values = Object.values(data[`Time Series (60min)`] || {}).map((item) => parseFloat(item["4. close"]));
          }

          return {
            label: asset.name,
            data: values,
            borderColor: asset.color,
            backgroundColor: asset.color + "33",
            tension: 0.3,
          };
        });

        setChartData({ labels, datasets });
        setLoading(false);
      } catch (error) {
        console.error("Erreur API :", error);
      }
    };

    fetchMarketData();
  }, []);

  return (
    <div>
      <h2>Évolution des Actifs (24h)</h2>
      {loading ? <p>Chargement des données...</p> : <Line data={chartData} />}
    </div>
  );
};

export default MarketTrendChart;
