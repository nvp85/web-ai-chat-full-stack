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
import { startChat, getChatMessages, getChatById } from "../../api/api";

// This component displays the currently open chat and a side bar with the chat list on the left side
// manages the chat
export default function ChatPage() {
	const { token, handleUnauthorized } = useAuth();
	const { chats, setChats, chatsLoading, fetchChats } = useChatList();
	const location = useLocation();
	// this useRef is to make sure that a new chat request is sent only once
	const firstMessage = useRef(location.state?.firstMessage ? { content: location.state?.firstMessage, role: "user" } : null);
	const [generating, setGenerating] = useState(false);
	const [error, setError] = useState("");
	const chatBottom = useRef();
	const [loading, setLoading] = useState(firstMessage.current ? false : true);
	const navigate = useNavigate();

	// extracts the Chat's id from the path params
	const { id } = useParams();

	// the current chat's messages
	const [messages, setMessages] = useState(firstMessage.current ? [firstMessage.current] : null);

	// If the messages fetch returns not found
	// this flag will be set to true
	const [notFound, setNotFound] = useState(false);

	const chat = chats?.find(chat => chat.id == id) || null;

	// fetches the chat's messages
	useEffect(() => {
		async function fetchMessages() {
			setLoading(true);
			try {
				const messagesData = await getChatMessages(id, token);
				setMessages(messagesData);
				// if the chat exists in the DB but is not on the list for some reason
				if (!chat) {
					await fetchChats(); // sync with the DB
				}
			} catch (err) {
				if (err.message === "Invalid credentials.") {
					handleUnauthorized();
				} else if (err.message === "Not found") {
					await fetchChats();
					setNotFound(true);
				} else {
					setError("Failed to fetch the messages: " + err.message);
				}
			} finally {
				setLoading(false);
			}
		}
		// it doesn't need to fetch messages if it's a new chat 
		// only new chats have the length of 1
		if (!messages || messages.length > 1) {
			fetchMessages();
		}
	}, [id]); // runs if chat ID changes

	// if there is a first message then a new chat should be created on the backend
	useEffect(() => {
		async function sendNewChatRequest() {
			let newChat = {
				chat: {
					id: id,
					llModel: chat?.llModel
				},
				message: firstMessage.current
			}
			firstMessage.current = null;
			window.history.replaceState({}, ''); // removes the first message from the history
			setGenerating(true);
			try { // sends a new chat request
				// new chat is a DTO obj {chat, message}
				newChat = await startChat(newChat, token);
				setChats(prev => [...prev.filter(chat => chat.id != id), newChat.chat]);
				setMessages(prev => [...prev, newChat.message]);
			} catch (err) {
				if (err.message === "Invalid credentials.") {
					handleUnauthorized(); // the user will be navigated to /login
					return;
				}
				setError("Failed to start the chat: " + err.message);
				setChats(prev => prev.filter(chat => chat.id != id));
			} finally {
				setGenerating(false);
			}
		}
		if (firstMessage.current && chat) {
			sendNewChatRequest();
		}
	}, []);

	// double checks that generating is set to false if the AI response is received
	if (messages?.length && ["assistant", "model"].includes(messages.at(-1).role) && generating) {
		setGenerating(false);
	}

	// handles submit of a new message
	async function submitMessage(userInput) {
		// make sure that nothing is loading or generating (no calls are in progress)
		if (generating || loading) {
			return;
		}
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
			setGenerating(true);
			// the response is a chatDTO
			const response = await sendMessage(id, userInput, token);
			setMessages(prev => [
				...prev,
				response.message
			]);
			setChats(prev => [...prev.filter(c => c.id !== id), response.chat]);
		} catch (err) {
			if (err.message === "Invalid credentials.") {
				handleUnauthorized(); // the user will be navigated to /login
				return;
			}
			setError("Response wasn't generated. " + err.message);
		} finally {
			setGenerating(false);
		}
	}
	// scroll to the bottom of the chat
	useEffect(() => {
		!loading && !chatsLoading && chatBottom?.current?.scrollIntoView({ behavior: 'smooth' });
	}, [generating]);


	if (!chats && chatsLoading) {
		return (
			<div>
				<p>The chats are loading <PiSpinnerGap className="spinner" /></p>
			</div>
		)
	}

	if (notFound) {
		return <NotFound />
	}

	return (
		<div className="two-column-container">
			<div>
				<ChatList currentChatId={id} />
				<p><Link to="/">Start new chat</Link></p>
			</div>
			<div id="chat-container">
				<div id="chat-title"><span className="bold-text">{chat?.llModel.name}:</span> {chat?.title}</div>
				<div id="chat-model"><span className="bold-text">{chat?.llModel.name}</span></div>
				<div id="chatbox">
					{loading ?
						<div>Loading the messages <PiSpinnerGap className="spinner" /></div>
						:
						<>
							{messages?.map((message, index) => <MessageBubble message={message} key={index} />)}
							{generating
								&& <p>generating response <PiSpinnerGap className="spinner" /></p>}
							{
								error &&
								<Modal
									onClose={() => {
										setError("");
										if (error.includes("Failed to start the chat")) {
											navigate("/");
										}
									}}
									btnText='Close'>
									<h3>Error</h3>
									<p className='red-text'>{error}</p>
								</Modal>
							}
							<div ref={chatBottom} />
						</>}
				</div>
				<div id="chat-input-box">
					<ChatTextarea handleClick={submitMessage} />
				</div>
			</div>
		</div>
	)
}