import { useState, useEffect, useRef } from "react";
import ChatList from "../ChatList/ChatList";
import { Link, useNavigate, useParams, useLocation } from 'react-router';
import MessageBubble from "../MessageBubble/MessageBubble";
import ChatTextarea from "../ChatTextarea/ChatTextarea";
import './ChatPage.css';
import messagesData from '../../assets/messages.json'
import { useUser } from '../../hooks/useUser';
import useSyncLocalstorage from '../../hooks/useSyncLocalstorage';
import sendMessage from "../../api/api";
import { PiSpinnerGap } from "react-icons/pi";
import Modal from '../Modal/Modal';
import NotFound from "../NotFound";
import { useChatList } from "../../hooks/useChatList";
import { generateTitle } from "../../api/api";

// displays a side bar with the chat list and an individual chat on the right
// manages the chat
export default function ChatPage() {
	const { currentUser } = useUser();
	const { chats, setChats } = useChatList();
	const location = useLocation();
	const [loading, setLoading] = useState(location.state?.firstMessage ? true : false);
	const [error, setError] = useState("");
	const chatBottom = useRef();
	let needsTitle = location.state?.firstMessage ? true : false;
	// extracts uuid of the chat
	const { id } = useParams();
	// fetch chats messages from the local storage
	const [messages, setMessages, removeMessages] = useSyncLocalstorage(id, messagesData[id] || []);

	let chat = null;
	try {
		chat = chats.find(chat => chat.id == id);
		if (!chat || currentUser.id != chat.userId) {
			throw new Error("No chat was found.");
		}
	} catch {
		return <NotFound />
	}

	useEffect(() => {
		if (location.state?.firstMessage &&
			messages.at(-1).role === "developer") {
			try {
				handleSubmit(location.state.firstMessage);
			} catch {
				setError("Something went wrong. Failed to start the chat.");
				setLoading(false);
			} finally {
				window.history.replaceState({}, '');
			}
		}
	}, [id]);

	useEffect(() => {
		if (needsTitle) {
			try {
				generateTitle(location.state?.firstMessage).then((title) => {
					setChats(prev => [...prev.filter(chat => chat.id != id), { ...chat, title: title }]);
				});
			} catch {
				// if it fails it fails.
			} finally {
				needsTitle = false;
			}
		};
	}, []);

	if (messages.length && messages.at(-1).role === "assistant" && loading) {
		setLoading(false);
	}

	async function handleSubmit(userInput) {
		if (!userInput.trim()) {
			setError("Your message should not be empty.");
			return;
		}
		const convo = [
			...messages,
			{
				role: "user",
				content: userInput,
			}
		];
		try {
			setMessages(convo);
			setChats(prev => [...prev.filter(chat => chat.id != id), { ...chat, lastModified: Date.now() }]);
			setLoading(true);
			const response = await sendMessage(convo);
			setMessages([
				...convo,
				{
					role: "assistant",
					content: response
				}
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