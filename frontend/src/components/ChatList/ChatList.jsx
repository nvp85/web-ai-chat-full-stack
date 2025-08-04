import { useState } from 'react';
import { useNavigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';
import ChatListItem from '../ChatListItem/ChatListItem';
import Modal from '../Modal/Modal';
import { useChatList } from '../../hooks/useChatList';
import { updateChatTitle, deleteChat } from "../../api/api";
import { PiSpinnerGap } from "react-icons/pi";
import './ChatList.css';
import LoadingMessage from '../LoadingMessage/LoadingMessage';

// This component displays the list of chats,
// handles renaming and deletion of a chat
export default function ChatList({ currentChatId = null }) {
    // chat is an object that has a title, id, llModel, and lastModified properties
    // chats is an array of chat objects 
    const { token, handleUnauthorized } = useAuth();
    const { chats, setChats, fetchChats, chatsLoading, chatsError, setChatsError } = useChatList();
    const displayedChats = chats?.toSorted((a, b) => b.lastModified - a.lastModified);
    const navigate = useNavigate();
    const [isModalOpen, setIsModalOpen] = useState(false);
    // the text for the loading message that is displayed when an API call is in progress
    const [loadingText, setLoadingText] = useState(null);

    // this current chat is for the modal dialog context
    const [currChat, setCurrChat] = useState(null);

    const [error, setError] = useState("");

    // sends the chat deletion request
    async function handleDelete(id) {
        setIsModalOpen(false);
        // try to delete the chat
        try {
            setLoadingText("Deleting the chat");
            await deleteChat(id, token);
            // the chats come from the provider so it's ok to set them even in case of un-mounting
            setChats(prev => [...prev.filter(chat => chat.id != id)]);
            if (id === currentChatId) {
                navigate("/");
            } else {
                setLoadingText(null);
            }
        } catch (err) {
            if (err.message === "Invalid token.") {
                handleUnauthorized(); // will navigate to the login page
                return;
            }
            setLoadingText(null);
            if (err.message === "Not found") {
                setError("The chat was not found.");
                fetchChats(); // to sync with the DB
            } else {
                setError("Failed to delete the chat.");
            }
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
            setLoadingText("Updating the chat");
            const chat = await updateChatTitle(id, title, token);
            setChats(prev => [...prev.filter(chat => chat.id != id), chat]);
        } catch (err) {
            if (err.message === "Invalid token.") {
                handleUnauthorized(); // will navigate to the login page
                return;
            }
            if (err.message === "Not found") {
                setError("The chat was not found.");
                fetchChats(); // to sync with the DB
            } else {
                setError("Failed to update the title.");
            }
        }
        setLoadingText(null);
    }

    // opens a modal dialog to confirm deletion
    function confirmDelete(chat) {
        setCurrChat(chat);
        setIsModalOpen(true);
    }

    function closeErrorModal() {
        setError("");
        setCurrChat(null);
    }

    // returns a spinner if the chats are loading
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
                <Modal
                    onClose={() => {
                        setIsModalOpen(false);
                        setCurrChat(null);
                    }}>
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
            {
                !error && chatsError &&
                <Modal onClose={() => setChatsError(null)} btnText='Close'>
                    <h3>Error</h3>
                    <p className='red-text'>{chatsError}</p>
                </Modal>
            }
            {loadingText &&
                <LoadingMessage text={loadingText} />
            }
        </div>
    )
}