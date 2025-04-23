import React, { useState, useEffect } from "react";
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend,
} from "chart.js";
import { Bar, Pie } from "react-chartjs-2";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import axiosInstance from "../../utils/axiosConfig";
import { getTareas } from "../../API";

// Register ChartJS components
ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  Title,
  Tooltip,
  Legend
);

const Dashboard = () => {
  const [userStats, setUserStats] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [selectedSprint, setSelectedSprint] = useState("all");
  const [completedTasks, setCompletedTasks] = useState([]);
  const [users, setUsers] = useState([]);

  const filteredTasks = completedTasks.filter((task) =>
    selectedSprint === "all" || task.sprint === selectedSprint
  );


  useEffect(() => {
    const fetchUserStats = async () => {
      setLoading(true);
      try {
        const response = await axiosInstance.get("/dashboard/user-stats");
        setUserStats(response.data);
        setError("");
      } catch (err) {
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };
    const fetchData = async () => {
      setLoading(true);
      try {
        // Fetch tasks
        const tasksResponse = await getTareas();
        const completed = tasksResponse.filter((task) => task.estadoID === 3); // Filter completed tasks

        // Fetch users
        const usersResponse = await axiosInstance.get("/usuarios/all"); // Adjust endpoint as needed
        const userList = usersResponse.data;
        setUsers(userList);

        // Map user names to tasks
        const tasksWithUserNames = completed.map((task) => {
          const assignedUser = userList.find((user) => user.usuarioID === task.usuarioID);
          return {
            ...task,
            usuarioNombre: assignedUser ? assignedUser.nombre : "Unassigned",
          };
        });

        setCompletedTasks(tasksWithUserNames);
        setError("");
      } catch (err) {
        console.error("Failed to fetch data:", err.message);
        setError("Failed to load data. Please try again later.");
        setCompletedTasks([]);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
    fetchUserStats();
  }, []);

  // Process data for Completed Tasks (Bar Chart)
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
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col w-full">
        <Header 
          headerTitle="Dashboard"
          showVisibilityToggle={false} 
          showFilter={true} 
          showUserFilter={true}
          showAddButton={false}  
        />

        <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
          {loading ? (
            <p>Loading data...</p>
          ) : error ? (
            <p className="text-red-500">{error}</p>
          ) : (

            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* First Row */}
              {/* Completed Tasks Bar Chart */}
              <div className="lg:col-span-2">
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
              </div>

              {/* Task Deviation Table */}
              <div className="lg:col-span-1">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200 h-full">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Deviation</h2>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            User
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Assigned Tasks
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Deviation
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {userStats.map((user, index) => (
                          <tr key={index}>
                            <td className="px-4 py-2 text-s whitespace-nowrap">{user.userName}</td>
                            <td className="px-4 py-2 text-s whitespace-nowrap">{user.tasksAssigned}</td>
                            <td className="px-4 py-2 text-s whitespace-nowrap">{user.taskDeviation}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              {/* Second Row */}
              {/* Performance Pie Chart */}
              <div className="lg:col-span-1">
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
              </div>

              {/* Task Completion Table */}
              <div className="lg:col-span-2">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Completion Table</h2>
                  <div className="overflow-x-auto">
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            User
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Tasks To Do
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Completed Tasks
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Total Tasks
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Percentage Completed
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Total Real Hours
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {userStats.map((user, index) => (
                          <tr key={index}>
                            <td className="px-4 py-2 whitespace-nowrap">{user.userName}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.tasksToDo}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.completedTasks}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.totalTasks}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.percentageCompleted}%</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.totalRealHours}</td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
                
              {/* Third rows */}
              {/* Completed Tasks Table */}
              <div className="lg:col-span-3">
                <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks</h2>
                  <div className="overflow-x-auto overflow-y-auto" style={{ maxHeight: "400px" }}>
                    <table className="min-w-full divide-y divide-gray-200">
                      <thead className="bg-gray-50">
                        <tr>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Task Name
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Developer
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Estimated Hours
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Real Hours
                          </th>
                          <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                            Status
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {completedTasks.map((task, index) => (
                          <tr key={index}>
                            <td className="px-4 py-2 whitespace-nowrap">{task.tareaNombre}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{task.usuarioNombre}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{task.horasEstimadas || "N/A"}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{task.horasReales || "N/A"}</td>
                            <td className="px-4 py-2 whitespace-nowrap">
                              {task.estadoID === 3 ? "Completed" : "N/A"}
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Dashboard;
