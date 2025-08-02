
// base url
const base_url = "http://localhost:8080";


// Auth

// login user: get a token 
// creds = {username, password}
export async function getAuthToken(creds) {
    const response = await fetch(base_url + "/auth/login",
        {
            method: "post",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify(creds)
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid credentials.");
    }
    if (!response.ok) {
        console.log(response.status)
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
    const response = await fetch(api_url + "/users/me",
        {
            method: "get",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token // localStorage.getItem('auth-token')
            }
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid token.")
    }
    if (!response.ok) {
        throw new Error("Failed to fetch user data.");
    }
    const data = await response.json();
    return data;
}

// register a new user acc
export async function createUser(newUser) {
    const response = await fetch(api_url + "/users",
        {
            method: "post",
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(newUser)
        }
    );
    if (response.status === 409) {
        throw new Error("This email is already in use.");
    }
    if (!response.ok) {
        throw new Error("Failed to register a new user.");
    }
}

// update User profile
export async function updateUserProfile(newProfile, token) {
    const response = await fetch(api_url + "/users/me",
        {
            method: "put",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify(newProfile)
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid token.");
    }
    if (response.status === 409) {
        throw new Error("This email is already in use.");
    }
    if (!response.ok) {
        throw new Error("Failed to update the profile.");
    }
}

// Chat list

export async function getChatList(token) {
    const response = await fetch(api_url + "/chats",
        {
            method: "get",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            }
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid token.");
    }
    if (!response.ok) {
        throw new Error("Failed to fetch user's chat list.");
    }
    const data = await response.json();
    return data;
}

// Chat 

export async function getChatById(chatId, token) {
    const response = await fetch(api_url + "/chats/" + chatId,
        {
            method: "get",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            }
        }
    );
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid token.")
    }
    if (!response.ok) {
        throw new Error("Failed to fetch the chat.");
    }
    return data;
}

// accepts chat and its first prompt
export async function startChat(chatCreationObj, token) {
    const response = await fetch(api_url + "/chats",
        {
            method: "post",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify(chatCreationObj)
        }
    );
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid token.");
    }
    if (response.status === 400) {
        throw new Error("Bad request.");
    }
    if (response.status === 409) {
        throw new Error("The chat already exists.");
    }
    if (!response.ok) {
        throw new Error("Failed to create a new chat.");
    }
    return data; // returns the new chat obj and a LLM's response
}

export async function updateChatTitle(chatId, newTitle, token) {
    const response = await fetch(api_url + "/chats/" + chatId,
        {
            method: "put",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({ title: newTitle })
        }
    );
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid token.");
    } else if (response.status === 400) {
        throw new Error("Bad request.");
    } else if (!response.ok) {
        throw new Error("Failed to update the chat.");
    }
    return data; // returns the updated chat obj
}

export async function deleteChat(chatId, token) {
    const response = await fetch(api_url + "/chats/" + chatId,
        {
            method: "delete",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid token.");
    } else if (response.status === 400) {
        throw new Error("Bad request.");
    } else if (!response.ok) {
        throw new Error("Failed to update the chat.");
    }
}

export async function getChatMessages(chatId, token) {
    const url = `${api_url}/chats/${chatId}/messages`;
    const response = await fetch(url,
        {
            method: "get",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
        }
    );
    if (response.status === 401) {
        throw new Error("Invalid token.");
    } else if (response.status === 400) {
        throw new Error("Bad request.");
    } else if (response.status === 404) {
        throw new Error("Not found");
    } else if (!response.ok) {
        throw new Error("Failed to fetch the chat messages.");
    }
    const data = await response.json();
    return data; // returns an array of messages
}

// Messages

// the message is just a string here
export default async function sendMessage(chatId, message, token) {
    const url = `${api_url}/chats/${chatId}/messages`;
    const response = await fetch(url,
        {
            method: "post",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': token
            },
            body: JSON.stringify({ content: message })
        }
    );
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid token.");
    } else if (response.status === 400) {
        throw new Error("Bad request.");
    } else if (!response.ok) {
        throw new Error("Failed to send the message");
    }
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




// let testUser = {
// 	name: "demoUser",
// 	email: "test@example.co",
// 	password: "qwertyD1*!vvv"
// }
// createUser(testUser)

//getChatById("83ee66e5-5650-41a1-bda6-660eaa2f1a8c", token).then(console.log);
// getChatList(token).then(console.log);

// let newChat = {
// 	"chat": {
// 	"id": "0eb9e3c4-47ba-48b3-8abd-55fbf43b9cba",
// 	"llModel": {
// 		"id": 2
// 	}
// 	},
// 	"firstPrompt": "Describe the primary responsibilities of a Software Developer on a team."
// }

// startChat(newChat, token).then(console.log);
// updateChatTitle("0eb9e3c4-47ba-48b3-8abd-55fbf43b9cba", "Responsibilities of a software dev", token).then(console.log);
// deleteChat("76e4bda5-817c-4acf-9608-04f6d01feae6", token); 
// getChatList(token).then(console.log); - there was a phantom read? I could see the chat that has just been deleted!
// getChatMessages("0eb9e3c4-47ba-48b3-8abd-55fbf43b9cba", token).then(console.log);
// sendMessage("0eb9e3c4-47ba-48b3-8abd-55fbf43b9cba", "What do designers do?", token).then(console.log);
// getChatMessages("1f3afa62-7f29-4eee-b677-1fc8ec91406c", token).then(console.log);
// sendMessage("1f3afa62-7f29-4eee-b677-1fc8ec91406c", "Tell about CBT.", token).then(console.log);
