import { createContext, useState } from "react";
import users from '../assets/users.json';
import chatsData from '../assets/chats.json';


export const UserContext = createContext();

export default function UserProvider({children}) {
    const [currentUser, setCurrentUser] = useState(JSON.parse(localStorage.getItem("user")) || null);

    // save a user object to the local storage
    const saveUser = (user) => {
        setCurrentUser(user);
        user = JSON.stringify(user);
        localStorage.setItem("user", user);
    }

    // set up user's data from the json file
    const login = (email, password) => {
        const user = users.find(user => user.email === email);
        if (!user || user.password !== password) {
            throw new Error("Invalid credentials.");
        }
        try {
            let chats = JSON.stringify(chatsData.filter(chat => user.id == chat.userId));
            setCurrentUser(user);
            localStorage.setItem("user", JSON.stringify(user));
            localStorage.setItem("chats", chats);
        } catch {
            // rethrow with our own message
            throw new Error("Something went wrong. Failed to login.");
        }
    }

    // remove the user from the storage
    const logout = () => {
        setCurrentUser(null);
        localStorage.removeItem("user");
        const chats = JSON.parse(localStorage.getItem("chats"));
        if (chats) {
            chats.forEach(chat => localStorage.removeItem(chat.id));
            localStorage.removeItem("chats");
        }
    }

    const register = (newUser) => {
        const duplicate = users.find(user => user.email === newUser.email);
        if (duplicate) {
            throw new Error("This email is already in use.");
        }
        try {
            saveUser(newUser);
            localStorage.setItem("chats", JSON.stringify([]));
        } catch {
            throw new Error("Something went wrong. Failed to create an account.")
        }
    }

    return (
        <UserContext.Provider value={{currentUser, login, logout, saveUser, register}}>
            {children}
        </UserContext.Provider>
    )
}
