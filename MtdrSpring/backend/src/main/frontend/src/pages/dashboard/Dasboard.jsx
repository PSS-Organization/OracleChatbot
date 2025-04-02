import React from 'react';
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
    // Dummy data 
    const completedTasksData = {
        labels: ['Team A', 'Team B', 'Team C', 'Team D', 'Team E'],
        datasets: [
            {
                label: 'Completed Tasks',
                data: [12, 19, 15, 8, 22],
                backgroundColor: [
                    'rgba(0, 149, 214, 0.8)',
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(36, 116, 166, 0.8)',
                    'rgba(27, 79, 114, 0.8)',
                    'rgba(40, 116, 166, 0.8)',
                ],
                borderColor: [
                    'rgba(0, 149, 214, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(36, 116, 166, 1)',
                    'rgba(27, 79, 114, 1)',
                    'rgba(40, 116, 166, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    // Dummy data for performance pie chart
    const performanceData = {
        labels: ['Oscar', 'Hugo', 'Diego', 'Valentino', 'Rodrigo'],
        datasets: [
            {
                label: 'Completed Tasks',
                data: [12, 19, 8, 15, 7],
                backgroundColor: [
                    'rgba(0, 99, 132, 0.8)',
                    'rgba(54, 162, 235, 0.8)',
                    'rgba(75, 192, 192, 0.8)',
                    'rgba(153, 102, 255, 0.8)',
                    'rgba(0, 170, 215, 0.8)',
                ],
                borderColor: [
                    'rgba(0, 99, 132, 1)',
                    'rgba(54, 162, 235, 1)',
                    'rgba(75, 192, 192, 1)',
                    'rgba(153, 102, 255, 1)',
                    'rgba(0, 170, 215, 1)',
                ],
                borderWidth: 1,
            },
        ],
    };

    // Dummy data 
    const tableData = [
        { user: 'Oscar', tasksAssigned: 15, tasksCompleted: 12, totalTasks: 27, percentage: '80%' },
        { user: 'Hugo', tasksAssigned: 20, tasksCompleted: 15, totalTasks: 35, percentage: '75%' },
        { user: 'Diego', tasksAssigned: 12, tasksCompleted: 10, totalTasks: 22, percentage: '83%' },
        { user: 'Valentino', tasksAssigned: 18, tasksCompleted: 8, totalTasks: 26, percentage: '44%' },
        { user: 'Rodrigo', tasksAssigned: 14, tasksCompleted: 11, totalTasks: 25, percentage: '79%' },
    ];

    // Dummy data 
    const unfinishedTasks = [
        { id: 1, title: 'Implement login page', assignee: 'Oscar', deadline: '2023-04-15' },
        { id: 2, title: 'Fix navigation bug', assignee: 'Hugo', deadline: '2023-04-12' },
        { id: 3, title: 'Add user profile page', assignee: 'Diego', deadline: '2023-04-20' },
    ];

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
                                                        text: 'Tasks'
                                                    }
                                                },
                                                x: {
                                                    title: {
                                                        display: true,
                                                        text: 'Teams'
                                                    }
                                                }
                                            }
                                        }}
                                    />
                                </div>
                            </div>

                            {/* Task Completion Table */}
                            <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Percentage de Tareas Completadas</h2>
                                <div className="overflow-x-auto">
                                    <table className="min-w-full divide-y divide-gray-200">
                                        <thead className="bg-gray-50">
                                            <tr>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Usuario</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tareas Asignadas</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tareas Completadas</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Tareas</th>
                                                <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Porcentaje</th>
                                            </tr>
                                        </thead>
                                        <tbody className="bg-white divide-y divide-gray-200">
                                            {tableData.map((row, index) => (
                                                <tr key={index}>
                                                    <td className="px-6 py-4 whitespace-nowrap">{row.user}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap">{row.tasksAssigned}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap">{row.tasksCompleted}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap">{row.totalTasks}</td>
                                                    <td className="px-6 py-4 whitespace-nowrap">{row.percentage}</td>
                                                </tr>
                                            ))}
                                        </tbody>
                                    </table>
                                </div>
                            </div>
                        </div>

                        {/* Right column - Performance and Unfinished Tasks */}
                        <div className="lg:col-span-1">
                            {/* Performance Pie Chart */}
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
                                <div className="mt-4 grid grid-cols-2 gap-2">
                                    {performanceData.labels.map((label, index) => (
                                        <div key={index} className="flex items-center">
                                            <div
                                                className="w-3 h-3 rounded-full mr-2"
                                                style={{ backgroundColor: performanceData.datasets[0].backgroundColor[index] }}
                                            ></div>
                                            <span className="text-sm">{label}</span>
                                        </div>
                                    ))}
                                </div>
                            </div>

                            {/* Unfinished Tasks */}
                            <div className="bg-white p-4 rounded-lg shadow-md border-2 border-gray-200">
                                <h2 className="text-xl font-bold mb-4 border-b pb-2">Unfinished Tasks</h2>
                                <div className="space-y-3">
                                    {unfinishedTasks.map(task => (
                                        <div key={task.id} className="p-3 bg-gray-50 rounded-md">
                                            <div className="font-medium">{task.title}</div>
                                            <div className="text-sm text-gray-500">Assignee: {task.assignee}</div>
                                            <div className="text-sm text-gray-500">Deadline: {task.deadline}</div>
                                        </div>
                                    ))}
                                </div>
                            </div>
                        </div>
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Dashboard;
