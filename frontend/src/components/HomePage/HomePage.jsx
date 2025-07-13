import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router';
import ChatList from "../ChatList/ChatList";
import ChatTextarea from "../ChatTextarea/ChatTextarea";
import { useUser } from '../../hooks/useUser';
import './HomePage.css';
import Modal from '../Modal/Modal';
import { useChatList } from '../../hooks/useChatList';
import bot from '../../assets/chat-bot.png';


export default function HomePage() {
    // dispays the side bar with the list of chats on the left side
    // and a chat starter form on the right side
    // when user starts a new chat it navigates the user to an individual chat route
    const manageUser = useUser();
    const { chats, setChats } = useChatList();
    const [error, setError] = useState("");
    const navigate = useNavigate();


    async function startNewChat(userInput) {
        if (!userInput.trim()) {
            setError("Your message should not be empty.");
            return;
        }
        const chatId = crypto.randomUUID();
        const instructionMessage = {
            role: "developer",
            content: "Be succinct. Answer in 3-5 sentences."
        };
        // TODO: generate a title (probably as a separate convo)
        // untitled chat for now
        const chat = {
            id: chatId,
            userId: manageUser.currentUser.id,
            title: "untitled",
            lastModified: Date.now()
        };
        // the new chat goes to local storage
        setChats(prev => [...prev, chat]);

        // sets the value before navigating - it will be available on the chat page
        // and it will subscribe to its changes
        localStorage.setItem(chat.id, JSON.stringify([instructionMessage]));
        // passes the first message to the chat page that will send it
        navigate(`chats/${chatId}`, { state: { firstMessage: userInput } });
    }
    if (!manageUser.currentUser) {
        return (
            <div id="landing">

                <div id="landing-pic"><img src={bot} alt="A cartoonish picture of a robot" /></div>
                <h1>Welcome to my demo project</h1>
                <h2>A minimalistic web interface for chatting with an LLM</h2>
                <p>To try it out please <Link to="login">login</Link> as a demo user or <Link to="register">create</Link> a new account.</p>
            </div>
        )
    }

    return (
        <div className="two-column-container">
            <div>
                <ChatList chats={chats} />
            </div>
            <div id="right-column">
                <ChatTextarea handleClick={startNewChat} />
                {
                    error &&
                    <Modal onClose={() => setError("")} btnText='Close'>
                        <h3>Error</h3>
                        <p className='red-text'>{error}</p>
                    </Modal>
                }
            </div>
        </div>
    )
}