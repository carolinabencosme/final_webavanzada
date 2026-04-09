# Plataforma de Gestión de Hospedaje (Microservicios)

Proyecto de arquitectura de microservicios para **búsqueda de propiedades, reservas y reseñas de hospedaje**.

I> Este repositorio **no** implementa venta de libros. El dominio principal es alojamiento temporal (propiedades, estadías, pagos y notificaciones)

## Arquitectura

- **frontend** (React + Vite)
- **api-gateway** (entrada única `/api/**`)
- **auth-service** (autenticación/autorización JWT)
- **catalog-service** (propiedades y búsqueda)
- **cart-order-service** (carrito + reservas + checkout)
- **review-service** (reseñas y rating)
- **notification-report-service** (emails + factura PDF)
- **config-service** + **discovery-service**
- **RabbitMQ** para eventos asíncronos de confirmación de reserva

## Flujo funcional

1. Usuario inicia sesión y obtiene JWT.
2. Usuario consulta propiedades con filtros (ciudad, precio, fechas, tipo).
3. Usuario agrega propiedad al carrito y confirma la reserva.
4. `cart-order-service` publica evento de reserva confirmada.
5. `notification-report-service` envía email y genera factura PDF.
6. Usuario puede publicar reseña de una propiedad reservada.

## Endpoints reales (vía API Gateway)

> Prefijo global: `http://localhost:8080/api`

### 1) Propiedades (`catalog-service`)

- `GET /properties`
  - Filtros soportados: `page`, `size`, `city`, `propertyType`, `roomType`, `minPrice`, `maxPrice`, `q`, `checkIn`, `checkOut`
- `GET /properties/{id}`
- `POST /properties/batch`
- `PUT /properties/{id}/rating?averageRating={valor}&totalReviews={n}` (uso interno)

### 2) Reservas (`cart-order-service`)

- `POST /reservations` (crea reserva desde carrito)
- `POST /reservations/checkout` (confirma reserva)
- `GET /reservations` (historial del usuario)
- `GET /reservations/{reservationId}`
- `PUT /reservations/{reservationId}` (actualizar fechas/datos permitidos)
- `PUT /reservations/{reservationId}/cancel`
- `GET /reservations/availability?propertyId=...&checkIn=YYYY-MM-DD&checkOut=YYYY-MM-DD`

#### PayPal / checkout

- `GET /reservations/paypal/config` (alias: `/reservations/paypal/public-config`)
- `POST /reservations/paypal/create`
- `POST /reservations/paypal/capture`

#### Administración

- `GET /reservations/admin/all`
- `GET /reservations/admin/stats`

### 3) Reseñas (`review-service`)

- `GET /reviews/property/{propertyId}`
- `GET /reviews/property/{propertyId}/rating`
- `POST /reviews/{userId}`
- `DELETE /reviews/{userId}/{reviewId}`

### 4) Reportes / Facturas (`notification-report-service`)

- `GET /reports/invoice/{orderId}`
- `GET /reports/invoice/public/{orderId}?token=...`

> Nota: el endpoint de factura conserva `orderId` en la ruta por compatibilidad, aunque representa el identificador de reserva.

## Ejemplos rápidos

### Buscar propiedades

```bash
curl "http://localhost:8080/api/properties?city=Santo%20Domingo&checkIn=2026-05-10&checkOut=2026-05-13&page=0&size=12"
```

### Verificar disponibilidad

```bash
curl "http://localhost:8080/api/reservations/availability?propertyId=67f5f8f7d2c7e12ab349901a&checkIn=2026-05-10&checkOut=2026-05-13"
```

### Confirmar reserva

```bash
curl -X POST http://localhost:8080/api/reservations/checkout \
  -H "Authorization: Bearer <JWT>" \
  -H "Content-Type: application/json" \
  -d '{"paymentMethod":"MOCK"}'
```

### Obtener reseñas

```bash
curl "http://localhost:8080/api/reviews/property/67f5f8f7d2c7e12ab349901a"
```

## Variables y ejecución

La orquestación del proyecto se realiza con Docker Compose y configuración centralizada en `config-repo/`.

Pasos típicos:

```bash
make up
make logs
make down
```

## Credenciales demo

Revisar inicialización de usuarios en `auth-service` y/o scripts de arranque según el entorno.
