import Markdown from 'react-markdown';
import './MessageBubble.css';

export default function MessageBubble(props) {
    // displays one chat message
    const message = props.message;
    if (message.role === "developer") {
        return;
    }
    const messageClass = message.role === "user" ? "user-message" : "ai-message"; 
    return (
        <div className={`message ${messageClass}`}>
            <Markdown>{ message.content }</Markdown>
        </div>
    ) 
}