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
	const { token } = useAuth();
	const { chats, setChats, chatsLoading } = useChatList();
	const location = useLocation();
	// this useRef to make sure that a new chat request is sent only once
	const firstMessage = useRef(location.state?.firstMessage ? { content: location.state?.firstMessage, role: "user" } : null);
	const [generating, setGenerating] = useState(firstMessage.current ? true : false);
	const [error, setError] = useState("");
	const chatBottom = useRef();
	const [ loading, setLoading ] = useState(firstMessage.current ? false : true);
	// always clear the state so a new chat will not be created more than once
	// although there is already useRef for this purpose 
	// it won't hurt to clear out the location state, too
	window.history.replaceState({}, '');

	// extracts uuid of the chat
	const { id } = useParams();

	const [messages, setMessages] = useState(firstMessage.current ? [firstMessage.current] : null);

	let chat = null;
	try {
		chat = chats.find(chat => chat.id == id);
		if (!chat) {
			throw new Error("No chat was found.");
		}
	} catch {
		return <NotFound />
	}

	useEffect(() => {
		async function fetchMessages() {
			setLoading(true);
			try {
				const messagesData = await getChatMessages(id, token);
				setMessages(messagesData);
			} catch(err) {
				console.log(err.message);
				setError("Failed to fetch messages.");
			} finally {
				setLoading(false);
			}
		}
		// it doesn't need to fetch messages if it's a new chat 
		// only new chats have the length of 1
		if (!messages || messages.length > 1) {
			fetchMessages();
		}
		
	}, [id]);

	useEffect(() => {
		console.log("running the use effect");
		(async function () {
			if (firstMessage.current) {
				console.log("trying to send the request");
				
				console.log(location.state);
				let newChat = {
					chat: {
						id: id,
						llModel: { id: 1 }
					},
					firstPrompt: firstMessage.current.content
				}
				firstMessage.current = null;
				// TODO: this needs to be handled in a uniform way
				const role = newChat.chat.llModel.id === 1 ? "assistant" : "model";
				try {
					newChat = await startChat(newChat, token);
					setChats(prev => [...prev.filter(chat => chat.id != id), newChat.chat]);
					setMessages(prev => [...prev, {content: newChat.response, role}]);
				} catch {
					setError("Something went wrong. Failed to start the chat.");
					setGenerating(false);
				}
			}
		})();
		return () => {
			console.log("run cleanup");
			//window.history.replaceState({}, '');
		};
	}, []);


	if (messages?.length && ["assistant", "model"].includes(messages.at(-1).role) && generating) {
		setGenerating(false);
	}

	// handles submit of a new message
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
		!loading && chatBottom.current.scrollIntoView({ behavior: 'smooth' });
	}, [generating]);

	if (loading) {
		return(	
		<div>
			<p>loading</p>
		</div>
		)
	}


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
					{generating
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