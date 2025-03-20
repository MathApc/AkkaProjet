import { createContext, useContext, useState, useEffect } from "react";

const AuthContext = createContext();

export const AuthProvider = ({ children }) => {
    const [isAuthenticated, setIsAuthenticated] = useState(false);
    const [userId, setUserId] = useState(null);

    useEffect(() => {
        const token = localStorage.getItem("token");
        const storedUserId = localStorage.getItem("userId");

        if (token && storedUserId) {
            setIsAuthenticated(true);
            setUserId(storedUserId);
        }
    }, []);

    const login = (token, userId) => {
        localStorage.setItem("token", token);
        localStorage.setItem("userId", userId);
        setIsAuthenticated(true);
        setUserId(userId);
    };

    const logout = () => {
        localStorage.removeItem("token");
        localStorage.removeItem("userId");
        setIsAuthenticated(false);
        setUserId(null);
    };

    return (
        <AuthContext.Provider value={{ isAuthenticated, userId, login, logout }}>
            {children}
        </AuthContext.Provider>
    );
};

export const useAuth = () => useContext(AuthContext);
