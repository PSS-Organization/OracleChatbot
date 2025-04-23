import React from "react";

const TaskDeviationTable = ({ userStats }) => {
  return (
    <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200 h-full">
      <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Deviation</h2>
      <div className="overflow-x-auto">
        <table className="min-w-full divide-y divide-gray-200">
          <thead className="bg-gray-50">
            <tr>
              <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                User
              </th>
              <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Assigned Tasks
              </th>
              <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                Deviation
              </th>
            </tr>
          </thead>
          <tbody className="bg-white divide-y divide-gray-200">
            {userStats.map((user, index) => (
              <tr key={index}>
                <td className="px-4 py-2 text-s whitespace-nowrap">{user.userName}</td>
                <td className="px-4 py-2 text-s whitespace-nowrap">{user.tasksAssigned}</td>
                <td className="px-4 py-2 text-s whitespace-nowrap">{user.taskDeviation}</td>
              </tr>
            ))}
          </tbody>
        </table>
      </div>
    </div>
  );
};

export default TaskDeviationTable;