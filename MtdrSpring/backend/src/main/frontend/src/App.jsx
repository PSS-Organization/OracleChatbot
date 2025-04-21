import React from "react";
import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/authentication/Login";
import Signup from "./pages/authentication/Signup";
import TodoList from "./pages/TodoList";
import Board from "./pages/board/Board";
import Sprint from "./pages/sprint/Sprint";
import TestAuth from "./components/TestAuth";
import Dashboard from "./pages/dashboard/Dasboard";
import ProtectedRoute from "./components/ProtectedRoute";

function App() {
  return (
    <Routes>
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route
        path="/home"
        element={
          <ProtectedRoute>
            <TodoList />
          </ProtectedRoute>
        }
      />
      <Route
        path="/board"
        element={
          <ProtectedRoute>
            <Board />
          </ProtectedRoute>
        }
      />
      <Route
        path="/sprint"
        element={
          <ProtectedRoute>
            <Sprint />
          </ProtectedRoute>
        }
      />
      <Route
        path="/test-auth"
        element={
          <ProtectedRoute>
            <TestAuth />
          </ProtectedRoute>
        }
      />
      <Route
        path="/dashboard"
        element={
          <ProtectedRoute>
            <Dashboard />
          </ProtectedRoute>
        }
      />
      <Route path="/" element={<Navigate to="/login" />} />
    </Routes>
  );
}

export default App;
