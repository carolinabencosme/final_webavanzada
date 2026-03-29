# 📚 BookStore Platform

Plataforma universitaria de venta de libros construida con una arquitectura de microservicios sobre Spring Boot, diseñada para alta disponibilidad, trazabilidad distribuida y comunicación asíncrona.

---

## Descripción del Proyecto

**BookStore Platform** es una aplicación de comercio electrónico orientada a la venta de libros universitarios. Permite a los usuarios registrarse, explorar un catálogo de libros, gestionar un carrito de compras, realizar pedidos con pago simulado, escribir reseñas y recibir notificaciones por correo electrónico. Los administradores pueden gestionar el catálogo y consultar reportes en PDF.

El proyecto demuestra el uso de:
- **Microservicios independientes** con Spring Boot 3
- **Service Discovery** con Eureka
- **API Gateway** con Spring Cloud Gateway y filtros JWT
- **Mensajería asíncrona** con RabbitMQ
- **Trazabilidad distribuida** con Zipkin
- **Configuración centralizada** con Spring Cloud Config
- **Alta disponibilidad** mediante múltiples instancias de cada servicio
- **Frontend SPA** integrado como contenedor Docker

---

## Arquitectura General

```
                        ┌─────────────┐
                        │   Frontend  │  :3000
                        │  (React/Ng) │
                        └──────┬──────┘
                               │ HTTP
                        ┌──────▼──────┐
                        │ API Gateway │  :8080
                        │ (JWT Filter)│
                        └──────┬──────┘
                               │ Eureka LB
          ┌────────────────────┼────────────────────┐
          │                    │                    │
   ┌──────▼──────┐    ┌────────▼──────┐   ┌────────▼──────┐
   │auth-service │    │catalog-service│   │cart-order-svc │
   │  x2 inst.   │    │   x2 inst.    │   │   x2 inst.    │
   └──────┬──────┘    └───────┬───────┘   └───────┬───────┘
          │                   │                   │
   ┌──────▼──────┐    ┌───────▼───────┐   ┌───────▼───────┐
   │  PostgreSQL │    │    MongoDB    │   │  PostgreSQL   │
   │  (authdb)   │    │  (catalogdb)  │   │  (orderdb)    │
   └─────────────┘    └───────────────┘   └───────┬───────┘
                                                  │ RabbitMQ
                                          ┌───────▼───────┐
                                          │notification-  │
                                          │report-service │
                                          └───────┬───────┘
                                                  │ SMTP
                                          ┌───────▼───────┐
                                          │   MailHog     │
                                          │  :1025/:8025  │
                                          └───────────────┘

   ┌─────────────┐    ┌───────────────┐
   │review-svc   │    │ Zipkin        │
   │  x2 inst.   │    │    :9411      │
   └──────┬──────┘    └───────────────┘
          │
   ┌──────▼──────┐
   │ PostgreSQL  │
   │ (reviewdb)  │
   └─────────────┘
```

---

## Diagrama de Componentes

| Capa              | Componente                   | Tecnología              |
|-------------------|------------------------------|-------------------------|
| Frontend          | SPA de usuario               | React / Angular / Nginx |
| Edge              | API Gateway                  | Spring Cloud Gateway    |
| Registro          | Discovery Service            | Netflix Eureka          |
| Configuración     | Config Service               | Spring Cloud Config     |
| Identidad         | Auth Service (×2)            | Spring Boot + JWT       |
| Catálogo          | Catalog Service (×2)         | Spring Boot + MongoDB   |
| Comercio          | Cart & Order Service (×2)    | Spring Boot + PostgreSQL|
| Reseñas           | Review Service (×2)          | Spring Boot + PostgreSQL|
| Notificaciones    | Notification & Report (×1)   | Spring Boot + RabbitMQ  |
| Mensajería        | RabbitMQ                     | RabbitMQ 3.12           |
| BD relacional     | PostgreSQL                   | PostgreSQL 15           |
| BD documental     | MongoDB                      | MongoDB 6               |
| Trazabilidad      | Zipkin                       | OpenZipkin              |
| Correo (dev)      | MailHog                      | MailHog                 |

---

## Microservicios

### 1. `discovery-service` — Puerto 8761
Servidor Eureka para registro y descubrimiento de servicios. Todos los microservicios se registran aquí al iniciar. El API Gateway consulta Eureka para enrutar las peticiones con balanceo de carga del lado del cliente.

### 2. `config-service` — Puerto 8888
Servidor de configuración centralizada. Lee los archivos YAML del directorio `config-repo/` y los sirve a los microservicios en tiempo de arranque. Permite cambiar configuración sin redesplegar.

### 3. `api-gateway` — Puerto 8080
Punto de entrada único del sistema. Implementa:
- Enrutamiento dinámico vía Eureka
- Filtro JWT para autenticación en cada petición
- CORS configurado para el frontend
- Balanceo de carga automático entre instancias

### 4. `auth-service` — Puerto 8081 (×2 instancias)
Gestión de usuarios y autenticación. Provee:
- `POST /api/auth/register` — Registro de nuevo usuario
- `POST /api/auth/login` — Login y emisión de token JWT
- `GET  /api/auth/me` — Perfil del usuario autenticado
- Publica evento `user.registered` en RabbitMQ al registrar usuarios

### 5. `catalog-service` — Puerto 8082 (×2 instancias)
CRUD completo del catálogo de libros almacenado en MongoDB:
- `GET    /api/catalog/books` — Listar libros (con paginación y filtros)
- `GET    /api/catalog/books/{id}` — Detalle de un libro
- `POST   /api/catalog/books` — Crear libro (requiere rol ADMIN)
- `PUT    /api/catalog/books/{id}` — Actualizar libro (requiere rol ADMIN)
- `DELETE /api/catalog/books/{id}` — Eliminar libro (requiere rol ADMIN)

### 6. `cart-order-service` — Puerto 8083 (×2 instancias)
Gestión del carrito y procesamiento de pedidos:
- `GET    /api/cart` — Ver carrito del usuario
- `POST   /api/cart/items` — Agregar libro al carrito
- `DELETE /api/cart/items/{id}` — Quitar ítem del carrito
- `POST   /api/orders` — Confirmar pedido (pago mock incluido)
- `GET    /api/orders` — Historial de pedidos del usuario
- `GET    /api/orders/{id}` — Detalle de pedido
- `GET    /api/orders/{id}/invoice` — Descargar factura PDF
- Publica evento `order.confirmed` en RabbitMQ

### 7. `review-service` — Puerto 8084 (×2 instancias)
Sistema de reseñas y calificaciones:
- `GET    /api/reviews/book/{bookId}` — Reseñas de un libro
- `POST   /api/reviews` — Publicar reseña (usuario autenticado)
- `DELETE /api/reviews/{id}` — Eliminar reseña propia o como ADMIN

### 8. `notification-report-service` — Puerto 8085
Consumidor de eventos RabbitMQ que:
- Envía email de bienvenida al registrar usuario (`user.registered`)
- Envía email de confirmación al completar pedido (`order.confirmed`)
- Genera facturas en PDF con iText/JasperReports
- `GET /api/reports/orders` — Reporte general de pedidos (ADMIN)
- `GET /api/reports/orders/{id}/pdf` — Factura PDF de un pedido

---

## Stack Tecnológico y Justificación

| Tecnología           | Justificación                                                                 |
|----------------------|-------------------------------------------------------------------------------|
| **Spring Boot 3**    | Framework maduro, ecosistema amplio, soporte nativo para microservicios       |
| **Spring Cloud**     | Integración nativa de Eureka, Gateway, Config y Resilience4j                 |
| **JWT**              | Autenticación stateless ideal para arquitecturas distribuidas                 |
| **PostgreSQL**       | BD relacional robusta para datos transaccionales (usuarios, pedidos, reseñas) |
| **MongoDB**          | BD documental flexible para catálogo de libros con atributos variables        |
| **RabbitMQ**         | Message broker confiable para comunicación asíncrona entre servicios          |
| **Zipkin**           | Trazabilidad distribuida para depuración de flujos entre microservicios       |
| **Docker Compose**   | Orquestación local simple y reproducible para todos los contenedores          |
| **MailHog**          | Servidor SMTP de prueba que captura emails sin enviarlos realmente            |
| **Nginx**            | Servidor ligero para servir el frontend estático como contenedor              |

---

## Requisitos Previos

- **Docker** ≥ 24.0 y **Docker Compose** ≥ 2.20
- **Java 17** y **Maven 3.9** (sólo para desarrollo local sin Docker)
- **Node.js 18+** (sólo para desarrollo local del frontend)
- Puertos disponibles: `3000, 5432, 5672, 8025, 8080–8085, 8181–8184, 8761, 8888, 9411, 15672, 27017`

---

## Cómo Ejecutar con Docker Compose

```bash
# 1. Clonar el repositorio
git clone <url-del-repo>
cd final_webavanzada

# 2. (Opcional) Copiar y ajustar variables de entorno
cp .env.example .env

# 3. Construir y levantar todos los servicios
docker-compose up -d --build

# 4. Verificar que todos los contenedores estén corriendo
docker-compose ps

# 5. Ver logs en tiempo real
docker-compose logs -f

# 6. Apagar todo
docker-compose down

# 7. Apagar y eliminar volúmenes (base de datos incluida)
docker-compose down -v
```

> **Nota:** El primer arranque puede tardar 3–5 minutos mientras Maven descarga dependencias y los contenedores de infraestructura realizan sus health checks.

### Orden de arranque

Docker Compose respeta las dependencias definidas con `condition: service_healthy`:

1. `postgres`, `mongodb`, `rabbitmq`, `zipkin`, `mailhog`
2. `discovery-service` (Eureka)
3. `config-service`, `api-gateway`
4. Todos los microservicios de negocio

---

## Cómo Ejecutar en Local (Desarrollo)

```bash
# Terminal 1 — Infraestructura solamente
docker-compose up -d postgres mongodb rabbitmq zipkin mailhog

# Terminal 2 — Discovery
cd services/discovery-service
mvn spring-boot:run

# Terminal 3 — Config
cd services/config-service
mvn spring-boot:run

# Terminal 4 — API Gateway
cd services/api-gateway
mvn spring-boot:run -Dspring-boot.run.jvmArguments="-DEUREKA_HOST=localhost"

# Terminales 5-9 — Microservicios (uno por terminal)
cd services/auth-service
mvn spring-boot:run

cd services/catalog-service
mvn spring-boot:run

cd services/cart-order-service
mvn spring-boot:run

cd services/review-service
mvn spring-boot:run

cd services/notification-report-service
mvn spring-boot:run

# Terminal 10 — Frontend
cd frontend
npm install && npm start
```

---

## Usuarios de Prueba

Una vez que el sistema está corriendo, puede registrar usuarios vía API o usar los seeds precargados (si el servicio incluye `DataInitializer`):

| Usuario           | Email                     | Contraseña   | Rol   |
|-------------------|---------------------------|--------------|-------|
| Admin Principal   | admin@bookstore.com       | Admin1234!   | ADMIN |
| Usuario Demo      | usuario@bookstore.com     | User1234!    | USER  |
| Usuario Demo 2    | juan.perez@email.com      | Juan1234!    | USER  |

### Registrar un usuario manualmente:

```bash
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "nombre": "Juan Pérez",
    "email": "juan@ejemplo.com",
    "password": "MiClave123!"
  }'
```

### Hacer login y obtener token JWT:

```bash
TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"Admin1234!"}' \
  | jq -r '.token')

echo $TOKEN
```

---

## Flujo Funcional del Sistema

```
Usuario → [Frontend :3000]
        → POST /api/auth/login           → auth-service (JWT emitido)
        → GET  /api/catalog/books        → catalog-service (listado)
        → POST /api/cart/items           → cart-order-service (agregar libro)
        → POST /api/orders               → cart-order-service (confirmar pedido)
                                              ↓ publica "order.confirmed" en RabbitMQ
                                         notification-report-service
                                              ↓ envía email de confirmación
                                         MailHog (:8025) captura el email
        → GET  /api/orders/{id}/invoice  → cart-order-service (factura PDF)
        → POST /api/reviews              → review-service (publicar reseña)
```

---

## Variables de Entorno

| Variable             | Descripción                              | Valor por defecto                             |
|----------------------|------------------------------------------|-----------------------------------------------|
| `JWT_SECRET`         | Clave secreta para firmar tokens JWT     | `bookstoreSecretKey2024bookstoreSecretKey2024XD` |
| `DB_HOST`            | Host de PostgreSQL                       | `localhost` / `postgres` en Docker            |
| `DB_NAME`            | Nombre de la base de datos               | Según servicio (`authdb`, `orderdb`, etc.)    |
| `DB_USER`            | Usuario de PostgreSQL                    | `postgres`                                    |
| `DB_PASSWORD`        | Contraseña de PostgreSQL                 | `postgres`                                    |
| `MONGO_HOST`         | Host de MongoDB                          | `localhost` / `mongodb` en Docker             |
| `MONGO_DB`           | Nombre de la base de datos Mongo         | `catalogdb` / `notificationdb`                |
| `RABBITMQ_HOST`      | Host de RabbitMQ                         | `localhost` / `rabbitmq` en Docker            |
| `EUREKA_HOST`        | Host del servidor Eureka                 | `localhost` / `discovery-service` en Docker   |
| `MAIL_HOST`          | Host del servidor SMTP                   | `mailhog`                                     |
| `MAIL_PORT`          | Puerto SMTP                              | `1025`                                        |
| `PAYMENT_MOCK`       | Activar pago simulado (sin PayPal real)  | `true`                                        |
| `PAYPAL_CLIENT_ID`   | Client ID de PayPal (producción)         | —                                             |
| `PAYPAL_CLIENT_SECRET` | Secret de PayPal (producción)          | —                                             |

---

## Acceso a Servicios

| Servicio                        | URL                                  |
|---------------------------------|--------------------------------------|
| **Frontend**                    | http://localhost:3000                |
| **API Gateway** (entrada única) | http://localhost:8080                |
| **Eureka Dashboard**            | http://localhost:8761                |
| **RabbitMQ Management**         | http://localhost:15672 (guest/guest) |
| **Zipkin UI**                   | http://localhost:9411                |
| **MailHog UI**                  | http://localhost:8025                |
| **Config Service**              | http://localhost:8888                |
| Auth Service (directo)          | http://localhost:8081                |
| Auth Service instancia 2        | http://localhost:8181                |
| Catalog Service (directo)       | http://localhost:8082                |
| Catalog Service instancia 2     | http://localhost:8182                |
| Cart/Order Service (directo)    | http://localhost:8083                |
| Cart/Order Service instancia 2  | http://localhost:8183                |
| Review Service (directo)        | http://localhost:8084                |
| Review Service instancia 2      | http://localhost:8184                |
| Notification Service (directo)  | http://localhost:8085                |

---

## Guion de Presentación de 10 Minutos

### Minuto 0–1: Introducción
> "BookStore Platform es una plataforma de microservicios para venta de libros universitarios. Consta de 8 servicios independientes orquestados con Docker Compose. Vamos a demostrar cada uno de los requisitos del proyecto."

### Minuto 1–2: Levantar el sistema
```bash
docker-compose up -d
docker-compose ps        # mostrar todos los contenedores "healthy"
```
> Abrir http://localhost:8761 — mostrar Eureka con todas las instancias registradas.

### Minuto 2–3: Registro y Login (Auth + JWT)
```bash
# Registrar usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Demo User","email":"demo@test.com","password":"Demo1234!"}'

# Login
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Demo1234!"}'
```
> Mostrar token JWT decodificado en https://jwt.io

### Minuto 3–4: Catálogo de Libros
```bash
curl http://localhost:8080/api/catalog/books
curl http://localhost:8080/api/catalog/books?categoria=ingenieria
```
> Mostrar listado de libros desde MongoDB.

### Minuto 4–5: Carrito y Pedido
```bash
export TOKEN="<token-del-login>"

# Agregar al carrito
curl -X POST http://localhost:8080/api/cart/items \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId":"<id>","cantidad":1}'

# Confirmar pedido
curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"
```

### Minuto 5–6: Correo Transaccional y Factura PDF
> Abrir http://localhost:8025 — mostrar el email de confirmación de pedido recibido en MailHog.
```bash
curl http://localhost:8080/api/orders/<id>/invoice \
  -H "Authorization: Bearer $TOKEN" \
  --output factura.pdf && open factura.pdf
```

### Minuto 6–7: Reseñas
```bash
curl -X POST http://localhost:8080/api/reviews \
  -H "Authorization: Bearer $TOKEN" \
  -H "Content-Type: application/json" \
  -d '{"bookId":"<id>","calificacion":5,"comentario":"Excelente libro!"}'
```

### Minuto 7–8: Alta Disponibilidad
```bash
# Ver instancias en Eureka
curl http://localhost:8761/eureka/apps | grep -i instanceId

# Simular caída de una instancia
docker-compose stop auth-service-1

# El sistema sigue respondiendo mediante auth-service-2
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"demo@test.com","password":"Demo1234!"}'

# Restaurar
docker-compose start auth-service-1
```

### Minuto 8–9: Trazabilidad con Zipkin
> Abrir http://localhost:9411 → Run Query → seleccionar cualquier servicio → ver el trace completo del flujo de petición atravesando gateway → auth → catalog.

### Minuto 9–10: Seguridad y Cierre
```bash
# Intentar acceder a un endpoint protegido sin token
curl http://localhost:8080/api/orders
# Respuesta: 401 Unauthorized

# Intentar acceder a endpoint de admin con rol USER
curl http://localhost:8080/api/catalog/books \
  -X POST \
  -H "Authorization: Bearer $USER_TOKEN"
# Respuesta: 403 Forbidden
```
> Mostrar RabbitMQ Management en http://localhost:15672 con las colas de eventos activas.

---

## Checklist de Requisitos Cumplidos

- [x] **8 microservicios** con Spring Boot 3
- [x] **Service Discovery** con Netflix Eureka
- [x] **API Gateway** con Spring Cloud Gateway
- [x] **Configuración centralizada** con Spring Cloud Config
- [x] **Autenticación JWT** stateless
- [x] **2 bases de datos diferentes** (PostgreSQL + MongoDB)
- [x] **Mensajería asíncrona** con RabbitMQ (eventos de registro y pedido)
- [x] **Correos transaccionales** con JavaMailSender + MailHog
- [x] **Generación de PDF** (factura de pedido)
- [x] **Alta disponibilidad** — 2 instancias de auth, catalog, cart-order y review
- [x] **Balanceo de carga** vía Eureka + Spring Cloud LoadBalancer
- [x] **Trazabilidad distribuida** con Zipkin
- [x] **Docker Compose** funcional con health checks
- [x] **Frontend SPA** contenerizado
- [x] **CORS** configurado en el gateway
- [x] **Variables de entorno** parametrizadas
- [x] **Roles de usuario** (USER / ADMIN)

---

## Arquitectura y Explicación del Diseño

### Patrón API Gateway
Todo el tráfico externo entra por el API Gateway en el puerto 8080. El gateway valida el token JWT antes de enrutar la petición al servicio correspondiente. Esto centraliza la seguridad y evita exponer los microservicios directamente.

### Comunicación síncrona vs. asíncrona
- **Síncrona (HTTP/REST):** Usada para operaciones que requieren respuesta inmediata (login, consulta de catálogo, operaciones de carrito).
- **Asíncrona (RabbitMQ):** Usada para notificaciones. Cuando se confirma un pedido, `cart-order-service` publica un evento en RabbitMQ sin esperar a que `notification-report-service` procese el email. Esto mejora el tiempo de respuesta y la resiliencia.

### Separación de bases de datos
Cada servicio tiene su propia base de datos (Database per Service pattern):
- `authdb` — datos de usuarios y credenciales (PostgreSQL)
- `catalogdb` — libros con estructura flexible (MongoDB)
- `orderdb` — pedidos y carritos (PostgreSQL)
- `reviewdb` — reseñas y calificaciones (PostgreSQL)
- `notificationdb` — log de notificaciones enviadas (MongoDB)

Esto garantiza el aislamiento y permite escalar cada base de datos de forma independiente.

### Configuración centralizada
El `config-service` sirve la configuración desde el directorio `config-repo/`. En producción, este directorio sería un repositorio Git externo, lo que permite cambiar configuración (por ejemplo, el host de RabbitMQ o el secret JWT) sin redesplegar los servicios.

---

## Justificación del Frontend y Backend

### Backend — Spring Boot + Spring Cloud
Se eligió el ecosistema Spring por su madurez en entornos empresariales, la facilidad de integración entre componentes (Eureka, Gateway, Config, Security) y el soporte activo de la comunidad. Spring Security con JWT es el estándar de facto para APIs RESTful seguras.

### Frontend — SPA contenerizada
El frontend está construido como una Single Page Application servida por Nginx. La comunicación con el backend pasa íntegramente por el API Gateway, lo que significa que el frontend solo necesita conocer una única URL base (`http://localhost:8080`).

---

## Cómo Demostrar Alta Disponibilidad

```bash
# 1. Verificar instancias registradas en Eureka
curl -s http://localhost:8761/eureka/apps/AUTH-SERVICE | grep -o '<instanceId>[^<]*</instanceId>'

# 2. Detener una instancia de auth-service
docker-compose stop auth-service-1

# 3. Esperar ~30 segundos para que Eureka detecte la caída

# 4. Realizar una petición de login — debe responder via auth-service-2
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"Admin1234!"}'
# Debe retornar 200 OK con token JWT

# 5. Restaurar la instancia caída
docker-compose start auth-service-1
```

El balanceo de carga lo gestiona automáticamente Spring Cloud LoadBalancer integrado en el API Gateway, consultando el registro de Eureka en cada petición.

---

## Cómo Demostrar Seguridad

```bash
# Sin token — debe retornar 401 Unauthorized
curl -i http://localhost:8080/api/orders

# Con token expirado o inválido — debe retornar 401
curl -i http://localhost:8080/api/orders \
  -H "Authorization: Bearer token.invalido.aqui"

# Con token de USER intentando operación de ADMIN — debe retornar 403
export USER_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"usuario@bookstore.com","password":"User1234!"}' | jq -r '.token')

curl -i -X DELETE http://localhost:8080/api/catalog/books/algún-id \
  -H "Authorization: Bearer $USER_TOKEN"
# Respuesta: 403 Forbidden

# Con token de ADMIN — debe funcionar
export ADMIN_TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"admin@bookstore.com","password":"Admin1234!"}' | jq -r '.token')

curl -i -X DELETE http://localhost:8080/api/catalog/books/algún-id \
  -H "Authorization: Bearer $ADMIN_TOKEN"
# Respuesta: 200 OK
```

---

## Cómo Demostrar Monitoreo con Zipkin

1. Abrir http://localhost:9411 en el navegador
2. Realizar algunas peticiones a través del API Gateway
3. En Zipkin, hacer clic en **"Run Query"**
4. Seleccionar un servicio del dropdown (e.g., `api-gateway`)
5. Se verán los traces con:
   - **Duración total** de la petición
   - **Spans** por cada microservicio involucrado
   - **Errores** resaltados en rojo
6. Hacer clic en un trace para ver el detalle completo del flujo

El trace de un pedido completo mostrará spans en: `api-gateway` → `cart-order-service` → `notification-report-service`

---

## Cómo Demostrar Correos Transaccionales

```bash
# 1. Registrar un nuevo usuario
curl -X POST http://localhost:8080/api/auth/register \
  -H "Content-Type: application/json" \
  -d '{"nombre":"Test User","email":"test@ejemplo.com","password":"Test1234!"}'

# 2. Verificar el email de bienvenida en MailHog
# Abrir http://localhost:8025
# Debe aparecer un email de bienvenida en la bandeja de entrada

# 3. Hacer un pedido
export TOKEN=$(curl -s -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"test@ejemplo.com","password":"Test1234!"}' | jq -r '.token')

curl -X POST http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN"

# 4. Volver a MailHog — debe aparecer un nuevo email de confirmación de pedido
# El email incluye el número de pedido, lista de libros y total
```

---

## Cómo Demostrar Factura PDF

```bash
# 1. Obtener el ID del pedido recién creado
export ORDER_ID=$(curl -s http://localhost:8080/api/orders \
  -H "Authorization: Bearer $TOKEN" | jq -r '.[0].id')

# 2. Descargar la factura PDF
curl -s http://localhost:8080/api/orders/$ORDER_ID/invoice \
  -H "Authorization: Bearer $TOKEN" \
  --output factura_$ORDER_ID.pdf

# 3. Verificar que el PDF fue descargado
ls -lh factura_$ORDER_ID.pdf

# 4. Abrir el PDF (en sistemas con visor disponible)
xdg-open factura_$ORDER_ID.pdf   # Linux
open factura_$ORDER_ID.pdf        # macOS
```

La factura PDF incluye:
- Logo y nombre de la plataforma
- Número de pedido y fecha
- Datos del comprador
- Tabla de libros (título, cantidad, precio unitario, subtotal)
- Total con impuestos
- Código de referencia único

---

*Desarrollado como proyecto final de Web Avanzada — Universidad.*
