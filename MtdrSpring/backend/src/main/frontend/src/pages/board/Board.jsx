import Sidebar from "../../components/SideBar/SideBar"; 
import Header from "../../components/Header/Header";
import TaskCard from "../../components/TaskCard/TaskCard";

const tasks = [
    { id: 1, title: "Create landing page mockup", description: "Design a modern landing page", tag: "Design", tagColor: "bg-blue-200 text-blue-800", status: "To Do", users: [{ avatar: "https://randomuser.me/api/portraits/women/1.jpg" }], date: "Mar 25" },
    { id: 2, title: "Competitor analysis", description: "Research main competitors", tag: "Research", tagColor: "bg-purple-200 text-purple-800", status: "To Do", users: [{ avatar: "https://randomuser.me/api/portraits/men/2.jpg" }], date: "Mar 28" },
    { id: 3, title: "Setup analytics dashboard", description: "Implement tracking", tag: "Development", tagColor: "bg-green-200 text-green-800", status: "In Progress", users: [{ avatar: "https://randomuser.me/api/portraits/women/3.jpg" }], date: "Mar 30" },
    { id: 4, title: "Social media content plan", description: "Review and approve content", tag: "Marketing", tagColor: "bg-orange-200 text-orange-800", status: "Review", users: [{ avatar: "https://randomuser.me/api/portraits/men/4.jpg" }], date: "Apr 2" },
    { id: 5, title: "Brand guidelines", description: "Update color palette", tag: "Completed", tagColor: "bg-gray-200 text-gray-800", status: "Done", users: [{ avatar: "https://randomuser.me/api/portraits/women/5.jpg" }], date: "Apr 5" },
];

// Agrupamos las tareas en las columnas correspondientes
const columns = [
    { title: "To Do", status: "To Do" },
    { title: "In Progress", status: "In Progress" },
    { title: "Review", status: "Review" },
    { title: "Done", status: "Done" },
];


const Board = () => {
    return (
        <div className="flex h-screen">
            {/* Sidebar fijo */}
            <Sidebar />

            {/* Contenedor Principal */}
            <div className="flex-1 flex flex-col bg-gray-100">
                {/* Header en la parte superior */}
                <Header />

                {/* Contenido principal del board */}
                <div className="p-6">
                    <h1 className="text-2xl font-bold mb-4">Marketing Campaign Q1</h1>
                    <p className="text-gray-500 mb-6">12 tasks in progress</p>

                    {/* Grid para las columnas */}
                    <div className="grid grid-cols-4 gap-6">
                        {columns.map((column) => (
                            <div key={column.status} className="bg-white p-4 rounded-lg shadow-md">
                                {/* Título de la columna */}
                                <div className="flex justify-between items-center mb-4">
                                    <h2 className="text-lg font-semibold">{column.title}</h2>
                                    <span className="bg-gray-200 text-gray-700 px-2 py-1 rounded-full text-sm">
                                        {tasks.filter(task => task.status === column.status).length}
                                    </span>
                                </div>

                                {/* Tarjetas de tareas */}
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