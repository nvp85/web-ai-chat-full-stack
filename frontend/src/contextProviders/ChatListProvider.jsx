import { useState, createContext, useEffect, useContext } from "react";
import { useAuth } from '../hooks/useAuth';
import { getChatList, updateChatTitle, deleteChat, getChatById } from "../api/api";


export const ChatListContext = createContext();

export default function ChatListProvider({ children }) {
    const { currentUser, initialChats, token } = useAuth();
    const [chats, setChats] = useState(initialChats || null);


    async function fetchChats() {
        try {
            const chatData = await getChatList(token);
            setChats(chatData);
        } catch {
// error handling
        }
    }

    useEffect(() => {
        if (!currentUser) {
            return;
        }
        fetchChats();
    }, [token]);


    return (
        <ChatListContext value={{ chats, setChats, fetchChats }}>
            {children}
        </ChatListContext>
    )
}
