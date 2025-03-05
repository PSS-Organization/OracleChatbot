import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import '../css/Login.css';

const Login = () => {
    const [email, setEmail] = useState('');
    const [password, setPassword] = useState('');

    const handleSubmit = (e) => {
        e.preventDefault();
        console.log('Email:', email);
        console.log('Password:', password);
    };

    return (
        <div className="login-container">

            <form onSubmit={handleSubmit} className="login-form">
                <h1>Welcome back</h1>
                <p>Please enter your details to sign in</p>

                <label className="parameter-text" htmlFor="email">Email address</label>
                <input
                    id="email"
                    type="email"
                    placeholder="employee@java.com"
                    value={email}
                    onChange={(e) => setEmail(e.target.value)}
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

                <div className="forgot-password">
                    <a href="/forgot-password">Forgot password?</a>
                </div>

                <button type="submit">Sign in</button>

                <div className="signup-link">
                    <p>
                        Don't have an account? <Link to="/signup">Sign up</Link>
                    </p>
                </div>
            </form>
        </div>
    );
};

export default Login;