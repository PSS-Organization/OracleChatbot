import {
  persistAuthState, 
  clearAuthState,
  getStoredToken,
  getStoredUserData,
  getStoredUserId
} from '../utils/authStorage';

// Token management
export const getToken = () => {
  return getStoredToken();
};

export const setToken = (token) => {
  persistAuthState(token, getUserData());
};

export const removeToken = () => {
  const userData = getUserData();
  clearAuthState();
  return userData;
};

// User authentication state
export const isAuthenticated = () => {
  const token = getToken();
  if (!token) return false;

  try {
    // Check if token is expired
    const payload = JSON.parse(atob(token.split('.')[1]));
    const expiry = payload.exp * 1000; // Convert to milliseconds
    const isValid = expiry > Date.now();
    
    // If token is invalid, clean up
    if (!isValid) {
      clearAuthState();
    }
    
    return isValid;
  } catch (error) {
    console.error('Error validating token:', error);
    clearAuthState();
    return false;
  }
};

// User data management
export const setUserData = (userData) => {
  persistAuthState(getToken(), userData);
};

export const getUserData = () => {
  return getStoredUserData();
};

export const getUserId = () => {
  return getStoredUserId();
};

// Logout functionality
export const logout = () => {
  clearAuthState();
  window.location.href = '/login';
};