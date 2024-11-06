const rooms = new Map();
const MAX_PLAYERS = 2; // Maximum number of players in a room

// Create room when no room available
function createRoom() {
    const roomId = Date.now().toString();
    rooms.set(roomId, {
        players: new Map(),
        isFull: false
    });
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



// Get index of player in a room
function getPlayerIndex(roomId, playerId) {
    const room = rooms.get(roomId);
    if (!room) {
        return -1; // If room not found
    }

    let index = 0;
    for (let [id, player] of room.players.entries()) {
        if (id === playerId) {
            return index; // If player found
        }
        index++;
    }
    return -1; // If player not found
}


// Send message to all players in the room
const broadcast = (roomId) => {
    const room = rooms.get(roomId);
    if (!room) return;

    // Convert player data to an array or another suitable format
    const playersData = Array.from(room.players.values()).map(player => (
        {
            id: player.id,
            pseudo: player.pseudo,
            x: player.x,
            y: player.y,
            life: player.life,
            isAlive: player.isAlive,
            listPosition: player.listPosition
        }));

    // Create a message containing all players' data
    const message = JSON.stringify({ type: 'updatePlayersData', players: playersData });
    console.log("send data of all player to one player ", message)

    // Send this message to each player in the room
    room.players.forEach((_, client) => client.send(message));
};


module.exports = { rooms, MAX_PLAYERS, findAvailableRoom, getPlayerIndex, broadcast };