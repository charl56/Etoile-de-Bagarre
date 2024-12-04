# **WebSocket Server Setup Guide**

## **Prerequisites**  
- Docker Desktop must be installed on your machine.



## **Starting and Connecting to the WebSocket Locally**  

1. Navigate to the `NomDuJeu/backend` directory:  
   ```bash
   cd NomDuJeu/backend
   ```

2. Build the Docker image:  
   ```bash
   docker build -t websocket-server .
   ```

3. Start the Docker container:  
   ```bash
   docker run -p 127.0.0.1:5025:5025 --name websocket-server-container -d websocket-server
   ```

4. You can connect to the WebSocket at:  
   `ws://127.0.0.1:5025`


### **Connecting from an Android Studio Emulator**  
- Use the following address in the `Websocket` class (package `fr.eseo.ld.android.cp.nomdujeu.service`) to access the WebSocket from an emulator:  
  `ws://10.0.2.2:5025`


### **Managing the Docker Container**  

- **To stop the container:**  
  ```bash
  docker stop websocket-server-container
  ```

- **If changes are made to the server code:**  
  1. Stop the container:  
     ```bash
     docker stop websocket-server-container
     ```
  2. Rebuild the image:  
     ```bash
     docker build -t websocket-server .
     ```
  3. Restart the container:  
     ```bash
     docker run -p 127.0.0.1:5025:5025 --name websocket-server-container -d websocket-server
     ```

- **To view the container logs in live, use Docker Desktop or the following command:**  
  ```bash
  docker logs -f websocket-server-container
  ```


## **Server Development and Deployment**  
- To prepare the server and deploy the WebSocket, refer to `backend/server_config.md`.


## **Technologies Used**  

**Server**  
- **Node.js**: Manages the server.  
- **WebSocket**: Real-time communication between clients.  

**Modules**  
- `room-manager.js`: Manages rooms and player positions.  
- `client-handler.js`: Handles player actions.  

**Containerization**  
- **Docker**: Used to deploy the WebSocket on a server.  
