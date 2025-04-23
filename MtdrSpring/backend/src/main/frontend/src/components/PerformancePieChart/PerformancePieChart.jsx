import React from "react";
import { Pie } from "react-chartjs-2";

const PerformancePieChart = ({ userStats }) => {
  // Process data for Performance Pie Chart
  const performanceData = {
    labels: userStats.map((user) => user.userName),
    datasets: [
      {
        label: "Tasks Completed",
        data: userStats.map((user) => user.completedTasks),
        backgroundColor: [
          "rgba(75, 192, 192, 0.8)",
          "rgba(153, 102, 255, 0.8)",
          "rgba(255, 159, 64, 0.8)",
          "rgba(255, 99, 132, 0.8)",
          "rgba(54, 162, 235, 0.8)",
        ],
        borderColor: [
          "rgba(75, 192, 192, 1)",
          "rgba(153, 102, 255, 1)",
          "rgba(255, 159, 64, 1)",
          "rgba(255, 99, 132, 1)",
          "rgba(54, 162, 235, 1)",
        ],
        borderWidth: 1,
      },
    ],
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
      <h2 className="text-xl font-bold mb-4 border-b pb-2">Performance</h2>
      <div className="h-64 flex justify-center">
        <Pie
          data={performanceData}
          options={{
            responsive: true,
            maintainAspectRatio: false,
          }}
        />
      </div>
    </div>
  );
};

export default PerformancePieChart;