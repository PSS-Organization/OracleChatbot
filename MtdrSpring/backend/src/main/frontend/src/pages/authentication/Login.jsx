import React, { useState } from 'react';
import { Link } from 'react-router-dom';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Email:', email);
        console.log('Password:', password);
    };

    return (
        <div className="w-full min-h-screen flex items-center justify-center bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg w-80 shadow-md">
                <h1 className="mb-1 text-2xl font-semibold text-gray-900 text-center">Welcome back</h1>
                <p className="mb-6 text-gray-600">Please enter your details to sign in</p>

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="email">Email address</label>
                <input
                    className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500"
                    id="email"
                    type="email"
                    placeholder="employee@java.com"
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