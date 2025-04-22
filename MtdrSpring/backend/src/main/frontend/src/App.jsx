import { Routes, Route, Navigate } from "react-router-dom";
import Login from "./pages/authentication/Login";
import Signup from "./pages/authentication/Signup";
import TodoList from "./pages/TodoList";
import Board from "./pages/board/Board";
import Sprint from "./pages/sprint/Sprint";
import Team from "./pages/team/Team";
import TestAuth from "./components/TestAuth";
import Dashboard from "./pages/dashboard/Dasboard";
import Home from "./pages/home/Home";

//import Sidebar from "./components/SideBar/SideBar";

function App() {
  return (
    <Routes>
      {/* Default route redirects to /login */}
      <Route path="/" element={<Navigate to="/login" />} />
      <Route path="/login" element={<Login />} />
      <Route path="/signup" element={<Signup />} />
      <Route path="/home" element={<Home />} />
      <Route path="/board" element={<Board />} />
      <Route path="/sprint" element={<Sprint />} />
      <Route path="/test-auth" element={<TestAuth />} />
      <Route path="/dashboard" element={<Dashboard />} />
      <Route path="/team" element={<Team />} />

      {/* Add other routes as needed */}
    </Routes>

  );
}

export default App;
