import { Outlet, Navigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';

// protects the routes that require authorization 
export default function ProtectedRoutes() {
    const user = useAuth();
    
    // displays the loading message instead of the children
    if (user.authLoading) {
        return (
            <div>
                <p>Loading user data...</p>
            </div>
        )
    } 

    if (!user.currentUser) {
        return <Navigate to="login" />
    }

    return (
        <Outlet />
    )
}