import { Outlet, Navigate } from 'react-router';
import { useAuth } from '../../hooks/useAuth';


export default function ProtectedRoutes() {
    const user = useAuth();

    if (!user.currentUser) {
        return <Navigate to="login" />
    }

    return (
        <Outlet />
    )
}