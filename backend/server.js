const WebSocket = require('ws');
const http = require('http');
const url = require('url');
// const { setInterval } = require('timers/promises');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const rooms = new Map();
const MAX_PLAYERS = 2;

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



const broadcast = (roomId, typeBordcasted) => {
    const room = rooms.get(roomId);
    if (!room) return;

    // Convert player data to an array or another suitable format
    const playersData = Array.from(room.players.values()).map(player => ({
        id: player.id,
        position: player.position,
        life: player.life,

    }));

    // Create a message containing all players' data
    const message = JSON.stringify({ type: typeBordcasted, players: playersData });
    console.log("send data of all player to one player ", message)

    // Send this message to each player in the room
    room.players.forEach((_, client) => client.send(message));

};

// Connecting to websocket. With ws war, we can know who is connected
wss.on('connection', (ws, req) => {

    if (ws.roomId != undefined) {
        var room = rooms.get(ws.roomId);
    }

    ws.on('message', (message) => {
        try {
            const msg = JSON.parse(message);
            console.log("on message", msg);
            switch (msg.type) {
                case 'joinWaitingRoom':
                    JoinWaitingRoom()
                    break;

                case 'leaveWaitingRoom':
                    leaveWaitingRoom()
                    break;

                case 'updatePlayerData':
                    UpdatePlayerData(msg.data)
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
            leaveWaitingRoom()
        } catch (error) {
            console.error('Error closing connection:', error);
        }
    });


    function JoinWaitingRoom() {
        // Add to a room, if available, else create new room
        const roomId = findAvailableRoom();
        ws.roomId = roomId;
        room = rooms.get(roomId);
        // Add player to room
        room.players.set(ws, ws);

        // When a player come in a room, notifiy all players in the room
        const message = JSON.stringify({ type: 'playerCount', count: room.players.size });
        room.players.forEach((_, client) => client.send(message));

        // Then check if room is ready
        if (room.players.size === MAX_PLAYERS) {
            room.isFull = true;
            room.players.forEach((_, client) => client.send(JSON.stringify({ type: 'gameStart' })));

            // Each room have their own interval loop
            if (!room.updateInterval) {
                room.updateInterval = setInterval(() => {
                    for (const [roomId, room] of rooms) {
                        if (!room.isFull) continue;
                        broadcast(roomId, 'updatePlayersData');
                    }
                }, 1000 / 60);
            }

        }
    }

    function leaveWaitingRoom() {
        if (!room || !room.players.has(ws)) return;

        // Remove from room
        room.players.delete(ws);

        // Delete room if empty
        if (room.players.size === 0) {
            // Each room have their own interval loop
            if (room.updateInterval) {
                clearInterval(room.updateInterval);
                delete room.updateInterval;
            }
            rooms.delete(ws.roomId);
        } else {
            // Notify all players in the room
            const message = JSON.stringify({ type: 'playerCount', count: room.players.size });
            room.players.forEach((_, client) => client.send(message));
        }
    }

    function UpdatePlayerData(data) {
        console.log("data from one player ", data);
        try {
            console.log("Parse data : ", JSON.parse(data));
        } catch (error) {
            console.log("error parsing data player")
        }
        // room.players.set(ws, data.player);
    }

});


const PORT = 5025;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`WebSocket server is running on port ${PORT}`);
});