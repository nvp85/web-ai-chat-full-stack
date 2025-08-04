import { Outlet, Navigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';
import { PiSpinnerGap } from "react-icons/pi";

// protects the routes that require authorization 
export default function ProtectedRoutes() {
    const user = useAuth();
    
    // displays the loading message instead of the children
    // if the user data is still loading
    if (user.authLoading) {
        return (
            <div>
                <p>Loading user data <PiSpinnerGap className="spinner" /></p>
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