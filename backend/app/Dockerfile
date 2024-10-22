# Utiliser une image Node.js officielle comme image de base
FROM node:14

# Définir le répertoire de travail dans le conteneur
WORKDIR /server.js ./server.js

# Copier les fichiers package.json et package-lock.json (si disponible)
COPY package*.json ./

# Installer les dépendances du projet
RUN npm install

# Copier les fichiers et dossiers du projet dans le répertoire de travail du conteneur
COPY . .

# Rendre le port 5025 disponible pour le monde extérieur à ce conteneur
EXPOSE 5025

# Définir la commande pour exécuter l'application
CMD [ "node", "server.js" ]

# Create