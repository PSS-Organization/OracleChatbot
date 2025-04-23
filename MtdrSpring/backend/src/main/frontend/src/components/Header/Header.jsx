import React, { useState, useEffect } from "react";
import {
  FilterList,
  Add,
  Close,
  Visibility,
  VisibilityOff,
} from "@mui/icons-material";
import axiosInstance from "../../utils/axiosConfig";
import { API_SPRINTS } from "../../API";

const Header = ({
  headerTitle = "Default Title",
  addButtonLabel = "Add Something",
  AddComponent,
  onAddComplete,
  onFilterChange = () => {},
  showFilter = true, // New prop to control filter visibility
  showUserFilter = true, // Control user filter visibility
  showVisibilityToggle = false, // Control visibility toggle
  showAddButton = false, // Control Add Task button
  showMyTasksOnly = false, // State for visibility toggle
  onToggleMyTasks = () => {}, // Callback for visibility toggle
}) => {
  const [isFilterOpen, setIsFilterOpen] = useState(false);
  const [sprints, setSprints] = useState([]);
  const [usuarios, setUsuarios] = useState([]);
  const [selectedSprints, setSelectedSprints] = useState([]);
  const [selectedUsers, setSelectedUsers] = useState([]);
  const [isModalOpen, setModalOpen] = useState(false);

  // Fetch sprints and users for filter
  useEffect(() => {
    const fetchFilterData = async () => {
      try {
        // Fetch sprints
        const sprintsResponse = await axiosInstance.get(API_SPRINTS);
        if (sprintsResponse.data) {
          // API may return an array or a paginated object with 'content'
          const sprintList = Array.isArray(sprintsResponse.data)
            ? sprintsResponse.data
            : sprintsResponse.data.content || [];
          setSprints(sprintList);
        }

        // Fetch users
        const usersResponse = await axiosInstance.get("/usuarios/all");
        if (usersResponse.data) {
          setUsuarios(usersResponse.data);
        }
      } catch (err) {
        console.error("Error fetching filter data:", err);
      }
    };

    fetchFilterData();
  }, [showUserFilter]);

  const toggleFilterDropdown = () => {
    setIsFilterOpen(!isFilterOpen);
  };

  const handleSprintChange = (e, sprintId) => {
    if (e.target.checked) {
      setSelectedSprints([...selectedSprints, sprintId]);
    } else {
      setSelectedSprints(selectedSprints.filter((id) => id !== sprintId));
      setSelectedSprints(selectedSprints.filter((id) => id !== sprintId));
    }
  };

  const handleUserChange = (e, userId) => {
    if (e.target.checked) {
      setSelectedUsers([...selectedUsers, userId]);
    } else {
      setSelectedUsers(selectedUsers.filter((id) => id !== userId));
      setSelectedUsers(selectedUsers.filter((id) => id !== userId));
    }
  };

  const applyFilters = () => {
    onFilterChange({
      sprints: selectedSprints,
      users: selectedUsers,
    });
    setIsFilterOpen(false);
  };

  const clearFilters = () => {
    setSelectedSprints([]);
    setSelectedUsers([]);
    onFilterChange({
      sprints: [],
      users: [],
    });
  };

  const handleAddSuccess = () => {
    setModalOpen(false);
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
        {/* Visibility Toggle */}
        {showVisibilityToggle && (
          <button
            className={`flex items-center space-x-1 ${
              showMyTasksOnly ? "bg-blue-100 text-blue-800" : "bg-gray-100"
            } px-3 py-2 rounded-md`}
            onClick={onToggleMyTasks}
            title={showMyTasksOnly ? "Showing only my tasks" : "Showing all tasks"}
          >
            {showMyTasksOnly ? <Visibility /> : <VisibilityOff />}
            <span>{showMyTasksOnly ? "My Tasks" : "All Tasks"}</span>
          </button>
        )}

        {/* Filter dropdown (conditionally rendered) */}
        {showFilter && (
          <div className="relative">
            <button
              className={`flex items-center space-x-1 ${
                selectedSprints.length > 0 || selectedUsers.length > 0
                  ? "bg-blue-100 text-blue-800"
                  : "bg-gray-100"
              } px-3 py-2 rounded-md`}
              onClick={toggleFilterDropdown}
            >
              <FilterList />
              <span>Filter</span>
            </button>

            {isFilterOpen && (
              <div className="absolute right-0 mt-2 w-64 bg-white rounded-md shadow-lg z-50 border border-gray-200">
                <div className="p-4">
                  <h3 className="font-bold text-lg mb-2">Filters</h3>

                  {/* Sprints filter section */}
                  <div className="mb-4">
                    <h4 className="font-semibold mb-2">Sprints</h4>
                    <div className="max-h-40 overflow-y-auto">
                      {sprints.map((sprint) => (
                        <div key={sprint.sprintID} className="flex items-center mb-1">
                          <input
                            type="checkbox"
                            id={`sprint-${sprint.sprintID}`}
                            checked={selectedSprints.includes(sprint.sprintID)}
                            onChange={(e) => handleSprintChange(e, sprint.sprintID)}
                            className="mr-2"
                          />
                          <label htmlFor={`sprint-${sprint.sprintID}`}>{sprint.nombreSprint}</label>
                        </div>
                      ))}
                    </div>
                  </div>

                  {/* Users filter section (conditionally rendered) */}
                  {showUserFilter && (
                    <div className="mb-4">
                      <h4 className="font-semibold mb-2">Users</h4>
                      <div className="max-h-40 overflow-y-auto">
                        {usuarios.map((user) => (
                          <div key={user.usuarioID} className="flex items-center mb-1">
                            <input
                              type="checkbox"
                              id={`user-${user.usuarioID}`}
                              checked={selectedUsers.includes(user.usuarioID)}
                              onChange={(e) => handleUserChange(e, user.usuarioID)}
                              className="mr-2"
                            />
                            <label htmlFor={`user-${user.usuarioID}`}>{user.nombre}</label>
                          </div>
                        ))}
                      </div>
                    </div>
                  )}

                  {/* Filter actions */}
                  <div className="flex justify-between">
                    <button
                      className="bg-gray-200 text-gray-700 px-2 py-1 rounded"
                      onClick={clearFilters}
                    >
                      Clear
                    </button>
                    <button
                      className="bg-blue-500 text-white px-2 py-1 rounded"
                      onClick={applyFilters}
                    >
                      Apply
                    </button>
                  </div>
                </div>
              </div>
            )}
          </div>
        )}

        {/* Add Task Button */}
        {showAddButton && (
          <button
            className="flex items-center space-x-1 bg-blue-500 text-white px-4 py-2 rounded-md"
            onClick={() => setModalOpen(true)}
          >
            <Add />
            <span>{addButtonLabel}</span>
          </button>
        )}
      </div>

      {/* Add Task Modal */}
      {isModalOpen && (
        <div 
          className="fixed inset-0 z-50"
          aria-labelledby="modal-title"
          role="dialog"
          aria-modal="true"
        >
          {/* Background overlay */}
          <div className="fixed inset-0 bg-gray-500 bg-opacity-75 transition-opacity"></div>

          {/* Modal panel */}
          <div className="fixed inset-0 z-50 overflow-y-auto">
            <div className="flex min-h-full items-center justify-center p-4 text-center sm:p-0">
              <div
                className="relative transform overflow-hidden rounded-lg bg-white text-left shadow-xl transition-all sm:my-8 sm:w-full sm:max-w-lg"
                onClick={(e) => e.stopPropagation()}
              >
                {/* Modal content */}
                <div className="bg-white p-6 pb-4 sm:p-6 sm:pb-4">
                  <div className="absolute top-0 right-0 pt-2 pr-2">
                    <button
                      type="button"
                      className="rounded-md bg-white text-gray-400 hover:text-gray-500 focus:outline-none"
                      onClick={() => setModalOpen(false)}
                    >
                      <span className="sr-only">Close</span>
                      <Close />
                    </button>
                  </div>
                  
                  {/* Component */}
                  {AddComponent && <AddComponent onAddComplete={handleAddSuccess} />}
                </div>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default Header;