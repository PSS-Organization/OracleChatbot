import { useState, useEffect } from "react";
import Header from "../../components/Header/Header";
import Sidebar from "../../components/SideBar/SideBar";
import AddTask from "../../components/AddTask/AddTask";
import TaskCard from "../../components/TaskCard/TaskCard";
import { API_TAREAS } from "../../API";
import {
    DragDropContext,
    Droppable,
    Draggable,
} from "react-beautiful-dnd";

const sprintColors = {
    1: "bg-blue-200 text-blue-800",
    2: "bg-purple-200 text-purple-800",
    3: "bg-green-200 text-green-800",
    4: "bg-orange-200 text-orange-800",
};

const statusMapping = {
    1: "To Do",
    2: "In Progress",
    3: "Review",
    4: "Done",
};


const Board = () => {
    const [tasks, setTasks] = useState([]);
    const [allTasks, setAllTasks] = useState([]);
    const [showMyTasksOnly, setShowMyTasksOnly] = useState(true);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUserId, setCurrentUserId] = useState(null);
    const [isAdmin, setIsAdmin] = useState(false);
    const [filters, setFilters] = useState({ sprints: [], users: [] });
    const [completionModal, setCompletionModal] = useState({
        visible: false,
        taskId: null,
        taskName: "",
        realHours: "",
        comment: "",
        isSubmitting: false
    });

    useEffect(() => {
        // Get current user ID from localStorage
        const userId = localStorage.getItem("userId");
        setCurrentUserId(userId);
        
        // Check if user is admin
        if (userId) {
            checkAdminStatus(userId);
        }
    }, []);

    const checkAdminStatus = async (userId) => {
        try {
            const response = await fetch(`/usuarios/is-admin/${userId}`);
            if (response.ok) {
                const data = await response.json();
                setIsAdmin(data.isAdmin);
            }
        } catch (err) {
            console.error("❌ Error checking admin status:", err);
        }
    };
    useEffect(() => {
        if (currentUserId) {
            fetchTasks();
        }
    }, [currentUserId]);


    const fetchTasks = async () => {
        setLoading(true);
        try {
            const response = await fetch(API_TAREAS);
            if (!response.ok) throw new Error("Error fetching tasks");
            const data = await response.json();

            const formattedTasks = data.map(task => ({
                id: task.tareaID,
                title: task.tareaNombre,
                description: task.descripcion,
                tag: `Sprint ${task.sprintID}`,
                tagColor: sprintColors[task.sprintID] || "bg-gray-200 text-gray-800",
                status: statusMapping[task.estadoID] || "In Progress",
                users: [{ avatar: "https://randomuser.me/api/portraits/men/1.jpg" }],
                date: new Date(task.fechaEntrega).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
                usuarioID: task.usuarioID, // Store the user ID for filtering
                sprintID: task.sprintID, // Store the sprint ID for filtering
                estadoID: task.estadoID, // Store the status ID for status changes
                completed: task.completado || false, // Store completion status
                realHours: task.horasReales || null // Store real hours
            }));

            setAllTasks(formattedTasks);
            filterTasks(formattedTasks);
            setError("");
        } catch (err) {
            setError(err.message);
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        if (currentUserId) {
            filterTasks();
        }
    }, [showMyTasksOnly, filters, currentUserId]);

    // Filter tasks based on the toggle state and filters
    const filterTasks = (tasksToFilter = allTasks) => {
        let filteredTasks = [...tasksToFilter];
        
        // Filter by user ownership if showMyTasksOnly is true
        if (showMyTasksOnly && currentUserId) {
            filteredTasks = filteredTasks.filter(task =>
                task.usuarioID != null && task.usuarioID.toString() === currentUserId.toString()
            );
        }
        
        // Apply sprint filters if any are selected
        if (filters.sprints && filters.sprints.length > 0) {
            filteredTasks = filteredTasks.filter(task => 
                task.sprintID != null && filters.sprints.includes(task.sprintID)
            );
        }
        
        // Apply user filters if any are selected
        if (filters.users && filters.users.length > 0) {
            filteredTasks = filteredTasks.filter(task => 
                task.usuarioID != null && filters.users.includes(task.usuarioID)
            );
        }
        
        setTasks(filteredTasks);
    };

    // Toggle between showing all tasks and only user's tasks
    const toggleMyTasks = () => {
        setShowMyTasksOnly(prev => {
            const newState = !prev;
            filterTasks(allTasks); // reapply filters
            return newState;
        });
    };

    // Handle filter changes from Header component
    const handleFilterChange = (newFilters) => {
        setFilters(newFilters);
        filterTasks(allTasks); // Re-filter with the current tasks
    };

    // Fetch sprints on component mount
    useEffect(() => {
        fetchTasks();
    }, []);

    // Re-filter when userId changes or filters change
    useEffect(() => {
        filterTasks();
    }, [currentUserId, filters, showMyTasksOnly]);


    const columns = [
        { title: "To Do", status: "To Do" },
        { title: "In Progress", status: "In Progress" },
        { title: "Review", status: "Review" },
        { title: "Done", status: "Done" },
    ];

    // PARA ELIMINAR TAREAS
    const handleDeleteTask = async (taskId) => {
        const confirmDelete = window.confirm("¿Estás seguro de eliminar esta tarea?");
        if (!confirmDelete) return;

        try {
            const response = await fetch(`${API_TAREAS}/${taskId}`, {
                method: "DELETE",
            });

            if (!response.ok) throw new Error("Error al eliminar la tarea");

            // Actualizar estado local (opcional si no quieres volver a llamar fetchTasks)
            const updatedAllTasks = allTasks.filter(task => task.id !== taskId);
            setAllTasks(updatedAllTasks);
            filterTasks(updatedAllTasks);
        } catch (error) {
            console.error("❌ Error al eliminar tarea:", error);
        }
    };

    // Handle drag end and task completion
    const handleDragEnd = async (result) => {
        const { destination, source, draggableId } = result;

        // Si se soltó fuera de un droppable válido
        if (!destination) return;

        // Si no se movió a otra columna, no hacemos nada
        if (
            destination.droppableId === source.droppableId &&
            destination.index === source.index
        ) {
            return;
        }

        // Encontrar la tarea movida
        const movedTask = tasks.find((task) => task.id.toString() === draggableId);

        if (!movedTask) return;
        
        // Check if user has permission to move this task
        const taskUserId = movedTask.usuarioID != null ? movedTask.usuarioID.toString() : null;
        const currentId = currentUserId != null ? currentUserId.toString() : null;
        
        if (!isAdmin && taskUserId !== currentId) {
            alert("No tienes permiso para modificar esta tarea porque no eres el propietario.");
            return;
        }

        // If moving to "Done", show completion modal
        if (destination.droppableId === "Done" && !movedTask.completed) {
            setCompletionModal({
                visible: true,
                taskId: movedTask.id,
                taskName: movedTask.title,
                realHours: "",
                comment: "",
                isSubmitting: false,
                sourceDroppableId: source.droppableId,
                destinationDroppableId: destination.droppableId,
                sourceIndex: source.index,
                destinationIndex: destination.index
            });
            return; // Don't update yet until form is submitted
        }

        // For other columns, proceed with the update
        await updateTaskStatus(movedTask.id, destination.droppableId);
    };

    // Update task status
    const updateTaskStatus = async (taskId, newStatus) => {
        // Determine the new estadoID (1, 2, 3, or 4)
        const estadoID = Object.entries(statusMapping).find(
            ([_, name]) => name === newStatus
        )?.[0] || 1;
        
        // Update local state
        const updatedTasks = tasks.map((task) =>
            task.id.toString() === taskId.toString()
                ? { ...task, status: newStatus, estadoID: parseInt(estadoID) }
                : task
        );
        setTasks(updatedTasks);

        // Also update allTasks
        const updatedAllTasks = allTasks.map((task) =>
            task.id.toString() === taskId.toString()
                ? { ...task, status: newStatus, estadoID: parseInt(estadoID) }
                : task
        );
        setAllTasks(updatedAllTasks);

        // Call backend to update task status
        try {
            const response = await fetch(`${API_TAREAS}/${taskId}/estado`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ estadoID: parseInt(estadoID) }),
            });

            if (!response.ok) {
                console.error("❌ Error al actualizar estado en el backend");
            }
        } catch (err) {
            console.error("❌ Error en updateTaskStatus:", err);
        }
    };

    // Handle task completion form submission
    const handleCompletionSubmit = async (e) => {
        e.preventDefault();
        
        if (!completionModal.realHours) {
            alert("Por favor ingresa las horas reales para completar la tarea.");
            return;
        }
        
        setCompletionModal(prev => ({ ...prev, isSubmitting: true }));
        
        try {
            // Update task completion status and real hours
            const response = await fetch(`${API_TAREAS}/${completionModal.taskId}/completar`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    horasReales: parseInt(completionModal.realHours),
                    comentario: completionModal.comment,
                    completado: 1
                }),
            });

            if (!response.ok) {
                throw new Error("Error al marcar la tarea como completada");
            }
            
            // Update local state after successful API call
            const updatedAllTasks = allTasks.map(task => 
                task.id === completionModal.taskId
                    ? { 
                        ...task, 
                        completed: true,
                        realHours: parseFloat(completionModal.realHours),
                        status: "Done",
                        estadoID: 4
                    }
                    : task
            );
            
            setAllTasks(updatedAllTasks);
            filterTasks(updatedAllTasks);
            
            // Also update task status
            await updateTaskStatus(completionModal.taskId, "Done");
            
            // Close the modal
            setCompletionModal({
                visible: false,
                taskId: null,
                taskName: "",
                realHours: "",
                comment: "",
                isSubmitting: false
            });
            
        } catch (error) {
            console.error("❌ Error al completar la tarea:", error);
            alert("Error al completar la tarea: " + error.message);
        } finally {
            setCompletionModal(prev => ({ ...prev, isSubmitting: false }));
        }
    };

    // Handle cancellation of task completion
    const handleCompletionCancel = () => {
        setCompletionModal({
            visible: false,
            taskId: null,
            taskName: "",
            realHours: "",
            comment: "",
            isSubmitting: false
        });
    };

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col w-full">
                <Header
                    headerTitle="Sprint Manager"
                    addButtonLabel="Add Task"
                    AddComponent={AddTask}
                    onAddComplete={fetchTasks}
                    showMyTasksOnly={showMyTasksOnly}
                    onToggleMyTasks={toggleMyTasks}
                    onFilterChange={handleFilterChange}
                />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    <h1 className="text-2xl font-bold mb-4">Task Board</h1>
                    {loading && <p>Loading sprints...</p>}
                    {error && <p className="text-red-500">{error}</p>}
                    <p className="text-gray-500 mb-6">
                        {tasks.length} tasks in progress 
                        {showMyTasksOnly ? " (showing only my tasks)" : ""}
                        {filters.sprints.length > 0 ? ` (filtered by ${filters.sprints.length} sprint${filters.sprints.length > 1 ? 's' : ''})` : ""}
                        {filters.users.length > 0 ? ` (filtered by ${filters.users.length} user${filters.users.length > 1 ? 's' : ''})` : ""}
                    </p>
                    <DragDropContext onDragEnd={handleDragEnd}>
                        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-4 lg:gap-6">
                            {columns.map((column) => (
                                <Droppable droppableId={column.status} key={column.status}>
                                    {(provided) => (
                                        <div
                                            ref={provided.innerRef}
                                            {...provided.droppableProps}
                                            className="bg-white p-4 rounded-lg shadow-md"
                                        >
                                            <div className="flex justify-between items-center mb-4">
                                                <h2 className="text-lg font-semibold">{column.title}</h2>
                                                <span className="bg-gray-200 text-gray-700 px-2 py-1 rounded-full text-sm">
                                                    {tasks.filter((task) => task.status === column.status).length}
                                                </span>
                                            </div>
                                            <div className="space-y-4">
                                                {tasks
                                                    .filter((task) => task.status === column.status)
                                                    .map((task, index) => (
                                                        <Draggable
                                                            key={task.id}
                                                            draggableId={task.id.toString()}
                                                            index={index}
                                                            isDragDisabled={!isAdmin && (task.usuarioID == null || task.usuarioID.toString() !== currentUserId?.toString())}
                                                        >
                                                            {(provided) => (
                                                                <div
                                                                    ref={provided.innerRef}
                                                                    {...provided.draggableProps}
                                                                    {...provided.dragHandleProps}
                                                                    className={!isAdmin && (task.usuarioID == null || task.usuarioID.toString() !== currentUserId?.toString()) ? "opacity-70" : ""}
                                                                >
                                                                    <TaskCard 
                                                                        task={task} 
                                                                        onDelete={handleDeleteTask}
                                                                        editable={isAdmin || (task.usuarioID != null && task.usuarioID.toString() === currentUserId?.toString())}
                                                                    />
                                                                </div>
                                                            )}
                                                        </Draggable>
                                                    ))}
                                                {provided.placeholder}
                                            </div>
                                        </div>
                                    )}
                                </Droppable>
                            ))}
                        </div>
                    </DragDropContext>
                </div>
            </div>

            {/* Task Completion Modal */}
            {completionModal.visible && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-96 relative">
                        <h2 className="text-xl font-bold mb-4">Completar Tarea</h2>
                        <p className="mb-4">Estás a punto de marcar como completada la tarea: <strong>{completionModal.taskName}</strong></p>
                        
                        <form onSubmit={handleCompletionSubmit}>
                            <div className="mb-4">
                                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="realHours">
                                    Horas Reales <span className="text-red-500">*</span>
                                </label>
                                <input
                                    id="realHours"
                                    type="number"
                                    step="0.5"
                                    min="0"
                                    required
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    value={completionModal.realHours}
                                    onChange={(e) => setCompletionModal(prev => ({ ...prev, realHours: e.target.value }))}
                                    placeholder="Ingresa las horas reales que tomó la tarea"
                                />
                            </div>
                            
                            <div className="mb-6">
                                <label className="block text-gray-700 text-sm font-bold mb-2" htmlFor="comment">
                                    Comentario (opcional)
                                </label>
                                <textarea
                                    id="comment"
                                    className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                                    value={completionModal.comment}
                                    onChange={(e) => setCompletionModal(prev => ({ ...prev, comment: e.target.value }))}
                                    placeholder="Comentarios adicionales (opcional)"
                                    rows="3"
                                />
                            </div>
                            
                            <div className="flex justify-between">
                                <button
                                    type="button"
                                    onClick={handleCompletionCancel}
                                    className="bg-gray-300 hover:bg-gray-400 text-black font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                                    disabled={completionModal.isSubmitting}
                                >
                                    Cancelar
                                </button>
                                <button
                                    type="submit"
                                    className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                                    disabled={completionModal.isSubmitting}
                                >
                                    {completionModal.isSubmitting ? "Procesando..." : "Completar Tarea"}
                                </button>
                            </div>
                        </form>
                    </div>
                </div>
            )}
        </div>
    );
};

export default Board;
