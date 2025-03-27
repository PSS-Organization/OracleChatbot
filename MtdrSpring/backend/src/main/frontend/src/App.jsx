import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/authentication/Login";
import Signup from "./pages/authentication/Signup";
import TodoList from "./pages/TodoList";
import Board from "./pages/board/Board";
import Sprint from "./pages/sprint/Sprint";

//import Sidebar from "./components/SideBar/SideBar";

function App() {
  return (
    <Routes>
      {/* Default route redirects to /login */}
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/home" element={<TodoList />} />
      <Route path="/board" element={<Board />} />
      <Route path="/sprint" element={<Sprint />} />

      {/* Add other routes as needed */}
    </Routes>

  );
}

export default App;
