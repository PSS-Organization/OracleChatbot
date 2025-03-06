import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../../css/Global.css';

const SignupScreen = () => {
    const [email, setEmail] = useState('');
    const [fullname, setFullname] = useState('');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');
    const [error, setError] = useState('');

    const handlePhoneChange = (event) => {
        let input = event.target.value.replace(/\D/g, '');
        if (input.startsWith('52')) {
            input = input.substring(2);
        }
        if (input.length > 10) {
            input = input.substring(0, 10);
        }
        setPhone(input);
    };

    const formatPhoneDisplay = (input) => {
        if (input.length <= 3) return `+52-${input}`;
        if (input.length <= 6) return `+52-${input.substring(0, 3)}-${input.substring(3)}`;
        return `+52-${input.substring(0, 3)}-${input.substring(3, 6)}-${input.substring(6, 10)}`;
    };

    const handleSubmit = (e) => {
        e.preventDefault();
        if (password !== confirmPassword) {
            setError('Passwords do not match!');
            return;
        }
        if (phone.length !== 10) {
            setError('Phone number must be exactly 10 digits.');
            return;
        }
        setError('');
        console.log('Email:', email);
        console.log('Full Name:', fullname);
        console.log('Phone:', `+52${phone}`);
        console.log('Password:', password);
    };

    return (
        <div className="w-full min-h-screen flex items-center justify-center bg-gray-100">
            <form onSubmit={handleSubmit} className="bg-white p-8 rounded-lg w-96 shadow-md">
                <h1 className="mb-1 text-2xl font-semibold text-gray-900 text-center">Welcome random!</h1>
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

                <button className="bg-blue-500 text-white px-4 py-2 rounded-md w-full hover:bg-blue-600 transition duration-200" type="submit">Register</button>

                <div className="mt-4 text-center">
                    <p>Already have an account? <Link className="text-blue-500 hover:underline" to="/">Log in</Link></p>
                </div>
            </form>
        </div>
    );
};

export default SignupScreen;
