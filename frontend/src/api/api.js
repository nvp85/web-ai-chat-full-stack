

// TODO: check if the convo contains less tokens than the models context window size
export default async function sendMessage(messages) {

}

export async function generateTitle(text) {

}

// base url
const base_url = "http://localhost:8080";

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
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid credentials.");
    }
    if (!response.ok) {
        console.log(response.status)
        throw new Error("Something went wrong.");
    }
    return data.accessToken.token;
}

const api_url = base_url + "/api";

// fetch user's data
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
    const data = await response.json();
    if (response.status === 401) {
        throw new Error("Invalid token.")
    }
    if (!response.ok) {
        throw new Error("Failed to fetch user data.");
    }
    return data;
}

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
    console.log(response);
    if (!response.ok) {
        throw new Error("Failed to register a new user.");
    }
}

// getAuthToken({ username: "demouser@example.com", password: "qwerty" })
//     .then(getUserData).then(console.log)

// let testUser = {
// 	name: "demoUser",
// 	email: "test@example.co",
// 	password: "qwertyD1*!vvv"
// }
// createUser(testUser)