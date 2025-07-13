import { ChatListContext } from "../contextProviders/ChatListProvider";
import { useContext } from "react";

export function useChatList() {
    return useContext(ChatListContext);
}