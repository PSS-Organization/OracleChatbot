import React, { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { signup } from "../../API";
import { setToken } from "../../services/authService";
import "../../css/Global.css";

const Signup = () => {
  const [fullname, setFullname] = useState("");
  const [email, setEmail] = useState("");
  const [phone, setPhone] = useState("");
  const [password, setPassword] = useState("");
  const [confirmPassword, setConfirmPassword] = useState("");
  const [error, setError] = useState("");
  const [loading, setLoading] = useState(false);
  const navigate = useNavigate();

  const handlePhoneChange = (e) => {
    const input = e.target.value.replace(/\D/g, "");
    setPhone(input);
  };

  const formatPhoneDisplay = (input) => {
    if (!input) return "";
    if (input.length <= 3) return input;
    if (input.length <= 6)
      return `${input.substring(0, 3)}-${input.substring(3)}`;
    return `${input.substring(0, 3)}-${input.substring(3, 6)}-${input.substring(
      6,
      10
    )}`;
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError("");
    setLoading(true);

    if (password !== confirmPassword) {
      setError("Las contraseñas no coinciden");
      setLoading(false);
      return;
    }

    if (phone.length !== 10) {
      setError("El número de teléfono debe tener exactamente 10 dígitos");
      setLoading(false);
      return;
    }

    const userData = {
      nombre: fullname,
      correo: email,
      telefono: phone,
      contrasena: password,
      rolUsuario: "USER",
      esAdmin: false,
      equipoID: null,
    };

    try {
      const response = await signup(userData);

      if (response.success) {
        // Guardar el token JWT
        setToken(response.token);

        // Guardar información del usuario
        localStorage.setItem("user", JSON.stringify(response.usuario));
        localStorage.setItem("userId", response.usuario.usuarioID);

        navigate("/home");
      } else {
        setError(response.message || "Error en el registro");
      }
    } catch (err) {
      setError(err.message || "Error en el registro");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="w-full min-h-screen flex items-center justify-center bg-gray-100">
      <form
        onSubmit={handleSubmit}
        className="bg-white p-8 rounded-lg w-96 shadow-md"
      >
        <h1 className="text-2xl font-semibold text-center mb-6">
          Create an account
        </h1>

        <label
          className="block mb-2 font-semibold text-gray-700"
          htmlFor="fullname"
        >
          Full Name
        </label>
        <input
          className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          id="fullname"
          type="text"
          placeholder="John Doe"
          value={fullname}
          onChange={(e) => setFullname(e.target.value)}
          required
        />

        <label
          className="block mb-2 font-semibold text-gray-700"
          htmlFor="email"
        >
          Email
        </label>
        <input
          className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          id="email"
          type="email"
          placeholder="john@example.com"
          value={email}
          onChange={(e) => setEmail(e.target.value)}
          required
        />

        <label
          className="block mb-2 font-semibold text-gray-700"
          htmlFor="phone"
        >
          Phone
        </label>
        <input
          className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          id="phone"
          type="text"
          placeholder="123-456-7890"
          value={formatPhoneDisplay(phone)}
          onChange={handlePhoneChange}
          required
        />

        <label
          className="block mb-2 font-semibold text-gray-700"
          htmlFor="password"
        >
          Password
        </label>
        <input
          className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          id="password"
          type="password"
          placeholder="********"
          value={password}
          onChange={(e) => setPassword(e.target.value)}
          required
        />

        <label
          className="block mb-2 font-semibold text-gray-700"
          htmlFor="confirmPassword"
        >
          Confirm Password
        </label>
        <input
          className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
          id="confirmPassword"
          type="password"
          placeholder="********"
          value={confirmPassword}
          onChange={(e) => setConfirmPassword(e.target.value)}
          required
        />

        {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

        <button
          className="bg-blue-500 text-white px-4 py-2 rounded-md w-full hover:bg-blue-600 transition duration-200"
          type="submit"
          disabled={loading}
        >
          {loading ? "Creating account..." : "Sign up"}
        </button>

        <div className="mt-4 text-center">
          <p>
            Already have an account?{" "}
            <Link className="text-blue-500 hover:underline" to="/login">
              Sign in
            </Link>
          </p>
        </div>
      </form>
    </div>
  );
};

export default Signup;
