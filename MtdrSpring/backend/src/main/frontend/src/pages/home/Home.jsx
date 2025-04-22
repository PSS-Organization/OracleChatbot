import React, { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import { Bar, Pie } from "react-chartjs-2";

const Home = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchUserInfo = async () => {
            setLoading(true);
            try {
                const userId = localStorage.getItem("userId"); // Get the logged-in user's ID
                if (!userId) throw new Error("User not logged in");

                // Fetch user-specific KPIs from /home/user-kpis
                const response = await fetch(`/home/user-kpis`, {
                    method: "GET",
                    headers: {
                        "Content-Type": "application/json",
                        "userId": userId, // Pass the userId in the request header
                    },
                });
                if (!response.ok) throw new Error("Error fetching user KPIs");
                const data = await response.json();

                // Set user info and tasks based on the response
                setUserInfo({ nombre: data.userName }); // Assuming userName is part of the response
                setTasks(data.tasks || []); // If tasks are included in the response
                setError("");
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchUserInfo();
    }, []);

    // Calculate Key Performance Indicators (KPIs)
    const totalTasks = tasks.length;
    const completedTasks = tasks.filter((task) => task.completado).length;
    const percentageCompleted = totalTasks > 0 ? ((completedTasks / totalTasks) * 100).toFixed(2) : 0;

    const totalEstimatedHours = tasks.reduce((sum, task) => sum + (task.horasEstimadas || 0), 0);
    const totalRealHours = tasks.reduce((sum, task) => sum + (task.horasReales || 0), 0);

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col">
            <Header
                headerTitle="Home"
                onFilterChange={(filters) => {
                    const selectedSprint = filters.sprints[0] || "all";
                    if (selectedSprint === "all") {
                    setTasks(tasks); // Show all tasks
                    } else {
                    setTasks(tasks.filter((task) => task.sprint === selectedSprint));
                    }
                }}
                showUserFilter={false}
            />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    {loading ? (
                        <p>Loading user information...</p>
                    ) : error ? (
                        <p className="text-red-500">{error}</p>
                    ) : (
                        <>
                            {/* User Info Section */}
                            <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200 mb-6">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Welcome, {userInfo.nombre}</h2>
                            </div>

                            {/* Sprint Card */}
                            <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200 mb-6">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Sprint Overview</h2>
                                <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                                    <div className="bg-blue-100 p-4 rounded-lg shadow-md">
                                        <h3 className="text-lg font-bold mb-2">Completed vs Uncompleted Tasks</h3>
                                        <Pie
                                            data={{
                                                labels: ["Completed", "Uncompleted"],
                                                datasets: [
                                                    {
                                                        data: [
                                                            completedTasks,
                                                            totalTasks - completedTasks,
                                                        ],
                                                        backgroundColor: ["#4CAF50", "#FF6384"],
                                                    },
                                                ],
                                            }}
                                        />
                                    </div>
                                    <div className="bg-green-100 p-4 rounded-lg shadow-md">
                                        <h3 className="text-lg font-bold mb-2">Hours Done for Sprint</h3>
                                        <Bar
                                            data={{
                                                labels: ["Real Hours", "Estimated Hours"],
                                                datasets: [
                                                    {
                                                        label: "Hours",
                                                        data: [totalRealHours, totalEstimatedHours],
                                                        backgroundColor: ["#36A2EB", "#FFCE56"],
                                                    },
                                                ],
                                            }}
                                            options={{
                                                scales: {
                                                    y: {
                                                        beginAtZero: true,
                                                    },
                                                },
                                            }}
                                        />
                                    </div>
                                </div>
                            </div>

                            {/* My Tasks Section */}
                            <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">My Tasks</h2>
                                {tasks.length === 0 ? (
                                    <p>No tasks available!</p>
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="min-w-full divide-y divide-gray-200">
                                            <thead className="bg-gray-50">
                                                <tr>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Task</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Deadline</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Sprint</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                                </tr>
                                            </thead>
                                            <tbody className="bg-white divide-y divide-gray-200">
                                                {tasks.map((task, index) => (
                                                    <tr key={index}>
                                                        <td className="px-6 py-4 whitespace-nowrap">{task.nombre}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">
                                                            {task.fechaLimite
                                                                ? new Date(task.fechaLimite).toLocaleDateString()
                                                                : "No deadline"}
                                                        </td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{task.sprint || "N/A"}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">
                                                            {task.completado ? "Completed" : "In Progress"}
                                                        </td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Home;