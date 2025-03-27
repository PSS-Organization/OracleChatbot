import React, { useState, useEffect } from "react";
import Header from "../../components/Header/Header";
import Sidebar from "../../components/SideBar/SideBar";
import AddSprint from "../../components/AddSprint/AddSprint";
import SprintCard from "../../components/SprintCard/SprintCard";
import { API_SPRINTS } from "../../API";

const Sprint = () => {
  const [sprints, setSprints] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");

  // Function to fetch sprints from the backend
  const fetchSprints = async () => {
    setLoading(true);
    try {
      const response = await fetch(API_SPRINTS);
      if (!response.ok) {
        throw new Error("Error fetching sprints");
      }
      const data = await response.json();
      setSprints(data);
      setError("");
    } catch (err) {
      setError(err.message);
    } finally {
      setLoading(false);
    }
  };

  // Fetch sprints on component mount
  useEffect(() => {
    fetchSprints();
  }, []);

  return (
    <div className="flex min-h-screen bg-gray-100">
      <Sidebar />
      <div className="flex-1 flex flex-col">
        <Header
          headerTitle="Sprint Manager"
          addButtonLabel="Add Sprint"
          AddComponent={AddSprint}
          onAddComplete={fetchSprints} // refresh sprints when a new sprint is added
        />
        <div className="p-4 lg:p-6 mt-16 lg:mt-0 overflow-auto">
          <h1 className="text-2xl font-bold mb-4">Sprints</h1>
          {loading && <p>Loading sprints...</p>}
          {error && <p className="text-red-500">{error}</p>}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
            {sprints.map((sprint) => (
              <SprintCard key={sprint.sprintID} sprint={sprint} />
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};

export default Sprint;
