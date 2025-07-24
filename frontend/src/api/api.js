

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
export async function getUserData() {
    const response = await fetch(api_url + "users",
        {
            method: "get",
            headers: {
                'Content-Type': 'application/json',
                'Authorization': localStorage.getItem('auth-token')
            }   
        }
    );
    const data = await response.json();
    if (!response.ok) {
        throw new Error("Failed to fetch user data.");
    }
    return data;
}


