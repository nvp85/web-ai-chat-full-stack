import { useState, useEffect, useRef } from "react";
import ChatList from "../ChatList/ChatList";
import { Link, useNavigate, useParams, useLocation } from 'react-router';
import MessageBubble from "../MessageBubble/MessageBubble";
import ChatTextarea from "../ChatTextarea/ChatTextarea";
import './ChatPage.css';
import { useAuth } from '../../hooks/useAuth';
import sendMessage from "../../api/api";
import { PiSpinnerGap } from "react-icons/pi";
import Modal from '../Modal/Modal';
import NotFound from "../NotFound";
import { useChatList } from "../../hooks/useChatList";
import { startChat } from "../../api/api";

// displays a side bar with the chat list and an individual chat on the right
// manages the chat
export default function ChatPage() {
	const { token } = useAuth();
	const { chats, setChats } = useChatList();
	const firstMessage = location.state?.firstMessage ? {content: location.state?.firstMessage, role: "user"} : null;
	const location = useLocation();
	const [loading, setLoading] = useState(location.state?.firstMessage ? true : false);
	const [error, setError] = useState("");
	const chatBottom = useRef();

	// extracts uuid of the chat
	const { id } = useParams();

	const [messages, setMessages] = useState(firstMessage ? [firstMessage] : null);

	let chat = null;
	try {
		chat = chats.find(chat => chat.id == id);
		if (!chat || currentUser.id != chat.userId) {
			throw new Error("No chat was found.");
		}
	} catch {
		return <NotFound />
	}

	useEffect(async () => {
		if (location.state?.firstMessage && messages === null) {
			let newChat = {
				chat: {
					id: id,
					llModel: {id: 1}
				},
				firstPrompt: firstMessage
			}
			try {
				newChat = await startChat(newChat, token);
				setChats(prev => [...prev.filter(chat => chat.id != id), newChat.chat]); 
				setMessages(prev => [...prev, newChat.response]);
			} catch {
				setError("Something went wrong. Failed to start the chat.");
				setLoading(false);
			} finally {
				window.history.replaceState({}, '');
			}
		}
	}, []);


	if (messages.length && ["assistant", "model"].includes(messages.at(-1).role) && loading) {
		setLoading(false);
	}

	async function handleSubmit(userInput) {
		if (!userInput.trim()) {
			setError("Your message should not be empty.");
			return;
		}
		const newMessage = {
				role: "user",
				content: userInput,
			};
		try {
			setMessages(prev => [...prev, newMessage]);
			setLoading(true);
			const response = await sendMessage(id, newMessage, token);
			setMessages(prev => [
				...prev,
				response
			]);
		} catch {
			setError("Something went wrong. Response wasn't generated.");
		} finally {
			setLoading(false);
		}
	}
	// scroll to the bottom of the chat
	useEffect(() => {
		chatBottom.current.scrollIntoView({ behavior: 'smooth' });
	}, [loading]);


	return (
		<div className="two-column-container">
			<div>
				<ChatList currentChatId={id} />
				<p><Link to="/">Start new chat</Link></p>
			</div>
			<div id="chat-container">
				<div id="chat-title"><span className="bold-text">Title:</span> {chat.title}</div>
				<div id="chatbox">
					{messages.map((message, index) => <MessageBubble message={message} key={index} />)}
					{loading
						&& <p>generating response <PiSpinnerGap className="spinner" /></p>}
					{
						error &&
						<Modal onClose={() => setError("")} btnText='Close'>
							<h3>Error</h3>
							<p className='red-text'>{error}</p>
						</Modal>
					}
					<div ref={chatBottom} />
				</div>
				<div id="chat-input-box">
					<ChatTextarea handleClick={handleSubmit} />
				</div>
			</div>
		</div>
	)
}