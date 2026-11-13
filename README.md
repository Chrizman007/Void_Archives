# VOID ARCHIVES

Sistema distribuido basado en microservicios y cliente móvil nativo Android (Java) para la gestión y consulta del archivo de crónicas **Void Archives**.

## Arquitectura del Proyecto

```text
Void_Archives/
├── backend_Void_Archives/          # Backend SOA / Microservicios + API Gateway
│   └── api/v1/
│       ├── nginx/                  # API Gateway (Nginx en puerto 3000)
│       ├── servicios/
│       │   ├── usuarios/           # Microservicio de Autenticación JWT (Puerto 3002)
│       │   ├── articulos/          # Microservicio de Crónicas REST (3001) y Multimedia gRPC (50051)
│       │   └── comentarios/        # Microservicio de Comentarios REST (Puerto 3004)
│       └── docker-compose.yml      # Orquestación de contenedores
└── FrontMobil/                     # Cliente Móvil Nativo Android (Java, XML, Retrofit 2, Glide)
```

## Historial de Versiones (SemVer)

- **`v1.0.0`**: Configuración base del Backend SOA, API Gateway Nginx y orquestación en Docker Compose.
- **`v1.1.0`**: Refactorización de los microservicios (`articulos`, `usuarios`, `comentarios`) en arquitectura limpia por capas (`dominio`, `infraestructura`, `negocios`) y transición a *Void Archives*.
- **`v1.2.0`**: Implementación del cliente móvil nativo en Android Studio (`FrontMobil`) con Java, Retrofit 2, Gson y Glide.
- **`v1.2.1`**: Integración de selector de imágenes desde la galería del dispositivo, filtrado de crónicas publicadas y respaldo automático de conexión USB/Wi-Fi.
