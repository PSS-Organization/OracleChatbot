export const getBackendUrl = async () => {
    try {
        // Get the current hostname dynamically
        const currentHost = window.location.hostname;
        const port = ':8080'; // Your backend port
        
        // If running locally, use localhost
        if (currentHost === 'localhost' || currentHost === '127.0.0.1') {
            return `http://localhost${port}`;
        }
        
        // In production/cloud, use the current hostname
        return `http://${currentHost}`;
    } catch (error) {
        console.error('Error getting backend URL:', error);
        // Fallback to a default URL if something goes wrong
        return 'http://localhost:8080';
    }
};