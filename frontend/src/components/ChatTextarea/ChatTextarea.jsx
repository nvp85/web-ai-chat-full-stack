import { useState } from "react";
import './ChatTextarea.css';


// Reusable textarea component with a submit button
// It is used by HomePage and ChatPage components to display the textbox for a message
export default function ChatTextarea(props) {
    const handleClick = props.handleClick;
    const [userInput, setUserInput] = useState("");
    const [ loading, setLoading ] = useState();

    // actual logic is in the parent components
    async function handleSubmit(e) {
        setLoading(true);
        e.preventDefault();
        setUserInput("");
        await handleClick(userInput);
        setLoading(false);
    }
    return (
        <form className="input-container" onSubmit={handleSubmit}>
            <textarea
                id="chat-textarea"
                name='userInput'
                value={userInput}
                onChange={(e) => setUserInput(e.target.value)}
                placeholder="Ask anything..."
                maxLength="1000"
                required>
            </textarea>
            <button type="submit" disabled={loading}>Send</button>
        </form>
    )
}