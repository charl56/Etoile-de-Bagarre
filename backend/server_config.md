# WebSocket Server Setup Guide

This guide covers the setup of a WebSocket server on a Linux server, using Docker and configuring Apache as a reverse proxy.

## Prerequisites

- Access via SSH
- Docker installed on the server
- Apache installed on the server

## 1. SSH Connection

Connect to the server using SSH. For example, using MobaXterm


## 2. Create Directory and Set Permissions (for user `etudiant` here)

```bash
mkdir /home/etudiant/android
chown etudiant /home/etudiant/android
chgrp etudiant /home/etudiant/android
```

Copy the `backend` folder into the `/home/etudiant/android` directory.

## 3. Build the Docker Image

Navigate to the `backend` folder, then run:

```bash
docker build -t websocket-server .
```

If you encounter an error like `ERROR: failed to solve: node:20.18-alpine3.20`, try:

```bash
docker pull node:20.18-alpine3.20
docker build -t websocket-server .
```

## 4. Run the Docker Container

To start the Docker container on port `5025`:

```bash
docker run -p 127.0.0.1:5025:5025 --name websocket-server-container -d websocket-server
```

Check if the container is running:

```bash
docker ps
```

## 5. Apache Configuration

To configure Apache as a reverse proxy for the WebSocket server:

1. Edit the Apache configuration file:

   ```bash
   sudo nano /etc/apache2/sites-enabled/000-default.conf
   ```

2. Add the following lines inside the `<VirtualHost *:80>` block:

   ```apache
   ProxyPass /ws-edb/ ws://127.0.0.1:5025/
   ProxyPassReverse /ws-edb/ ws://127.0.0.1:5025/
   ```

3. Enable required modules:

   ```bash
   a2enmod proxy
   a2enmod proxy_http
   ```

4. Restart Apache to apply changes:

   ```bash
   systemctl restart apache2
   ```

--- 

**Note:** You can now try to connect to this websocket at : 
```
ws://<adress-server>/ws-edb/
```

## 6. Updates

1. Update fonctionnalities in server files

2. Update backend folder on the server

3. ``` docker stop websocket-server-container ```

4. Then run build and start commands