import React, { useState } from "react";
import { API_TAREAS } from "../../API";



const AddTask = ({ onTaskAdded }) => {
    const [taskName, setTaskName] = useState("");
    const [description, setDescription] = useState("");
    const [priority, setPriority] = useState("BAJA"); // Valor por defecto
    const [dueDate, setDueDate] = useState("");
    const [estimatedHours, setEstimatedHours] = useState("");
    const [userID, setUserID] = useState("");
    const [sprintID, setSprintID] = useState(""); // Estado para Sprint
    const [error, setError] = useState("");
    const [loading, setLoading] = useState(false);

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError("");

        if (!taskName || !description || !dueDate || !userID) {
            setError("Todos los campos son obligatorios.");
            return;
        }

        const newTask = {
            tareaNombre: taskName,
            descripcion: description,
            fechaEntrega: new Date(dueDate).toISOString(),
            prioridad: priority,
            horasEstimadas: estimatedHours ? parseInt(estimatedHours) : null,
            horasReales: null, // Agregar explícitamente
            usuarioID: parseInt(userID),
            sprintID: sprintID ? parseInt(sprintID) : null, 
            estadoID: 1, // Estado por defecto (ejemplo: "To Do")
            completado: 0,
        };

        try {
            setLoading(true);
            console.log("📤 Enviando tarea al backend:", newTask);

            const response = await fetch(API_TAREAS, {  // ✅ Ahora usa API_TAREAS directamente
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify(newTask),
            });

            console.log("🔗 Endpoint usado:", API_TAREAS);


            const data = await response.json();

            console.log("🔴 Respuesta del servidor:", response.status, data);
            if (!response.ok) throw new Error(data.message || "Error al agregar tarea");

            setLoading(false);
            onTaskAdded(); // Refrescar lista de tareas
            setTaskName("");
            setDescription("");
            setDueDate("");
            setEstimatedHours("");
            setUserID("");
            setSprintID(""); // Resetear Sprint ID
        } catch (err) {
            setError(err.message);
            setLoading(false);
        }
    };

    return (
        <div className="p-6 bg-white rounded-lg shadow-md">
            <h2 className="text-xl font-bold mb-4">Agregar Nueva Tarea</h2>

            {error && <p className="text-red-500 mb-4">{error}</p>}

            <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4">
                <input
                    type="text"
                    placeholder="Nombre de la tarea"
                    value={taskName}
                    onChange={(e) => setTaskName(e.target.value)}
                    className="p-2 border rounded"
                />

                <textarea
                    placeholder="Descripción"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    className="p-2 border rounded"
                />

                <select
                    value={priority}
                    onChange={(e) => setPriority(e.target.value)}
                    className="p-2 border rounded"
                >
                    <option value="BAJA">Baja</option>
                    <option value="MEDIA">Media</option>
                    <option value="ALTA">Alta</option>
                </select>

                <input
                    type="date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                    className="p-2 border rounded"
                />

                <input
                    type="number"
                    placeholder="Horas estimadas (opcional)"
                    value={estimatedHours}
                    onChange={(e) => setEstimatedHours(e.target.value)}
                    className="p-2 border rounded"
                />

                <input
                    type="number"
                    placeholder="ID del usuario asignado"
                    value={userID}
                    onChange={(e) => setUserID(e.target.value)}
                    className="p-2 border rounded"
                />

                <input
                    type="number"
                    placeholder="ID del Sprint"
                    value={sprintID}
                    onChange={(e) => setSprintID(e.target.value)}
                    className="p-2 border rounded"
                />

                <button
                    type="submit"
                    className={`p-2 rounded text-white ${loading ? "bg-gray-500" : "bg-blue-500 hover:bg-blue-600"}`}
                    disabled={loading} // 🔥 No permite el envío mientras carga
                >
                    {loading ? "Agregando..." : "Agregar Tarea"}
                </button>
            </form>
        </div>
    );
};

export default AddTask;
