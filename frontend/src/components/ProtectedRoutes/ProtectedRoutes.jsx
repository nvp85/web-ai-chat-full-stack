import { Outlet, Navigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';


export default function ProtectedRoutes() {
    const user = useAuth();
    
    if (user.authLoading) {
        return (
            <div>
                <p>Loding user data...</p>
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