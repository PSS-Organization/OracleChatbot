import { useState } from "react";
import { FilterList, Add, Close } from "@mui/icons-material"; // Nuevos íconos de MUI
import AddTask from "../AddTask/AddTask";

const Header = ({ onTaskAdded }) => {
    const [isModalOpen, setModalOpen] = useState(false);

    return (
        <div className="flex items-center justify-between bg-white p-4 shadow-md">
            {/* Título y subtítulo */}
            <div>
                <h1 className="text-xl font-bold">Proyecto PSS - Oracle</h1>
            </div>

            {/* Botones */}
            <div className="flex space-x-2">
                <button className="flex items-center space-x-1 bg-gray-100 px-3 py-2 rounded-md">
                    <FilterList />
                    <span>Filter</span>
                </button>

                {/* Botón para abrir el modal */}
                <button
                    className="flex items-center space-x-1 bg-blue-500 text-white px-4 py-2 rounded-md"
                    onClick={() => setModalOpen(true)}
                >
                    <Add />
                    <span>Add Task</span>
                </button>
            </div>

            {/* MODAL DE FORMULARIO */}
            {isModalOpen && (
                <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center">
                    <div className="bg-white p-6 rounded-lg shadow-lg w-96">
                        <div className="flex justify-between items-center mb-4">
                            <h2 className="text-xl font-bold">Nueva Tarea</h2>
                            <button onClick={() => setModalOpen(false)} className="text-gray-500 hover:text-gray-700">
                                <Close />
                            </button>
                        </div>

                        {/* Componente de Agregar Tarea */}
                        <AddTask
                            onTaskAdded={() => {
                                setModalOpen(false);
                                if (onTaskAdded) onTaskAdded(); // Refrescar la lista de tareas
                            }}
                        />
                    </div>
                </div>
            )}
        </div>
    );
};

export default Header;
