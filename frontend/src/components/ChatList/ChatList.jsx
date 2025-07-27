import { useState } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';
import ChatListItem from '../ChatListItem/ChatListItem';
import Modal from '../Modal/Modal';
import { useChatList } from '../../hooks/useChatList';
import { updateChatTitle, deleteChat, getChatById } from "../../api/api";


export default function ChatList({ currentChatId = null }) {
    // displays the list of chats
    // handles renaming and deletion of a chat
    // chat is an object that has a title, id, userId, and lastModified properties
    // chats is an array of objects 
    const { currentUser, token } = useAuth();
    const {chats, setChats, fetchChats} = useChatList();
    const displayedChats = chats.toSorted((a, b) => b.lastModified - a.lastModified);
    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState(false);
    // this chat is for the modal dialog context
    const [currChat, setCurrChat] = useState(null);
    const [error, setError] = useState("");

    async function handleDelete(id) { 
        try {
            await deleteChat(id, token);
            setChats(prev => [...prev.filter(chat => chat.id != id)]);
        } catch {
            setError("Failed to delete the chat.");
        } finally {
            setIsModalOpen(false);
        }
    }

    async function rename(id, title) {
        title = title.trim();
        if (!title) {
            setError("Title should not be empty.");
            return;
        }
        try {
            const chat = await updateChatTitle(id, title, token);
            setChats(prev => [...prev.filter(chat => chat.id != id), chat]); 

        } catch {
            setError("Failed to rename the chat.");
        }
    }

    function confirmDelete(chat) {
        setCurrChat(chat);
        setIsModalOpen(true);
    }

    function closeErrorModal() {
        setError("");
        setCurrChat(null);
    }

    return (
        <div id="chat-list">
            <h3>Your chats</h3>
            <ul>
                {displayedChats.length > 0
                ? displayedChats.map(chat => <ChatListItem chat={chat} key={chat.id} deleteChat={confirmDelete} rename={rename} />)
                : <li style={{textAlign: "center"}}>The chat list is empty</li>
                    }
            </ul>
            {currChat && isModalOpen &&
                <Modal onClose={() => setIsModalOpen(false)}>
                    <h3>Do you want to delete this chat?</h3>
                    <p>{currChat.title}</p>
                    <button onClick={() => handleDelete(currChat.id)} className='delete-btn btn'>Delete</button>
                </Modal>
            }
            {
                error && 
                <Modal onClose={closeErrorModal} btnText='Close'>
                    <h3>Error</h3>
                    <p className='red-text'>{error}</p>
                </Modal>
            }
        </div>
    )
}