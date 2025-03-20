import { Pie } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";
import "./AssetDistributionChart.css";

ChartJS.register(ArcElement, Tooltip, Legend);

const AssetDistributionChart = ({ assetAllocation }) => {
  const chartData = {
    labels: Object.keys(assetAllocation),
    datasets: [
      {
        label: "Répartition des actifs",
        data: Object.values(assetAllocation),
        backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#4CAF50"],
      },
    ],
  };

  console.log("Format final des données du camembert :", chartData);

  return (
    <div className="chart-container">
      <h2 className="chart-title">Répartition des Actifs</h2>
      <Pie data={chartData} />
    </div>
  );
};

export default AssetDistributionChart;
