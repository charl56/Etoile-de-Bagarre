const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const rooms = new Map();
const MAX_PLAYERS = 3;

// Create room when no room available
function createRoom() {
    const roomId = Date.now().toString();
    rooms.set(roomId, { players: new Map(), isFull: false });
    return roomId;
}

// Try to find room when connecting
function findAvailableRoom() {
    for (const [roomId, room] of rooms) {
        if (room.players.size < MAX_PLAYERS && !room.isFull) {
            return roomId;
        }
    }
    return createRoom();
}

// Connecting to websocket
wss.on('connection', (ws, req) => {
    const parameters = url.parse(req.url, true);
    let roomId = parameters.query.roomId;
    console.log("parameters.query", parameters.query);
    console.log("Connection to room", roomId);

    if (!roomId || !rooms.has(roomId)) {
        roomId = findAvailableRoom();
    }

    const room = rooms.get(roomId);

    if (room.players.size >= MAX_PLAYERS || room.isFull) {
        ws.send(JSON.stringify({ type: 'error', message: 'Room is full' }));
        ws.close();
        return;
    }

    room.players.set(ws, null);
    ws.roomId = roomId;



    broadcastPlayerCount();

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            console.log("on message", data);
            switch (data.type) {
                case 'playerJoinWaitingRoom':
                    
                case 'playerInfo':
                    room.players.set(ws, data.player);
                    broadcastPlayers();
                    break;
                // Add other case if needed
            }
        } catch (error) {
            console.error('Error processing message:', error);
        }
    });


    ws.on('close', () => {
        room.players.delete(ws);
        if (room.players.size === 0) {
            rooms.delete(roomId);
        } else {
            broadcastPlayerCount();
            broadcastPlayers();
        }
    });

    // Send player count
    function broadcastPlayerCount() {
        const message = JSON.stringify({ type: 'playerCount', count: room.players.size });
        console.log("Broadcasting player count", message);
        room.players.forEach((_, client) => client.send(message));
    }

    // Send player list
    function broadcastPlayers() {
        const playerList = Array.from(room.players.values()).filter(player => player !== null);
        const message = JSON.stringify({ type: 'playerList', players: playerList });
        room.players.forEach((_, client) => client.send(message));
    }


    if (room.players.size === MAX_PLAYERS) {
        room.isFull = true;
        room.players.forEach((_, client) => client.send(JSON.stringify({ type: 'gameStart' })));
    }

});


const PORT = 5025;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`WebSocket server is running on port ${PORT}`);
});