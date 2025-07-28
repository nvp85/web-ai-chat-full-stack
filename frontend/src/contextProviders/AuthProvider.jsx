import { createContext, useEffect, useState } from "react";
import { createUser, getAuthToken, getUserData, updateUserProfile } from "../api/api";

export const AuthContext = createContext();

// This context provider is responsible for authentication, registration, and other user related actions.
// It passes the user, the token, the functions, and the loading state down to its children. 
export default function AuthProvider({ children }) {
    const [currentUser, setCurrentUser] = useState(null);
    const [token, setToken] = useState(localStorage.getItem('auth-token') || null);
    const [initialChats, setInitialChats] = useState(null);
    const [loading, setLoading] = useState(true);

    useEffect(() => {
        async function fetchInitialData() {
            setLoading(true);
            try {
                console.log("fetching initial data");
                console.log(token);
                const userData = await getUserData(token);
                setCurrentUser({ username: userData.username, email: userData.email });
                setInitialChats(userData.chats);
            } catch {
                setToken(null);
                setCurrentUser(null);
            } finally {
                setLoading(false);
            }
        };
        if (token) {
            fetchInitialData();
        } else {
            // navigate to homepage or to login?
        }

    }, [token]);

    // save a user object to the DB
    const updateUser = async (user) => {
        setLoading(true);
        // the errors that the updateProfile function might throw 
        // will be caught in the calling component (the user profile component)
        await updateUserProfile(user);
        setCurrentUser(user);
        setLoading(false);
    }

    // load up user's data from the DB
    const login = async (email, password) => {
        setLoading(true);
        try {
            // we use email to login instead of username
            const newToken = await getAuthToken({ username: email, password: password });
            setToken(newToken);
            localStorage.setItem("auth-token", JSON.stringify(newToken));
            const userData = await getUserData(newToken);
            setCurrentUser({ username: userData.username, email: userData.email });
            // here user is an object with properties: username, email, chats (list of chat objects)
            setInitialChats(userData.chats);
            console.log(userData.chats);
        } catch {
            // TODO: error page or error message

        }
        setLoading(false);
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
    if (loading) {
        return (
            <div>
                <p>Loding user data...</p>
            </div>
        )
    }

    return (
        <AuthContext.Provider value={{ currentUser, login, logout, updateUser, register, initialChats, token, authLoading: loading }}>
            {children}
        </AuthContext.Provider>
    )
}
