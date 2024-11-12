const { rooms, MAX_PLAYERS, findAvailableRoom, getPlayerIndex, broadcast } = require('./room-manager');

// When a player join a room
function joinWaitingRoom(ws) {
    // Add to a room, if available, else create new room
    const roomId = findAvailableRoom();
    ws.roomId = roomId;
    var room = getRoomById(roomId);
    // Add player to room
    room.players.set(ws, ws);

    // Set player position in list
    room.players.get(ws).listPosition = getPlayerIndex(roomId, ws);

    // When a player come in a room, notifiy all players in the room
    room.players.forEach((_, client) => {
        const message = JSON.stringify({ type: 'playerCount', count: room.players.size, listPosition: getPlayerIndex(roomId, client) });
        client.send(message);
    })

    // Then check if room is ready
    if (room.players.size === MAX_PLAYERS) {
        room.isFull = true;
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
            client.listPosition = getPlayerIndex(ws.roomId, client);    // Update value of position in list
            const message = JSON.stringify({ type: 'playerCount', count: room.players.size, listPosition: client.listPosition });
            client.send(message);
        });
    }
}


// 1 player send his data to update in liste of room
function updatePlayerData(data, ws) {
    try {
        var room = getRoomById(ws.roomId);
        const parsedData = JSON.parse(data);
        console.log("updata data of a player ", parsedData)
        // Check if player is in the room
        if (room.players.has(ws)) {
            // Get the player object from the room, to update its attributes
            const player = room.players.get(ws);

            // Update
            player.id = parsedData.id || player.id;
            player.pseudo = parsedData.pseudo || player.pseudo;
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
        console.log("onHit", parsedData)

        if (room && room.players.has(ws)) {
            // Pointeur ? être sur que quand on modifie une var victim ou shooter, cela met a jour dans la liste
            const victim = getPlayerById(room, parsedData.victimId);
            const shooter = getPlayerById(room, parsedData.shooterId);

            if (victim && victim.isAlive) {
                victim.life -= parsedData.damage;
                if (victim.life <= 0) {
                    victim.isAlive = false;
                    console.log("victime is dead", data)

                    if (shooter) {
                        shooter.kills = (shooter.kills || 0) + 1;
                    }
                    // TODO : renvoyer le message a tous les joueurs ???
                    const message = JSON.stringify({ type: 'isDead', shooterId: parsedData.shooter.id, victimId: parsedData.victim.id });
                    room.players.forEach((_, client) => client.send(message));
                }
            }
        }
    } catch (error) {
        console.error("Error processing hit data:", error);
    }
}


function getRoomById(roomId) {
    return rooms.get(roomId);
}

function getPlayerById(room, playerId) {
    return Array.from(room.players.values()).find(player => player.id === playerId);
}


module.exports = { joinWaitingRoom, leaveWaitingRoom, updatePlayerData, onHit };