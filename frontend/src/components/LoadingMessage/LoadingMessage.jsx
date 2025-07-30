import './LoadingMessage.css';
import { PiSpinnerGap } from "react-icons/pi";

export default function LoadingMessage(props) {
    return (
        <div className='loading overlay'>
            <div className='loading-message'>
                <p>{props.text} <PiSpinnerGap className="spinner" /></p>
            </div>
        </div>
    )
}