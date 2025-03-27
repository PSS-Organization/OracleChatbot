import React, { useState } from "react";
import { API_SPRINTS } from "../../API";

const AddSprint = ({ onAddComplete }) => {
  const [nombreSprint, setNombreSprint] = useState("");
  const [numeroSprint, setNumeroSprint] = useState("");
  const [startDate, setStartDate] = useState("");
  const [endDate, setEndDate] = useState("");
  const [error, setError] = useState("");
  const [successMessage, setSuccessMessage] = useState("");
  const [loading, setLoading] = useState(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setSuccessMessage("");
    setLoading(true);

    // Basic validation
    if (!nombreSprint || !numeroSprint || !startDate || !endDate) {
      setError("All fields are required.");
      setLoading(false);
      return;
    }

    // Build the sprint object
    const sprintData = {
      nombreSprint,
      numeroSprint: parseInt(numeroSprint),
      startDate: new Date(startDate).toISOString(),
      endDate: new Date(endDate).toISOString(),
    };

    try {
      const response = await fetch(API_SPRINTS, {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(sprintData),
      });

      if (!response.ok) {
        const data = await response.json();
        throw new Error(data.message || "Failed to create sprint.");
      }

      const createdSprint = await response.json();
      console.log("Sprint created:", createdSprint);
      setSuccessMessage("Sprint created successfully!");
      setLoading(false);

      // Clear the form fields
      setNombreSprint("");
      setNumeroSprint("");
      setStartDate("");
      setEndDate("");

      // Notify parent to refresh the list
      if (onAddComplete) onAddComplete(createdSprint);
    } catch (err) {
      console.error("Error creating sprint:", err.message);
      setError(err.message);
      setLoading(false);
    }
  };

  return (
    <div className="p-4 bg-white rounded shadow-md">
      <h2 className="text-xl font-bold mb-4">Add New Sprint</h2>

      {error && <p className="text-red-500 mb-3">{error}</p>}
      {successMessage && <p className="text-green-500 mb-3">{successMessage}</p>}

      <form onSubmit={handleSubmit} className="grid grid-cols-1 gap-4">
        <input
          type="text"
          placeholder="Sprint Name"
          value={nombreSprint}
          onChange={(e) => setNombreSprint(e.target.value)}
          className="p-2 border rounded"
          required
        />

        <input
          type="number"
          placeholder="Sprint Number"
          value={numeroSprint}
          onChange={(e) => setNumeroSprint(e.target.value)}
          className="p-2 border rounded"
          required
        />

        <input
          type="date"
          placeholder="Start Date"
          value={startDate}
          onChange={(e) => setStartDate(e.target.value)}
          className="p-2 border rounded"
          required
        />

        <input
          type="date"
          placeholder="End Date"
          value={endDate}
          onChange={(e) => setEndDate(e.target.value)}
          className="p-2 border rounded"
          required
        />

        <button
          type="submit"
          className={`p-2 rounded text-white ${
            loading ? "bg-gray-500" : "bg-blue-500 hover:bg-blue-600"
          }`}
          disabled={loading}
        >
          {loading ? "Creating Sprint..." : "Add Sprint"}
        </button>
      </form>
    </div>
  );
};

export default AddSprint;
