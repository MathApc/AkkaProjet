import { Line } from "react-chartjs-2";
import { Chart as ChartJS, CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend } from "chart.js";
import "./PerformanceChart.css";

ChartJS.register(CategoryScale, LinearScale, PointElement, LineElement, Title, Tooltip, Legend);

const PerformanceChart = () => {
  const data = {
    labels: ["Jan", "Fév", "Mar", "Avr", "Mai", "Juin"],
    datasets: [
      {
        label: "Valeur du portefeuille (€)",
        data: [1000, 1200, 1400, 1300, 1500, 1700],
        borderColor: "#36A2EB",
        backgroundColor: "rgba(54,162,235,0.2)",
      },
    ],
  };

  return (
    <div className="performance-chart-container">
      <h2 className="chart-title">Performance du portefeuille</h2>
      <Line data={data} />
    </div>
  );
};

export default PerformanceChart;
