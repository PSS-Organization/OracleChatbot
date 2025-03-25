import { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
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
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    useEffect(() => {
        const fetchTasks = async () => {
            try {
                const response = await fetch(API_TAREAS);
                if (!response.ok) throw new Error("Error fetching tasks");
                const data = await response.json();
                
                const formattedTasks = data.map(task => ({
                    id: task.tareaID,
                    title: task.tareaNombre,
                    description: task.descripcion,
                    tag: `Sprint ${task.sprintID}`,
                    tagColor: sprintColors[task.sprintID] || "bg-gray-200 text-gray-800", // Color por defecto
                    status: statusMapping[task.estadoID] || "To Do", // Mapeo de estado
                    users: [{ avatar: "https://randomuser.me/api/portraits/men/1.jpg" }], // Simulación de usuario
                    date: new Date(task.fechaEntrega).toLocaleDateString('en-US', { month: 'short', day: 'numeric' }) // Formato fecha
                }));
                
                setTasks(formattedTasks);
            } catch (err) {
                setError(err.message);
            } finally {
                setLoading(false);
            }
        };
        
        fetchTasks();
    }, []);

    const columns = [
        { title: "To Do", status: "To Do" },
        { title: "In Progress", status: "In Progress" },
        { title: "Review", status: "Review" },
        { title: "Done", status: "Done" },
    ];

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
      

    

    if (loading) return <p className="text-center">Loading tasks...</p>;
    if (error) return <p className="text-center text-red-500">Error: {error}</p>;

    return (
        <div className="flex min-h-screen bg-gray-100">
            <Sidebar />
            <div className="flex-1 flex flex-col w-full">
                <Header />
                <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto"> 
                    <h1 className="text-2xl font-bold mb-4">Task Board</h1>
                    <p className="text-gray-500 mb-6">{tasks.length} tasks in progress</p>
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
                                                <TaskCard task={task} />
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
