import { useState, useEffect } from "react";
import Sidebar from "../../components/SideBar/SideBar";
import Header from "../../components/Header/Header";
import TaskCard from "../../components/TaskCard/TaskCard";
import { API_TAREAS } from "../../API";

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

    if (loading) return <p className="text-center">Loading tasks...</p>;
    if (error) return <p className="text-center text-red-500">Error: {error}</p>;

    return (
        <div className="flex h-screen">
            <Sidebar />
            <div className="flex-1 flex flex-col bg-gray-100">
                <Header />
                <div className="p-6">
                    <h1 className="text-2xl font-bold mb-4">Task Board</h1>
                    <p className="text-gray-500 mb-6">{tasks.length} tasks in progress</p>
                    <div className="grid grid-cols-4 gap-6">
                        {columns.map((column) => (
                            <div key={column.status} className="bg-white p-4 rounded-lg shadow-md">
                                <div className="flex justify-between items-center mb-4">
                                    <h2 className="text-lg font-semibold">{column.title}</h2>
                                    <span className="bg-gray-200 text-gray-700 px-2 py-1 rounded-full text-sm">
                                        {tasks.filter(task => task.status === column.status).length}
                                    </span>
                                </div>
                                <div className="space-y-4">
                                    {tasks.filter(task => task.status === column.status).map(task => (
                                        <TaskCard key={task.id} task={task} />
                                    ))}
                                </div>
                            </div>
                        ))}
                    </div>
                </div>
            </div>
        </div>
    );
};

export default Board;
