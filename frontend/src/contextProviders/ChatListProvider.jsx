import { useState, createContext, useEffect, useContext } from "react";
import { useAuth } from '../hooks/useAuth';
import { getChatList, updateChatTitle, deleteChat, getChatById } from "../api/api";


export const ChatListContext = createContext();

// This context provider is responsible for passing the chat list to its consumers
export default function ChatListProvider({ children }) {
    const { currentUser, initialChats, token, authLoading } = useAuth();
    const [chats, setChats] = useState(null);
    const [chatsLoading, setChatsLoading] = useState(authLoading);

    // This function is for fetching the chat list
    // error handling is on the consumers
    async function fetchChats() {
        setChatsLoading(true);
        const chatData = await getChatList(token);
        setChats(chatData);
        setChatsLoading(false);
    }

    // if the auth provider has just loaded initial data then 
    // set the chats
    useEffect(() => {
        if (currentUser && !authLoading) {
            setChats(initialChats);
            setChatsLoading(false);
        }
    }, [authLoading]);

    return (
        <ChatListContext value={{ chats, setChats, fetchChats, chatsLoading }}>
            {children}
        </ChatListContext>
    )
}
