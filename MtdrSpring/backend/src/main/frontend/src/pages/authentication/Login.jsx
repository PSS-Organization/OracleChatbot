import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { API_SIGNUP } from '../../API';
import '../../css/Global.css';
import axios from 'axios';
import { getBackendUrl } from '../../utils/getBackendUrl';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');
    const [error, setError] = useState(null);
    const navigate = useNavigate();

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError(null); // Clear previous errors

        try {
            // Get the backend URL dynamically
            const backendUrl = await getBackendUrl();

            // Send login request to backend
            const response = await axios.post(`${backendUrl}/usuarios/login`, {
                email,
                password
            });

            if (response.data.success) {

                localStorage.setItem('user', JSON.stringify(response.data.usuario));

                // Store the userId separately for easier access
                const userId = response.data.usuario.usuarioID;
                localStorage.setItem('userId', userId);
                console.log('Stored userId in localStorage:', userId);

                navigate('/board');
            } else {

                setError(response.data.message || 'Invalid credentials');
            }
        } catch (err) {
            setError('Login failed. Please check your credentials.');
        }
    };

    return (
        <div className="w-full min-h-screen flex items-center justify-center bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg w-80 shadow-md">
                <h1 className="mb-1 text-2xl font-semibold text-gray-900 text-center">Welcome back</h1>
                <p className="mb-6 text-gray-600 text-center">Please enter your details to sign in</p>
                <p>Prueba S5</p>

                {error && <p className="text-red-500 text-center">{error}</p>}

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="email">Email address</label>
                <input
                    className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    id="email"
                    type="email"
                    placeholder="employee@oracle.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="password">Password</label>
                <input
                    className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    id="password"
                    type="password"
                    placeholder="********"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <div className="text-right mb-4">
                    <Link className="text-blue-500 hover:underline" to="/forgot-password">Forgot password?</Link>
                </div>

                <button className="bg-blue-500 text-white px-4 py-2 rounded-md w-full hover:bg-blue-600 transition duration-200" type="submit">Sign in</button>

                <div className="mt-4 text-center">
                    <p>
                        Don't have an account? <Link className="text-blue-500 hover:underline" to="/signup">Sign up</Link>
                    </p>
                </div>
            </form>
        </div>
    );
};

export default Login;
