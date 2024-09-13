<template>
    <div class="chat-container d-flex vh-100">
        <!-- User List (Left Sidebar) -->
        <div class="user-list d-flex flex-column p-3 bg-light border-end" style="width: 250px;">
            <h4 class="mb-3 text-uppercase text-decoration-underline text-muted">{{ loggedInUsername }}</h4>

            <!-- User List -->
            <div class="flex-grow-1 overflow-auto">
                <div
                    v-for="user in users"
                    :key="user.username"
                    class="user-item p-2 mb-2 border rounded d-flex justify-content-between align-items-center cursor-pointer"
                    :aria-disabled="!websocketConnected"
                    @click="selectUser(user)"
                    :class="{'bg-info text-white': user.username === selectedRecipient?.username}">
                    <div>
                        <span>{{ user.username }}</span>
                        <span v-if="user?.unreadMessage" class="badge bg-danger ms-2">{{ user?.unreadMessage }}</span>
                        <small class="d-block text-muted">{{ user.lastLogin }}</small>
                    </div>
                    <div>
                        <span v-if="user.isOnline" class="badge bg-success">Online</span>
                        <span v-if="!user.isOnline" class="badge bg-secondary">Offline</span>
                    </div>
                </div>
            </div>

            <!-- Logout Button (Over User List Sidebar) -->
            <div class="pt-3">
                <button @click="logout" class="btn btn-dark w-100">Logout</button>
            </div>
        </div>

        <!-- Chat Panel -->
        <div class="chat-panel flex-grow-1 d-flex flex-column">
            <div class="chat-header p-3 bg-white shadow-sm text-center">
                <h5>{{ selectedRecipient ? selectedRecipient.username : 'Select a user to chat' }}</h5>
            </div>

            <!-- Chat Body -->
            <div class="chat-body p-3 flex-grow-1 position-relative">
                <!-- Loader component when data is being fetched -->
                <ChatLoader v-if="isLoading" />

                <div v-if="!isLoading && selectedRecipient">
                    <div v-for="message in chatHistory" :key="message.id" class="message">
                        <!-- Differentiate messages by sender -->
                        <div :class="{'text-end': message?.sender?.username === loggedInUsername}">
                            <div :class="message?.sender?.username === loggedInUsername ? 'bg-primary text-white p-2 rounded d-inline-block' : 'bg-secondary text-white p-2 rounded d-inline-block'">
                                {{ message.content }}
                            </div>
                            <small class="d-block text-muted my-1">
                                {{ message.timestamp }}
                                <!-- Message Status (seen, delivered, sent) -->
                                <span v-if="message?.sender?.username === loggedInUsername" class="ms-2">
                                    <i v-if="message?.seen" class="bi bi-check2-all text-primary"></i> <!-- Blue double tick (seen) -->
                                    <i v-else-if="message?.delivered" class="bi bi-check2-all text-secondary"></i> <!-- Gray double tick (delivered) -->
                                    <i v-else class="bi bi-check2 text-muted"></i> <!-- Single gray tick (sent) -->
                                </span>
                            </small>
                        </div>
                    </div>
                </div>
                <div v-else-if="!isLoading" class="text-muted text-center mt-5">No user selected.</div>
            </div>

            <div class="chat-footer p-3 bg-light">
                <input :disabled="!selectedRecipient || !websocketConnected" v-model="newMessage" type="text" class="form-control" placeholder="Type a message..." @keyup.enter="sendMessage" />
            </div>
        </div>
    </div>
</template>

<script>
import Stomp from 'stompjs';
import SockJS from 'sockjs-client';
import {getAllUsers, getChatHistory, logout} from "@/service/http-client-service";
import ChatLoader from "@/components/ChatLoader.vue";

export default {
    components: {ChatLoader},
    data() {
        return {
            websocketConnected: false,
            stompClient: null,
            loggedInUser: null,
            loggedInUsername: null,
            users: [],
            selectedRecipient: null,
            chatHistory: [],
            newMessage: '',
            isLoading: false
        };
    },
    methods: {
        // Load all users except the logged-in user
        loadUsers() {
            getAllUsers().then(response => {
                this.users = response?.data
                    ?.filter(item => item.username !== this.loggedInUsername)
                    ?.map(user => ({...user, unreadMessage: 0}));
            });
        },
        // Handle selecting a user for chatting
        selectUser(user) {
            if (!this.websocketConnected) {
                console.log("Please wait, WebSocket not connected yet.");
                return;
            }
            this.selectedRecipient = user;
            this.loadChatHistory();
            this.sendMarkedAsSeen();
        },
        // Mark messages as seen for the selected recipient
        sendMarkedAsSeen() {
            if (this.selectedRecipient) {
                this.stompClient.send('/app/chat.markSeen', {}, JSON.stringify({
                    seen: true,
                    senderUsername: this.loggedInUsername,
                    recipientUsername: this.selectedRecipient.username,
                }));
            }
        },
        // Reset unread message count for the selected recipient
        resetUnreadMessage(username) {
            const recipient = this.users.find(user => user.username === username);
            if (recipient) {
                recipient.unreadMessage = 0;
            }
        },
        // Load chat history between the logged-in user and the selected recipient
        loadChatHistory() {
            this.isLoading = true;
            getChatHistory(this.loggedInUsername, this.selectedRecipient?.username)
                .then(response => {
                    this.chatHistory = response.data;
                })
                .catch(error => {
                    console.log(error);
                })
                .finally(() => {
                    this.isLoading = false;
                });
        },
        // Establish WebSocket connection and set up subscriptions
        connectToWebSocket() {
            const socket = new SockJS(`/ws?username=${this.loggedInUsername}`);
            this.stompClient = Stomp.over(socket);

            // Connect to WebSocket
            this.stompClient.connect({'X-Username': this.loggedInUsername}, (frame) => {
                this.websocketConnected = true;
                console.log('Connected to WebSocket:', frame);

                // Subscribe to receive messages for recipient
                this.stompClient.subscribe(`/user/${this.loggedInUsername}/queue/messages`, (message) => {
                    const recipientMessage = JSON.parse(message.body);
                    if (this.isCurrentChatHistory(recipientMessage.sender.username, recipientMessage.recipient.username)) {
                        this.chatHistory.push(recipientMessage);
                        this.sendMarkedAsSeen();
                    } else {
                        this.incrementUnreadMessages(recipientMessage.sender.username);
                    }
                });

                // Subscribe to receive messages for sender
                this.stompClient.subscribe('/topic/messages/' + this.loggedInUsername, (message) => {
                    const senderMessage = JSON.parse(message.body);
                    if (this.isCurrentChatHistory(senderMessage.sender.username, senderMessage.recipient.username)) {
                        // Add the sender message to chat history
                        this.chatHistory.push(senderMessage);
                    }
                });

                // Subscribe to message-seen notifications
                this.stompClient.subscribe(`/topic/messagesSeen/${this.loggedInUsername}`, (message) => {
                    const seenMessage = JSON.parse(message.body);
                    this.handleSeenMessage(seenMessage);
                });

                // Subscribe to resetUnread updates
                this.stompClient.subscribe(`/topic/resetUnread/${this.loggedInUsername}`, (message) => {
                    console.log(message)
                    const resetUnreadMessage = JSON.parse(message.body);
                    if (resetUnreadMessage.senderUsername === this.loggedInUsername) {
                        this.resetUnreadMessage(resetUnreadMessage.recipientUsername);
                    }
                });

                // Subscribe to user status updates
                this.stompClient.subscribe('/topic/userStatus', (message) => {
                    const userMessage = JSON.parse(message.body);
                    this.updateUserList(userMessage);
                });
            });
        },
        // Check if a message belongs to the current chat history
        isCurrentChatHistory(senderUsername, recipientUsername) {
            return (
                this.selectedRecipient &&
                ((senderUsername === this.loggedInUsername && recipientUsername === this.selectedRecipient.username) ||
                    (recipientUsername === this.loggedInUsername && senderUsername === this.selectedRecipient.username))
            );
        },
        // Increment unread message count for a user
        incrementUnreadMessages(username) {
            const recipient = this.users.find(user => user.username === username);
            if (recipient) {
                recipient.unreadMessage = (recipient.unreadMessage || 0) + 1;
            }
        },
        // Handle seen message notifications and update the chat history
        handleSeenMessage(seenMessage) {
            if (this.isCurrentChatHistory(seenMessage.senderUsername, seenMessage.recipientUsername)) {
                this.chatHistory = this.chatHistory.map(chat => {
                    if (chat.sender.username === seenMessage.recipientUsername) {
                        return {...chat, seen: true, delivered: true};
                    }
                    return chat;
                });
            }
        },
        // Update user list with the latest user status
        updateUserList(userMessage) {
            const existingUser = this.users.find(user => user.username === userMessage.username);
            if (userMessage.username === this.loggedInUsername && !userMessage.isOnline) {
                this.logout();
            } else if (existingUser) {
                this.users = this.users.map(user =>
                    user.username === userMessage.username ? {...user, ...userMessage} : user
                );
            } else {
                this.users.push(userMessage);
            }
        },
        // Send a message to the selected recipient
        sendMessage() {
            if (this.isLoading || !(this.newMessage && this.selectedRecipient)) return;
            const message = {
                senderUsername: this.loggedInUsername,
                recipientUsername: this.selectedRecipient.username,
                content: this.newMessage
            };
            this.stompClient.send('/app/chat.sendMessage', {}, JSON.stringify(message));
            this.newMessage = ''; // Clear the input field
        },
        // Handle user logout and disconnect WebSocket
        logout() {
            if (!this.websocketConnected) return;
            logout(this.loggedInUser?.id).then(() => {
                localStorage.removeItem("user");
                setTimeout(() => {
                    this.$router.push('/login');
                }, 200);
            });
        }
    },
    created() {
        this.loggedInUser = JSON.parse(localStorage.getItem('user'));
        if (this.loggedInUser) {
            this.loggedInUsername = this.loggedInUser.username;
            this.connectToWebSocket();
        } else {
            this.$router.push('/login');
        }
    },
    mounted() {
        this.loadUsers();
    },
    unmounted() {
        if (this.stompClient && this.websocketConnected) {
            this.stompClient.disconnect();
            this.websocketConnected = false;
        }
    }
};
</script>


<style scoped>
.chat-container {
    height: 100vh;
}
.user-list {
    width: 300px;
    border-right: 1px solid #dee2e6;
}
.chat-panel {
    display: flex;
    flex-direction: column;
    justify-content: space-between;
}
.chat-header {
    border-bottom: 1px solid #dee2e6;
}
.chat-body {
    overflow-y: auto;
    max-height: calc(100vh - 160px);
}
.chat-footer {
    border-top: 1px solid #dee2e6;
}
.message {
    margin-bottom: 15px;
}
</style>
