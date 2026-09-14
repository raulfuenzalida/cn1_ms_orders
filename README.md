# MS-Orders

Microservicio de gestión de pedidos para PrintWorks. Este servicio es responsable de administrar la creación, seguimiento y gestión de pedidos del sistema, incluyendo items, snapshots históricos, cantidades, subtotales, total, estados y generación de comprobantes PDF.

## Tecnologías

- Java 21
- Spring Boot 3.4.11
- Maven
- Spring Data JPA
- Spring Security con OAuth2 Resource Server
- MySQL 8.0
- Lombok
- SpringDoc OpenAPI (Swagger)
- iText 8 (generación de PDF)
- JUnit 5
- Docker

## Características

- Gestión de pedidos (crear, listar, detalle, confirmar, completar, cancelar)
- Integración con ms_products para obtener información de productos
- API REST versionada bajo `/api/v1/orders`
- Autenticación mediante OAuth2 Resource Server (JWT Bearer Token de Microsoft Entra ID)
- Documentación API con Swagger/OpenAPI
- Validación de datos con Jakarta Validation
- Manejo centralizado de excepciones
- Soporte para CORS
- Generación de comprobantes PDF bajo demanda
- Tests unitarios y de integración
- Contenedor Docker con docker-compose

## Arquitectura

El microservicio sigue la arquitectura estándar de Spring Boot:

```
Controller -> Service -> Repository -> Model
```

Las entidades JPA nunca se exponen directamente; se utilizan DTOs para las solicitudes y respuestas.

## Endpoints

### Pedidos

- `GET /api/v1/orders` - Obtener todos los pedidos
- `GET /api/v1/orders?status={status}` - Obtener pedidos por estado
- `GET /api/v1/orders?customerName={name}` - Obtener pedidos por nombre de cliente
- `GET /api/v1/orders?customerEmail={email}` - Obtener pedidos por email de cliente
- `GET /api/v1/orders/{id}` - Obtener pedido por ID
- `POST /api/v1/orders` - Crear nuevo pedido
- `POST /api/v1/orders/{id}/confirm` - Confirmar pedido
- `POST /api/v1/orders/{id}/complete` - Completar pedido
- `POST /api/v1/orders/{id}/cancel` - Cancelar pedido
- `GET /api/v1/orders/{id}/receipt` - Obtener comprobante PDF

Todos los endpoints requieren autenticación mediante OAuth2 Access Token de Microsoft Entra ID.

## Estados de Pedidos

Los pedidos pueden tener los siguientes estados:

- `CREATED` - Pedido creado inicialmente
- `CONFIRMED` - Pedido confirmado
- `COMPLETED` - Pedido completado
- `CANCELLED` - Pedido cancelado

### Transiciones Permitidas

- `CREATED` → `CONFIRMED`
- `CREATED` → `CANCELLED`
- `CONFIRMED` → `COMPLETED`
- `CONFIRMED` → `CANCELLED`

## Configuración

### Variables de Entorno

El servicio utiliza las siguientes variables de entorno (ver `.env.example`):

- `SERVER_PORT` - Puerto del servidor (default: 8082)
- `DB_HOST` - Host de base de datos MySQL
- `DB_PORT` - Puerto de base de datos MySQL (default: 3306)
- `DB_USER` - Usuario de base de datos
- `DB_PASSWORD` - Contraseña de base de datos
- `MS_PRODUCTS_BASE_URL` - URL base del servicio ms_products
- `CORS_ALLOWED_ORIGINS` - Orígenes permitidos para CORS
- `OAUTH2_ISSUER_URI` - URI del emisor OAuth2 (Microsoft Entra ID)
- `OAUTH2_AUDIENCE` - Audiencia OAuth2 (ID de cliente de la aplicación)
- `OAUTH2_AUDIENCE_ID` - ID de cliente de la aplicación (formato raw sin prefijo)

### Base de Datos

El esquema de base de datos se encuentra en `src/main/resources/database/init.sql` y se inicializa automáticamente al iniciar el servicio.

#### Tablas

- `orders` - Almacena información de pedidos
- `order_items` - Almacena items de pedidos con snapshots históricos

## Instalación y Ejecución

### Requisitos Previos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker (opcional, para ejecución con contenedores)

### Ejecución Local

1. Clonar el repositorio
2. Configurar las variables de entorno en `.env` (basado en `.env.example`)
3. Ejecutar con Maven:

```bash
mvn spring-boot:run
```

### Ejecución con Docker

1. Construir la imagen Docker:

```bash
docker build -t ms-orders .
```

2. Ejecutar con docker-compose:

```bash
docker-compose up -d
```

### Ejecución de Tests

```bash
mvn test
```

### Build del Proyecto

```bash
mvn clean package
```

## Documentación API

La documentación Swagger está disponible en:

```
http://localhost:8082/swagger-ui.html
```

## Seguridad

- El servicio utiliza OAuth2 Resource Server para validar tokens JWT de Microsoft Entra ID
- Los endpoints protegidos requieren un Access Token válido en el header `Authorization: Bearer <token>`
- El ID Token no es aceptado como sustituto del Access Token
- CORS está configurado para permitir orígenes específicos (configurable en producción)

## Integración con ms_products

Este microservicio depende de `ms_products` para:

- Obtener información de productos (nombre, precio final, disponibilidad)
- Validar que los productos estén disponibles antes de crear pedidos
- Almacenar snapshots históricos de precios y nombres de productos

No consulta directamente `products_db` ni duplica la lógica de precios de `ms_products`.

## Comprobantes PDF

Los comprobantes PDF se generan bajo demanda cuando se solicita el endpoint `/api/v1/orders/{id}/receipt`. El PDF incluye:

- Nombre PrintWorks
- Número visible del pedido (formato PW-XXXXXX)
- Fecha y estado del pedido
- Información del cliente (nombre y email)
- Lista de productos con cantidades, precios unitarios y subtotales
- Total del pedido
- Información de cancelación cuando corresponda
- Nota: "Este comprobante no constituye un documento tributario"

Los comprobantes no se persisten permanentemente en base de datos o filesystem.

## Tests

El proyecto incluye:

- Tests unitarios de servicios (Mockito)
- Tests de seguridad de API (Spring Security Test)
- Tests de validación de DTOs
- Tests de integración con H2 Database
- Tests de generación de PDF
- Tests de transiciones de estados

## Estructura del Proyecto

```
ms-orders/
├── src/main/java/duoc/cn1/ms_orders/
│   ├── MsOrdersApplication.java
│   ├── client/
│   │   ├── ProductServiceClient.java
│   │   └── ProductDto.java
│   ├── config/
│   │   ├── OpenApiConfig.java
│   │   └── RestTemplateConfig.java
│   ├── controller/
│   │   └── OrderController.java
│   ├── dto/
│   │   ├── request/
│   │   │   ├── OrderCreateRequest.java
│   │   │   └── OrderItemRequest.java
│   │   └── response/
│   │       ├── OrderResponse.java
│   │       ├── OrderSummaryResponse.java
│   │       └── OrderItemResponse.java
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorResponse.java
│   │   ├── OrderNotFoundException.java
│   │   ├── InvalidOrderTransitionException.java
│   │   ├── InvalidQuantityException.java
│   │   ├── TooManyDistinctProductsException.java
│   │   ├── DuplicateProductException.java
│   │   ├── ProductNotFoundException.java
│   │   ├── ProductNotAvailableException.java
│   │   ├── ProductServiceUnavailableException.java
│   │   ├── InvalidProductResponseException.java
│   │   └── InvalidCustomerDataException.java
│   ├── model/
│   │   ├── Order.java
│   │   ├── OrderItem.java
│   │   └── OrderStatus.java
│   ├── repository/
│   │   ├── OrderRepository.java
│   │   └── OrderItemRepository.java
│   ├── security/
│   │   └── SecurityConfig.java
│   └── service/
│       ├── OrderService.java
│       └── PdfService.java
├── src/main/resources/
│   ├── application.yml
│   └── database/
│       └── init.sql
├── src/test/
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```

## Notas Importantes

- Este microservicio es propietario de `orders_db` y administra exclusivamente la gestión de pedidos
- Depende de `ms_products` para obtener información confiable de productos
- Almacena snapshots históricos de precios y nombres de productos que no cambian con el tiempo
- Para este MVP, es un microservicio de gestión administrativa de pedidos para `front_admin`
- No implementa funcionalidad de "Mis pedidos" para clientes autenticados
- No utiliza Cognito; utiliza exclusivamente Microsoft Entra ID
- Los datos del cliente (customerName, customerEmail) son datos comerciales del pedido, no identidad de autorización
- El Access Token de Entra ID identifica al usuario administrativo que utiliza el sistema

## Distribución Local

Para desarrollo local, los microservicios se distribuyen en los siguientes puertos:

- `ms_config` → localhost:8080
- `ms_products` → localhost:8081
- `ms_orders` → localhost:8082

## Licencia

Apache 2.0

## Contacto

Duoc UC - Desarrollo Cloud Native 1
