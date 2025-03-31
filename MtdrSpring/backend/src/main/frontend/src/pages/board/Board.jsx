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
    const [showMyTasksOnly, setShowMyTasksOnly] = useState(false);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);
    const [currentUserId, setCurrentUserId] = useState(null);

    useEffect(() => {
        // Get current user ID from localStorage
        const userId = localStorage.getItem("userId");
        setCurrentUserId(userId);
    }, []);

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
                status: statusMapping[task.estadoID] || "To Do",
                users: [{ avatar: "https://randomuser.me/api/portraits/men/1.jpg" }],
                date: new Date(task.fechaEntrega).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }),
                usuarioID: task.usuarioID // Store the user ID for filtering
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

    // Filter tasks based on the toggle state
    const filterTasks = (tasksToFilter = allTasks) => {
        if (showMyTasksOnly && currentUserId) {
            setTasks(tasksToFilter.filter(task =>
                task.usuarioID && task.usuarioID.toString() === currentUserId.toString()
            ));
        } else {
            setTasks(tasksToFilter);
        }
    };

    // Toggle between showing all tasks and only user's tasks
    const toggleMyTasks = () => {
        const newState = !showMyTasksOnly;
        setShowMyTasksOnly(newState);
        // Apply the filter with the new state
        if (newState && currentUserId) {
            setTasks(allTasks.filter(task =>
                task.usuarioID && task.usuarioID.toString() === currentUserId.toString()
            ));
        } else {
            setTasks(allTasks);
        }
    };

    // Fetch sprints on component mount
    useEffect(() => {
        fetchTasks();
    }, []);

    // Re-filter when userId changes
    useEffect(() => {
        filterTasks();
    }, [currentUserId]);


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



    // PARA MOVER TAREAS 

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

        // Actualizar el estado local
        const updatedTasks = tasks.map((task) =>
            task.id.toString() === draggableId
                ? { ...task, status: destination.droppableId }
                : task
        );
        setTasks(updatedTasks);

        // Also update allTasks
        const updatedAllTasks = allTasks.map((task) =>
            task.id.toString() === draggableId
                ? { ...task, status: destination.droppableId }
                : task
        );
        setAllTasks(updatedAllTasks);

        // Determinar el nuevo estadoID (1, 2 o 3)
        const estadoID =
            Object.entries(statusMapping).find(
                ([_, name]) => name === destination.droppableId
            )?.[0] || 1;

        // 🔁 Llamar al backend para actualizar la tarea
        try {
            const response = await fetch(`${API_TAREAS}/${draggableId}/estado`, {
                method: "PUT",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({ estadoID: parseInt(estadoID) }),
            });

            if (!response.ok) {
                console.error("❌ Error al actualizar estado en el backend");
            }
        } catch (err) {
            console.error("❌ Error en handleDragEnd:", err);
        }
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
                />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
                    <h1 className="text-2xl font-bold mb-4">Task Board</h1>
                    {loading && <p>Loading sprints...</p>}
                    {error && <p className="text-red-500">{error}</p>}
                    <p className="text-gray-500 mb-6">{tasks.length} tasks in progress {showMyTasksOnly ? "(showing only my tasks)" : ""}</p>
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
                                                        >
                                                            {(provided) => (
                                                                <div
                                                                    ref={provided.innerRef}
                                                                    {...provided.draggableProps}
                                                                    {...provided.dragHandleProps}
                                                                >
                                                                    <TaskCard task={task} onDelete={handleDeleteTask} />
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
        </div>
    );
};

export default Board;
