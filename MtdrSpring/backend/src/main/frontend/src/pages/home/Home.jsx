import React, { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import { getUserKPIs, getTareas } from "../../API"; // Import getTareas

const Home = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [tasks, setTasks] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [daysLeft, setDaysLeft] = useState(null); // Days left in the sprint

    useEffect(() => {
        const fetchUserInfo = async () => {
            setLoading(true);
            try {
                const userId = localStorage.getItem("userId");
                if (!userId) throw new Error("User not logged in");

                // Fetch user KPIs
                const data = await getUserKPIs(userId);
                setUserInfo({ nombre: data.userName });
                setError("");

                // Fetch tasks
                const allTasks = await getTareas();
                const userTasks = allTasks.filter((task) => task.usuarioID === parseInt(userId));
                setTasks(userTasks);

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

    // Filter "To Do" tasks
    const toDoTasks = tasks.filter((task) => task.estadoID === 1); // Assuming "To Do" status has estadoID = 1

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col">
                <Header
                    headerTitle={`${userInfo?.nombre || "User"} Home`}
                    showUserFilter={false}
                />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    {loading ? (
                        <p>Loading user information...</p>
                    ) : error ? (
                        <p className="text-red-500">{error}</p>
                    ) : (
                        <>
                            {/* Sprint Overview */}
                            <h2 className="text-xl font-bold mb-4 border-b pb-2">Sprint Overview</h2>
                            <div className="grid grid-cols-1 md:grid-cols-4 gap-6">
                                {/* Tasks Completed vs Remaining */}
                                <div className="bg-blue-100 p-4 rounded-lg shadow-md flex flex-col items-center">
                                <div className="text-blue-600 text-4xl mb-2">
                                    <i className="fas fa-tasks"></i> {/* Task Icon */}
                                </div>
                                <p className="text-2xl font-bold">{completedTasks} / {totalTasks}</p>
                                <p className="text-sm text-gray-600 mb-2">Tasks Completed</p>
                                {/* Progress Bar */}
                                <div className="w-full bg-blue-200 rounded-full h-4">
                                    <div
                                    className="bg-blue-600 h-4 rounded-full"
                                    style={{ width: `${percentageCompleted}%` }}
                                    ></div>
                                </div>
                                </div>

                                {/* Real Hours Accomplished */}
                                <div className="bg-green-100 p-4 rounded-lg shadow-md flex flex-col items-center">
                                    <div className="text-green-600 text-4xl mb-2">
                                        <i className="fas fa-clock"></i> {/* Time Icon */}
                                    </div>
                                    <p className="text-2xl font-bold">{totalRealHours}</p>
                                    <p className="text-sm text-gray-600">Hours Done</p>
                                </div>

                                {/* Key Performance Indicator */}
                                <div className="bg-purple-100 p-4 rounded-lg shadow-md flex flex-col items-center">
                                    <div className="text-purple-600 text-4xl mb-2">
                                        <i className="fas fa-chart-line"></i> {/* KPI Icon */}
                                    </div>
                                    <p className="text-2xl font-bold">{percentageCompleted}%</p>
                                    <p className="text-sm text-gray-600">Completion Rate</p>
                                </div>

                                {/* Days Left in Sprint */}
                                <div className="bg-red-100 p-4 rounded-lg shadow-md flex flex-col items-center">
                                    <div className="text-red-600 text-4xl mb-2">
                                        <i className="fas fa-calendar-alt"></i> {/* Calendar Icon */}
                                    </div>
                                    <p className="text-2xl font-bold">{daysLeft !== null ? daysLeft : "8"}</p>
                                    <p className="text-sm text-gray-600">Current Sprint</p>
                                </div>
                            </div>

                            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 mt-6">
                                {/* To Do Tasks Table */}
                                <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                                    <h2 className="text-xl font-bold mb-4 border-b pb-2">To Do Tasks</h2>
                                    {toDoTasks.length === 0 ? (
                                    <p>No tasks in "To Do" status!</p>
                                    ) : (
                                    <div className="overflow-x-auto">
                                        <table className="min-w-full divide-y divide-gray-200">
                                        <thead className="bg-gray-50">
                                            <tr>
                                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Task Name</th>
                                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Due Date</th>
                                            <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Status</th>
                                            </tr>
                                        </thead>
                                        <tbody className="bg-white divide-y divide-gray-200">
                                            {toDoTasks.map((task, index) => (
                                            <tr key={index}>
                                                <td className="px-4 py-2 text-xs whitespace-nowrap">{task.tareaNombre}</td>
                                                <td className="px-4 py-2 text-xs whitespace-nowrap">
                                                {task.fechaEntrega
                                                    ? new Date(task.fechaEntrega).toLocaleDateString()
                                                    : "No deadline"}
                                                </td>
                                                <td className="px-4 py-2 text-xs whitespace-nowrap">To Do</td>
                                            </tr>
                                            ))}
                                        </tbody>
                                        </table>
                                    </div>
                                    )}
                                </div>

                                {/* Completed Tasks and Sprint Info Card */}
                                <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                                    <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks Overview</h2>
                                    <div className="space-y-4">
                                        <div className="flex justify-between">
                                            <span className="font-medium text-gray-600">Completed Tasks:</span>
                                            <span className="font-bold text-gray-800">{completedTasks}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="font-medium text-gray-600">Estimated Hours:</span>
                                            <span className="font-bold text-gray-800">{totalEstimatedHours}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="font-medium text-gray-600">Real Hours:</span>
                                            <span className="font-bold text-gray-800">{totalRealHours}</span>
                                        </div>
                                        <div className="flex justify-between">
                                            <span className="font-medium text-gray-600">Sprint:</span>
                                            <span className="font-bold text-gray-800">
                                            {tasks.length > 0 && tasks[0].sprint ? tasks[0].sprint : "N/A"}
                                            </span>
                                        </div>
                                    </div>
                                </div>
                            </div>
                        </>
                    )}
                </div>
            </div>
        </div>
    );
};

export default Home;