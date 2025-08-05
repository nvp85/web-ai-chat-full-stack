import { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router';
import ChatList from "../ChatList/ChatList";
import ChatTextarea from "../ChatTextarea/ChatTextarea";
import './HomePage.css';
import Modal from '../Modal/Modal';
import { useChatList } from '../../hooks/useChatList';
import bot from '../../assets/chat-bot.png';
import { useAuth } from '../../hooks/useAuth';
import { getLLMs } from '../../api/api';


export default function HomePage() {
    // dispays the side bar with the list of chats on the left side
    // and a chat starter form on the right side
    // when user starts a new chat it navigates the user to an individual chat route
    const { token, currentUser, authLoading } = useAuth();
    const { chats, setChats } = useChatList();
    const [error, setError] = useState("");
    const navigate = useNavigate();

    const llms = getLLMs();
    const [selectedLlmId, setSelectedLlmId] = useState(1);
    const llmsJSX = llms.map(llm => <option key={llm.id} value={llm.id}>{`${llm.name}, ${llm.provider}`}</option>);

    async function startNewChat(userInput) {
        if (!userInput.trim()) {
            setError("Your message should not be empty.");
            return;
        }
        const chatId = crypto.randomUUID();
        // temporary chat object that will be replaced by the returned from the backend chat
        const chat = {
            id: chatId,
            title: "untitled",
            lastModified: Date.now(),
            llModel: llms[selectedLlmId - 1]
        };
        setChats(prev => [...prev, chat]);
        // passes the first message to the chat page that will send it
        navigate(`chats/${chatId}`, { state: { firstMessage: userInput } });
    }

    if (authLoading) {
        return (
            <div>
                <p>Loding user data...</p>
            </div>
        )
    }
    if (!currentUser) {
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
                <select id="llm-select" name={selectedLlmId} value={selectedLlmId} onChange={(e) => setSelectedLlmId(e.target.value)}>
                    {llmsJSX}
                </select>
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