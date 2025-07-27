import { createContext, useEffect, useState } from "react";
import { createUser, getAuthToken, getUserData, updateUserProfile } from "../api/api";

export const AuthContext = createContext();

export default function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('auth-token') || null);
    const [initialChats, setInitialChats] = useState(null);

    useEffect(() => {
        if (token) {
            getUserData(token)
                .then((userData) => {
                    setCurrentUser({ username: userData.username, email: userData.email });
                    setInitialChats(userData.chats);
                })
                .catch(); // failed to load data
        }
    }, []);

    // save a user object to the DB
    const updateUser = (user) => {
        try {
            setCurrentUser(user);
            // update user profile
            updateUserProfile(user);
        } catch {
            // error handling
        }
    }

    // load up user's data from the DB
    const login = async (email, password) => {
        try {
            // we use email to login instead of username
            const newToken = await getAuthToken({ username: email, password: password });
            setToken(newToken);
            localStorage.setItem("auth-token", JSON.stringify(newToken));
            const userData = await getUserData(newToken);
            setCurrentUser({ username: userData.username, email: userData.email });
            // here user is an object with properties: username, email, chats (list of chat objects)
            setInitialChats(userData.chats);
        } catch {
            // TODO: error page or error message

        }
    }

    // remove the token from LocalStorage
    const logout = () => {
        setCurrentUser(null);
        setToken(null);
        localStorage.removeItem("auth-token");
    }

    const register = (newUser) => {
        try {
            createUser(newUser);
        } catch {
            // error page

        }
    }

    return (
        <AuthContext.Provider value={{ currentUser, login, logout, updateUser, register, initialChats, token }}>
            {children}
        </AuthContext.Provider>
    )
}
