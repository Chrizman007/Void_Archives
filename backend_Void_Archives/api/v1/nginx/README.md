# Nginx API Gateway for Void Archives

This folder contains the Nginx configuration to act as the single entry point (API Gateway) for the Void Archives microservices architecture.

## Endpoints Exposed on Port 3000
- `GET /health` -> Gateway health check
- `/auth/*` -> Auth & Users Microservice (`usuarios:3002`)
- `/articulos/*` -> Articles & Lore Microservice (`articulos:3001`)
- `/comentarios/*` -> Comments Microservice (`comentarios:3004`)
