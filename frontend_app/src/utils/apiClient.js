import axios from 'axios';
import { API_CONFIG } from '../config/api.config';

const apiClient = axios.create({
  baseURL: API_CONFIG.BASE_URL,
  timeout: API_CONFIG.TIMEOUT,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Normalize API errors without authentication-specific retries.
apiClient.interceptors.response.use(
  (response) => response,
  (error) => {
    // Transform error response to a consistent shape
    const responseData = error.response?.data;
    const errorMessage =
      responseData?.message ||
      responseData?.detail ||
      (typeof responseData?.error === 'string' ? responseData.error : undefined) ||
      (typeof responseData === 'string' ? responseData : undefined) ||
      error.message ||
      'An unexpected error occurred';
    return Promise.reject(new Error(errorMessage));
  }
);

export default apiClient;
