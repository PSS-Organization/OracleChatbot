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
import { Bar, Pie, Line } from "react-chartjs-2";
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

// Define a consistent color palette for team members
const userColors = {
  backgroundColor: [
    "rgba(75, 192, 192, 0.8)",
    "rgba(153, 102, 255, 0.8)",
    "rgba(255, 159, 64, 0.8)",
    "rgba(255, 99, 132, 0.8)",
    "rgba(54, 162, 235, 0.8)",
    "rgba(255, 206, 86, 0.8)",
    "rgba(231, 233, 237, 0.8)",
    "rgba(104, 132, 245, 0.8)",
    "rgba(173, 216, 230, 0.8)",
    "rgba(144, 238, 144, 0.8)",
  ],
  borderColor: [
    "rgba(75, 192, 192, 1)",
    "rgba(153, 102, 255, 1)",
    "rgba(255, 159, 64, 1)",
    "rgba(255, 99, 132, 1)",
    "rgba(54, 162, 235, 1)",
    "rgba(255, 206, 86, 1)",
    "rgba(231, 233, 237, 1)",
    "rgba(104, 132, 245, 1)",
    "rgba(173, 216, 230, 1)",
    "rgba(144, 238, 144, 1)",
  ],
};

// Priority color mapping
const priorityColors = {
  "ALTA": { bg: "bg-red-200", text: "text-red-800" },
  "MEDIA": { bg: "bg-yellow-200", text: "text-yellow-800" },
  "BAJA": { bg: "bg-green-200", text: "text-green-800" },
};

// Status color mapping
const statusColors = {
  1: { bg: "bg-blue-200", text: "text-blue-800", label: "To Do" },
  2: { bg: "bg-yellow-200", text: "text-yellow-800", label: "In Progress" },
  3: { bg: "bg-green-200", text: "text-green-800", label: "Done" },
};

// Sprint color mapping function
const getSprintColor = (sprintID) => {
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
  return colorPool[sprintID % colorPool.length];
};

const Dashboard = () => {
  const [userStats, setUserStats] = useState([]);
  const [deviationData, setDeviationData] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [allTasks, setAllTasks] = useState([]);
  const [users, setUsers] = useState([]);
  const [filters, setFilters] = useState({ sprints: [], users: [] });
  const [userColorMap, setUserColorMap] = useState({});
  const [sprints, setSprints] = useState([]);
  const [viewMode, setViewMode] = useState("individual"); // "individual" or "group"

  // For testing, we'll manually map user names to their IDs
  // This helps handle inconsistencies in the data
  const userNameToIdMap = {
    'Developer 1': 1,
    'Developer 2': 2, 
    'Developer 3': 3,
    'Developer 4': 4
  };

  // Create a detailed debug log to track filter operations
  const debugFilter = (source, data, filterResult, reason) => {
    console.log(`Filter operation on ${source}:`, { 
      inputData: data,
      filterResult: filterResult,
      reason: reason,
      appliedFilters: filters
    });
  };

  // Helper function to match user by id, regardless of property name
  const userIdMatches = (user, filterIds) => {
    if (!filterIds || filterIds.length === 0) return true;
    
    // Try all possible ID properties
    let possibleIds = [
      user.userId, 
      user.usuarioID, 
      user.id
    ].filter(Boolean).map(String);
    
    // For user stats, try to get the ID from the name mapping
    if (user.userName && userNameToIdMap[user.userName]) {
      possibleIds.push(String(userNameToIdMap[user.userName]));
    }
    
    // For deviation data, try to get the ID from the usuario field
    if (user.usuario && userNameToIdMap[user.usuario]) {
      possibleIds.push(String(userNameToIdMap[user.usuario]));
    }
    
    // Debug the IDs
    console.log(`User ID comparison for ${user.userName || user.usuario || 'unknown user'}:`, {
      possibleUserIds: possibleIds,
      filterIds: filterIds.map(String),
      user: user
    });
    
    // If we still have no valid IDs to match against, try to match by name as a fallback
    if (possibleIds.length === 0 && (user.userName || user.usuario)) {
      const userName = user.userName || user.usuario;
      const matchingUser = users.find(u => u.nombre === userName);
      if (matchingUser) {
        possibleIds.push(String(matchingUser.usuarioID));
        console.log(`Found matching user by name: ${userName}`, { matchingUserId: matchingUser.usuarioID });
      }
    }
    
    // Check if any of the user's possible IDs match any of the filter IDs
    const matches = filterIds.some(filterId => possibleIds.includes(String(filterId)));
    console.log(`ID match result for ${user.userName || user.usuario || 'unknown user'}: ${matches}`);
    
    return matches;
  };

  // Filter tasks based on selected filters
  const filteredTasks = allTasks.filter((task) => {
    // Apply sprint filter
    const sprintMatch = filters.sprints.length === 0 || 
      (task.sprintID && filters.sprints.some(id => String(id) === String(task.sprintID)));
    
    // Apply user filter
    const userMatch = filters.users.length === 0 || 
      (task.usuarioID && filters.users.some(id => String(id) === String(task.usuarioID)));
    
    const result = sprintMatch && userMatch;
    
    // Debug this specific filter if debugging is enabled
    if (filters.users.length > 0 || filters.sprints.length > 0) {
      debugFilter('task', task, result, 
        `sprintMatch: ${sprintMatch}, userMatch: ${userMatch}`);
    }
    
    return result;
  }).sort((a, b) => {
    // First sort by sprint ID
    if (a.sprintID !== b.sprintID) {
      // Handle null sprintID values (null values come last)
      if (a.sprintID === null) return 1;
      if (b.sprintID === null) return -1;
      return a.sprintID - b.sprintID;
    }
    
    // Then sort by user name
    const userA = a.usuarioNombre || '';
    const userB = b.usuarioNombre || '';
    return userA.localeCompare(userB);
  });

  // Create a function to check if a user has tasks in the filtered sprints
  const userHasTasksInFilteredSprints = (user) => {
    // If no sprint filters are active, return true
    if (filters.sprints.length === 0) {
      return true;
    }
    
    // Get all possible user IDs
    const possibleIds = [
      user.userId, 
      user.usuarioID, 
      user.id
    ].filter(Boolean).map(String);
    
    // Check if the user has any tasks in the filtered sprints
    const result = allTasks.some(task => 
      possibleIds.some(id => String(task.usuarioID) === id) && 
      filters.sprints.some(sprintId => String(sprintId) === String(task.sprintID))
    );
    
    return result;
  };

  // Filter user stats based on selected filters
  const filteredUserStats = userStats.filter((user) => {
    // First check user filter
    const userMatch = userIdMatches(user, filters.users);
    
    // Then check sprint filter (if user has tasks in filtered sprints)
    const sprintMatch = userHasTasksInFilteredSprints(user);
    
    const result = userMatch && sprintMatch;
    
    // Debug this specific filter if debugging is enabled
    if (filters.users.length > 0 || filters.sprints.length > 0) {
      debugFilter('userStats', user, result, 
        `userMatch: ${userMatch}, sprintMatch: ${sprintMatch}`);
    }
    
    return result;
  });

  // Filter deviation data based on selected filters
  const filteredDeviationData = deviationData.filter((user) => {
    // First check user filter
    const userMatch = userIdMatches(user, filters.users);
    
    // Then check sprint filter (if user has tasks in filtered sprints)
    const sprintMatch = userHasTasksInFilteredSprints(user);
    
    const result = userMatch && sprintMatch;
    
    // Debug this specific filter if debugging is enabled
    if (filters.users.length > 0 || filters.sprints.length > 0) {
      debugFilter('deviationData', user, result, 
        `userMatch: ${userMatch}, sprintMatch: ${sprintMatch}`);
    }
    
    return result;
  });

  // Function to fetch task deviation data
  const fetchTaskDeviation = async () => {
    try {
      // Build the URL with optional sprint filter parameters
      let url = "/dashboard/task-deviation";
      
      // If there are sprint filters, add them as query parameters
      if (filters.sprints && filters.sprints.length > 0) {
        const sprintParams = filters.sprints.map(id => `sprintIds=${id}`).join('&');
        url = `${url}?${sprintParams}`;
      }
      
      // Fetch the task deviation data from the backend
      const response = await axiosInstance.get(url);
      console.log("Task deviation data (raw):", response.data);
      
      // Validate the response data
      if (Array.isArray(response.data)) {
        // Ensure each item has the required fields
        const validData = response.data.map(item => {
          // Make sure we have a valid object
          if (!item) return null;
          
          // Normalize the data by ensuring all required fields exist
          return {
            usuarioID: item.usuarioID || null,
            usuario: item.usuario || null,
            tareas_asignadas: item.tareas_asignadas || 0,
            desviacion_individual: item.desviacion_individual !== undefined ? 
              item.desviacion_individual : null
          };
        }).filter(Boolean); // Remove any null items
        
        // Update the color mapping to include these users
        const updatedColorMap = { ...userColorMap };
        validData.forEach((user, index) => {
          if (user.usuario && !updatedColorMap[user.usuario]) {
            updatedColorMap[user.usuario] = {
              backgroundColor: userColors.backgroundColor[index % userColors.backgroundColor.length],
              borderColor: userColors.borderColor[index % userColors.borderColor.length]
            };
          }
        });
        
        // Update the color map if new entries were added
        if (Object.keys(updatedColorMap).length > Object.keys(userColorMap).length) {
          console.log("Updated user color map:", updatedColorMap);
          setUserColorMap(updatedColorMap);
        }
        
        console.log("Task deviation data (processed):", validData);
        setDeviationData(validData);
      } else {
        console.error("Invalid task deviation data format:", response.data);
        setDeviationData([]);
      }
    } catch (err) {
      console.error("Failed to fetch task deviation data:", err.message);
      // Fallback to empty data
      setDeviationData([]);
    }
  };

  // For debugging: log filter status whenever it changes
  useEffect(() => {
    if (filters.users.length > 0 || filters.sprints.length > 0) {
      console.log("Filters changed:", filters);
      console.log("Filtered user stats:", filteredUserStats);
      console.log("Filtered deviation data:", filteredDeviationData);
      console.log("Filtered tasks:", filteredTasks);
    }
  }, [filters, filteredUserStats, filteredDeviationData, filteredTasks]);

  // Fetch all data
  useEffect(() => {
    const fetchUserStats = async () => {
      try {
        const response = await axiosInstance.get("/dashboard/user-stats");
        const userData = response.data;
        console.log("User stats data (raw):", userData);
        
        // Ensure userId is properly set for each user
        const processedUserStats = userData.map(user => ({
          ...user,
          // Make sure userId is a number
          userId: user.userId || user.usuarioID || parseInt(user.userName.replace(/\D/g, ''), 10) || null
        }));
        
        console.log("User stats data (processed):", processedUserStats);
        setUserStats(processedUserStats);
        
        // Create color mapping for users
        const colorMapping = {};
        processedUserStats.forEach((user, index) => {
          colorMapping[user.userName] = {
            backgroundColor: userColors.backgroundColor[index % userColors.backgroundColor.length],
            borderColor: userColors.borderColor[index % userColors.borderColor.length]
          };
        });
        setUserColorMap(colorMapping);
      } catch (err) {
        console.error("Failed to fetch user stats:", err.message);
        setError(err.message);
      }
    };
    
    const fetchSprints = async () => {
      try {
        const response = await axiosInstance.get("/sprints");
        setSprints(response.data);
      } catch (err) {
        console.error("Failed to fetch sprints:", err.message);
      }
    };
    
    const fetchData = async () => {
      setLoading(true);
      try {
        // Fetch all tasks
        const tasksResponse = await getTareas();

        // Fetch users
        const usersResponse = await axiosInstance.get("/usuarios/all");
        const userList = usersResponse.data;
        setUsers(userList);

        // Map user names to tasks
        const tasksWithUserNames = tasksResponse.map((task) => {
          const assignedUser = userList.find((user) => user.usuarioID === task.usuarioID);
          return {
            ...task,
            usuarioNombre: assignedUser ? assignedUser.nombre : "Unassigned",
          };
        });

        setAllTasks(tasksWithUserNames);
        setError("");
      } catch (err) {
        console.error("Failed to fetch data:", err.message);
        setError("Failed to load data. Please try again later.");
        setAllTasks([]);
      } finally {
        setLoading(false);
      }
    };

    // Fetch all data in parallel
    Promise.all([
      fetchData(),
      fetchUserStats(),
      fetchTaskDeviation(),
      fetchSprints()
    ]);
  }, []);

  // Refresh task deviation data when users data changes
  useEffect(() => {
    // Only run this if we have users data and the deviation data hasn't been loaded yet
    if (users.length > 0 && deviationData.length === 0) {
      console.log("Users data loaded, refreshing task deviation data");
      fetchTaskDeviation();
    }
  }, [users]);

  // Process data for Completed Tasks (Bar Chart)
  const completedTasksData = {
    labels: filteredUserStats.map((user) => user.userName),
    datasets: [
      {
        label: "Completed Tasks",
        data: filteredUserStats.map((user) => user.completedTasks),
        backgroundColor: filteredUserStats.map(user => userColorMap[user.userName]?.backgroundColor || "rgba(0, 149, 214, 0.8)"),
        borderColor: filteredUserStats.map(user => userColorMap[user.userName]?.borderColor || "rgba(0, 149, 214, 1)"),
        borderWidth: 1,
      },
    ],
  };

  // Process data for Developer Hours Bar Chart (replacing Performance Pie Chart)
  const developerHoursData = {
    labels: filteredUserStats.map((user) => user.userName),
    datasets: [
      {
        label: "Total Hours Invested",
        data: filteredUserStats.map((user) => user.totalRealHours),
        backgroundColor: filteredUserStats.map(user => userColorMap[user.userName]?.backgroundColor || "rgba(0, 149, 214, 0.8)"),
        borderColor: filteredUserStats.map(user => userColorMap[user.userName]?.borderColor || "rgba(0, 149, 214, 1)"),
        borderWidth: 1,
      },
    ],
  };

  // Group tasks by sprint for progression tracking
  const getTasksBySprintAndUser = () => {
    const result = {};
    
    // Initialize the result object with all sprints
    sprints.forEach(sprint => {
      result[sprint.sprintID] = {
        sprintName: sprint.nombreSprint || `Sprint ${sprint.sprintID}`,
        userStats: {}
      };
    });
    
    // Group tasks by sprint and user
    // Use filteredTasks instead of allTasks to respect current filters
    filteredTasks.forEach(task => {
      if (!task.sprintID || !task.usuarioID) return;
      
      // Initialize sprint if needed
      if (!result[task.sprintID]) {
        result[task.sprintID] = {
          sprintName: `Sprint ${task.sprintID}`,
          userStats: {}
        };
      }
      
      // Get or create user stats for this sprint
      const sprintUserStats = result[task.sprintID].userStats;
      
      // Find user name
      const userName = users.find(u => u.usuarioID === task.usuarioID)?.nombre || `User ${task.usuarioID}`;
      
      // Initialize user stats if needed
      if (!sprintUserStats[userName]) {
        sprintUserStats[userName] = {
          tasksCompleted: 0,
          hoursInvested: 0
        };
      }
      
      // Update stats based on task completion
      if (task.estadoID === 3) { // Completed task
        sprintUserStats[userName].tasksCompleted += 1;
      }
      // Always add hours regardless of completion status
      sprintUserStats[userName].hoursInvested += task.horasReales || 0;
    });
    
    return result;
  };

  const taskProgressionBySprintAndUser = getTasksBySprintAndUser();
  
  // Prepare sprint progression data for charts
  const getSprintProgressionData = () => {
    // Get all sprints sorted by ID, but only include those that have data
    const sortedSprintIds = Object.keys(taskProgressionBySprintAndUser)
      .map(Number)
      .filter(sprintId => {
        // Only include sprints that have user data
        const hasUserData = Object.keys(taskProgressionBySprintAndUser[sprintId].userStats).length > 0;
        return hasUserData;
      })
      .sort((a, b) => a - b);
    
    // Get unique user names across all sprints
    const userNames = new Set();
    sortedSprintIds.forEach(sprintId => {
      Object.keys(taskProgressionBySprintAndUser[sprintId].userStats).forEach(userName => {
        userNames.add(userName);
      });
    });
    
    // Calculate datasets based on view mode
    let completedTasksDatasets = [];
    let hoursInvestedDatasets = [];
    
    if (viewMode === "individual") {
      // Individual view - one line per user
      completedTasksDatasets = Array.from(userNames).map((userName, index) => ({
        label: userName,
        data: sortedSprintIds.map(sprintId => {
          const userStats = taskProgressionBySprintAndUser[sprintId].userStats[userName];
          return userStats ? userStats.tasksCompleted : 0;
        }),
        backgroundColor: userColorMap[userName]?.backgroundColor || userColors.backgroundColor[index % userColors.backgroundColor.length],
        borderColor: userColorMap[userName]?.borderColor || userColors.borderColor[index % userColors.borderColor.length],
        borderWidth: 2,
        tension: 0.4,
        fill: false,
        pointBackgroundColor: userColorMap[userName]?.borderColor || userColors.borderColor[index % userColors.borderColor.length],
        pointBorderColor: '#fff',
        pointRadius: 4,
        pointHoverRadius: 6
      }));
      
      hoursInvestedDatasets = Array.from(userNames).map((userName, index) => ({
        label: userName,
        data: sortedSprintIds.map(sprintId => {
          const userStats = taskProgressionBySprintAndUser[sprintId].userStats[userName];
          return userStats ? userStats.hoursInvested : 0;
        }),
        backgroundColor: userColorMap[userName]?.backgroundColor || userColors.backgroundColor[index % userColors.backgroundColor.length],
        borderColor: userColorMap[userName]?.borderColor || userColors.borderColor[index % userColors.borderColor.length],
        borderWidth: 2,
        tension: 0.4,
        fill: false,
        pointBackgroundColor: userColorMap[userName]?.borderColor || userColors.borderColor[index % userColors.borderColor.length],
        pointBorderColor: '#fff',
        pointRadius: 4,
        pointHoverRadius: 6
      }));
    } else {
      // Group view - one line for total
      const totalCompletedTasks = sortedSprintIds.map(sprintId => {
        let sprintTotal = 0;
        Object.values(taskProgressionBySprintAndUser[sprintId].userStats).forEach(stats => {
          sprintTotal += stats.tasksCompleted || 0;
        });
        return sprintTotal;
      });
      
      const totalHoursInvested = sortedSprintIds.map(sprintId => {
        let sprintTotal = 0;
        Object.values(taskProgressionBySprintAndUser[sprintId].userStats).forEach(stats => {
          sprintTotal += stats.hoursInvested || 0;
        });
        return sprintTotal;
      });
      
      completedTasksDatasets = [{
        label: 'Total Tasks Completed',
        data: totalCompletedTasks,
        backgroundColor: 'rgba(75, 192, 192, 0.2)',
        borderColor: 'rgba(75, 192, 192, 1)',
        borderWidth: 3,
        tension: 0.4,
        fill: true,
        pointBackgroundColor: 'rgba(75, 192, 192, 1)',
        pointBorderColor: '#fff',
        pointRadius: 5,
        pointHoverRadius: 7
      }];
      
      hoursInvestedDatasets = [{
        label: 'Total Hours Invested',
        data: totalHoursInvested,
        backgroundColor: 'rgba(54, 162, 235, 0.2)',
        borderColor: 'rgba(54, 162, 235, 1)',
        borderWidth: 3,
        tension: 0.4,
        fill: true,
        pointBackgroundColor: 'rgba(54, 162, 235, 1)',
        pointBorderColor: '#fff',
        pointRadius: 5,
        pointHoverRadius: 7
      }];
    }
    
    // Prepare data for completed tasks progression chart (line chart)
    const completedTasksProgression = {
      labels: sortedSprintIds.map(sprintId => taskProgressionBySprintAndUser[sprintId].sprintName),
      datasets: completedTasksDatasets
    };
    
    // Prepare data for hours invested progression chart (line chart)
    const hoursInvestedProgression = {
      labels: sortedSprintIds.map(sprintId => taskProgressionBySprintAndUser[sprintId].sprintName),
      datasets: hoursInvestedDatasets
    };
    
    return {
      completedTasksProgression,
      hoursInvestedProgression,
      hasSprints: sortedSprintIds.length > 0
    };
  };
  
  const sprintProgressionData = getSprintProgressionData();

  // Handle filter changes
  const handleFilterChange = (newFilters) => {
    console.log("New filters applied:", newFilters);
    console.log("User filters:", newFilters.users);
    console.log("Sprint filters:", newFilters.sprints);
    
    // Map through user stats to see what IDs we have
    console.log("Available user IDs in userStats:", userStats.map(u => ({
      name: u.userName, 
      userId: u.userId
    })));
    console.log("Available user IDs in deviation data:", deviationData.map(d => ({
      name: d.usuario, 
      userId: d.usuarioID
    })));
    
    setFilters(newFilters);
  };

  // Update user task stats when the allTasks or filters change
  useEffect(() => {
    // Skip if no tasks data yet
    if (allTasks.length === 0) return;

    // Refresh task deviation data when filters change
    fetchTaskDeviation();
    
    // Temporary fix: Manually create mapping between filters and user data
    // This ensures that when filters are changed, the user stats are immediately updated
    if (userStats.length > 0 && (filters.users.length > 0 || filters.sprints.length > 0)) {
      console.log("Manually refreshing user stats with filter data");
      
      // Create a deep copy of user stats
      const updatedUserStats = JSON.parse(JSON.stringify(userStats));
      
      // Update the userId fields to match the exact format used in the filters
      updatedUserStats.forEach(userStat => {
        // For each user stat, find the corresponding user in the users list
        const matchedUser = users.find(u => 
          u.nombre === userStat.userName || 
          String(u.usuarioID) === String(userStat.userId)
        );
        
        if (matchedUser) {
          // Ensure the userId matches exactly
          userStat.userId = matchedUser.usuarioID;
          console.log(`Updated user stat ID for ${userStat.userName} to ${userStat.userId}`);
        }
      });
      
      // Replace the userStats with the updated copy
      setUserStats(updatedUserStats);
    }
  }, [allTasks, filters]);

  // Get sprint name by ID
  const getSprintName = (sprintID) => {
    const sprint = sprints.find(s => s.sprintID === sprintID);
    return sprint ? sprint.nombreSprint : `Sprint ${sprintID}`;
  };

  // Get user name from ID
  const getUserName = (userId) => {
    const user = users.find(u => u.usuarioID === userId);
    return user ? user.nombre : `User ${userId}`;
  };

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col w-full">
        {/* Make Header sticky with proper z-index and background */}
        <div className="sticky top-0 z-20 bg-white shadow-md">
        <Header 
          headerTitle="Dashboard"
          showVisibilityToggle={false} 
          showFilter={true} 
          showUserFilter={true}
          showAddButton={false}  
            onFilterChange={handleFilterChange}
          />
          
          {/* Add filter info inside the sticky header */}
          <div className="py-2 px-4 bg-white border-t">
            <div className="flex justify-between items-center">
              <div className="text-sm text-gray-600">
                {filters.users.length > 0 && (
                  <span className="mr-4">
                    Filtered by users: {filters.users.map(userId => 
                      getUserName(userId)
                    ).join(', ')}
                  </span>
                )}
                {filters.sprints.length > 0 && (
                  <span>
                    Filtered by sprints: {filters.sprints.map(sprintId => 
                      getSprintName(sprintId)
                    ).join(', ')}
                  </span>
                )}
                {filters.users.length === 0 && filters.sprints.length === 0 && (
                  <span>No filters applied - showing all data</span>
                )}
              </div>
              <div>
                <button 
                  onClick={() => handleFilterChange({ users: [], sprints: [] })}
                  className="px-3 py-1 bg-gray-200 hover:bg-gray-300 text-gray-700 text-xs rounded-md ml-2"
                >
                  Clear Filters
                </button>
              </div>
            </div>
          </div>
        </div>

        <div className="p-4 lg:p-6 overflow-auto">
          {/* Remove the old sticky filter bar since it's now in the header */}
          
          {loading ? (
            <p>Loading data...</p>
          ) : error ? (
            <p className="text-red-500">{error}</p>
          ) : (
            <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
              {/* OVERVIEW SECTION - First Row */}
              {/* Sprint Progression Charts */}
              <div className="lg:col-span-3">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                  <div className="flex justify-between items-center mb-4 border-b pb-2">
                    <h2 className="text-xl font-bold">Team Progress Overview</h2>
                    <div className="flex items-center">
                      <label htmlFor="viewMode" className="mr-2 text-sm text-gray-600">View Mode:</label>
                      <select 
                        id="viewMode" 
                        className="border rounded px-2 py-1 text-sm"
                        value={viewMode}
                        onChange={(e) => setViewMode(e.target.value)}
                      >
                        <option value="individual">Individual</option>
                        <option value="group">Team Total</option>
                      </select>
                    </div>
                  </div>
                  
                  {/* Tasks Completed by Sprint */}
                  <div className="mb-6">
                    <h3 className="text-lg font-semibold mb-2">Tasks Completed Across Sprints</h3>
                    <div className="h-64">
                      {sprintProgressionData.hasSprints ? (
                        <Line
                          data={sprintProgressionData.completedTasksProgression}
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
                                  text: "Sprints",
                                },
                              },
                            },
                            plugins: {
                              tooltip: {
                                mode: 'index',
                                intersect: false
                              },
                              legend: {
                                position: 'top',
                              }
                            }
                          }}
                        />
                      ) : (
                        <div className="flex items-center justify-center h-full">
                          <p className="text-gray-500">No sprint progression data available with current filters</p>
                        </div>
                      )}
                    </div>
                  </div>
                  
                  {/* Hours Invested by Sprint */}
                  <div>
                    <h3 className="text-lg font-semibold mb-2">Hours Invested Across Sprints</h3>
                    <div className="h-64">
                      {sprintProgressionData.hasSprints ? (
                        <Line
                          data={sprintProgressionData.hoursInvestedProgression}
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
                              x: {
                                title: {
                                  display: true,
                                  text: "Sprints",
                                },
                              },
                            },
                            plugins: {
                              tooltip: {
                                mode: 'index',
                                intersect: false
                              },
                              legend: {
                                position: 'top',
                              }
                            }
                          }}
                        />
                      ) : (
                        <div className="flex items-center justify-center h-full">
                          <p className="text-gray-500">No sprint hours data available with current filters</p>
                        </div>
                      )}
                    </div>
                  </div>
                </div>
              </div>

              {/* DEVELOPER PERFORMANCE SECTION - Second Row */}
              <div className="lg:col-span-3">
                <h2 className="text-xl font-bold mb-4">Developer Performance</h2>
              </div>
              
              {/* Developer Hours Bar Chart */}
              <div className="lg:col-span-1">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Developer Hours</h2>
                  <div className="h-64 flex justify-center">
                    {filteredUserStats.length > 0 ? (
                      <Bar
                        data={developerHoursData}
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
                            x: {
                              title: {
                                display: true,
                                text: "Users",
                              },
                            },
                          },
                        }}
                      />
                    ) : (
                      <div className="flex items-center justify-center h-full">
                        <p className="text-gray-500">No data available with current filters</p>
                      </div>
                    )}
                  </div>
                </div>
              </div>

              {/* Task Completion Table */}
              <div className="lg:col-span-2">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Developer Performance Summary</h2>
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
                        {filteredUserStats.length > 0 ? filteredUserStats.map((user, index) => (
                          <tr key={index}>
                            <td className="px-4 py-2 whitespace-nowrap">
                              <span
                                className="px-2 py-1 rounded-full"
                                style={{ 
                                  backgroundColor: userColorMap[user.userName]?.backgroundColor || "rgba(0, 149, 214, 0.2)",
                                  color: "black"
                                }}
                              >
                                {user.userName}
                              </span>
                            </td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.tasksToDo}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.completedTasks}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.totalTasks}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.percentageCompleted}%</td>
                            <td className="px-4 py-2 whitespace-nowrap">{user.totalRealHours}</td>
                          </tr>
                        )) : (
                          <tr>
                            <td colSpan="6" className="px-4 py-4 text-center text-gray-500">
                              No data available with current filters
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>

              {/* TASK ANALYSIS SECTION - Third Row */}
              <div className="lg:col-span-3">
                <h2 className="text-xl font-bold mb-4">Task Analysis</h2>
              </div>
              
              {/* Completed Tasks Bar Chart */}
              <div className="lg:col-span-2">
                <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200 h-full">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks by Developer</h2>
                  <div className="h-64">
                    {filteredUserStats.length > 0 ? (
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
                    ) : (
                      <div className="flex items-center justify-center h-full">
                        <p className="text-gray-500">No data available with current filters</p>
                      </div>
                    )}
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
                        {filteredDeviationData.length > 0 ? filteredDeviationData.map((user, index) => {
                          // Try several fallbacks to get the user name
                          let userName = null;
                          
                          // First try: use the usuario property from the backend
                          if (user.usuario) {
                            userName = user.usuario;
                          } 
                          // Second try: look up by user ID in the users list
                          else if (user.usuarioID) {
                            const matchedUser = users.find(u => u.usuarioID === user.usuarioID);
                            if (matchedUser) {
                              userName = matchedUser.nombre;
                            }
                          }
                          
                          // Final fallback
                          if (!userName) {
                            userName = "Unknown User";
                          }
                          
                          return (
                          <tr key={index}>
                              <td className="px-4 py-2 text-s whitespace-nowrap">
                                <span
                                  className="px-2 py-1 rounded-full"
                                  style={{ 
                                    backgroundColor: userColorMap[userName]?.backgroundColor || "rgba(0, 149, 214, 0.2)",
                                    color: "black"
                                  }}
                                >
                                  {userName}
                                </span>
                              </td>
                              <td className="px-4 py-2 text-s whitespace-nowrap">{user.tareas_asignadas || 0}</td>
                              <td className="px-4 py-2 text-s whitespace-nowrap">
                                {user.desviacion_individual !== undefined ? (
                                  <span className={`px-2 py-1 rounded-full text-xs ${
                                    user.desviacion_individual > 0 ? "bg-red-200 text-red-800" : 
                                    user.desviacion_individual < 0 ? "bg-blue-200 text-blue-800" : 
                                    "bg-green-200 text-green-800"
                                  }`}>
                                    {user.desviacion_individual > 0 ? `+${user.desviacion_individual}` : user.desviacion_individual}
                                  </span>
                                ) : (
                                  <span className="text-gray-500">N/A</span>
                                )}
                              </td>
                          </tr>
                          );
                        }) : (
                          <tr>
                            <td colSpan="3" className="px-4 py-4 text-center text-gray-500">
                              No deviation data available with current filters
                            </td>
                          </tr>
                        )}
                      </tbody>
                    </table>
                  </div>
                </div>
              </div>
                
              {/* TASK DETAILS SECTION - Fourth Row */}            
              {/* All Tasks Table */}
              <div className="lg:col-span-3">
                <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                  <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Details</h2>
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
                            Sprint
                          </th>
                        </tr>
                      </thead>
                      <tbody className="bg-white divide-y divide-gray-200">
                        {filteredTasks.length > 0 ? filteredTasks.map((task, index) => {
                          const priorityClass = task.prioridad ? 
                            `${priorityColors[task.prioridad.toUpperCase()]?.bg || "bg-gray-200"} ${priorityColors[task.prioridad.toUpperCase()]?.text || "text-gray-800"}` : 
                            "bg-gray-200 text-gray-800";
                          
                          const statusClass = 
                            `${statusColors[task.estadoID]?.bg || "bg-gray-200"} ${statusColors[task.estadoID]?.text || "text-gray-800"}`;
                            
                          const sprintClass = task.sprintID ? getSprintColor(task.sprintID) : "bg-gray-200 text-gray-800";
                          
                          const userColor = users.find(u => u.usuarioID === task.usuarioID)?.nombre 
                              ? userColorMap[users.find(u => u.usuarioID === task.usuarioID)?.nombre]?.backgroundColor
                              : "rgba(0, 149, 214, 0.2)";
                          
                          return (
                          <tr key={index}>
                            <td className="px-4 py-2 whitespace-nowrap">{task.tareaNombre}</td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span
                                  className="px-2 py-1 rounded-full"
                                  style={{ 
                                    backgroundColor: userColor,
                                    color: "black"
                                  }}
                                >
                                  {task.usuarioNombre}
                                </span>
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span className={`px-2 py-1 rounded-full text-xs ${priorityClass}`}>
                                  {task.prioridad || "N/A"}
                                </span>
                              </td>
                            <td className="px-4 py-2 whitespace-nowrap">{task.horasEstimadas || "N/A"}</td>
                            <td className="px-4 py-2 whitespace-nowrap">{task.horasReales || "N/A"}</td>
                            <td className="px-4 py-2 whitespace-nowrap">
                                <span className={`px-2 py-1 rounded-full text-xs ${statusClass}`}>
                                  {statusColors[task.estadoID]?.label || "Unknown"}
                                </span>
                              </td>
                              <td className="px-4 py-2 whitespace-nowrap">
                                <span className={`px-2 py-1 rounded-full text-xs ${sprintClass}`}>
                                  {getSprintName(task.sprintID) || "N/A"}
                                </span>
                              </td>
                            </tr>
                          );
                        }) : (
                          <tr>
                            <td colSpan="7" className="px-4 py-4 text-center text-gray-500">
                              No tasks match current filters
                            </td>
                          </tr>
                        )}
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
