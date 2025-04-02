import React, { useState, useEffect } from "react";
import { API_TAREAS } from "../../API";
import { API_SPRINTS } from "../../API";
import { getBackendUrl } from "../../utils/getBackendUrl";

const AddTask = ({ onAddComplete }) => {
    const [taskName, setTaskName] = useState("");
    const [description, setDescription] = useState("");
    const [priority, setPriority] = useState("BAJA");
    const [dueDate, setDueDate] = useState("");
    const [estimatedHours, setEstimatedHours] = useState("");
    const [userID, setUserID] = useState("");
    const [sprintID, setSprintID] = useState("");
    const [error, setError] = useState("");
    const [successMessage, setSuccessMessage] = useState("");
    const [loading, setLoading] = useState(false);
    const [currentUserID, setCurrentUserID] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);

    // cargar usuarios y sprints en formulario
    const [usuarios, setUsuarios] = useState([]);
    
    // Get current user info and check if admin
    useEffect(() => {
        const storedUserId = localStorage.getItem("userId");
        if (storedUserId) {
            setCurrentUserID(storedUserId);
            setUserID(storedUserId); // Set the current user as default assignee
            
            // Check if user is admin
            const checkAdminStatus = async () => {
                try {
                    const response = await fetch(`/usuarios/is-admin/${storedUserId}`);
                    if (response.ok) {
                        const data = await response.json();
                        setIsAdmin(data.isAdmin);
                    }
                } catch (err) {
                    console.error("Error checking admin status:", err);
                }
            };
            
            checkAdminStatus();
        }
    }, []);
    
    useEffect(() => {
        const fetchUsuarios = async () => {
          try {
            const backendUrl = await getBackendUrl();
            const response = await fetch(`${backendUrl}/usuarios/all`);
            if (!response.ok) throw new Error("Error fetching users");
            const data = await response.json();
            setUsuarios(data);
          } catch (err) {
            console.error("Error loading users:", err.message);
          }
        };
      
        fetchUsuarios();
      }, []);
    
    const [sprints, setSprints] = useState([]);
    useEffect(() => {
        const fetchSprints = async () => {
          try {
            const response = await fetch(API_SPRINTS);
            if (!response.ok) throw new Error("Error fetching sprints");
            const data = await response.json();
            setSprints(data);
          } catch (err) {
            console.error("Error loading sprints:", err.message);
          }
        };
      
        fetchSprints();
      }, []);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccessMessage("");
        setLoading(true);

        // Assign to current user if not admin
        const assigneeId = isAdmin ? userID : currentUserID;
        
        if (!taskName || !description || !dueDate || !assigneeId) {
            setError("Todos los campos son obligatorios.");
            setLoading(false);
            return;
        }

        const newTask = {
            tareaNombre: taskName,
            descripcion: description,
            fechaEntrega: new Date(dueDate).toISOString(),
            prioridad: priority,
            horasEstimadas: estimatedHours ? parseInt(estimatedHours) : null,
            usuarioID: parseInt(assigneeId),
            sprintID: parseInt(sprintID),
        };

        try {
            const response = await fetch(API_TAREAS, {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newTask),
            });

            if (!response.ok) {
                const data = await response.json();
                throw new Error(data.message || "Failed to create task.");
            }

            const createdTask = await response.json();
            setSuccessMessage("Task created successfully!");
            setLoading(false);

            // Clear the form fields
            setTaskName("");
            setDescription("");
            setPriority("BAJA");
            setDueDate("");
            setEstimatedHours("");
            setUserID(isAdmin ? "" : currentUserID);
            setSprintID("");

            // Notify parent to refresh the task list
            if (onAddComplete) onAddComplete(createdTask);
        } catch (err) {
            console.error("Error creating task:", err.message);
            setError(err.message);
            setLoading(false);
        }
    };

    return (
        <div className="p-4 bg-white rounded shadow-md">
            <h2 className="text-xl font-bold mb-4">Add New Task</h2>

            {error && <p className="text-red-500 mb-3">{error}</p>}
            {successMessage && <p className="text-green-500 mb-3">{successMessage}</p>}

            <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4">
                <input
                    type="text"
                    placeholder="Task Name"
                    value={taskName}
                    onChange={(e) => setTaskName(e.target.value)}
                    className="p-2 border rounded"
                    required
                />

                <textarea
                    placeholder="Description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="p-2 border rounded"
                    required
                />

                <input
                    type="date"
                    placeholder="Due Date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                    className="p-2 border rounded"
                    required
                />

                <input
                    type="number"
                    placeholder="Estimated Hours"
                    value={estimatedHours}
                    onChange={(e) => setEstimatedHours(e.target.value)}
                    className="p-2 border rounded"
                />

                {isAdmin ? (
                    // Admin can assign to anyone
                    <select
                        value={userID}
                        onChange={(e) => setUserID(e.target.value)}
                        className="p-2 border rounded"
                    >
                        <option value="">Select User</option>
                        {usuarios.map((user) => (
                            <option key={user.usuarioID} value={user.usuarioID}>
                                {user.usuarioID} - {user.nombre}
                            </option>
                        ))}
                    </select>
                ) : (
                    // Non-admin can only see and is auto-assigned
                    <div className="p-2 border rounded bg-gray-100">
                        <p className="text-gray-700">
                            {usuarios.find(u => u.usuarioID != null && currentUserID != null && u.usuarioID.toString() === currentUserID.toString())?.nombre || 'Current User'} (Auto-assigned)
                        </p>
                    </div>
                )}

                <select
                    value={sprintID}
                    onChange={(e) => setSprintID(e.target.value)}
                    className="p-2 border rounded"
                    required
                >
                    <option value="">Select Sprint</option>
                    {sprints.map((sprint) => (
                        <option key={sprint.sprintID} value={sprint.sprintID}>
                        {sprint.sprintID} - {sprint.nombreSprint}
                        </option>
                    ))}
                </select>

                <button
                    type="submit"
                    className={`p-2 rounded text-white ${
                        loading ? "bg-gray-500" : "bg-blue-500 hover:bg-blue-600"
                    }`}
                    disabled={loading}
                >
                    {loading ? "Creating Task..." : "Add Task"}
                </button>
            </form>
        </div>
    );
};

export default AddTask;