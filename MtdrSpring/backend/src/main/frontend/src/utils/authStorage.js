// Utility to handle token persistence across page loads
const TOKEN_KEY = 'token';
const USER_DATA_KEY = 'user';
const USER_ID_KEY = 'userId';

// Save the authentication state to sessionStorage to persist across page reloads
export const persistAuthState = (token, userData) => {
  try {
    // Store in both localStorage (for longer persistence) and sessionStorage (for current session)
    localStorage.setItem(TOKEN_KEY, token);
    sessionStorage.setItem(TOKEN_KEY, token);

    if (userData) {
      localStorage.setItem(USER_DATA_KEY, JSON.stringify(userData));
      sessionStorage.setItem(USER_DATA_KEY, JSON.stringify(userData));
      
      if (userData.id) {
        localStorage.setItem(USER_ID_KEY, userData.id.toString());
        sessionStorage.setItem(USER_ID_KEY, userData.id.toString());
      }
    }
    
    return true;
  } catch (error) {
    console.error('Error persisting auth state:', error);
    return false;
  }
};

// Clean up auth state from both storage mechanisms
export const clearAuthState = () => {
  try {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(USER_DATA_KEY);
    localStorage.removeItem(USER_ID_KEY);
    
    sessionStorage.removeItem(TOKEN_KEY);
    sessionStorage.removeItem(USER_DATA_KEY);
    sessionStorage.removeItem(USER_ID_KEY);
    
    return true;
  } catch (error) {
    console.error('Error clearing auth state:', error);
    return false;
  }
};

// Retrieve token with fallback between storages
export const getStoredToken = () => {
  try {
    // Try sessionStorage first (current session)
    let token = sessionStorage.getItem(TOKEN_KEY);
    
    // If not found, try localStorage (longer persistence)
    if (!token) {
      token = localStorage.getItem(TOKEN_KEY);
      
      // If found in localStorage but not in sessionStorage, restore to sessionStorage
      if (token) {
        sessionStorage.setItem(TOKEN_KEY, token);
      }
    }
    
    return token;
  } catch (error) {
    console.error('Error retrieving token:', error);
    return null;
  }
};

// Retrieve user data with fallback between storages
export const getStoredUserData = () => {
  try {
    // Try sessionStorage first
    let userData = sessionStorage.getItem(USER_DATA_KEY);
    
    // If not found, try localStorage
    if (!userData) {
      userData = localStorage.getItem(USER_DATA_KEY);
      
      // If found in localStorage but not in sessionStorage, restore to sessionStorage
      if (userData) {
        sessionStorage.setItem(USER_DATA_KEY, userData);
      }
    }
    
    return userData ? JSON.parse(userData) : null;
  } catch (error) {
    console.error('Error retrieving user data:', error);
    return null;
  }
};

// Get user ID with fallback between storages
export const getStoredUserId = () => {
  try {
    // Try sessionStorage first
    let userId = sessionStorage.getItem(USER_ID_KEY);
    
    // If not found, try localStorage
    if (!userId) {
      userId = localStorage.getItem(USER_ID_KEY);
      
      // If found in localStorage but not in sessionStorage, restore to sessionStorage
      if (userId) {
        sessionStorage.setItem(USER_ID_KEY, userId);
      }
    }
    
    return userId;
  } catch (error) {
    console.error('Error retrieving user ID:', error);
    return null;
  }
};

export default {
  persistAuthState,
  clearAuthState,
  getStoredToken,
  getStoredUserData,
  getStoredUserId
};
