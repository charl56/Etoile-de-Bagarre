const rooms = new Map();
const spawnPositions = {
    2: [
        {
            x: 20,
            y: 20
        },
        {
            x: 10,
            y: 20
        },
    ],
    3: [
        {
            x: 26,
            y: 25
        },
        {
            x: 8,
            y: 28
        },
        {
            x: 5,
            y: 14
        }
    ],
    4: [
        {
            x: 15,
            y: 7
        },
        {
            x: 26,
            y: 25
        },
        {
            x: 8,
            y: 28
        },
        {
            x: 5,
            y: 14
        }
    ]
}


// Create room when no room available
function createRoom(size) {
    const roomId = Date.now().toString();
    rooms.set(roomId, {
        players: new Map(),
        isFull: false,
        maxPlayers: size,
        isStarted: false
    });
    return roomId;

}

// Try to find room when connecting
function findAvailableRoom(size) {
    for (const [roomId, room] of rooms) {
        if (room.players.size < room.maxPlayers && !room.isFull && room.maxPlayers === size) {
            return roomId;
        }
    }
    return createRoom(size);
}

// Get index of player in a room
function getPlayerIndex(roomId, ws) {
    const room = rooms.get(roomId);
    if (!room) {
        return -1; // If room not found
    }

    let index = 0;
    for (let [id, player] of room.players.entries()) {
        if (id === ws) {
            return index; // If player found
        }
        index++;
    }
    return -1; // If player not found
}

function getSpawnPosition(roomSize, playerIndex) {
    // Define spawn positions for different room sizes
    return spawnPositions[roomSize][playerIndex] || {x: 0, y: 0};
}



// Send message to all players in the room
const broadcast = (roomId) => {
    const room = rooms.get(roomId);
    if (!room) return;

    // detect when to end the game : 1 player alive
    var alivePlayers;
    room.players.forEach((player) => {
        if(player.isAlive){
            alivePlayers++
        }
    })


    if(alivePlayers === 1 && room.isStarted){
        room.players.forEach((player) => {
            player.send(JSON.stringify({ type: 'endGame', winner: player.pseudo, kills: player.kills, id: player.id }));
        });
        return;
    }


    // Convert player data to an array or another suitable format
    const playersData = Array.from(room.players.values()).map(player => (
        {
            id: player.id,
            pseudo: player.pseudo,
            x: player.x,
            y: player.y,
            kills: player.kills,
            life: player.life,
            isAlive: player.isAlive,
        }));

    // Create a message containing all players' data
    const message = JSON.stringify({ type: 'updatePlayersData', players: playersData });
    // console.log("send data of all player for each player ", message)

    // Send this message to each player in the room
    room.players.forEach((_, client) => client.send(message));
};


module.exports = { rooms, findAvailableRoom, getPlayerIndex, getSpawnPosition, broadcast };