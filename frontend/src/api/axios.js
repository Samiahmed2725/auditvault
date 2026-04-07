import axios from 'axios';

const api = axios.create({
    // Uses the VITE_API_URL env variable in production (e.g., Netlify), falls back to local dev server
    baseURL: import.meta.env.VITE_API_URL || 'http://localhost:8082',
    headers: {
        'Content-Type': 'application/json',
    },
});

// Add a request interceptor to attach the token
api.interceptors.request.use(
    (config) => {
        const token = localStorage.getItem('token');
        if (token) {
            config.headers.Authorization = `Bearer ${token}`;
        }
        return config;
    },
    (error) => Promise.reject(error)
);

export default api;
