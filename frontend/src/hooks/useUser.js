import { useContext } from "react";
import { UserContext } from "../contextProviders/UserProvider";

// a hook for using the auth context
export function useUser() {
    return useContext(UserContext);
}
