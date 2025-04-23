import React from "react";

const CompletedTasksTable = ({ completedTasks, filters }) => {
  // Filter completedTasks based on selected users and sprints
  const filteredTasks = completedTasks.filter((task) => {
    const matchesUser = !filters.users.length || filters.users.includes(task.usuarioID);
    const matchesSprint = !filters.sprints.length || filters.sprints.includes(task.sprintID);
    return matchesUser && matchesSprint;
  });

  return (
    <div className="lg:col-span-3">
      <div className="bg-white p-6 rounded-lg shadow-md border-2 border-gray-200">
        <h2 className="text-xl font-bold mb-4 border-b pb-2">Completed Tasks</h2>
        <div className="overflow-x-auto overflow-y-auto" style={{ maxHeight: "400px" }}>
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Task Name
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Developer
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Estimated Hours
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Real Hours
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Status
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {filteredTasks.map((task, index) => (
                <tr key={index}>
                  <td className="px-4 py-2 whitespace-nowrap">{task.tareaNombre}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{task.usuarioNombre}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{task.horasEstimadas || "N/A"}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{task.horasReales || "N/A"}</td>
                  <td className="px-4 py-2 whitespace-nowrap">
                    {task.estadoID === 3 ? "Completed" : "N/A"}
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default CompletedTasksTable;