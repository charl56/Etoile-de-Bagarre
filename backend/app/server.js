const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const rooms = new Map();
const MAX_PLAYERS = 5;

// Create room when no room available
function createRoom() {
    const roomId = Date.now().toString();
    rooms.set(roomId, { players: new Set(), isFull: false });
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

    // Send player count
    function broadcastPlayerCount() {
        const message = JSON.stringify({ type: 'playerCount', count: room.players.size });
        room.players.forEach((_, client) => client.send(message));
    }

    // Send player list
    function broadcastPlayers() {
        const playerList = Array.from(room.players.values()).filter(player => player !== null);
        const message = JSON.stringify({ type: 'playerList', players: playerList });
        room.players.forEach((_, client) => client.send(message));
    }

    broadcastPlayerCount();

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            switch (data.type) {
                case 'playerInfo':
                    room.players.set(ws, data.player);
                    broadcastPlayers();
                    break;
                // Ajoutez d'autres cas si nécessaire
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


    if (room.players.size === MAX_PLAYERS) {
        room.isFull = true;
        room.players.forEach((_, client) => client.send(JSON.stringify({ type: 'gameStart' })));
    }

});

const PORT = 5025;
server.listen(PORT, () => {
    console.log(`WebSocket server is running on port ${PORT}`);
});