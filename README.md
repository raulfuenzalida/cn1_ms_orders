# MS-Orders

Microservicio de gestión de pedidos para **PrintWorks**.

Es responsable de la creación, consulta y gestión del ciclo de vida de los pedidos, incluyendo sus ítems, cantidades, snapshots históricos de productos, subtotales, total, estados y generación de comprobantes PDF.

## Tecnologías

- Java 21
- Spring Boot 3.4.11
- Maven
- Spring Data JPA
- Spring Security con OAuth2 Resource Server
- MySQL 8.0
- H2 Database para pruebas
- Lombok
- Jakarta Validation
- SpringDoc OpenAPI (Swagger)
- iText 8 para generación de PDF
- JUnit 5
- Mockito
- Docker

## Características

- Creación de pedidos
- Listado y consulta de pedidos
- Búsqueda por estado, nombre y correo del cliente
- Confirmación, completado y cancelación de pedidos
- Integración REST con `ms_products`
- Validación de existencia y disponibilidad de productos
- Cálculo de subtotales y total en el backend
- Snapshots históricos del nombre y precio de los productos
- Estados y transiciones controladas de pedidos
- Generación de comprobantes PDF bajo demanda
- API REST versionada bajo `/api/v1/orders`
- Autenticación mediante OAuth2 Resource Server
- JWT Bearer Token de Microsoft Entra ID
- Validación de datos mediante Jakarta Validation
- Manejo centralizado de excepciones
- Soporte para CORS
- Documentación mediante Swagger/OpenAPI
- Tests unitarios, de validación, contexto y generación de PDF
- Soporte para ejecución mediante Docker

## Arquitectura

El microservicio utiliza una arquitectura por capas:

```text
Controller
    ↓
Service
    ↓
Repository
    ↓
Model
```

Para la creación de pedidos existe además comunicación con `ms_products`:

```text
front_admin
     │
     ▼
 ms_orders
     │
     │ REST
     ▼
ms_products
```

Las entidades JPA no se exponen directamente mediante la API. Las solicitudes y respuestas utilizan DTOs.

`ms_orders` es propietario exclusivamente de `orders_db` y no accede directamente a `products_db` ni `config_db`.

## Endpoints

### Pedidos

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/orders` | Obtener todos los pedidos |
| GET | `/api/v1/orders?status={status}` | Filtrar pedidos por estado |
| GET | `/api/v1/orders?customerName={name}` | Buscar pedidos por nombre de cliente |
| GET | `/api/v1/orders?customerEmail={email}` | Buscar pedidos por correo del cliente |
| GET | `/api/v1/orders/{id}` | Obtener detalle de un pedido |
| POST | `/api/v1/orders` | Crear un pedido |
| POST | `/api/v1/orders/{id}/confirm` | Confirmar un pedido |
| POST | `/api/v1/orders/{id}/complete` | Completar un pedido |
| POST | `/api/v1/orders/{id}/cancel` | Cancelar un pedido |
| GET | `/api/v1/orders/{id}/receipt` | Generar comprobante PDF |

Los endpoints de `/api/v1/orders/**` requieren autenticación mediante un **OAuth2 Access Token** válido de Microsoft Entra ID.

## Creación de Pedidos

Ejemplo de solicitud:

```json
{
  "customerName": "Cliente Prueba",
  "customerEmail": "cliente@printworks.cl",
  "items": [
    {
      "idProduct": 1,
      "quantity": 2
    }
  ]
}
```

Al crear un pedido, `ms_orders`:

1. Valida los datos recibidos.
2. Comprueba que no existan productos duplicados.
3. Valida las cantidades solicitadas.
4. Consulta cada producto mediante `ms_products`.
5. Obtiene el nombre y precio vigente del producto.
6. Genera un snapshot del producto dentro del pedido.
7. Calcula cada subtotal.
8. Calcula el total del pedido.
9. Persiste el pedido y sus ítems en `orders_db`.
10. Asigna el estado inicial `CREATED`.

El precio del pedido **no es enviado ni calculado por el frontend**. El backend utiliza el precio obtenido desde `ms_products`.

## Snapshots de Productos

Cada `order_item` conserva información histórica del producto utilizado al momento de crear el pedido:

- ID del producto
- Nombre del producto
- Precio unitario
- Cantidad
- Subtotal

Esto permite que un pedido mantenga sus valores originales aunque posteriormente cambien el nombre, configuración o precio del producto en `ms_products`.

## Integración con ms_products

`ms_orders` depende de `ms_products` para obtener la información necesaria al crear un pedido.

La comunicación se realiza mediante:

```text
GET /api/v1/products/{id}
```

`ms_orders` utiliza la respuesta para obtener:

- ID del producto
- Nombre del producto
- Precio final vigente

El endpoint público de productos expone únicamente productos disponibles para catálogo, por lo que un producto inactivo o con precio desactualizado no puede utilizarse para crear un nuevo pedido.

`ms_orders` no:

- consulta directamente `products_db`;
- calcula el precio de los productos;
- duplica las reglas de cálculo de `ms_products`.

De esta manera se mantiene el principio **Database per Service**.

## Estados de Pedidos

Los pedidos pueden tener los siguientes estados:

- `CREATED` - Pedido creado
- `CONFIRMED` - Pedido confirmado
- `COMPLETED` - Pedido completado
- `CANCELLED` - Pedido cancelado

### Transiciones Permitidas

```text
CREATED ────────► CONFIRMED ────────► COMPLETED
   │                  │
   │                  │
   ▼                  ▼
CANCELLED          CANCELLED
```

Se permiten exclusivamente:

- `CREATED` → `CONFIRMED`
- `CREATED` → `CANCELLED`
- `CONFIRMED` → `COMPLETED`
- `CONFIRMED` → `CANCELLED`

Los estados `COMPLETED` y `CANCELLED` son terminales.

Por ejemplo, intentar cancelar un pedido `COMPLETED` genera un error de transición inválida y no modifica el pedido.

## Reglas de Negocio

Entre las principales reglas implementadas se encuentran:

- Un pedido debe contener al menos un producto.
- Un pedido puede contener como máximo 30 productos distintos.
- No se permiten productos duplicados dentro del mismo pedido.
- La cantidad de cada producto debe ser válida.
- El producto solicitado debe existir y estar disponible en `ms_products`.
- Los precios son obtenidos desde `ms_products`.
- Los subtotales son calculados por `ms_orders`.
- El total es calculado por `ms_orders`.
- El pedido comienza en estado `CREATED`.
- Solo se permiten las transiciones de estado definidas.
- Los pedidos completados o cancelados no pueden cambiar nuevamente de estado.

## Manejo de Errores

El microservicio implementa manejo centralizado de excepciones mediante `GlobalExceptionHandler`.

Entre los casos controlados se encuentran:

- Pedido inexistente
- Transición de estado inválida
- Cantidad inválida
- Exceso de productos distintos
- Productos duplicados
- Producto inexistente
- Producto no disponible
- Fallo de comunicación con `ms_products`
- Respuesta inválida de `ms_products`
- Datos inválidos de la solicitud

Ejemplo de respuesta de error:

```json
{
  "timestamp": "2026-09-13T23:44:43",
  "status": 404,
  "error": "Not Found",
  "code": "ORDER_NOT_FOUND",
  "message": "No se encontró el pedido solicitado con ID: 999",
  "path": "/api/v1/orders/999/cancel"
}
```

## Comprobantes PDF

Los comprobantes se generan bajo demanda mediante:

```text
GET /api/v1/orders/{id}/receipt
```

El PDF incluye:

- Nombre de PrintWorks
- Número visible del pedido
- Fecha
- Estado
- Nombre del cliente
- Correo del cliente
- Productos
- ID de producto
- Precio unitario
- Cantidad
- Subtotal
- Total
- Información correspondiente al estado del pedido

El número visible utiliza el formato:

```text
PW-000001
```

El documento incluye además la leyenda:

> Este comprobante no constituye un documento tributario.

Los PDF no se almacenan permanentemente en la base de datos ni en el filesystem. Se generan al momento de ser solicitados.

## Configuración

### Variables de Entorno

El servicio utiliza las siguientes variables, documentadas en `.env.example`:

| Variable | Descripción |
|---|---|
| `SERVER_PORT` | Puerto del microservicio. Por defecto `8082` |
| `DB_HOST` | Host de MySQL |
| `DB_PORT` | Puerto de MySQL. Por defecto `3306` |
| `DB_USER` | Usuario de `orders_db` |
| `DB_PASSWORD` | Contraseña de la base de datos |
| `MS_PRODUCTS_BASE_URL` | URL base de `ms_products` |
| `CORS_ALLOWED_ORIGINS` | Orígenes permitidos por CORS |
| `OAUTH2_ISSUER_URI` | Issuer URI de Microsoft Entra ID |
| `OAUTH2_AUDIENCE` | Audience OAuth2 en formato `api://...` |
| `OAUTH2_AUDIENCE_ID` | ID de la aplicación sin prefijo |

Ejemplo para desarrollo local:

```env
SERVER_PORT=8082

DB_HOST=localhost
DB_PORT=3306
DB_USER=orders_user
DB_PASSWORD=orders_password

MS_PRODUCTS_BASE_URL=http://localhost:8081

CORS_ALLOWED_ORIGINS=http://localhost:5173,https://raulfuenzalida.github.io

OAUTH2_ISSUER_URI=https://login.microsoftonline.com/YOUR_TENANT_ID/v2.0
OAUTH2_AUDIENCE=api://YOUR_CLIENT_ID
OAUTH2_AUDIENCE_ID=YOUR_CLIENT_ID
```

## Base de Datos

El microservicio utiliza su propia base de datos:

```text
orders_db
```

El script SQL se encuentra en:

```text
src/main/resources/database/init.sql
```

Su ejecución depende de la configuración de inicialización SQL utilizada en el entorno.

### Tablas principales

#### `orders`

Almacena la información general del pedido, incluyendo:

- cliente;
- correo;
- estado;
- total;
- fecha de creación;
- última actualización;
- fecha de confirmación;
- fecha de completado;
- fecha de cancelación.

#### `order_items`

Almacena los ítems y snapshots históricos asociados a cada pedido:

- producto;
- nombre del producto;
- precio unitario;
- cantidad;
- subtotal.

## Seguridad

El servicio utiliza **Spring Security OAuth2 Resource Server** para validar JWT emitidos por Microsoft Entra ID.

Las solicitudes protegidas utilizan:

```http
Authorization: Bearer <access_token>
```

El sistema utiliza **Access Token** para consumir la API. El ID Token no sustituye al Access Token.

Swagger permite autorizar las solicitudes protegidas utilizando un Bearer Token para realizar pruebas sobre los endpoints.

## CORS

Los orígenes permitidos se configuran mediante:

```text
CORS_ALLOWED_ORIGINS
```

Para desarrollo se contempla el frontend administrativo ejecutándose en:

```text
http://localhost:5173
```

## Distribución Local

La distribución utilizada para desarrollo es:

| Componente | Puerto |
|---|---:|
| `ms_config` | 8080 |
| `ms_products` | 8081 |
| `ms_orders` | 8082 |
| `front_admin` | 5173 |

Para crear pedidos, `ms_orders` debe poder comunicarse con `ms_products`.

## Instalación y Ejecución

### Requisitos Previos

- Java 21
- Maven 3.9+
- MySQL 8.0+
- Docker, opcional

### Ejecución Local

1. Clonar el repositorio.
2. Crear/configurar `.env` utilizando `.env.example` como referencia.
3. Configurar `orders_db`.
4. Asegurarse de que `ms_products` esté disponible.
5. Ejecutar:

```bash
mvn spring-boot:run
```

El servicio estará disponible por defecto en:

```text
http://localhost:8082
```

## Docker

### Construcción

```bash
docker build -t ms-orders .
```

### Docker Compose

```bash
docker-compose up -d
```

## Build

```bash
mvn clean package
```

## Tests

Para ejecutar la suite completa:

```bash
mvn test
```

La suite actual contempla:

- Tests unitarios de `OrderService`
- Tests de reglas de negocio
- Tests de transiciones de estados
- Tests de validación de DTOs
- Tests de generación de PDF
- Test de carga del contexto de Spring Boot
- H2 Database para el contexto de pruebas
- Mockito para aislar dependencias en pruebas unitarias

### Estado validado

La suite revisada actualmente contiene:

```text
OrderServiceTest          12/12
PdfServiceTest             5/5
OrderDtoValidationTest     9/9
MsOrdersApplicationTests   1/1
                         ─────
Total                     27/27
```

**27 tests ejecutados correctamente, sin fallos.**

Además de los tests automatizados, se verificó manualmente el flujo de integración:

```text
Crear pedido
     ↓
Consultar ms_products
     ↓
Guardar pedido + snapshots
     ↓
CREATED
     ↓
Confirmar / Cancelar
     ↓
CONFIRMED
     ↓
Completar / Cancelar
     ↓
COMPLETED / CANCELLED
     ↓
Generar comprobante PDF
```

También se verificaron respuestas de error para pedidos inexistentes, productos no disponibles y transiciones de estado inválidas.

## Swagger / OpenAPI

La interfaz Swagger está disponible localmente en:

```text
http://localhost:8082/swagger-ui/index.html
```

Desde Swagger pueden probarse los endpoints protegidos utilizando la opción **Authorize** con un Access Token válido.

## Integración con front_admin

El frontend administrativo consume este microservicio para:

- listar pedidos;
- visualizar detalles;
- confirmar pedidos;
- completar pedidos;
- cancelar pedidos;
- solicitar comprobantes PDF.

En desarrollo local:

```text
front_admin :5173
       │
       │ /api/v1/orders/*
       ▼
  Vite Proxy
       │
       ▼
 ms_orders :8082
```

## Estructura del Proyecto

```text
cn1_ms_orders/
├── src/
│   ├── main/
│   │   ├── java/duoc/cn1/ms_orders/
│   │   │   ├── MsOrdersApplication.java
│   │   │   ├── client/
│   │   │   │   ├── ProductServiceClient.java
│   │   │   │   └── ProductDto.java
│   │   │   ├── config/
│   │   │   │   ├── OpenApiConfig.java
│   │   │   │   └── RestTemplateConfig.java
│   │   │   ├── controller/
│   │   │   │   └── OrderController.java
│   │   │   ├── dto/
│   │   │   │   ├── request/
│   │   │   │   │   ├── OrderCreateRequest.java
│   │   │   │   │   └── OrderItemRequest.java
│   │   │   │   └── response/
│   │   │   │       ├── OrderItemResponse.java
│   │   │   │       ├── OrderResponse.java
│   │   │   │       └── OrderSummaryResponse.java
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   │   ├── Order.java
│   │   │   │   ├── OrderItem.java
│   │   │   │   └── OrderStatus.java
│   │   │   ├── repository/
│   │   │   │   ├── OrderItemRepository.java
│   │   │   │   └── OrderRepository.java
│   │   │   ├── security/
│   │   │   │   └── SecurityConfig.java
│   │   │   └── service/
│   │   │       ├── OrderService.java
│   │   │       └── PdfService.java
│   │   └── resources/
│   │       ├── application.yml
│   │       └── database/
│   │           └── init.sql
│   └── test/
│       └── java/duoc/cn1/ms_orders/
│           ├── MsOrdersApplicationTests.java
│           ├── dto/
│           │   └── OrderDtoValidationTest.java
│           └── service/
│               ├── OrderServiceTest.java
│               └── PdfServiceTest.java
├── Dockerfile
├── docker-compose.yml
├── .dockerignore
├── .env.example
├── pom.xml
└── README.md
```

## Notas Importantes

- `ms_orders` es propietario de `orders_db`.
- No accede directamente a las bases de datos de otros microservicios.
- Depende de `ms_products` para obtener información confiable de los productos.
- El backend calcula subtotales y total del pedido.
- Los precios no son confiados al frontend.
- Los snapshots permiten conservar el valor histórico de cada pedido.
- Los estados `COMPLETED` y `CANCELLED` son terminales.
- Para este MVP, los pedidos son gestionados administrativamente mediante `front_admin`.
- No se implementa una sección de "Mis pedidos" para clientes autenticados.
- No utiliza Amazon Cognito.
- La autenticación administrativa utiliza Microsoft Entra ID.
- `customerName` y `customerEmail` representan datos comerciales del pedido y no la identidad utilizada para autorización.
- El Access Token identifica y autoriza al usuario administrativo que consume la API.

## Licencia

Apache 2.0

## Contacto

Duoc UC - Desarrollo Cloud Native I