import { Pie } from "react-chartjs-2";
import { Chart as ChartJS, ArcElement, Tooltip, Legend } from "chart.js";

ChartJS.register(ArcElement, Tooltip, Legend);

const AssetDistributionChart = () => {
  const data = {
    labels: ["Bitcoin", "Ethereum", "Solana", "Autres"],
    datasets: [
      {
        data: [50, 30, 15, 5], // % par actif
        backgroundColor: ["#FF6384", "#36A2EB", "#FFCE56", "#4BC0C0"],
      },
    ],
  };

  return (
    <div>
      <h2>Répartition des actifs</h2>
      <Pie data={data} />
    </div>
  );
};

export default AssetDistributionChart;
