import { useState, createContext, useEffect, useContext } from "react";
import { useAuth } from '../hooks/useAuth';
import { getChatList, updateChatTitle, deleteChat, getChatById } from "../api/api";


export const ChatListContext = createContext();

export default function ChatListProvider({ children }) {
    const { currentUser, initialChats, token } = useAuth();
    const [chats, setChats] = useState(initialChats || null);
    const [ chatsLoading, setChatsLoading ] = useState(true);


    async function fetchChats() {
        setChatsLoading(true);
        try {
            const chatData = await getChatList(token);
            setChats(chatData);
        } catch {
// error handling
        } finally {
            setChatsLoading(false);
        }
    }

    useEffect(() => {
        if (!currentUser) {
            return;
        }
        fetchChats();
    }, [token]);


    return (
        <ChatListContext value={{ chats, setChats, fetchChats, chatsLoading }}>
            {children}
        </ChatListContext>
    )
}
