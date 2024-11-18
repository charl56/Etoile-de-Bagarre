# Utiliser une image Node.js officielle comme image de base
FROM node:20.18-alpine3.20

# Définir le répertoire de travail dans le conteneur
WORKDIR /server.js ./server.js

# Copier les fichiers package.json et package-lock.json (si disponible)
COPY package*.json ./

# Set proxy : ONLY IF CREATE IMAGE FROM ESEO NETWORK
# RUN npm config set proxy http://192.168.4.8:3128
# RUN npm config set https-proxy http://192.168.4.8:3128

# Installer les dépendances du projet
RUN npm install 
# WHEN USING PROXY
# RUN npm install --legacy-peer-deps


# Copier les fichiers et dossiers du projet dans le répertoire de travail du conteneur
COPY . .

# Rendre le port 5025 disponible pour le monde extérieur à ce conteneur
EXPOSE 5025

# Définir la commande pour exécuter l'application
CMD [ "node", "server.js" ]


