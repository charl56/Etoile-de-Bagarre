# Run Websocket local. Required : Docker Desktop
# Go to backend directory : cd backend
# Build image : docker build -t websocket-server .
# Run image : docker run -p 127.0.0.1:5025:5025 -d websocket-server

# Stop container, rebuild and run when changes are made


# Use Node image as base image
FROM node:20.18-alpine3.20

# Set the working directory in the container
WORKDIR /server.js ./server.js

# Copy the package.json file
COPY package*.json ./

# Set proxy : ONLY IF CREATE IMAGE FROM ESEO NETWORK
# RUN npm config set proxy http://192.168.4.8:3128
# RUN npm config set https-proxy http://192.168.4.8:3128

# Install dependancies
RUN npm install 
# IF USING PROXY
# RUN npm install --legacy-peer-deps


# Copy all the files from the project directory to the container
COPY . .

# Expose the port the app runs in
EXPOSE 5025

# Start the server, inside the container
CMD [ "node", "server.js" ]



