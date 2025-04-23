import React, { useState } from "react";
import { MoreVert } from "@mui/icons-material"; // Icono de ajustes (tres puntos)
import { Menu, MenuItem, IconButton } from "@mui/material";

const TaskCard = ({ task, onDelete, editable = true }) => {
  const [anchorEl, setAnchorEl] = useState(null);
  const open = Boolean(anchorEl);

  const handleMenuClick = (event) => {
    setAnchorEl(event.currentTarget);
  };

  const handleMenuClose = () => {
    setAnchorEl(null);
  };

  const handleDelete = () => {
    handleMenuClose();
    if (onDelete) {
      onDelete(task.id); // Le avisas al Board
    }
  };

  return (
    <div
      className={`bg-white p-4 rounded-lg shadow-md space-y-2 relative ${
        !editable ? "border-gray-200 border" : ""
      }`}
    >
      {/* Icono de menú en la esquina superior derecha - solo visible si es editable */}
      {editable && (
        <div className="absolute top-2 right-2">
          <IconButton size="small" onClick={handleMenuClick}>
            <MoreVert />
          </IconButton>
          <Menu anchorEl={anchorEl} open={open} onClose={handleMenuClose}>
            <MenuItem onClick={handleDelete}>Eliminar tarea</MenuItem>
          </Menu>
        </div>
      )}

      <span
        className={`text-xs font-semibold px-2 py-1 rounded-full bg-opacity-20 ${
          task.tagColor || "bg-gray-200 text-gray-800"
        }`}
      >
        {task.tag || "No Sprint"}
      </span>
      {task.prioridad && (
        <span className={`text-xs font-semibold px-2 py-1 rounded-full ${
          task.prioridad.toUpperCase() === "ALTA"
            ? "bg-red-100 text-red-800"
            : task.prioridad.toUpperCase() === "MEDIA"
            ? "bg-yellow-100 text-yellow-800"
            : "bg-green-100 text-green-800"
        }`}>
          Prioridad: {task.prioridad}
        </span>
      )}
      <h3 className="font-bold text-lg">{task.title || "Untitled Task"}</h3>
      <p className="text-gray-600 text-sm">
        {task.description || "No description"}
      </p>
      <div className="flex items-center space-x-2">
        {task.users &&
          task.users.map((user, index) => (
            <div key={index} className="flex items-center space-x-1">
              <span className="text-sm text-gray-800 font-semibold">{user.name}</span>
            </div>
          ))}
      </div>
      <div className="flex justify-between items-center">
        <p className="text-sm text-gray-400">Fecha de entrega: {task.date || "Sin fecha"}</p>
        {!editable && (
          <span className="text-xs bg-yellow-100 text-yellow-800 px-2 py-1 rounded-full">
            No editable
          </span>
        )}
      </div>

      {/* Mostrar horas reales si la tarea está completada */}
      {task.completed && task.realHours && (
        <div className="mt-2 pt-2 border-t border-gray-100">
          <p className="text-sm text-gray-600">
            <span className="font-semibold">Horas reales:</span>{" "}
            {task.realHours}
          </p>
        </div>
      )}
    </div>
  );
};

export default TaskCard;
