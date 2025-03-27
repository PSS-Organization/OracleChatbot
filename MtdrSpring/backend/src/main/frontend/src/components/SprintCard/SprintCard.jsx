// SprintCard.jsx
import React from "react";

const SprintCard = ({ sprint }) => {
  // Destructure your sprint object
  const {
    sprintID,
    nombreSprint,
    numeroSprint,
    startDate,
    endDate,
    // If you have tasks, you can pass them in as well
    // tasks = []
  } = sprint;

  // Format dates as needed
  const formattedStart = new Date(startDate).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
  });
  const formattedEnd = new Date(endDate).toLocaleDateString("en-US", {
    month: "short",
    day: "numeric",
  });

  // Placeholder tasks or statuses
  // const fakeTasks = tasks.length > 0 ? tasks : [ ...some placeholder data... ];

  return (
    <div className="bg-white rounded-lg shadow-md p-4 mb-4">
      {/* Sprint Header */}
      <div className="flex items-center justify-between mb-2">
        <h2 className="text-lg font-bold">
          Sprint {numeroSprint} — <span className="text-gray-600">#{sprintID}</span>
        </h2>
        <span className="text-sm text-gray-500">
          {formattedStart} - {formattedEnd}
        </span>
      </div>

      {/* Sprint Name */}
      <p className="text-gray-700 font-semibold mb-2">{nombreSprint}</p>

      {/* Example tasks or statuses (placeholder) */}
      <div className="text-sm space-y-1">
        {/* 
          For now, placeholders. 
          Later, you can map over actual tasks related to this sprint
        */}
        <div className="flex items-center justify-between bg-gray-50 p-2 rounded">
          <span>Task A (placeholder)</span>
          <span className="bg-green-100 text-green-800 px-2 py-1 text-xs rounded">
            Completed
          </span>
        </div>
        <div className="flex items-center justify-between bg-gray-50 p-2 rounded">
          <span>Task B (placeholder)</span>
          <span className="bg-yellow-100 text-yellow-800 px-2 py-1 text-xs rounded">
            In Progress
          </span>
        </div>
        {/* ... Add more placeholders or real tasks here */}
      </div>
    </div>
  );
};

export default SprintCard;
