import { useState, createContext, useEffect, useContext } from "react";
import { useUser } from '../hooks/useUser';


export const ChatListContext = createContext();

export default function ChatListProvider({ children }) {
    const { currentUser } = useUser();
    const [chats, setChats] = useState(JSON.parse(localStorage.getItem("chats")) || null);

    // simulating a call to a backend API to fetch the user's chats
    function fetchChats() {
        const storedChats = localStorage.getItem("chats");
        if (!storedChats) {
            throw new Error("Something went wrong.");
        }
        setChats(JSON.parse(storedChats));
    }

    useEffect(() => {
        if (!currentUser) {
            return;
        }
        fetchChats();
    }, [currentUser]);

    // simulating a call to a backend API to save/update the user's chats
    useEffect(() => {
        localStorage.setItem("chats", JSON.stringify(chats));
    }, [chats]);

    function deleteChat(id) {
        setChats(prev => prev.filter(chat => chat.id != id));
        localStorage.removeItem(id);
    }

    return (
        <ChatListContext value={{ chats, setChats, deleteChat }}>
            {children}
        </ChatListContext>
    )
}
