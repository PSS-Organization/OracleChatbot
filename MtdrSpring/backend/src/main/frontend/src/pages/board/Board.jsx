import { useState, useEffect, useCallback } from "react";
import Header from "../../components/Header/Header";
import Sidebar from "../../components/SideBar/SideBar";
import AddTask from "../../components/AddTask/AddTask";
import TaskCard from "../../components/TaskCard/TaskCard";
import { API_TAREAS, getTareas, updateTarea, deleteTarea } from "../../API";
import axiosInstance from "../../utils/axiosConfig";
import { DragDropContext, Droppable, Draggable } from "react-beautiful-dnd";

const colorPool = [
  "bg-blue-200 text-blue-800",
  "bg-purple-200 text-purple-800",
  "bg-green-200 text-green-800",
  "bg-orange-200 text-orange-800",
  "bg-red-200 text-red-800",
  "bg-pink-200 text-pink-800",
  "bg-yellow-200 text-yellow-800",
  "bg-teal-200 text-teal-800",
  "bg-indigo-200 text-indigo-800",
  "bg-rose-200 text-rose-800",
];

const getSprintColor = (sprintID) => {
  return colorPool[sprintID % colorPool.length];
};

const statusMapping = {
  1: "To Do",
  2: "In Progress",
  3: "Done",
};

const Board = () => {
  const [tasks, setTasks] = useState([]);
  const [allTasks, setAllTasks] = useState([]);
  const [showMyTasksOnly, setShowMyTasksOnly] = useState(true);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState(null);
  const [currentUserId, setCurrentUserId] = useState(null);
  const [isAdmin, setIsAdmin] = useState(false);
  const [filters, setFilters] = useState({ sprints: [], users: [] });
  const [usuarios, setUsuarios] = useState([]);
  const [completionModal, setCompletionModal] = useState({
    visible: false,
    taskId: null,
    taskName: "",
    realHours: "",
    comment: "",
    isSubmitting: false,
  });

  useEffect(() => {
    // Get current user ID from localStorage
    const userId = localStorage.getItem("userId");
    if (userId) {
      setCurrentUserId(userId);
      checkAdminStatus(userId);
    }

    // Iniciar carga de datos
    loadInitialData();
  }, []);

  const loadInitialData = async () => {
    try {
      await fetchUsuarios();
      await fetchTasks();
    } catch (error) {
      console.error("Error loading initial data:", error);
      setError("Error cargando datos iniciales");
    }
  };

  const checkAdminStatus = async (userId) => {
    try {
      const response = await axiosInstance.get(`/usuarios/is-admin/${userId}`);
      if (response.data) {
        setIsAdmin(response.data.isAdmin);
      }
    } catch (err) {
      console.error("❌ Error checking admin status:", err);
    }
  };

  const fetchUsuarios = async () => {
    try {
      const response = await axiosInstance.get("/usuarios/all");
      setUsuarios(response.data);
      return response.data;
    } catch (err) {
      console.error("Error fetching usuarios:", err);
      return [];
    }
  };

  const fetchTasks = async () => {
    setLoading(true);
    try {
      // Obtener usuarios si aún no están cargados
      let usersList = usuarios;
      if (usuarios.length === 0) {
        usersList = await fetchUsuarios();
      }

      const response = await getTareas();
      const formattedTasks = response.map((task) => {
        const assignedUser = usersList.find(
          (u) => u.usuarioID === task.usuarioID
        );
        return {
          id: task.tareaID,
          title: task.tareaNombre,
          description: task.descripcion,
          tag: `Sprint ${task.sprintID}`,
          tagColor: getSprintColor(task.sprintID),
          status: statusMapping[task.estadoID] || "Done",
          users: [
            {
              avatar: "https://randomuser.me/api/portraits/men/1.jpg",
              name: assignedUser ? assignedUser.nombre : "Sin asignar",
            },
          ],
          date: new Date(task.fechaEntrega).toLocaleDateString("en-US", {
            month: "short",
            day: "numeric",
          }),
          usuarioID: task.usuarioID,
          sprintID: task.sprintID,
          estadoID: task.estadoID,
          completed: task.completado || false,
          realHours: task.horasReales || null,
          prioridad: task.prioridad,
        };
      });

      setAllTasks(formattedTasks);
      applyFilters(formattedTasks);
      setError(null);
    } catch (err) {
      setError(err.message);
      console.error("Error fetching tasks:", err);
    } finally {
      setLoading(false);
    }
  };

  // Función para aplicar filtros que no depende de los estados
  const applyFilters = useCallback(
    (tasksToFilter) => {
      let filteredTasks = [...tasksToFilter];

      // Aplicar filtros de sprint si hay seleccionados
      if (filters.sprints && filters.sprints.length > 0) {
        filteredTasks = filteredTasks.filter(
          (task) => task.sprintID && filters.sprints.includes(task.sprintID)
        );
      }

      // Aplicar filtros de usuario si hay seleccionados
      if (filters.users && filters.users.length > 0) {
        filteredTasks = filteredTasks.filter(
          (task) => task.usuarioID && filters.users.includes(task.usuarioID)
        );
      }

      // Filtrar por tareas del usuario actual si está activada la opción
      if (showMyTasksOnly && currentUserId) {
        filteredTasks = filteredTasks.filter(
          (task) => task.usuarioID?.toString() === currentUserId?.toString()
        );
      }

      setTasks(filteredTasks);
    },
    [filters, showMyTasksOnly, currentUserId]
  );

  // Efecto para aplicar filtros cuando cambian las condiciones
  useEffect(() => {
    if (allTasks.length > 0) {
      applyFilters(allTasks);
    }
  }, [filters, showMyTasksOnly, currentUserId, applyFilters, allTasks]);

  // Toggle entre mostrar todas las tareas o solo las del usuario actual
  const toggleMyTasks = () => {
    setShowMyTasksOnly(!showMyTasksOnly);
  };

  // Manejador para los cambios de filtros desde el componente Header
  const handleFilterChange = (newFilters) => {
    setFilters(newFilters);
  };

  const columns = [
    { title: "To Do", status: "To Do" },
    { title: "In Progress", status: "In Progress" },
    { title: "Done", status: "Done" },
  ];

  // PARA ELIMINAR TAREAS
  const handleDeleteTask = async (taskId) => {
    const confirmDelete = window.confirm(
      "¿Estás seguro de eliminar esta tarea?"
    );
    if (!confirmDelete) return;

    try {
      await deleteTarea(taskId);
      const updatedAllTasks = allTasks.filter((task) => task.id !== taskId);
      setAllTasks(updatedAllTasks);
      applyFilters(updatedAllTasks);
    } catch (error) {
      console.error("❌ Error al eliminar tarea:", error);
    }
  };

  // Handle drag end and task completion
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

    // Prevent dragging completed tasks out of "Done"
    if (movedTask.completed && destination.droppableId !== "Done") {
      alert("Esta tarea ya está completada y no puede moverse.");
      return;
    }

    // Check if user has permission to move this task
    const taskUserId =
      movedTask.usuarioID != null ? movedTask.usuarioID.toString() : null;
    const currentId = currentUserId != null ? currentUserId.toString() : null;

    if (!isAdmin && taskUserId !== currentId) {
      alert(
        "No tienes permiso para modificar esta tarea porque no eres el propietario."
      );
      return;
    }

    // If moving to "Done", show completion modal
    if (destination.droppableId === "Done" && !movedTask.completed) {
      setCompletionModal({
        visible: true,
        taskId: movedTask.id,
        taskName: movedTask.title,
        realHours: "",
        comment: "",
        isSubmitting: false,
        sourceDroppableId: source.droppableId,
        destinationDroppableId: destination.droppableId,
        sourceIndex: source.index,
        destinationIndex: destination.index,
      });
      return; // Don't update yet until form is submitted
    }

    // For other columns, proceed with the update
    await updateTaskStatus(movedTask.id, destination.droppableId);
  };

  // Update task status
  const updateTaskStatus = async (taskId, newStatus) => {
    // Determine the new estadoID (1, 2, 3, or 4)
    const estadoID = Object.entries(statusMapping).find(
      ([_, name]) => name === newStatus
    )?.[0];

    // Update local state
    const updatedTasks = tasks.map((task) =>
      task.id.toString() === taskId.toString()
        ? { ...task, status: newStatus, estadoID: parseInt(estadoID) }
        : task
    );
    setTasks(updatedTasks);

    // Also update allTasks
    const updatedAllTasks = allTasks.map((task) =>
      task.id.toString() === taskId.toString()
        ? { ...task, status: newStatus, estadoID: parseInt(estadoID) }
        : task
    );
    setAllTasks(updatedAllTasks);

    // Call backend to update task status
    try {
      await axiosInstance.put(`${API_TAREAS}/${taskId}/estado`, {
        estadoID: parseInt(estadoID),
      });
    } catch (err) {
      console.error("❌ Error en updateTaskStatus:", err);
    }
  };

  // Handle task completion form submission
  const handleCompletionSubmit = async (e) => {
    e.preventDefault();

    if (!completionModal.realHours) {
      alert("Por favor ingresa las horas reales para completar la tarea.");
      return;
    }

    setCompletionModal((prev) => ({ ...prev, isSubmitting: true }));

    try {
      // Update task completion status and real hours using axios instance
      await axiosInstance.put(
        `${API_TAREAS}/${completionModal.taskId}/completar`,
        {
          horasReales: parseInt(completionModal.realHours),
          completado: 1,
          estadoID: 3,
        }
      );

      // Update local state after successful API call
      const updatedAllTasks = allTasks.map((task) =>
        task.id === completionModal.taskId
          ? {
              ...task,
              completed: true,
              realHours: parseFloat(completionModal.realHours),
              status: "Done",
              estadoID: 3,
            }
          : task
      );

      setAllTasks(updatedAllTasks);
      applyFilters(updatedAllTasks);

      // Close the modal
      setCompletionModal({
        visible: false,
        taskId: null,
        taskName: "",
        realHours: "",
        comment: "",
        isSubmitting: false,
      });
    } catch (error) {
      console.error("❌ Error al completar la tarea:", error);
      alert("Error al completar la tarea: " + error.message);
    } finally {
      setCompletionModal((prev) => ({ ...prev, isSubmitting: false }));
    }
  };

  // Handle cancellation of task completion
  const handleCompletionCancel = () => {
    setCompletionModal({
      visible: false,
      taskId: null,
      taskName: "",
      realHours: "",
      comment: "",
      isSubmitting: false,
    });
  };

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col w-full">
        <Header
          headerTitle="Sprint Manager"
          showUserFilter={true}
          showVisibilityToggle={true} // Enable visibility toggle
          showAddButton={true} // Enable Add Task button
          addButtonLabel="Add Task"
          AddComponent={AddTask}
          onAddComplete={fetchTasks}
          showMyTasksOnly={showMyTasksOnly}
          onToggleMyTasks={toggleMyTasks}
          onFilterChange={handleFilterChange}
        />
        <div className="p-4 lg:p-6 mt-0 overflow-auto">
          <h1 className="text-2xl font-bold mb-4">Task Board</h1>
          {loading && <p>Loading sprints...</p>}
          {error && <p className="text-red-500">{error}</p>}
          <p className="text-gray-500 mb-6">
            {tasks.length} tasks in progress
            {showMyTasksOnly ? " (showing only my tasks)" : ""}
            {filters.sprints.length > 0
              ? ` (filtered by ${filters.sprints.length} sprint${
                  filters.sprints.length > 1 ? "s" : ""
                })`
              : ""}
            {filters.users.length > 0
              ? ` (filtered by ${filters.users.length} user${
                  filters.users.length > 1 ? "s" : ""
                })`
              : ""}
          </p>
          <DragDropContext onDragEnd={handleDragEnd}>
            <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 lg:gap-6">
              {columns.map((column) => (
                <Droppable droppableId={column.status} key={column.status}>
                  {(provided) => (
                    <div
                      ref={provided.innerRef}
                      {...provided.droppableProps}
                      className="bg-white rounded-lg shadow-md overflow-y-auto max-h-[700px] relative"
                    >
                      <div className="flex justify-between items-center sticky top-0 z-10 bg-white py-3 px-4">
                        <span
                          className={
                            column.status === "To Do"
                              ? "text-blue-800 bg-blue-100 px-2 py-1 rounded-full text-sm font-semibold"
                              : column.status === "In Progress"
                              ? "text-yellow-800 bg-yellow-100 px-2 py-1 rounded-full text-sm font-semibold"
                              : "text-green-800 bg-green-100 px-2 py-1 rounded-full text-sm font-semibold"
                          }
                        >
                          {column.title}
                        </span>
                        <span className="bg-gray-200 text-gray-700 px-2 py-1 rounded-full text-sm">
                          {
                            tasks.filter(
                              (task) => task.status === column.status
                            ).length
                          }
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
                              isDragDisabled={
                                task.completed ||
                                (!isAdmin &&
                                  (task.usuarioID == null ||
                                    task.usuarioID.toString() !==
                                      currentUserId?.toString()))
                              }
                            >
                              {(provided) => (
                                <div
                                  ref={provided.innerRef}
                                  {...provided.draggableProps}
                                  {...provided.dragHandleProps}
                                  className={
                                    !isAdmin &&
                                    (task.usuarioID == null ||
                                      task.usuarioID.toString() !==
                                        currentUserId?.toString())
                                      ? "opacity-70"
                                      : ""
                                  }
                                >
                                  <TaskCard
                                    task={task}
                                    onDelete={handleDeleteTask}
                                    editable={
                                      isAdmin ||
                                      (task.usuarioID != null &&
                                        task.usuarioID.toString() ===
                                          currentUserId?.toString())
                                    }
                                  />
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

      {/* Task Completion Modal */}
      {completionModal.visible && (
        <div className="fixed inset-0 bg-black bg-opacity-50 flex justify-center items-center z-50">
          <div className="bg-white p-6 rounded-lg shadow-lg w-96 relative">
            <h2 className="text-xl font-bold mb-4">Completar Tarea</h2>
            <p className="mb-4">
              Estás a punto de marcar como completada la tarea:{" "}
              <strong>{completionModal.taskName}</strong>
            </p>

            <form onSubmit={(e) => e.preventDefault()}>
              <div className="mb-4">
                <label
                  className="block text-gray-700 text-sm font-bold mb-2"
                  htmlFor="realHours"
                >
                  Horas Reales <span className="text-red-500">*</span>
                </label>
                <input
                  id="realHours"
                  type="number"
                  step="0.5"
                  min="0"
                  required
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  value={completionModal.realHours}
                  onChange={(e) =>
                    setCompletionModal((prev) => ({
                      ...prev,
                      realHours: e.target.value,
                    }))
                  }
                  placeholder="Ingresa las horas reales que tomó la tarea"
                />
              </div>

              <div className="mb-6">
                <label
                  className="block text-gray-700 text-sm font-bold mb-2"
                  htmlFor="comment"
                >
                  Comentario (opcional)
                </label>
                <textarea
                  id="comment"
                  className="shadow appearance-none border rounded w-full py-2 px-3 text-gray-700 leading-tight focus:outline-none focus:shadow-outline"
                  value={completionModal.comment}
                  onChange={(e) =>
                    setCompletionModal((prev) => ({
                      ...prev,
                      comment: e.target.value,
                    }))
                  }
                  placeholder="Comentarios adicionales (opcional)"
                  rows="3"
                />
              </div>

              <div className="flex justify-between">
                <button
                  type="button"
                  onClick={handleCompletionCancel}
                  className="bg-gray-300 hover:bg-gray-400 text-black font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                  disabled={completionModal.isSubmitting}
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  onClick={handleCompletionSubmit}
                  className="bg-blue-500 hover:bg-blue-700 text-white font-bold py-2 px-4 rounded focus:outline-none focus:shadow-outline"
                  disabled={completionModal.isSubmitting}
                >
                  {completionModal.isSubmitting
                    ? "Procesando..."
                    : "Completar Tarea"}
                </button>
              </div>
            </form>
          </div>
        </div>
      )}
    </div>
  );
};

export default Board;
