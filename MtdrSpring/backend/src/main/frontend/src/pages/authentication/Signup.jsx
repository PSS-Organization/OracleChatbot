import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { API_SIGNUP } from '../../API';
import '../../css/Global.css';

const SignupScreen = () => {
    const [email, setEmail] = useState('');
    const [fullname, setFullname] = useState('');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');
    const [loading, setLoading] = useState(false);
    const navigate = useNavigate();

    const handlePhoneChange = (event) => {
        let input = event.target.value.replace(/\D/g, ''); 
        if (input.length > 10) input = input.substring(0, 10); 
        setPhone(input);
    };

    const formatPhoneDisplay = (input) => {
        if (input.length <= 3) return input;
        if (input.length <= 6) return `${input.substring(0, 3)}-${input.substring(3)}`;
        return `${input.substring(0, 3)}-${input.substring(3, 6)}-${input.substring(6, 10)}`;
    };

    const handleSubmit = async (e) => {
        e.preventDefault();
        setError('');
        setLoading(true);

        if (password !== confirmPassword) {
            setError('Passwords do not match!');
            setLoading(false);
            return;
        }

        if (phone.length !== 10) {
            setError('Phone number must be exactly 10 digits.');
            setLoading(false);
            return;
        }

        const userData = {
            nombre: fullname,
            correo: email,
            telefono: phone,
            contrasena: password,
            rolUsuario: 'USER',
            esAdmin: 0,
            equipoID: null
        };

        try {
            const response = await fetch(API_SIGNUP, {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify(userData)
            });

            const data = await response.json();
            if (!response.ok) throw new Error(data.error || 'Error al registrar usuario');

            console.log('✅ Usuario registrado:', data);

            // Guarda la información del usuario en localStorage
            localStorage.setItem('user', JSON.stringify(data.usuario));

            // Store the userId separately for easier access
            const userId = data.usuario.usuarioID;
            localStorage.setItem('userId', userId);
            console.log('Stored userId in localStorage:', userId);

            setLoading(false);

            // 🚀 Solo aquí rediriges al usuario si el registro fue exitoso
            navigate('/board');

        } catch (error) {
            console.error('❌ Error en el registro:', error.message);
            setError(error.message);
            setLoading(false);
        }
    };


    return (
        <div className="w-full min-h-screen flex items-center justify-center bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg w-96 shadow-md">
                <h1 className="mb-1 text-2xl font-semibold text-gray-900 text-center">Welcome rando!</h1>
                <p className="mb-6 text-gray-600 text-center">Enter your details to register</p>

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="email">Email address</label>
                <input className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" id="email" type="email" placeholder="employee@java.com" value={email} onChange={(e) => setEmail(e.target.value)} required />

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="fullname">Name</label>
                <input className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" id="fullname" type="text" placeholder="John Doe" value={fullname} onChange={(e) => setFullname(e.target.value)} required />

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="phone">Phone</label>
                <input className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" id="phone" type="text" placeholder="123-456-7890" value={formatPhoneDisplay(phone)} onChange={handlePhoneChange} required />

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="password">Password</label>
                <input className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" id="password" type="password" placeholder="********" value={password} onChange={(e) => setPassword(e.target.value)} required />

                <label className="block mb-2 font-semibold text-gray-700" htmlFor="confirmPassword">Confirm Password</label>
                <input className="w-full p-2 mb-4 border border-gray-300 rounded-md focus:outline-none focus:ring-2 focus:ring-blue-500" id="confirmPassword" type="password" placeholder="********" value={confirmPassword} onChange={(e) => setConfirmPassword(e.target.value)} required />

                {error && <p className="text-red-500 text-sm mb-4">{error}</p>}

                <button className="bg-blue-500 text-white px-4 py-2 rounded-md w-full hover:bg-blue-600 transition duration-200" type="submit" disabled={loading}>
                    {loading ? 'Registering...' : 'Register'}
                </button>

                <div className="mt-4 text-center">
                    <p>Already have an account? <Link className="text-blue-500 hover:underline" to="/">Log in</Link></p>
                </div>
            </form>
        </div>
    );
};

export default SignupScreen;
