# Verificación técnica de `cart-order-service`

Fecha de ejecución: 2026-04-06 (UTC)

## 1) Artefacto usado en Docker/K8s vs código actual

- El código fuente actual del módulo está en el paquete `com.hospedaje.cartorder`.  
- El JAR existente en `target/` contiene clases legacy `com.bookstore.cartorder`, lo que confirma residuos de compilación previos si no se hace `clean`.
- El `Dockerfile` de servicios recompila desde fuentes (`COPY . .` + `mvn ... package`) y luego copia `${MODULE}-1.0.0.jar` generado en build stage.

## 2) Build limpio antes de levantar contenedores

- Se intentó `mvn -pl cart-order-service clean package`.
- En este entorno falló por acceso a Maven Central (`403 Forbidden`), por lo que no se pudo completar la reconstrucción del JAR aquí.
- Recomendación operativa: ejecutar siempre `mvn -pl cart-order-service clean package` (o `docker compose build --no-cache cart-order-service-*`) antes de `up`.

## 3) Logs SQL Hibernate para `INSERT INTO cart_items` con `quantity`

- Se añadió una prueba de integración JPA (`CartItemInsertSqlTest`) que:
  - habilita logging SQL de Hibernate;
  - hace `saveAndFlush` de `CartItem`;
  - verifica en logs que aparezca `insert into cart_items` y `quantity`.

## 4) Confirmación de `.quantity(1)` en `CartService`

- El builder en `CartService#addToCart` mantiene `.quantity(1)`.

## 5) Config externa que altere estrategia de inserción

- Se revisó `application.yml`, `reservation-service.yml` (Config Server) y `docker-compose.yml`.
- No se encontró `@DynamicInsert`, `hibernate.dynamic_insert`, ni perfiles activos (`SPRING_PROFILES_ACTIVE`) que omitan columnas no tocadas/nulas en esta ruta.
