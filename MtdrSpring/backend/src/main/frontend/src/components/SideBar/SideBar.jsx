import { Home, Dashboard, Chat, Group } from "@mui/icons-material"; // Importamos los íconos de MUI
import { Link } from "react-router-dom";

const menuItems = [
    { name: "Home", icon: <Home />, path: "/home" },
    { name: "Board", icon: <Dashboard />, path: "/board" },
    { name: "Chatbot", icon: <Chat />, path: "/chatbot" },
    { name: "Team", icon: <Group />, path: "/team" },
];

const Sidebar = () => {
    return (
        <div className="w-64 h-screen bg-gray-900 text-white p-5">
            <h1 className="text-xl font-bold mb-5">ProjectFlow</h1>
            <nav>
                {menuItems.map((item, index) => (
                    <Link
                        key={index}
                        to={item.path}
                        className="flex items-center space-x-2 p-2 rounded hover:bg-gray-700 transition duration-300"
                    >
                        {item.icon}
                        <span>{item.name}</span>
                    </Link>
                ))}
            </nav>
        </div>
    );
};

export default Sidebar;
