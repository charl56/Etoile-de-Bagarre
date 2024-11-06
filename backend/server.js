const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const { JoinWaitingRoom, leaveWaitingRoom, UpdatePlayerData } = require('./service/client-handler');

// Connecting to websocket. With ws war, we can know who is connected
wss.on('connection', (ws, req) => {

    ws.on('message', (message) => {
        try {
            const msg = JSON.parse(message);
            switch (msg.type) {
                case 'joinWaitingRoom':
                    JoinWaitingRoom(ws)
                    break;

                case 'leaveWaitingRoom':
                    leaveWaitingRoom(ws)
                    break;

                case 'updatePlayerData':
                    UpdatePlayerData(msg.data, ws)
                    break;

                // TODO
                case 'onHit':
                    break;
                // TODO
                case 'onReceiveHot':
                    break;

                // Add other case if needed
            }
        } catch (error) {
            console.error('Error processing message:', error);
        }
    });


    ws.on('close', () => {
        try {
            leaveWaitingRoom(ws)
        } catch (error) {
            console.error('Error closing connection:', error);
        }
    });
});


const PORT = 5025;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`WebSocket server is running on port ${PORT}`);
});