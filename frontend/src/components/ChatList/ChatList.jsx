import { useState, useRef, useEffect } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';
import ChatListItem from '../ChatListItem/ChatListItem';
import Modal from '../Modal/Modal';
import { useChatList } from '../../hooks/useChatList';
import { updateChatTitle, deleteChat, getChatById } from "../../api/api";
import { PiSpinnerGap } from "react-icons/pi";
import './ChatList.css';

// displays the list of chats
// handles renaming and deletion of a chat
export default function ChatList({ currentChatId = null }) {
    // chat is an object that has a title, id, userId, and lastModified properties
    // chats is an array of chat objects 
    const { token } = useAuth();
    const { chats, setChats, fetchChats, chatsLoading } = useChatList();
    const displayedChats = chats?.toSorted((a, b) => b.lastModified - a.lastModified);
    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState(false);

    // this current chat is for the modal dialog context
    const [currChat, setCurrChat] = useState(null);
    const [error, setError] = useState("");

    // if the user was navigated away this ref will be set to true
    const navAway = useRef(false); 

    async function handleDelete(id) {
        if (id === currentChatId) {
            navigate("/");
            navAway.current = true;
        }
        try {
            // just to be sure that we don't set the state of the component that is un-mounted
            !navAway.current && setIsModalOpen(false);
            await deleteChat(id, token);
            // the chats come from the provider so it's ok to set them even in case of un-mounting
            setChats(prev => [...prev.filter(chat => chat.id != id)]);
        } catch {
            !navAway.current && setError("Failed to delete the chat.");
        }
    }

    // renames a chat
    async function renameChat(id, title) {
        title = title.trim();
        if (!title) {
            setError("Title should not be empty.");
            return;
        }
        try {
            const chat = await updateChatTitle(id, title, token);
            setChats(prev => [...prev.filter(chat => chat.id != id), chat]);
        } catch {
            setError("Failed to renameChat the chat.");
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

    if (!chats && chatsLoading) {
        return (
            <div id="chat-list">
                <p>Loading the chats <PiSpinnerGap className="spinner" /></p>
            </div>
        )
    }

    return (
        <div id="chat-list">
            <h3>Your chats</h3>
            <ul>
                {displayedChats?.length > 0
                    ? displayedChats.map(chat => <ChatListItem chat={chat} key={chat.id} deleteChat={confirmDelete} renameChat={renameChat} />)
                    : <li style={{ textAlign: "center" }}>The chat list is empty</li>
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