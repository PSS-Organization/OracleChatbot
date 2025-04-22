import React, { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";

const Team = () => {
    const [teamMembers, setTeamMembers] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");
    const [isAdmin, setIsAdmin] = useState(false);

    useEffect(() => {
        const fetchTeamMembers = async () => {
            setLoading(true);
            try {
                const userId = localStorage.getItem("userId"); // Get the logged-in user's ID
                if (!userId) throw new Error("User not logged in");

                // Check if the user is an admin
                const adminResponse = await fetch(`/usuarios/is-admin/${userId}`);
                if (!adminResponse.ok) throw new Error("Error checking admin status");
                const adminData = await adminResponse.json();
                setIsAdmin(adminData.isAdmin);

                // Fetch all team members
                const response = await fetch("/usuarios/all");
                if (!response.ok) throw new Error("Error fetching team members");
                const data = await response.json();
                setTeamMembers(data);
                setError("");
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };

        fetchTeamMembers();
    }, []);

    const handleRoleChange = async (userId, newRole) => {
        try {
            const updatedUser = { isAdmin: newRole === "Admin" };
            const response = await fetch(`/usuarios/${userId}`, {
                method: "PUT",
                headers: {
                    "Content-Type": "application/json",
                },
                body: JSON.stringify(updatedUser),
            });

            if (!response.ok) throw new Error("Error updating user role");

            // Update the role locally
            setTeamMembers(prevMembers =>
                prevMembers.map(member =>
                    member.usuarioID === userId ? { ...member, isAdmin: newRole === "Admin" } : member
                )
            );
        } catch (err) {
            alert("Failed to update role: " + err.message);
        }
    };

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
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Email</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Role</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tasks Assigned</th>
                                            <th className="px-6 py-3 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">Tasks Completed</th>
                                        </tr>
                                    </thead>
                                    <tbody className="bg-white divide-y divide-gray-200">
                                        {teamMembers.map((member, index) => (
                                            <tr key={index}>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.nombre}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.correo}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">
                                                    {isAdmin ? (
                                                        <select
                                                            value={member.isAdmin ? "Admin" : "Developer"}
                                                            onChange={e => handleRoleChange(member.usuarioID, e.target.value)}
                                                            className="border border-gray-300 rounded p-1"
                                                        >
                                                            <option value="Developer">Developer</option>
                                                            <option value="Admin">Admin</option>
                                                        </select>
                                                    ) : (
                                                        member.isAdmin ? "Admin" : "Developer"
                                                    )}
                                                </td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.tasksAssigned || 0}</td>
                                                <td className="px-6 py-4 whitespace-nowrap">{member.tasksCompleted || 0}</td>
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