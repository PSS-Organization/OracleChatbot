import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/authentication/Login";
import Signup from "./pages/authentication/Signup";
import TodoList from "./pages/TodoList";


function App() {
  return (
    <Routes>
      {/* Default route redirects to /login */}
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/home" element={<TodoList />} />

      {/* Add other routes as needed */}
    </Routes>

  );
}

export default App;
