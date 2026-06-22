# Sistema de Pólizas de Faltantes — Backend

API REST para la gestión de pólizas de faltantes en inventario. Permite registrar empleados, controlar el stock de artículos y generar pólizas que descuentan automáticamente la cantidad faltante del inventario.

## Stack tecnológico

| Capa | Tecnología |
|---|---|
| Lenguaje | Java 21 |
| Framework | Spring Boot 3.4.x |
| Base de datos | MySQL |
| ORM | Spring Data JPA / Hibernate |
| Seguridad | Spring Security + JWT (jjwt) |
| Testing | JUnit 5 + Mockito |
| Logs | SLF4J + Logback |
| Build | Maven |

---

## Requisitos previos

- Java 21
- Maven 3.8+
- MySQL 8+

---

## Configuración de la base de datos

Crea la base de datos en MySQL:

```sql
CREATE DATABASE polizas;
```

Crea las tablas:

```sql
CREATE TABLE inventario (
    sku VARCHAR(50) PRIMARY KEY NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    cantidad INT NOT NULL
);

CREATE TABLE empleados (
    id_empleado BIGINT PRIMARY KEY AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    puesto VARCHAR(50) NOT NULL,
    creado_en DATETIME,
    actualizado_en DATETIME,
    activo BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE polizas (
    id_poliza BIGINT PRIMARY KEY AUTO_INCREMENT,
    id_empleado BIGINT,
    sku VARCHAR(50),
    cantidad_faltante INT NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    actualizado_en DATETIME NOT NULL,
    CONSTRAINT fk_empleado_poliza FOREIGN KEY (id_empleado) REFERENCES empleados(id_empleado),
    CONSTRAINT fk_sku_poliza FOREIGN KEY (sku) REFERENCES inventario(sku)
);
```

---

## Configuración del proyecto

Crea el archivo `src/main/resources/application-dev.properties` con tus credenciales locales:

```properties
server.port=8080

spring.datasource.url=jdbc:mysql://localhost:3306/polizas?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
spring.datasource.username=tu_usuario
spring.datasource.password=tu_contraseña
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

spring.jackson.deserialization.fail-on-unknown-properties=false
spring.jackson.mapper.accept-case-insensitive-enums=true

jwt.secret=tu-clave-secreta-de-minimo-32-caracteres
jwt.expiration=3600000
```

> El archivo `application-dev.properties` está en `.gitignore` para proteger las credenciales. El `application.properties` en el repo solo activa el perfil `dev`.

---

## Ejecución

```bash
mvn spring-boot:run
```

El servidor levanta en `http://localhost:8080`.

---

## Credenciales de acceso

El sistema usa un usuario administrador hardcodeado — no requiere tabla de usuarios:

```
Usuario: admin
Contraseña: admin123
```

---

## Arquitectura

```
com.rdzvn.polizasdefaltantes/
├── config/          # Configuración de CORS y seguridad
├── controller/      # Endpoints REST — sin lógica de negocio
├── service/         # Lógica de negocio y reglas de dominio
├── repository/      # Acceso a datos — solo queries
├── entity/          # Entidades JPA
├── dto/
│   ├── request/     # DTOs de entrada — lo que recibe la API
│   └── response/    # DTOs de salida — lo que expone la API
├── exception/       # Excepciones custom y manejador global
├── security/        # Filtro JWT, UserDetailsService, configuración
└── PolizasDefaltantesApplication.java
```

### Reglas de arquitectura

- El **Controller** nunca contiene lógica de negocio
- El **Service** nunca accede al `HttpRequest`
- El **Repository** solo contiene acceso a datos
- Las **Entities** nunca se exponen directamente — siempre se usan DTOs
- Toda respuesta sigue el estándar `ApiResponse<T>`:

```json
{
  "exito": true,
  "mensaje": "OK",
  "datos": {},
  "timestamp": "2026-06-10T23:02:30"
}
```

---

## Endpoints

Todos los endpoints requieren el header `Authorization: Bearer <token>` excepto el login.

### Auth

| Método | Endpoint | Auth | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | No | Genera y retorna JWT |

**Body:**
```json
{
  "username": "admin",
  "password": "admin123"
}
```

**Respuesta:**
```json
{
  "token": "eyJhbGci..."
}
```

### Empleados

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/empleados` | Listar todos los activos |
| POST | `/api/v1/empleados` | Crear |
| PUT | `/api/v1/empleados/{id}` | Actualizar |
| DELETE | `/api/v1/empleados/{id}` | Soft delete |

### Inventario

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/inventario` | Listar todos |
| POST | `/api/v1/inventario` | Crear |
| PUT | `/api/v1/inventario/{sku}` | Actualizar |
| DELETE | `/api/v1/inventario/{sku}` | Eliminar |

### Pólizas

| Método | Endpoint | Descripción |
|---|---|---|
| GET | `/api/v1/polizas` | Listar todas |
| POST | `/api/v1/polizas` | Crear (descuenta inventario) |
| PUT | `/api/v1/polizas/{id}` | Actualizar (recalcula inventario) |
| DELETE | `/api/v1/polizas/{id}` | Eliminar (restaura inventario) |

---

## Reglas de negocio

### Al crear una póliza
1. Verifica que el empleado existe y está activo
2. Verifica que el artículo (SKU) existe
3. Verifica que `inventario.cantidad >= cantidadFaltante`
4. Descuenta la cantidad del inventario
5. Persiste la póliza
6. Registra log de la operación

### Al eliminar una póliza
1. Verifica que la póliza existe
2. Restaura la cantidad al inventario
3. Elimina la póliza
4. Registra log de la operación

### Al actualizar una póliza
1. Verifica que la póliza existe
2. Si el SKU es el mismo — ajusta el inventario según la diferencia
3. Si el SKU cambió — restaura el inventario anterior y descuenta del nuevo
4. Verifica que el inventario no quede negativo
5. Persiste los cambios
6. Registra log de la operación

---

## Seguridad

- Endpoint público: `POST /api/auth/login`
- Todos los demás endpoints requieren `Authorization: Bearer <token>`
- Token JWT con expiración configurable (`jwt.expiration` en ms)
- CORS configurado para permitir peticiones desde `http://localhost:4200`
- Sesiones stateless — el servidor no guarda estado de autenticación

---

## Pruebas unitarias

El proyecto cuenta con 10 pruebas unitarias sobre la capa Service usando JUnit 5 y Mockito:

**PolizaService:**
- `debeCrearPoliza_cuandoInventarioEsSuficiente`
- `debeLanzarExcepcion_cuandoInventarioEsInsuficiente`
- `debeLanzarExcepcion_cuandoEmpleadoNoExiste`
- `debeLanzarExcepcion_cuandoSkuNoExiste`
- `debeEliminarPoliza_yRestaurarInventario`
- `debeLanzarExcepcion_cuandoPolizaNoExisteAlEliminar`
- `debeActualizarPoliza_cuandoMismoSkuYCantidadMenor`
- `debeActualizarPoliza_cuandoMismoSkuYCantidadMayor`
- `debeActualizarPoliza_cuandoSkuDiferente`
- `debeLanzarExcepcion_cuandoPolizaNoExisteAlActualizar`

Para ejecutar las pruebas:

```bash
mvn test
```

---

## Logging

Las operaciones críticas de pólizas se registran con SLF4J:

| Nivel | Cuándo |
|---|---|
| `INFO` | Inicio y fin de operaciones exitosas |
| `WARN` | Reglas de negocio violadas (inventario insuficiente, etc.) |
| `ERROR` | Excepciones inesperadas |

