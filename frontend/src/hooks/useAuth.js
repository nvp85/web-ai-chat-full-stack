import { useContext } from "react";
import { AuthContext } from "../contextProviders/AuthProvider";

// a hook for using the auth context
export function useAuth() {
    return useContext(AuthContext);
}
