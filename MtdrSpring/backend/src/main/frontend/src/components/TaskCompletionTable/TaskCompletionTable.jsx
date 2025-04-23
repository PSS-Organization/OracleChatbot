import React from "react";

const TaskCompletionTable = ({ userStats }) => {
  return (
    <div className="lg:col-span-2">
      <div className="bg-white p-4 rounded-lg shadow-md mb-6 border-2 border-gray-200">
        <h2 className="text-xl font-bold mb-4 border-b pb-2">Task Completion Table</h2>
        <div className="overflow-x-auto">
          <table className="min-w-full divide-y divide-gray-200">
            <thead className="bg-gray-50">
              <tr>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  User
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Tasks To Do
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Completed Tasks
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Total Tasks
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Percentage Completed
                </th>
                <th className="px-4 py-2 text-left text-xs font-medium text-gray-500 uppercase tracking-wider">
                  Total Real Hours
                </th>
              </tr>
            </thead>
            <tbody className="bg-white divide-y divide-gray-200">
              {userStats.map((user, index) => (
                <tr key={index}>
                  <td className="px-4 py-2 whitespace-nowrap">{user.userName}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{user.tasksToDo}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{user.completedTasks}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{user.totalTasks}</td>
                  <td className="px-4 py-2 whitespace-nowrap">{user.percentageCompleted}%</td>
                  <td className="px-4 py-2 whitespace-nowrap">{user.totalRealHours}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};

export default TaskCompletionTable;