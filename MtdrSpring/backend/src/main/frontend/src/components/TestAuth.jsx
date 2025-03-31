import React, { useState, useEffect } from 'react';
import axios from 'axios';

const TestAuth = () => {
    const [userInfo, setUserInfo] = useState(null);
    const [userId, setUserId] = useState(null);
    const [adminStatus, setAdminStatus] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState(null);

    // Function to manually set a test user ID
    const setTestUserId = () => {
        const testId = "1"; // Change this to a valid user ID in your system
        localStorage.setItem('userId', testId);
        setUserId(testId);
        checkAdminStatus(testId);
    };

    // Function to check admin status for a user ID
    const checkAdminStatus = async (id) => {
        try {
            setLoading(true);
            const response = await axios.get(`/usuarios/is-admin/${id}`);
            console.log("Admin check response:", response.data);
            setAdminStatus(response.data.isAdmin);
        } catch (err) {
            console.error("Error checking admin status:", err);
            setError(`Error checking admin status: ${err.message}`);
        } finally {
            setLoading(false);
        }
    };

    // Load information on component mount
    useEffect(() => {
        // Check localStorage for user data
        const userData = localStorage.getItem('user');
        const storedUserId = localStorage.getItem('userId');

        if (userData) {
            try {
                setUserInfo(JSON.parse(userData));
            } catch (e) {
                console.error("Error parsing user data:", e);
            }
        }

        if (storedUserId) {
            setUserId(storedUserId);
            checkAdminStatus(storedUserId);
        } else {
            setLoading(false);
        }
    }, []);

    return (
        <div className="p-4 m-4 bg-white rounded shadow">
            <h2 className="text-xl font-bold mb-4">Authentication Debug</h2>

            <div className="mb-4">
                <h3 className="text-lg font-semibold">localStorage Content:</h3>
                <p>User ID: {userId || 'Not found'}</p>
                <p>Admin Status: {adminStatus === null ? 'Unknown' : adminStatus ? 'Yes' : 'No'}</p>
                <p>localStorage Keys: {Object.keys(localStorage).join(', ') || 'None'}</p>
            </div>

            <div className="mb-4">
                <h3 className="text-lg font-semibold">User Object:</h3>
                <pre className="bg-gray-100 p-2 rounded overflow-auto max-h-40">
                    {userInfo ? JSON.stringify(userInfo, null, 2) : 'No user data found'}
                </pre>
            </div>

            {error && (
                <div className="text-red-500 mb-4">
                    Error: {error}
                </div>
            )}

            <div className="mt-4">
                <button
                    className="bg-blue-500 text-white px-4 py-2 rounded mr-2"
                    onClick={setTestUserId}
                >
                    Set Test User ID (1)
                </button>

                <button
                    className="bg-red-500 text-white px-4 py-2 rounded"
                    onClick={() => {
                        localStorage.clear();
                        setUserInfo(null);
                        setUserId(null);
                        setAdminStatus(null);
                    }}
                >
                    Clear localStorage
                </button>
            </div>
        </div>
    );
};

export default TestAuth; 