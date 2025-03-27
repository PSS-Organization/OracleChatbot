import React, { useState } from "react";
import { API_TAREAS } from "../../API";

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

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");
        setSuccessMessage("");
        setLoading(true);

        if (!taskName || !description || !dueDate || !userID) {
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
            usuarioID: parseInt(userID),
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
            setUserID("");
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

                <input
                    type="number"
                    placeholder="User ID"
                    value={userID}
                    onChange={(e) => setUserID(e.target.value)}
                    className="p-2 border rounded"
                    required
                />

                <input
                    type="number"
                    placeholder="Sprint ID"
                    value={sprintID}
                    onChange={(e) => setSprintID(e.target.value)}
                    className="p-2 border rounded"
                />

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