import React, { useState, useEffect } from 'react';
import { Chart as ChartJS, ArcElement, CategoryScale, LinearScale, BarElement, Title, Tooltip, Legend } from 'chart.js';
import { Bar, Pie } from 'react-chartjs-2';
import Sidebar from '../../components/SideBar/SideBar';
import Header from '../../components/Header/Header';

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
    const [error, setError] = useState('');

    useEffect(() => {
        const fetchUserStats = async () => {
            setLoading(true);
            try {
                const response = await fetch('/dashboard/user-stats'); // Adjust the endpoint if necessary
                if (!response.ok) throw new Error('Error fetching user stats');
                const data = await response.json();
                setUserStats(data);
                setError('');
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchUserStats();
    }, []);

    // Process data for Completed Tasks (Bar Chart)
    const completedTasksData = {
        labels: userStats.map(user => user.userName),
        datasets: [
            {
                label: 'Completed Tasks',
                data: userStats.map(user => user.completedTasks),
                backgroundColor: 'rgba(0, 149, 214, 0.8)',
                borderColor: 'rgba(0, 149, 214, 1)',
                borderWidth: 1,
            },
        ],
    };

    // Process data for Performance Pie Chart
    const performanceData = {
        labels: userStats.map(user => user.userName),
        datasets: [
            {
                label: 'Tasks Completed',
                data: userStats.map(user => user.completedTasks),
                backgroundColor: [
                    'rgba(75, 192, 192, 0.8)',
                    'rgba(153, 102, 255, 0.8)',
                    'rgba(255, 159, 64, 0.8)',
                    'rgba(255, 99, 132, 0.8)',
                    'rgba(54, 162, 235, 0.8)',
                ],
                borderColor: [
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(255, 159, 64, 1)',
                    'rgba(255, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col w-full">
                <Header headerTitle="Dashboard" />

                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">

                        {/* Left column - Completed Tasks */}
                        <div className="lg:col-span-2">
                            <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks</h2>
                                {loading ? (
                                    <p>Loading completed tasks...</p>
                                ) : (
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
                                                            text: 'Tasks',
                                                        },
                                                    },
                                                    x: {
                                                        title: {
                                                            display: true,
                                                            text: 'Users',
                                                        },
                                                    },
                                                },
                                            }}
                                        />
                                    </div>
                                )}
                            </div>

                            {/* Task Completion Table */}
                            <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Completion Table</h2>
                                {loading ? (
                                    <p>Loading task completion data...</p>
                                ) : (
                                    <div className="overflow-x-auto">
                                        <table className="min-w-full divide-y divide-gray-200">
                                            <thead className="bg-gray-50">
                                                <tr>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">User</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tasks To Do</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Completed Tasks</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Tasks</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Percentage Completed</th>
                                                    <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Real Hours</th>
                                                </tr>
                                            </thead>
                                            <tbody className="bg-white divide-y divide-gray-200">
                                                {userStats.map((user, index) => (
                                                    <tr key={index}>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.userName}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.tasksToDo}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.completedTasks}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.totalTasks}</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.percentageCompleted}%</td>
                                                        <td className="px-6 py-4 whitespace-nowrap">{user.totalRealHours}</td>
                                                    </tr>
                                                ))}
                                            </tbody>
                                        </table>
                                    </div>
                                )}
                            </div>
                        </div>

                        {/* Right column - Performance and Unfinished Tasks */}
                        <div className="lg:col-span-1">
                            {/* Performance Pie Chart */}
                            <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Performance</h2>
                                {loading ? (
                                    <p>Loading performance data...</p>
                                ) : (
                                    <div className="h-64 flex justify-center">
                                        <Pie
                                            data={performanceData}
                                            options={{
                                                responsive: true,
                                                maintainAspectRatio: false,
                                            }}
                                        />
                                    </div>
                                )}
                            </div>
                        </div>

                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;