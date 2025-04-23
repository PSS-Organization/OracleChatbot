import React from "react";
import { Bar } from "react-chartjs-2";

const CompletedTasksBarChart = ({ userStats }) => {
  // Process data for Completed Tasks Bar Chart
  const completedTasksData = {
    labels: userStats.map((user) => user.userName),
    datasets: [
      {
        label: "Completed Tasks",
        data: userStats.map((user) => user.completedTasks),
        backgroundColor: "rgba(0, 149, 214, 0.8)",
        borderColor: "rgba(0, 149, 214, 1)",
        borderWidth: 1,
      },
    ],
  };

  return (
    <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200 h-full">
      <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks Data</h2>
      <div className="h-64">
        <Bar
          data={completedTasksData}
          options={{
            responsive: true,
            maintainAspectRatio: false,
            scales: {
              y: {
                beginAtZero: true,
                title: {
                  display: true,
                  text: "Tasks",
                },
              },
              x: {
                title: {
                  display: true,
                  text: "Users",
                },
              },
            },
          }}
        />
      </div>
    </div>
  );
};

export default CompletedTasksBarChart;