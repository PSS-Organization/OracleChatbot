import React, { useEffect } from "react";
import { BrowserRouter } from "react-router-dom";
import App from "./App";
import { isAuthenticated } from "./services/authService";

// Token management initialization component
const TokenInitializer = ({ children }) => {
  useEffect(() => {
    // This runs only on client-side at application initialization
    // It ensures the authentication state is properly loaded before rendering the app
    if (isAuthenticated()) {
      console.log("Valid token found in localStorage");
    } else {
      console.log("No valid token found, user will need to login");
    }
  }, []);

  return children;
};

// Root component with TokenInitializer
const Root = () => (
  <BrowserRouter>
    <TokenInitializer>
      <App />
    </TokenInitializer>
  </BrowserRouter>
);

export default Root;
