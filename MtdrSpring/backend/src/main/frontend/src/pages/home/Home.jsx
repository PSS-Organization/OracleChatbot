import React, { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import { getUserKPIs, getTareas } from "../../API";
import axiosInstance from "../../utils/axiosConfig";
import {
  Chart as ChartJS,
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler,
} from "chart.js";
import { Bar, Pie, Line } from "react-chartjs-2";

// Register ChartJS components
ChartJS.register(
  ArcElement,
  CategoryScale,
  LinearScale,
  BarElement,
  PointElement,
  LineElement,
  Title,
  Tooltip,
  Legend,
  Filler
);

// Status color mapping
const statusColors = {
  1: { bg: "bg-blue-200", text: "text-blue-800", label: "To Do" },
  2: { bg: "bg-yellow-200", text: "text-yellow-800", label: "In Progress" },
  3: { bg: "bg-green-200", text: "text-green-800", label: "Done" },
};

// Priority color mapping
const priorityColors = {
  ALTA: { bg: "bg-red-200", text: "text-red-800" },
  MEDIA: { bg: "bg-yellow-200", text: "text-yellow-800" },
  BAJA: { bg: "bg-green-200", text: "text-green-800" },
};

// User chart colors
const userChartColors = {
  backgroundColor: "rgba(54, 162, 235, 0.2)",
  borderColor: "rgba(54, 162, 235, 1)",
  pointBackgroundColor: "rgba(54, 162, 235, 1)",
  pointBorderColor: "#fff",
  pointHoverBackgroundColor: "#fff",
  pointHoverBorderColor: "rgba(54, 162, 235, 1)",
};

// Sprint color pool (same as dashboard)
const colorPool = [
  "bg-blue-200 text-blue-800",
  "bg-purple-200 text-purple-800",
  "bg-green-200 text-green-800",
  "bg-orange-200 text-orange-800",
  "bg-red-200 text-red-800",
  "bg-pink-200 text-pink-800",
  "bg-yellow-200 text-yellow-800",
  "bg-teal-200 text-teal-800",
  "bg-indigo-200 text-indigo-800",
  "bg-rose-200 text-rose-800",
];

const getSprintColor = (sprintID) => {
  return colorPool[sprintID % colorPool.length];
};

const Home = () => {
  const [userInfo, setUserInfo] = useState(null);
  const [tasks, setTasks] = useState([]);
  const [allTasks, setAllTasks] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [sprints, setSprints] = useState([]);
  const [users, setUsers] = useState([]);
  const [userSprintProgress, setUserSprintProgress] = useState({
    labels: [],
    data: [],
  });
  const [userStats, setUserStats] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);

  useEffect(() => {
    const fetchData = async () => {
      setLoading(true);
      try {
        const userId = localStorage.getItem("userId");
        if (!userId) throw new Error("User not logged in");

        setCurrentUserId(userId);

        // Fetch user KPIs
        const response = await axiosInstance.get("/dashboard/user-stats");
        const allUserStats = response.data;
        const currentUserStats = allUserStats.find(
          (user) => String(user.userId) === String(userId)
        );

        if (currentUserStats) {
          setUserInfo({ nombre: currentUserStats.userName });
          setUserStats(currentUserStats);
        } else {
          // Fallback if user stats not found
          const userData = await getUserKPIs(userId);
          setUserInfo({ nombre: userData.userName });
        }

        // Fetch all tasks
        const allTasksData = await getTareas();
        setAllTasks(allTasksData);

        // Filter user tasks
        const userTasks = allTasksData.filter(
          (task) => task.usuarioID === parseInt(userId)
        );
        setTasks(userTasks);

        // Fetch sprints
        const sprintsResponse = await axiosInstance.get("/sprints");
        setSprints(sprintsResponse.data);

        // Fetch users for reference
        const usersResponse = await axiosInstance.get("/usuarios/all");
        setUsers(usersResponse.data);

        // Get task progression by sprint
        const userTasksBySprintData = getTasksBySprintForUser(
          allTasksData,
          userId,
          sprintsResponse.data
        );
        setUserSprintProgress(userTasksBySprintData);
      } catch (err) {
        console.error("Error fetching data:", err);
        setError(err.message);
      } finally {
        setLoading(false);
      }
    };

    fetchData();
  }, []);

  // Get tasks grouped by sprint for the current user
  const getTasksBySprintForUser = (allTasks, userId, sprints) => {
    // Filter tasks for the user
    const userTasks = allTasks.filter(
      (task) => String(task.usuarioID) === String(userId)
    );

    // Group tasks by sprint
    const sprintMap = {};

    sprints.forEach((sprint) => {
      sprintMap[sprint.sprintID] = {
        name: sprint.nombreSprint || `Sprint ${sprint.sprintID}`,
        tasksCompleted: 0,
        totalTasks: 0,
        hoursInvested: 0,
      };
    });

    // Count tasks and hours by sprint
    userTasks.forEach((task) => {
      if (!task.sprintID) return;

      if (!sprintMap[task.sprintID]) {
        sprintMap[task.sprintID] = {
          name: `Sprint ${task.sprintID}`,
          tasksCompleted: 0,
          totalTasks: 0,
          hoursInvested: 0,
        };
      }

      sprintMap[task.sprintID].totalTasks++;

      if (task.estadoID === 3) {
        // Completed task
        sprintMap[task.sprintID].tasksCompleted++;
      }

      sprintMap[task.sprintID].hoursInvested += task.horasReales || 0;
    });

    // Convert to arrays for the chart
    const sprintIds = Object.keys(sprintMap)
      .map(Number)
      .sort((a, b) => a - b);

    return {
      labels: sprintIds.map((id) => sprintMap[id].name),
      data: {
        tasksCompleted: sprintIds.map((id) => sprintMap[id].tasksCompleted),
        totalTasks: sprintIds.map((id) => sprintMap[id].totalTasks),
        hoursInvested: sprintIds.map((id) => sprintMap[id].hoursInvested),
      },
    };
  };

  // Calculate KPI metrics
  const getKpiMetrics = () => {
    // If we have user stats from the API, use those
    if (userStats) {
      return {
        totalTasks: userStats.totalTasks || 0,
        completedTasks: userStats.completedTasks || 0,
        percentageCompleted: userStats.percentageCompleted || 0,
        totalRealHours: userStats.totalRealHours || 0,
      };
    }

    // Otherwise calculate from tasks
    const totalTasks = tasks.length;
    const completedTasks = tasks.filter((task) => task.estadoID === 3).length;
    const percentageCompleted =
      totalTasks > 0 ? Math.round((completedTasks / totalTasks) * 100) : 0;
    const totalRealHours = tasks.reduce(
      (sum, task) => sum + (task.horasReales || 0),
      0
    );

    return {
      totalTasks,
      completedTasks,
      percentageCompleted,
      totalRealHours,
    };
  };

  const kpi = getKpiMetrics();

  // Get upcoming tasks (To Do and In Progress)
  const upcomingTasks = tasks
    .filter((task) => task.estadoID === 1 || task.estadoID === 2)
    .sort((a, b) => {
      // Sort by status first (To Do before In Progress)
      if (a.estadoID !== b.estadoID) {
        return a.estadoID - b.estadoID;
      }

      // Then by due date if available
      if (a.fechaEntrega && b.fechaEntrega) {
        return new Date(a.fechaEntrega) - new Date(b.fechaEntrega);
      }

      return 0;
    });

  // Stats by current status
  const tasksByStatus = {
    toDo: tasks.filter((task) => task.estadoID === 1).length,
    inProgress: tasks.filter((task) => task.estadoID === 2).length,
    done: tasks.filter((task) => task.estadoID === 3).length,
  };

  // Calculate data for Task Status Distribution (Pie Chart)
  const taskStatusDistributionData = {
    labels: ["To Do", "In Progress", "Done"],
    datasets: [
      {
        data: [
          tasksByStatus.toDo,
          tasksByStatus.inProgress,
          tasksByStatus.done,
        ],
        backgroundColor: [
          "rgba(54, 162, 235, 0.5)",
          "rgba(255, 206, 86, 0.5)",
          "rgba(75, 192, 192, 0.5)",
        ],
        borderColor: [
          "rgba(54, 162, 235, 1)",
          "rgba(255, 206, 86, 1)",
          "rgba(75, 192, 192, 1)",
        ],
        borderWidth: 1,
      },
    ],
  };

  // Calculate data for Sprint Progress (Line Chart)
  const sprintProgressData = {
    labels: userSprintProgress.labels,
    datasets: [
      {
        label: "Completed Tasks",
        data: userSprintProgress.data.tasksCompleted,
        backgroundColor: "rgba(75, 192, 192, 0.2)",
        borderColor: "rgba(75, 192, 192, 1)",
        borderWidth: 2,
        tension: 0.4,
        fill: false,
      },
      {
        label: "Total Tasks",
        data: userSprintProgress.data.totalTasks,
        backgroundColor: "rgba(54, 162, 235, 0.2)",
        borderColor: "rgba(54, 162, 235, 1)",
        borderWidth: 2,
        tension: 0.4,
        fill: false,
      },
    ],
  };

  // Calculate data for Hours Invested (Bar Chart)
  const hoursInvestedData = {
    labels: userSprintProgress.labels,
    datasets: [
      {
        label: "Hours Invested",
        data: userSprintProgress.data.hoursInvested,
        backgroundColor: "rgba(153, 102, 255, 0.5)",
        borderColor: "rgba(153, 102, 255, 1)",
        borderWidth: 1,
      },
    ],
  };

  // Get user recent activity
  const getRecentCompletedTasks = () => {
    return tasks
      .filter((task) => task.estadoID === 3) // Completed tasks
      .sort((a, b) => {
        // Sort by completion date (assuming newer tasks appear later in the array)
        return b.id - a.id;
      })
      .slice(0, 5); // Get only the 5 most recent
  };

  const recentCompletedTasks = getRecentCompletedTasks();

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col w-full">
        <div className="sticky top-0 z-30 bg-white shadow-md">
          <Header
            //Prueba para OCI CI/CD
            headerTitle={`${
              userInfo?.nombre || "User"
            }'s Dashboard CI/CD Prueba`}
            showUserFilter={false}
            showFilter={false}
          />
        </div>

        <div className="p-4 lg:p-6 overflow-auto">
          {loading ? (
            <div className="flex justify-center items-center h-64">
              <p className="text-lg">Loading your data...</p>
            </div>
          ) : error ? (
            <div className="bg-red-100 p-4 rounded-lg text-red-700">
              <p>{error}</p>
            </div>
          ) : (
            <div className="space-y-6">
              {/* Welcome Section - Using the dashboard style */}
              <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                <h1 className="text-xl font-bold mb-4 border-b pb-2">
                  Welcome, {userInfo?.nombre || "User"}
                </h1>
                <p className="text-gray-600">
                  Here's an overview of your tasks and performance
                </p>
              </div>

              {/* KPI Overview Cards - Using dashboard card style */}
              <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4">
                {/* Tasks Card */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-700">
                      Tasks Completion
                    </h3>
                  </div>
                  <div className="flex flex-col items-center">
                    <p className="text-2xl font-bold">
                      {kpi.completedTasks} / {kpi.totalTasks}
                    </p>
                    <div className="w-full bg-gray-200 rounded-full h-2.5 mt-2">
                      <div
                        className="bg-blue-600 h-2.5 rounded-full"
                        style={{ width: `${kpi.percentageCompleted}%` }}
                      ></div>
                    </div>
                    <p className="text-sm text-gray-500 mt-1">
                      {kpi.percentageCompleted}% completed
                    </p>
                  </div>
                </div>

                {/* Hours Card */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-700">
                      Hours Invested
                    </h3>
                  </div>
                  <div className="flex flex-col items-center">
                    <p className="text-2xl font-bold">{kpi.totalRealHours}</p>
                    <p className="text-sm text-gray-500">Total Hours</p>
                  </div>
                </div>

                {/* Task Status Cards */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-700">In Progress</h3>
                  </div>
                  <div className="flex flex-col items-center">
                    <p className="text-2xl font-bold">
                      {tasksByStatus.inProgress}
                    </p>
                    <p className="text-sm text-gray-500">Tasks</p>
                  </div>
                </div>

                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <div className="flex items-center justify-between mb-2">
                    <h3 className="font-semibold text-gray-700">To Do</h3>
                  </div>
                  <div className="flex flex-col items-center">
                    <p className="text-2xl font-bold">{tasksByStatus.toDo}</p>
                    <p className="text-sm text-gray-500">Tasks</p>
                  </div>
                </div>
              </div>

              {/* Your Performance Section Title */}
              <div className="lg:col-span-3">
                <h2 className="text-xl font-bold mb-4">Your Performance</h2>
              </div>

              {/* Charts Section */}
              <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
                {/* Task Status Distribution */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">
                    Task Status Distribution
                  </h2>
                  <div className="h-64 flex justify-center items-center">
                    {kpi.totalTasks > 0 ? (
                      <Pie
                        data={taskStatusDistributionData}
                        options={{
                          responsive: true,
                          maintainAspectRatio: false,
                          plugins: {
                            legend: {
                              position: "bottom",
                            },
                          },
                        }}
                      />
                    ) : (
                      <p className="text-gray-500">No tasks available</p>
                    )}
                  </div>
                </div>

                {/* Sprint Progress */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200 lg:col-span-2">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">
                    Your Progress Across Sprints
                  </h2>
                  <div className="h-64">
                    {userSprintProgress.labels.length > 0 ? (
                      <Line
                        data={sprintProgressData}
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
                          },
                          plugins: {
                            legend: {
                              position: "top",
                            },
                          },
                        }}
                      />
                    ) : (
                      <div className="flex items-center justify-center h-full">
                        <p className="text-gray-500">
                          No sprint progress data available
                        </p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Sprint Performance Section Title */}
                <div className="lg:col-span-3">
                  <h2 className="text-xl font-bold mb-4">Sprint Performance</h2>
                </div>

                {/* Hours Invested */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200 lg:col-span-2">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">
                    Hours Invested Per Sprint
                  </h2>
                  <div className="h-64">
                    {userSprintProgress.labels.length > 0 ? (
                      <Bar
                        data={hoursInvestedData}
                        options={{
                          responsive: true,
                          maintainAspectRatio: false,
                          scales: {
                            y: {
                              beginAtZero: true,
                              title: {
                                display: true,
                                text: "Hours",
                              },
                            },
                          },
                        }}
                      />
                    ) : (
                      <div className="flex items-center justify-center h-full">
                        <p className="text-gray-500">No hours data available</p>
                      </div>
                    )}
                  </div>
                </div>

                {/* Recent Activity */}
                <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">
                    Recent Completed Tasks
                  </h2>
                  <div
                    className="space-y-3 overflow-y-auto"
                    style={{ maxHeight: "250px" }}
                  >
                    {recentCompletedTasks.length > 0 ? (
                      recentCompletedTasks.map((task) => (
                        <div
                          key={task.id}
                          className="p-3 bg-gray-50 rounded-lg border border-gray-200"
                        >
                          <p className="font-medium text-gray-800">
                            {task.tareaNombre}
                          </p>
                          <div className="flex justify-between items-center mt-1 text-sm">
                            <span className="text-gray-500">
                              {task.horasReales
                                ? `${task.horasReales} hours spent`
                                : "No hours recorded"}
                            </span>
                            <span
                              className={`${statusColors[task.estadoID]?.bg} ${
                                statusColors[task.estadoID]?.text
                              } px-2 py-1 rounded-full text-xs`}
                            >
                              {statusColors[task.estadoID]?.label || "Unknown"}
                            </span>
                          </div>
                        </div>
                      ))
                    ) : (
                      <p className="text-gray-500 py-4 text-center">
                        No completed tasks yet
                      </p>
                    )}
                  </div>
                </div>
              </div>

              {/* Task Details Section */}
              <div className="lg:col-span-3">
                <h2 className="text-xl font-bold mb-4">Task Details</h2>
              </div>

              {/* Task Details Table (matching dashboard style) */}
              <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                <h2 className="text-xl font-bold mb-4 border-b pb-2">
                  Your Tasks
                </h2>
                <div
                  className="overflow-x-auto overflow-y-auto"
                  style={{ maxHeight: "400px" }}
                >
                  <table className="min-w-full divide-y divide-gray-200">
                    <thead className="bg-gray-50">
                      <tr>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Task Name
                        </th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Sprint
                        </th>
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Priority
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
                        <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                          Due Date
                        </th>
                      </tr>
                    </thead>
                    <tbody className="bg-white divide-y divide-gray-200">
                      {tasks.length > 0 ? (
                        tasks.map((task, index) => {
                          // Find sprint name and color
                          const sprint = sprints.find(
                            (s) => s.sprintID === task.sprintID
                          );
                          const sprintName = sprint
                            ? sprint.nombreSprint
                            : `Sprint ${task.sprintID}`;
                          const sprintClass = task.sprintID
                            ? getSprintColor(task.sprintID)
                            : "bg-gray-200 text-gray-800";

                          // Format priority class
                          const priorityClass = task.prioridad
                            ? `${
                                priorityColors[task.prioridad.toUpperCase()]
                                  ?.bg || "bg-gray-200"
                              } ${
                                priorityColors[task.prioridad.toUpperCase()]
                                  ?.text || "text-gray-800"
                              }`
                            : "bg-gray-200 text-gray-800";

                          // Format status class
                          const statusClass = `${
                            statusColors[task.estadoID]?.bg || "bg-gray-200"
                          } ${
                            statusColors[task.estadoID]?.text || "text-gray-800"
                          }`;

                          return (
                            <tr key={index}>
                              <td className="px-4 py-2 whitespace-nowrap">
                                {task.tareaNombre}
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span
                                  className={`px-2 py-1 rounded-full text-xs ${sprintClass}`}
                                >
                                  {sprintName}
                                </span>
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span
                                  className={`px-2 py-1 rounded-full text-xs ${priorityClass}`}
                                >
                                  {task.prioridad || "N/A"}
                                </span>
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                {task.horasEstimadas || "N/A"}
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                {task.horasReales || "N/A"}
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span
                                  className={`px-2 py-1 rounded-full text-xs ${statusClass}`}
                                >
                                  {statusColors[task.estadoID]?.label ||
                                    "Unknown"}
                                </span>
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                {task.fechaEntrega
                                  ? new Date(
                                      task.fechaEntrega
                                    ).toLocaleDateString()
                                  : "No deadline"}
                              </td>
                            </tr>
                          );
                        })
                      ) : (
                        <tr>
                          <td
                            colSpan="7"
                            className="px-4 py-4 text-center text-gray-500"
                          >
                            No tasks assigned
                          </td>
                        </tr>
                      )}
                    </tbody>
                  </table>
                </div>
              </div>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};

export default Home;
