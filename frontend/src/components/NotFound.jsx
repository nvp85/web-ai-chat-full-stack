import { Link } from 'react-router';

export default function NotFound() {
    return (
        <div>
            <h1>404 - Not Found</h1>
            <p>The page you are looking for does not exist.</p>
            <Link to="/">Back to Home</Link>
        </div>
    );
}