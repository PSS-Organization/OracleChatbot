import { Home, Dashboard, Chat, Group, Menu, Close, Task } from "@mui/icons-material";
import { Link } from "react-router-dom";
import { useState, useEffect } from "react";
import axios from "axios";

const Sidebar = () => {
    const [isOpen, setIsOpen] = useState(false);
    const [isAdmin, setIsAdmin] = useState(false);
    const [loading, setLoading] = useState(true);

    const toggleSidebar = () => setIsOpen(!isOpen);

    useEffect(() => {
        const checkAdminStatus = async () => {
            try {
                // Try different possible localStorage keys for userId
                const userId = localStorage.getItem("userId") ||
                    localStorage.getItem("user_id") ||
                    localStorage.getItem("usuarioID") ||
                    localStorage.getItem("usuario_id");

                console.log("Local Storage Keys:", Object.keys(localStorage));
                console.log("Retrieved userId:", userId);

                // For testing purposes, if userId is null, use a default value (remove in production)
                const idToUse = userId || "1"; // Fallback to user ID 1 for testing

                if (idToUse) {
                    console.log("Fetching admin status for user ID:", idToUse);
                    const response = await axios.get(`/usuarios/is-admin/${idToUse}`);
                    console.log("Admin status response:", response.data);
                    setIsAdmin(response.data.isAdmin);
                }
            } catch (error) {
                console.error("Error checking admin status:", error);
            } finally {
                setLoading(false);
            }
        };

        checkAdminStatus();
    }, []);

    // Define menu items based on admin status
    const menuItems = [
        { name: "Home", icon: <Home />, path: "/home" },
        { name: "Board", icon: <Dashboard />, path: "/board" },
        { name: "Chatbot", icon: <Chat />, path: "/chatbot" },
        { name: "Team", icon: <Group />, path: "/team" },
        { name: "Sprint", icon: <Task />, path: "/sprint" },
    ];

    // Add Dashboard only for admin users
    if (isAdmin) {
        menuItems.push({ name: "Dashboard", icon: <Dashboard />, path: "/dashboard" });
    }

    return (
        <>
            {/* Botón móvil para mostrar/ocultar sidebar */}
            <button
                className="lg:hidden fixed top-4 left-4 z-20 p-2 rounded-md bg-gray-800 text-white"
                onClick={toggleSidebar}
            >
                {isOpen ? <Close /> : <Menu />}
            </button>

            {/* Sidebar */}
            <div className={`
                fixed lg:sticky top-0
                w-64 h-screen scrollbar-hide
                bg-gray-900 text-white p-5
                transform transition-transform duration-300 ease-in-out
                lg:transform-none
                ${isOpen ? 'translate-x-0' : '-translate-x-full lg:translate-x-0'}
                z-10
            `}>
                <h1 className="text-xl font-bold mb-5">ProjectFlow</h1>
                <nav>
                    {!loading && menuItems.map((item, index) => (
                        <Link
                            key={index}
                            to={item.path}
                            className="flex items-center space-x-2 p-2 rounded hover:bg-gray-700 transition duration-300"
                            onClick={() => setIsOpen(false)}
                        >
                            {item.icon}
                            <span>{item.name}</span>
                        </Link>
                    ))}
                </nav>
            </div>
        </>
    );
};

export default Sidebar;
