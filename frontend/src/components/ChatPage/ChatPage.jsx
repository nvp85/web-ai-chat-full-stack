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
import { startChat, getChatMessages } from "../../api/api";
import LoadingMessage from '../LoadingMessage/LoadingMessage';

// displays a side bar with the chat list and an individual chat on the right
// manages the chat
export default function ChatPage() {
	const { token, handleUnauthorized } = useAuth();
	const { chats, setChats, chatsLoading, fetchChats } = useChatList();
	const location = useLocation();
	// this useRef to make sure that a new chat request is sent only once
	const firstMessage = useRef(location.state?.firstMessage ? { content: location.state?.firstMessage, role: "user" } : null);
	const [generating, setGenerating] = useState(firstMessage.current ? true : false);
	const [error, setError] = useState("");
	const chatBottom = useRef();
	const [loading, setLoading] = useState(firstMessage.current ? false : true);
	const navigate = useNavigate();
	// always clear the state so a new chat will not be created more than once
	// although there is already useRef for this purpose 
	// it won't hurt to clear out the location state, too
	window.history.replaceState({}, '');

	// extracts uuid of the chat
	const { id } = useParams();

	const [messages, setMessages] = useState(firstMessage.current ? [firstMessage.current] : null);

	// If the messages fetch returns not found
	// this flag will be set to true
	const [notFound, setNotFound] = useState(false);

	const chat = chats?.find(chat => chat.id == id) || null;

	useEffect(() => {
		async function fetchMessages() {
			setLoading(true);
			try {
				//throw new Error("Invalid token.");
				const messagesData = await getChatMessages(id, token);
				setMessages(messagesData);
				 // if the chat exists in the DB but is not on the list for some reason
				if (!chat) {
					fetchChats();
				}
				setLoading(false);
			} catch (err) {
				if (err.message === "Invalid token.") {
					handleUnauthorized();
				} else if (err.message === "Not found") {
					setNotFound(true);
				} else {
					setError("Failed to fetch messages.");
				}
			} 
		}
		// it doesn't need to fetch messages if it's a new chat 
		// only new chats have the length of 1
		if (!messages || messages.length > 1) {
			fetchMessages();
		}

	}, [id]);

	useEffect(() => {
		async function sendNewChatRequest() {
			let newChat = {
				chat: {
					id: id,
					llModel: chat?.llModel || {id: 1}
				},
				firstPrompt: firstMessage.current.content
			}
			firstMessage.current = null;
			// TODO: this needs to be handled in a uniform way
			const role = newChat.chat.llModel.id === 1 ? "assistant" : "model";
			try {
				newChat = await startChat(newChat, token);
				setChats(prev => [...prev.filter(chat => chat.id != id), newChat.chat]);
				setMessages(prev => [...prev, { content: newChat.response, role }]);
			} catch {
				setError("Something went wrong. Failed to start the chat.");
				setGenerating(false);
				fetchChats(); // re-fetch the chats to remove the one that is not in the DB
			}

		}
		if (firstMessage.current && chat) {
			sendNewChatRequest();
		}
	}, []);


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
			const response = await sendMessage(id, userInput, token);
			setMessages(prev => [
				...prev,
				response
			]);
		} catch {
			setError("Something went wrong. Response wasn't generated.");
		} finally {
			setGenerating(false);
		}
	}
	// scroll to the bottom of the chat
	useEffect(() => {
		!loading && !chatsLoading && chatBottom?.current?.scrollIntoView({ behavior: 'smooth' });
	}, [generating]);


	if (chatsLoading) {
		return (
			<div>
				<p>The chats are loading...</p>
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
					<div>Loading the messages...</div>
					:
					 <>
					{messages.map((message, index) => <MessageBubble message={message} key={index} />)}
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