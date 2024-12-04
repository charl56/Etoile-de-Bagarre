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
    return spawnPositions[roomSize][playerIndex] || { x: 0, y: 0 };
}



// Send message to all players in the room
const broadcast = (roomId) => {
    const room = rooms.get(roomId);
    if (!room || !room.players) return;


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
            nextState: player.nextState
        }));

    // Create a message containing all players' data
    const message = JSON.stringify({ type: 'updatePlayersData', players: playersData });

    // Send this message to each player in the room
    room.players.forEach((_, client) => client.send(message));


    // detect when to end the game : 1 player alive
    var alivePlayers = 0;
    room.players.forEach((player) => {

        if (player.isAlive) {
            alivePlayers++
        }
    })

    if (alivePlayers === 1 && room.isStarted) {
        let winner;
        room.players.forEach((player) => {
            if (player.isAlive) {
                winner = player;
            }
        });

        if (winner) {
            room.players.forEach((player) => {
                player.send(JSON.stringify({ type: 'endGame', winnerId: winner.id, winnerPseudo: winner.pseudo, winnerKills: winner.kills }));
            });

            if (room.updateInterval) {
                clearInterval(room.updateInterval);
                delete room.updateInterval;
            }
        }
        return;
    }

};


module.exports = { rooms, findAvailableRoom, getPlayerIndex, getSpawnPosition, broadcast };