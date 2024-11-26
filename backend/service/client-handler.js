const { rooms, findAvailableRoom, getPlayerIndex, getSpawnPosition, broadcast } = require('./room-manager');

// When a player join a room
function joinWaitingRoom(ws, playerId, roomSize, pseudo) {
    roomSize = parseInt(roomSize, 10); // String to int
    // Validate roomSize
    roomSize = [2, 3, 4].includes(roomSize) ? roomSize : 2; // Default to 2 if invalid

    // Add to a room, if available, else create new room
    const roomId = findAvailableRoom(roomSize);
    ws.roomId = roomId;
    var room = getRoomById(roomId);
    // Add player to room
    room.players.set(ws, ws);
    room.roomSize = roomSize;

    // Set player position in list
    room.players.get(ws).id = playerId;

    const spawnIndex = getPlayerIndex(roomId, ws);
    const spawnPosition = getSpawnPosition(roomSize, spawnIndex);
    // Set spawn position and life
    room.players.get(ws).x = spawnPosition.x;
    room.players.get(ws).y = spawnPosition.y;       
    room.players.get(ws).life = 100;
    room.players.get(ws).kills = 0;
    room.players.get(ws).isAlive = true;
    room.players.get(ws).pseudo = pseudo;
    room.players.get(ws).nextState  = "IDLE";
    
    // When a player come in a room, notifiy all players in the room ( room.players.get(ws) == client )
    room.players.forEach((_, client) => {
        const message = JSON.stringify({ type: 'playerCount', count: room.players.size, maxPlayers: room.maxPlayers, spawnPositionX: client.x, spawnPositionY: client.y });
        client.send(message);
    })

    broadcast(roomId);  // Is used to send enemys spawn position

    // Then check if room is ready
    if (room.players.size === room.maxPlayers) {
        room.isFull = true;
        room.isStarted = true;
        room.players.forEach((_, client) => client.send(JSON.stringify({ type: 'gameStart' })));

        // Each room have their own interval loop
        if (!room.updateInterval) {
            room.updateInterval = setInterval(() => {
                for (const [roomId, room] of rooms) {
                    if (!room.isFull) continue;
                    broadcast(roomId);
                }
            }, 1000 / 60);
        }

    }
}

// When a player leave room
function leaveWaitingRoom(ws) {
    var room = getRoomById(ws.roomId);

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
        room.players.forEach((_, client) => {
            if(!room.isStarted){
                const spanwPosition = getSpawnPosition(room.roomSize, getPlayerIndex(ws.roomId, ws))
                client.x = spanwPosition.x;
                client.y = spanwPosition.y;
            }
        
            const message = JSON.stringify({ type: 'playerCount', count: room.players.size, spawnPositionX: client.x, spawnPositionY: client.y });
            client.send(message);
        });
    }
}


// 1 player send his data to update in liste of room
function updatePlayerData(data, ws) {
    try {
        const room = getRoomById(ws.roomId);
        const parsedData = JSON.parse(data);

        // Check if player is in the room
        if (room.players.has(ws)) {
            // Get the player object from the room, to update its attributes
            const player = room.players.get(ws);

            // Update
            player.id = parsedData.id || player.id;
            player.x = parsedData.x || player.x;
            player.y = parsedData.y || player.y;
            player.kills = parsedData.kills || player.kills;
            player.life = parsedData.life || player.life;
            player.isAlive = parsedData.isAlive !== undefined ? parsedData.isAlive : player.isAlive;
            // Data direct update in the list of the room

        }
    } catch (error) {
        console.error("Error parsing or updating player data:", error);
    }
}

// Used to update life of player data when a hit is detectedte
function onHit(ws, data) {  // data {victimId: playerId, shooterId: playerId, damage: number}
    try {
        const room = getRoomById(ws.roomId);
        const parsedData = JSON.parse(data);

        // If room existe and player who do request is inside
        if (room && room.players.has(ws)) {
            // Pointeur ? être sur que quand on modifie une var victim ou shooter, cela met a jour dans la liste
            const victim = getPlayerById(room, parsedData.victimId);
            const shooter = getPlayerById(room, parsedData.shooterId);

            if (victim && victim.isAlive) {
                victim.life -= parsedData.damage;

                if (victim.life <= 0) {
                    victim.isAlive = false;

                    if (shooter) {
                        shooter.kills = Number(shooter.kills || 0) + 1;
                    }

                    const message = JSON.stringify({ type: 'isDead', shooterId: parsedData.shooterId, victimId: parsedData.victimId });
                    room.players.forEach((_, client) => client.send(message));
                }
            }
        }
    } catch (error) {
        console.error("Error processing hit data:", error);
    }
}

function updatePlayerState(data, ws) {
    try {
        const room = getRoomById(ws.roomId);
        const parsedData = JSON.parse(data);

        // Check if player is in the room
        if (room.players.has(ws)) {

            const player = room.players.get(ws);
            // Update
            player.id = parsedData.id || player.id;
            player.nextState  = parsedData.nextState  || player.nextState ;
        }
    } catch (error) {
        console.error("Error parsing or updating player data:", error);
    }

}


function getRoomById(roomId) {
    return rooms.get(roomId);
}

function getPlayerById(room, playerId) {
    return Array.from(room.players.values()).find(player => player.id === playerId);
}


module.exports = { joinWaitingRoom, leaveWaitingRoom, updatePlayerData, onHit, updatePlayerState };