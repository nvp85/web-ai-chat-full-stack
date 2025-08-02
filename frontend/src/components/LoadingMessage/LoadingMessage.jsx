import './LoadingMessage.css';
import { PiSpinnerGap } from "react-icons/pi";

// This component displays a text message with a spinner in the middle of the screen
// to indicate that an API call is in progress
export default function LoadingMessage(props) {
    return (
        <div className='loading-overlay'>
            <div className='loading-message'>
                <p>{props.text} <PiSpinnerGap className="spinner" /></p>
            </div>
        </div>
    )
}