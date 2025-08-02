import { createContext, useEffect, useState } from "react";
import { createUser, getAuthToken, getUserData, updateUserProfile } from "../api/api";
import { useNavigate } from "react-router";

export const AuthContext = createContext();

// This context provider is responsible for authentication, registration, and other user related actions.
// It passes the user, the token, the functions, and the loading state down to its children. 
export default function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(null);
    const [token, setToken] = useState(JSON.parse(localStorage.getItem('auth-token')) || null);
    const [initialChats, setInitialChats] = useState(null);
    const [loading, setLoading] = useState(token ? true : false);
    const [error, setError] = useState(null);

    // fetches the user's profile and chats in one call 
    useEffect(() => {
        async function fetchInitialData() {
            setLoading(true);
            try {
                console.log("fetching initial data");
                console.log(token);
                const userData = await getUserData(token);
                console.log(userData);
                setCurrentUser({ username: userData.username, email: userData.email });
                setInitialChats(userData.chats);
            } catch (err) {
                console.log(err.message);
                if (err.message?.includes("token")) {
                    handleUnauthorized();
                } else {
                    setError(err.message);
                }
            } finally {
                setLoading(false);
            }
        };
        if (token) {
            fetchInitialData();
        }
    }, [token]);

    // save a user object to the DB
    const updateUser = async (user, token) => {
        //setLoading(true);
        // the errors that the updateProfile function might throw 
        // will be caught in the calling component (the user profile component)
        await updateUserProfile(user, token);
        setCurrentUser(user);
       // setLoading(false);
    }

    // aquires the access token and puts it to the local storage
    // any errors will be handled in the calling component
    const login = async (email, password) => {
        setLoading(true);
        // we use email to login instead of username
        const newToken = await getAuthToken({ username: email, password: password });
        setToken(newToken);
        localStorage.setItem("auth-token", JSON.stringify(newToken));
        setLoading(false);
    }

    // remove the token from LocalStorage
    const logout = () => {
        setCurrentUser(null);
        setToken(null);
        localStorage.removeItem("auth-token");
    }

    const register = async (newUser) => {
        // any errors will be handled in the calling component
        await createUser(newUser);
    }

    function handleUnauthorized(message = null) {
        // whenever we get 401 most likely it means the token has expired
        // there is no refresh tokens so the app should redirect the user
        // to the login page and show a message that they were logged out
        // and should login again
        logout();
        setError("Session expired. Please log in again.");
    }

    return (
        <AuthContext.Provider value={{
            currentUser,
            login, logout, updateUser, register, initialChats, token,
            handleUnauthorized,
            authLoading: loading,
            authError: error,
            setAuthError: setError
        }}>
            {children}
        </AuthContext.Provider>
    )
}
