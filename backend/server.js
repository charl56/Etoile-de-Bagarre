const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const { joinWaitingRoom, leaveWaitingRoom, updatePlayerData, onHit } = require('./service/client-handler');

// Connecting to websocket. With ws war, we can know who is connected
wss.on('connection', (ws, req) => {

    ws.on('message', (message) => {
        try {
            const msg = JSON.parse(message);
            console.log("ouai le message ", msg)
            switch (msg.type) {
                case 'joinWaitingRoom':
                    joinWaitingRoom(ws, msg.playerId, msg.roomSize)
                    break;

                case 'leaveWaitingRoom':
                    leaveWaitingRoom(ws)
                    break;

                case 'updatePlayerData':
                    updatePlayerData(msg.data, ws)
                    break;

                // TODO
                case 'onHit':
                    onHit(ws, msg.data)
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