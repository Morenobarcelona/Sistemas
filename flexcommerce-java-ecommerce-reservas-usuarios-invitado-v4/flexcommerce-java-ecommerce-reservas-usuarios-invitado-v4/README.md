# FlexCommerce Java v4 - Compra invitado + usuarios opcionales

Proyecto Java web listo para abrir en Eclipse como **Maven Project**.

Incluye una tienda ecommerce adaptable a distintos negocios, carrito de compras en Java por sesión HTTP, compra como invitado sin registro, registro/login opcional de clientes, calendario anual de reservas con citas ocupadas, panel de administración con login, CRUD de productos, gestión de pedidos y gestión de reservas.

## Punto importante: compra sin registro y simultaneidad

No es obligatorio registrarse para comprar.

El sistema permite dos formas de compra:

1. **Compra como invitado**: el cliente compra sin cuenta, rellenando nombre, email, teléfono y dirección/comentarios.
2. **Compra con cuenta**: el cliente se registra o inicia sesión y el pedido queda asociado a su cuenta.

La simultaneidad de clientes invitados está resuelta con **sesiones HTTP independientes**. Cada navegador/cliente recibe su propia sesión y su propio carrito en servidor:

```text
Cliente invitado A -> Session A -> Carrito A
Cliente invitado B -> Session B -> Carrito B
Cliente registrado C -> Session C -> Carrito C
```

Los carritos no se mezclan aunque varios clientes anónimos compren al mismo tiempo.

Además, en checkout se usa bloqueo pesimista de producto mediante JPA para reducir el riesgo de vender el mismo stock dos veces si dos clientes compran exactamente a la vez.

## Tecnologías usadas

- Java 17 o superior
- Spring Boot 3.5.0
- Spring MVC
- Spring Security
- Thymeleaf
- Spring Data JPA
- Hibernate ORM
- H2 Database persistente en archivo
- MySQL Connector/J preparado para migración futura
- HTML5, CSS3 y JavaScript
- Maven
- Tomcat embebido

## Funcionalidades

### Web pública

- Página de inicio adaptable a cualquier negocio.
- Catálogo de productos y servicios.
- Carrito de compras en sesión HTTP Java.
- Botón para seguir comprando después de añadir productos.
- Checkout con dos modos: invitado o cliente registrado.
- Registro de cuenta de cliente.
- Login de cliente y administrador.
- Área `Mi cuenta` para ver pedidos asociados al usuario.
- Sistema de reserva de citas.
- Calendario anual con días ocupados y horas reservadas visibles.
- Bloqueo de horas ya reservadas.
- Validación backend para evitar solapes aunque el usuario manipule el navegador.

### Usuarios incluidos

Administrador demo:

```text
Usuario: admin
Contraseña: admin123
```

Cliente demo:

```text
Usuario: cliente@demo.com
Contraseña: cliente123
```

### Panel administrador

Ruta:

```text
/admin
```

Incluye:

- Dashboard de productos, reservas y pedidos.
- CRUD de productos.
- Alta, edición, activación, desactivación y borrado de productos.
- Gestión de stock, SKU, IVA, categoría, precio e imagen.
- Gestión de reservas.
- Cambio de estado de reserva: PENDIENTE, CONFIRMADA, REALIZADA, CANCELADA.
- Gestión de pedidos.
- Identificación de pedidos de invitado o de cliente registrado.
- Cambio de estado de pedido: RECIBIDO, PENDIENTE_PAGO, PAGADO, EN_PREPARACION, ENVIADO, ENTREGADO, CANCELADO.

### API REST preparada para app móvil futura

Rutas incluidas:

```text
GET  /api/products
GET  /api/appointments/occupied?date=YYYY-MM-DD
POST /api/appointments
```

Ejemplo JSON para crear reserva:

```json
{
  "customerName": "Cliente Prueba",
  "email": "cliente@example.com",
  "phone": "600000000",
  "serviceType": "Consulta inicial",
  "appointmentDate": "2026-06-01",
  "appointmentTime": "10:00",
  "comments": "Reserva desde API"
}
```

## Cómo ejecutarlo en Eclipse

1. Descomprime el ZIP.
2. Abre Eclipse.
3. Ve a `File > Import > Maven > Existing Maven Projects`.
4. Selecciona la carpeta donde está `pom.xml`.
5. Pulsa `Finish`.
6. Espera a que Maven descargue dependencias.
7. Ejecuta:

```text
src/main/java/com/alfonso/flexcommerce/FlexCommerceApplication.java
```

Con:

```text
Run As > Java Application
```

8. Abre:

```text
http://localhost:8081
```

## Rutas principales

```text
/                  Inicio
/productos         Tienda
/carrito           Carrito
/checkout          Finalizar compra invitado o registrado
/registro          Crear cuenta de cliente
/login             Login cliente/admin
/mi-cuenta         Área cliente
/reservas          Reserva de citas
/admin             Panel administrador
/h2-console        Consola base de datos H2
/api/products      API productos
```

## Base de datos H2

La aplicación usa H2 persistente en archivo:

```text
jdbc:h2:file:./data/flexcommerce
```

Esto significa que productos, usuarios, pedidos y reservas se guardan aunque cierres la aplicación.

Consola H2:

```text
http://localhost:8081/h2-console
```

Datos:

```text
JDBC URL: jdbc:h2:file:./data/flexcommerce
User: sa
Password: dejar vacío
```

## Migración futura a MySQL

En `application-mysql.properties` tienes preparada la configuración para MySQL.

Ejemplo:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/flexcommerce?useSSL=false&serverTimezone=Europe/Madrid&allowPublicKeyRetrieval=true
spring.datasource.username=root
spring.datasource.password=tu_password
spring.datasource.driverClassName=com.mysql.cj.jdbc.Driver
spring.jpa.database-platform=org.hibernate.dialect.MySQLDialect
spring.jpa.hibernate.ddl-auto=update
```

Antes crea la base de datos:

```sql
CREATE DATABASE flexcommerce CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

## Estructura del proyecto

```text
src/main/java/com/alfonso/flexcommerce
├── config
│   ├── DataInitializer.java
│   └── SecurityConfig.java
├── controller
│   ├── AccountController.java
│   ├── AdminController.java
│   ├── ApiController.java
│   ├── AppointmentController.java
│   ├── AuthController.java
│   ├── CartController.java
│   ├── CheckoutController.java
│   ├── GlobalModelAttributes.java
│   ├── HomeController.java
│   └── ProductController.java
├── model
│   ├── AppUser.java
│   ├── Appointment.java
│   ├── CustomerOrder.java
│   ├── OrderLine.java
│   ├── Product.java
│   └── ServiceItem.java
├── repository
│   ├── AppUserRepository.java
│   ├── AppointmentRepository.java
│   ├── CustomerOrderRepository.java
│   ├── ProductRepository.java
│   └── ServiceItemRepository.java
└── web
    ├── Cart.java
    └── CartLine.java
```

## Próximas mejoras recomendadas

- Facturas PDF.
- Envío de email al cliente.
- Pasarela de pago Stripe, PayPal o Redsys.
- Control de roles EMPLEADO y permisos más finos.
- Recuperación de contraseña.
- Dockerfile y docker-compose.
- App Android consumiendo la API REST.
