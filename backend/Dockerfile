# Utiliser une image Node.js officielle comme image de base
FROM node:20.18-alpine3.20

# Définir le répertoire de travail dans le conteneur
WORKDIR /server.js ./server.js

# Copier les fichiers package.json et package-lock.json (si disponible)
COPY package*.json ./

# Set proxy
RUN npm config set proxy http://192.168.4.8:3128
RUN npm config set https-proxy http://192.168.4.8:3128

# Installer les dépendances du projet
RUN npm install --legacy-peer-deps


# Copier les fichiers et dossiers du projet dans le répertoire de travail du conteneur
COPY . .

# Rendre le port 5025 disponible pour le monde extérieur à ce conteneur
EXPOSE 5025

# Définir la commande pour exécuter l'application
CMD [ "node", "server.js" ]


# Build image : docker build -t websocket-server .
# Run image on local port 5025 : docker run -p 127.0.0.1:5025:5025 -d websocket-server
# View direct logs : docker logs -f <id-container>

