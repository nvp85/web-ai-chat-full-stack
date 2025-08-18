
// In this file there are functions for making API calls to the backend
// that are used across the app

// base url
const base_url = "http://localhost:8080";

// API request - fetch wrapper (throws common errors)
async function APIrequest(url, method, bodyObj, token) {
    const authHeaders = token ? { 'Authorization': token } : {};
    const body = bodyObj ? { body: JSON.stringify(bodyObj) } : {};
    const response = await fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
            ...authHeaders
        },
        ...body
    });
    if (response.status === 400) {
        throw new Error("Bad request.");
    }
    if (response.status === 401) {
        throw new Error("Invalid credentials.");
    }
    if (response.status === 404) {
        throw new Error("Not found");
    }
    if (response.status === 503) {
        throw new Error("API is currently unavailable, please try again later.");
    }
    return response;
}

// Auth

// login user: get a token 
// creds = {username, password}
export async function getAuthToken(creds) {
    const response = await APIrequest(base_url + "/auth/login", "post", creds, null);
    if (!response.ok) {
        throw new Error("Something went wrong.");
    }
    const data = await response.json();
    return data.accessToken.token;
}

const api_url = base_url + "/api";

// User

// fetch user's data - initial user's data including the list of chats
// gets fetched when the user/auth provider mounts 
export async function getUserData(token) {
    const response = await APIrequest(api_url + "/users/me", "get", null, token);
    if (!response.ok) {
        throw new Error("Something went wrong.");
    }
    const data = await response.json();
    return data;
}

// register a new user acc
export async function createUser(newUser) {
    const response = await APIrequest(api_url + "/users", "post", newUser, null);
    if (response.status === 409) {
        throw new Error("This email is already in use.");
    }
    if (!response.ok) {
        throw new Error("Failed to register a new user.");
    }
}

// update User profile
export async function updateUserProfile(newProfile, token) {
    const response = await APIrequest(api_url + "/users/me", "put", newProfile, token);
    if (response.status === 409) {
        throw new Error("This email is already in use.");
    }
    if (!response.ok) {
        throw new Error("Failed to update the profile.");
    }
}

// Chat list

// get user's chats
export async function getChatList(token) {
    const response = await APIrequest(api_url + "/chats", "get", null, token);
    if (!response.ok) {
        throw new Error("Failed to fetch user's chat list.");
    }
    const data = await response.json();
    return data;
}

// Chat 

export async function getChatById(chatId, token) {
    const response = await APIrequest(api_url + "/chats/" + chatId, "get", null, token);
    if (!response.ok) {
        throw new Error("Failed to fetch the chat.");
    }
    const data = await response.json();
    return data;
}

// Starts a new chat,
// accepts chat and its first prompt in one obj
export async function startChat(chatCreationObj, token) {
    const response = await APIrequest(api_url + "/chats", "post", chatCreationObj, token);
    if (response.status === 409) {
        throw new Error("The chat already exists.");
    }
    if (!response.ok) {
        throw new Error("Something went wrong.");
    }
    const data = await response.json();
    return data; // returns the new chat obj and a LLM's response
}

export async function updateChatTitle(chatId, newTitle, token) {
    const response = await APIrequest(api_url + "/chats/" + chatId, "put", { title: newTitle }, token);
    if (!response.ok) {
        throw new Error("Failed to update the chat.");
    }
    const data = await response.json();
    return data; // returns the updated chat obj
}

export async function deleteChat(chatId, token) {
    const response = await APIrequest(api_url + "/chats/" + chatId, "delete", null, token);
    if (!response.ok) {
        throw new Error("Failed to delete the chat.");
    }
}

// fetches chat messages by chat ID
export async function getChatMessages(chatId, token) {
    const url = `${api_url}/chats/${chatId}/messages`;
    const response = await APIrequest(url, "get", null, token);
    if (!response.ok) {
        throw new Error("Something went wrong.");
    }
    const data = await response.json();
    return data; // returns an array of messages
}

// Messages

// sends a message in a chat
// the message is just a string here
export default async function sendMessage(chatId, message, token) {
    const url = `${api_url}/chats/${chatId}/messages`;
    const response = await APIrequest(url, "post", { content: message }, token);
    if (!response.ok) {
        throw new Error("Failed to send the message");
    }
    const data = await response.json();
    return data; // returns the LLM's response
}

// LLM
// get a list of supported LLMs (temporarily hardcoded)
export function getLLMs() {
    return [{
        id: 1,
        name: "gpt-4o-mini",
        provider: "OpenAI",
    },
    {
        id: 2,
        name: "Gemini",
        provider: "Google",
    },
    ]
}

export async function searchMessages(query, type) {
    const url = `${api_url}/messages/search?q=${query}&type=${type}`;
    const response = await APIrequest(url, "get", null, null);
    if (!response.ok) {
        throw new Error("Something went wrong.");
    }
    const data = await response.json();
    return data; // list of messages

}
