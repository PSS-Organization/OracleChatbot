import axios from 'axios';
import { getToken } from '../services/authService';
import { getBackendUrl } from './getBackendUrl';

const instance = axios.create();

instance.interceptors.request.use(
    async (config) => {
        const token = getToken();
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        
        // Set dynamic backend URL
        const backendUrl = await getBackendUrl();
        config.baseURL = backendUrl;
        
        return config;
    },
    (error) => {
        return Promise.reject(error);
    }
);

instance.interceptors.response.use(
    (response) => response,
    (error) => {
        if (error.response && error.response.status === 401) {
            // Token expirado o inválido
            localStorage.clear();
            window.location.href = '/login';
        }
        return Promise.reject(error);
    }
);

export default instance;