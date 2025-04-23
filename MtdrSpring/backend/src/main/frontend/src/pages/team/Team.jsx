import React, { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import axiosInstance from "../../utils/axiosConfig";

const Team = () => {
    const [teamMembers, setTeamMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        const fetchTeamMembers = async () => {
            setLoading(true);
            try {
                // Fetch user stats from the backend
                const response = await axiosInstance.get("/dashboard/user-stats");
                setTeamMembers(response.data); // Store the fetched data in state
                setError("");
            } catch (err) {
                setError("Failed to fetch team members: " + err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTeamMembers();
    }, []);

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col">
                <Header 
                    headerTitle="Team Overview"
                    showFilter={false}
                />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
                        <h2 className="text-xl font-bold mb-4 border-b pb-2">Team Members</h2>
                        {loading ? (
                            <p>Loading team members...</p>
                        ) : error ? (
                            <p className="text-red-500">{error}</p>
                        ) : (
                            <div className="overflow-x-auto">
                                <table className="min-w-full divide-y divide-gray-200">
                                    <thead className="bg-gray-50">
                                        <tr>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Name</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tasks Assigned</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tasks Completed</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Tasks</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Percentage Completed</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Total Real Hours</th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {teamMembers.map((member, index) => (
                                            <tr key={index}>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.userName}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.tasksAssigned}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.completedTasks}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.totalTasks}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.percentageCompleted}%</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.totalRealHours}</td>
                                            </tr>
                                        ))}
                                    </tbody>
                                </table>
                            </div>
                        )}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Team;