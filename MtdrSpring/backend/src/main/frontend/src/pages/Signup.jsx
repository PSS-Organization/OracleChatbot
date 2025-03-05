import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../css/Login.css';


const SignupScreen = () => {
    const [email, setEmail] = useState('');
    const [fullname, setFullname] = useState('');
    const [phone, setPhone] = useState('');
    const [password, setPassword] = useState('');
    const [confirmPassword, setConfirmPassword] = useState('');

    // Holds in-page validation feedback
    const [error, setError] = useState('');

    // ✅ Strip non-digit characters, remove leading "52" if present, cap at 10 digits
    const handlePhoneChange = (event) => {
        let input = event.target.value.replace(/\D/g, '');
        if (input.startsWith('52')) {
            input = input.substring(2); // Ensure "52" is not duplicated
        }
        if (input.length > 10) {
            input = input.substring(0, 10); // Allow exactly 10 digits
        }
        setPhone(input); // Store only raw numbers, e.g., "1234567890"
    };

    // ✅ Function to format phone number for display (dashes added dynamically)
    const formatPhoneDisplay = (input) => {
        if (input.length <= 3) return `+52-${input}`;
        if (input.length <= 6) return `+52-${input.substring(0, 3)}-${input.substring(3)}`;
        return `+52-${input.substring(0, 3)}-${input.substring(3, 6)}-${input.substring(6, 10)}`;
    };

    // Form submission
    const handleSubmit = (e) => {
        e.preventDefault();

        // Check password match
        if (password !== confirmPassword) {
            setError('Passwords do not match!');
            return;
        }

        // Ensure phone is exactly 10 digits
        if (phone.length !== 10) {
            setError('Phone number must be exactly 10 digits.');
            return;
        }

        // Clear any old error and proceed
        setError('');

        // Example: handle the signup logic, e.g. call an API endpoint
        console.log('Email:', email);
        console.log('Full Name:', fullname);
        console.log('Phone:', `+52${phone}`); // If you store the phone with +52
        console.log('Password:', password);
    };

    return (
        <div className="login-container">
            <form onSubmit={handleSubmit} className="login-form">
                <h1>Welcome rando!</h1>
                <p>Enter your details to register</p>

                <label className="parameter-text" htmlFor="email">Email address</label>
                <input
                    id="email"
                    type="email"
                    placeholder="employee@java.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
                    required
                />

                <label className="parameter-text" htmlFor="fullname">Name</label>
                <input
                    id="fullname"
                    type="text"
                    placeholder="John Doe"
                    value={fullname}
                    onChange={(e) => setFullname(e.target.value)}
                    required
                />

                <label className="parameter-text" htmlFor="phone">Phone</label>
                <input
                    id="phone"
                    type="text"
                    placeholder="123-456-7890"
                    value={formatPhoneDisplay(phone)}
                    onChange={handlePhoneChange}
                    required
                />

                <label className="parameter-text" htmlFor="password">Password</label>
                <input
                    id="password"
                    type="password"
                    placeholder="********"
                    value={password}
                    onChange={(e) => setPassword(e.target.value)}
                    required
                />

                <label className="parameter-text" htmlFor="confirmPassword">Confirm Password</label>
                <input
                    id="confirmPassword"
                    type="password"
                    placeholder="********"
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    required
                />

                {/* Show the error message if present */}
                {error && <p className="error" style={{ color: 'red' }}>{error}</p>}

                <button type="submit">Register</button>

                <div className="signup-link">
                    <p>
                        Already have an account? <Link to="/">Log in</Link>
                    </p>
                </div>
            </form>
        </div>
    );
};

export default SignupScreen;
