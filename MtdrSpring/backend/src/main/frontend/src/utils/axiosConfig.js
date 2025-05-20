import axios from 'axios';
import { getToken, isAuthenticated, logout } from '../services/authService';
import { getBackendUrl } from './getBackendUrl';

const instance = axios.create();

// Request interceptor
instance.interceptors.request.use(
    async (config) => {
        try {
            // Check authentication status
            if (isAuthenticated()) {
                const token = getToken();
                if (token) {
                    config.headers.Authorization = `Bearer ${token}`;
                }
            }
            
            // Set dynamic backend URL
            const backendUrl = await getBackendUrl();
            config.baseURL = backendUrl;
            
            return config;
        } catch (error) {
            console.error('Error in request interceptor:', error);
            return config;
        }
    },
    (error) => {
        console.error('Request interceptor error:', error);
        return Promise.reject(error);
    }
);

// Response interceptor
instance.interceptors.response.use(
    (response) => response,
    (error) => {
        // Handle authentication errors
        if (error.response) {
            const status = error.response.status;
            
            // Authentication errors
            if (status === 401 || status === 403) {
                console.warn(`Authentication error (${status}):`, error.response.data);
                
                // Only redirect to login if not already there
                const currentPath = window.location.pathname;
                if (!['/login', '/signup'].includes(currentPath)) {
                    console.log('Redirecting to login due to authentication error');
                    logout();
                }
            }
        }
        
        return Promise.reject(error);
    }
);

export default instance;