// Header.jsx
import React, { useState } from "react";
import { FilterList, Add, Close } from "@mui/icons-material";

const Header = ({
  headerTitle = "Default Title",
  addButtonLabel = "Add Something",
  AddComponent,
  onAddComplete // callback al terminar el formulario
}) => {
  const [isModalOpen, setModalOpen] = useState(false);

  const handleCloseModal = () => {
    setModalOpen(false);
  };

  const handleAddSuccess = () => {
    // 1) Cerrar el modal
    setModalOpen(false);
    // 2) Notificar al padre (si existe)
    if (onAddComplete) {
      onAddComplete();
    }
  };

  return (
    <div className="flex items-center justify-between bg-white p-4 shadow-md">
      <div>
        <h1 className="text-xl font-bold">{headerTitle}</h1>
      </div>

      <div className="flex space-x-2">
        <button className="flex items-center space-x-1 bg-gray-100 px-3 py-2 rounded-md">
          <FilterList />
          <span>Filter</span>
        </button>

        {/* Botón para abrir modal */}
        <button
          className="flex items-center space-x-1 bg-blue-500 text-white px-4 py-2 rounded-md"
          onClick={() => setModalOpen(true)}
        >
          <Add />
          <span>{addButtonLabel}</span>
        </button>
      </div>

      {/* Modal */}
      {isModalOpen && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96 relative">
            <button
              onClick={handleCloseModal}
              className="absolute top-2 right-2 text-gray-500 hover:text-gray-700"
            >
              <Close />
            </button>

            {/* Título opcional */}
            {/* <h2 className="text-xl font-bold mb-4">{addButtonLabel}</h2> */}

            {/* Renderizamos el componente dinámico */}
            {AddComponent && (
              <AddComponent onAddComplete={handleAddSuccess} />
            )}
          </div>
        </div>
      )}
    </div>
  );
};

export default Header;
