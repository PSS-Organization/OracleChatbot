import React, { useState } from "react";
import { MoreVert } from "@mui/icons-material"; // Icono de ajustes (tres puntos)
import { Menu, MenuItem, IconButton } from "@mui/material";

const TaskCard = ({ task, onDelete }) => {
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
    <div className="bg-white p-4 rounded-lg shadow-md space-y-2 relative">
      {/* Icono de menú en la esquina superior derecha */}
      <div className="absolute top-2 right-2">
        <IconButton size="small" onClick={handleMenuClick}>
          <MoreVert />
        </IconButton>
        <Menu
          anchorEl={anchorEl}
          open={open}
          onClose={handleMenuClose}
        >
          <MenuItem onClick={handleDelete}>Eliminar tarea</MenuItem>
        </Menu>
      </div>

      <span className={`text-xs font-semibold px-2 py-1 rounded-full bg-opacity-20 ${task.tagColor}`}>
        {task.tag}
      </span>
      <h3 className="font-bold text-lg">{task.title}</h3>
      <p className="text-gray-600 text-sm">{task.description}</p>
      <div className="flex items-center space-x-2">
        {task.users.map((user, index) => (
          <img key={index} src={user.avatar} alt="User" className="w-6 h-6 rounded-full" />
        ))}
      </div>
      <p className="text-sm text-gray-400">{task.date}</p>
    </div>
  );
};

export default TaskCard;
