import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';

class WebSocketService {
    constructor() {
        this.stompClient = null;
        this.connected = false;
    }

    connect(user, onMessageReceived) {
        const socket = new SockJS('/ws'); // Replace with your backend WebSocket endpoint
        this.stompClient = new Client({
            webSocketFactory: () => socket,
            reconnectDelay: 5000, // Auto-reconnect in 5 seconds
            onConnect: () => {
                this.connected = true;
                console.log('Connected to WebSocket server');

                // Subscribe to the user message queue
                this.stompClient.subscribe(`/user/${user}/queue/messages`, message => {
                    onMessageReceived(JSON.parse(message.body));
                });
            },
            onDisconnect: () => {
                this.connected = false;
                console.log('Disconnected from WebSocket server');
            }
        });

        this.stompClient.activate();
    }

    sendMessage(recipient, message) {
        if (this.stompClient && this.connected) {
            this.stompClient.publish({
                destination: `/app/chat/${recipient}`, // Your backend controller mapping
                body: JSON.stringify(message)
            });
        }
    }
}

export default new WebSocketService();
