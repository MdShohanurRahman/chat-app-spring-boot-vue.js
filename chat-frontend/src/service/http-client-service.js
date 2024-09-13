import axios  from "axios";

const api = axios.create({
    baseURL: '/api',
    headers: {
        'Content-Type': 'application/json'
    }
})

// users
export const login = (username) => {
    return api.post("/users/login", {username: username});
}

export const logout = (id) => {
    return api.post(`/users/logout/${id}`,);
}

export const getAllUsers = () => {
    return api.get("/users");
}

// chat
export const getChatHistory = (senderUsername, recipientUserName) => {
    return api.get(`/chat/history/sender/${senderUsername}/recipient${recipientUserName}`);
}