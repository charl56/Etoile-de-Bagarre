# WebSocket Server Setup Guide


Guide pour utiliser la webnsocket en local 

Instructions de lancement en local 

En mode développement, on peut lancer la websocket en local. Pour cela on lance le container docker (voir backend/Dockerfile) 

Sur la websocket, on met l’adresse sur serveur : ws://10.0.2.2:5025 

On peut ensuite s’y connecter en lançant l’app depuis un émulateur sur Android Studio 

 
## Technologies Utilisées 

- Node.js : Pour la gestion du serveur. 

- WebSocket : Pour la communication en temps réel entre les clients. 

## Modules : 

- room-manager.js : Gestion des salles et des positions. 

- client-handler.js : Gestion des actions des joueurs. 

- Docker : Pour la mise en place de la websocket sur un serveur (voir backend/server.md pour la préparation du serveur, et déploiement de la websocket) 