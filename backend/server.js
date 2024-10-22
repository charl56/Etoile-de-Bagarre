const WebSocket = require('ws');
const http = require('http');
const url = require('url');

const server = http.createServer();
const wss = new WebSocket.Server({ server });

const rooms = new Map();
const MAX_PLAYERS = 2;

// Create room when no room available
function createRoom() {
    const roomId = Date.now().toString();
    rooms.set(roomId, { players: new Map(), isFull: false });
    console.log("rooms create", rooms);

    return roomId;
}

// Try to find room when connecting
function findAvailableRoom() {
    console.log("rooms", rooms);
    for (const [roomId, room] of rooms) {
        console.log("room", room);
        console.log("room", room.players.size);
        if (room.players.size < MAX_PLAYERS && !room.isFull) {
            return roomId;
        }
    }
    return createRoom();
}


// Connecting to websocket. With ws war, we can know who is connected
wss.on('connection', (ws, req) => {
    
    if(ws.roomId != undefined) {
        var room = rooms.get(ws.roomId);
    }

    ws.on('message', (message) => {
        try {
            const data = JSON.parse(message);
            console.log("on message", data);
            switch (data.type) {
                case 'joinWaitingRoom':
                    JoinWaitingRoom()
                    break;

                case 'leavingWaitingRoom':
                    LeavingWaitingRoom()
                    break;

                case 'playerInfo':
                  
                    break;
                // Add other case if needed
            }
        } catch (error) {
            console.error('Error processing message:', error);
        }
    });


    ws.on('close', () => {

        
    });


    function JoinWaitingRoom(){
        console.log("JoinWaitingRoom");
        // Add to a room, if available, else create new room
        const roomId = findAvailableRoom();
        ws.roomId = roomId;
        console.log("roomId", roomId);
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
        }
    }

    function LeavingWaitingRoom(){
        if (!room || !room.players.has(ws)) return;

        // Remove from room
        room.players.delete(ws);
        // Delete room if empty
        if (room.players.size === 0) {
            rooms.delete(ws.roomId);
        } else {
            // Notify all players in the room
            const message = JSON.stringify({ type: 'playerCount', count: room.players.size });
            room.players.forEach((_, client) => client.send(message));
        }
    }

});


const PORT = 5025;
server.listen(PORT, '0.0.0.0', () => {
    console.log(`WebSocket server is running on port ${PORT}`);
});