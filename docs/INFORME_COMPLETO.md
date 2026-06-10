# Informe técnico completo — Colegio Gestión de Acceso

**Versión del documento:** 1.0  
**Fecha:** Junio 2026  
**Proyecto:** Sistema de control de acceso escolar con notificaciones WhatsApp  
**Repositorio:** `Gestion_De_Estudiantes-main`

---

## Tabla de contenidos

1. [Resumen ejecutivo](#1-resumen-ejecutivo)
2. [Objetivo y alcance](#2-objetivo-y-alcance)
3. [Arquitectura del sistema](#3-arquitectura-del-sistema)
4. [Stack tecnológico](#4-stack-tecnológico)
5. [Estructura del repositorio](#5-estructura-del-repositorio)
6. [Backend — API Spring Boot](#6-backend--api-spring-boot)
7. [Frontend — Angular](#7-frontend--angular)
8. [WhatsApp Bridge](#8-whatsapp-bridge)
9. [Base de datos](#9-base-de-datos)
10. [Seguridad y autenticación](#10-seguridad-y-autenticación)
11. [Referencia completa de la API](#11-referencia-completa-de-la-api)
12. [Flujos de negocio](#12-flujos-de-negocio)
13. [Configuración y variables de entorno](#13-configuración-y-variables-de-entorno)
14. [Instalación y ejecución](#14-instalación-y-ejecución)
15. [Docker y despliegue](#15-docker-y-despliegue)
16. [CI/CD](#16-cicd)
17. [Pruebas](#17-pruebas)
18. [Monitoreo y operación](#18-monitoreo-y-operación)
19. [Limitaciones conocidas](#19-limitaciones-conocidas)
20. [Mejoras futuras recomendadas](#20-mejoras-futuras-recomendadas)
21. [Apéndices](#21-apéndices)

---

## 1. Resumen ejecutivo

**Colegio — Gestión de Acceso** es una aplicación web para colegios que permite:

- Registrar **entradas, salidas, tardanzas e inasistencias** de alumnos desde portería.
- Gestionar el **padrón de alumnos** con datos de contacto de padres.
- Enviar **notificaciones automáticas por WhatsApp** a los padres al registrar cada evento.
- Consultar **historial, dashboard diario y log de notificaciones**.
- Operar con **roles** (administrador y personal de portería) y **autenticación JWT**.

El sistema está compuesto por **tres servicios** que se ejecutan en paralelo:

| Servicio | Puerto | Función |
|---|---|---|
| Frontend Angular | 4200 | Interfaz web |
| Backend Spring Boot | 8080 | API REST + lógica de negocio |
| WhatsApp Bridge Node.js | 3001 | Envío de mensajes vía WhatsApp Web |

En desarrollo usa **SQLite**; en producción puede usar **PostgreSQL**. Incluye **Docker Compose**, **CI en GitHub Actions** y documentación de producción.

---

## 2. Objetivo y alcance

### 2.1 Problema que resuelve

Los colegios necesitan registrar quién entra y sale, controlar asistencia (tardanzas, faltas) e informar a los padres de forma oportuna. Este sistema centraliza ese registro y automatiza la comunicación.

### 2.2 Usuarios del sistema

| Rol | Permisos principales |
|---|---|
| **ADMIN** | Todo: CRUD alumnos, portería, historial, dashboard, crear usuarios |
| **PERSONAL** | Portería, consulta alumnos, historial, dashboard, notificaciones (sin crear/editar/eliminar alumnos) |

### 2.3 Funcionalidades implementadas

- Login con JWT (24 h de validez por defecto).
- CRUD de alumnos con validación DNI (8 dígitos) y teléfonos Perú.
- Portería: búsqueda por código y registro de:
  - `ENTRADA`
  - `SALIDA`
  - `TARDANZA` (detalle opcional)
  - `NO_ASISTIO` (confirmación + vista previa WhatsApp)
  - `NO_ASISTIO_CON_PERMISO` (motivo obligatorio + vista previa)
- WhatsApp automático a `telefonoPadre1` y `telefonoPadre2` (si existe).
- Log de notificaciones con estados y reintentos.
- Dashboard con métricas del día.
- Swagger / OpenAPI.
- Health check (Actuator).
- Soporte opcional para **WhatsApp Business API** (Meta) además del bridge local.

### 2.4 Fuera de alcance actual

- App móvil nativa para padres.
- QR / carnet físico del alumno (solo búsqueda manual por código).
- Anulación de registros o cola de envío con gracia (planificado como mejora).
- Multi-colegio / multi-sede.
- Reportes PDF o exportación Excel.

---

## 3. Arquitectura del sistema

### 3.1 Diagrama de componentes

```
┌─────────────────────────────────────────────────────────────────┐
│                        USUARIO (navegador)                       │
│                   http://127.0.0.1:4200                          │
└────────────────────────────┬────────────────────────────────────┘
                             │ HTTP + JWT
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              FRONTEND — colegio-gestion-acceso                   │
│              Angular 21 · SPA · Guards · Interceptors            │
└────────────────────────────┬────────────────────────────────────┘
                             │ REST /api/*
                             ▼
┌─────────────────────────────────────────────────────────────────┐
│              BACKEND — gestion-acceso-backend                    │
│              Spring Boot 3.5 · Java 21 · JPA · Security          │
│  ┌──────────┐  ┌──────────┐  ┌──────────────┐  ┌────────────┐ │
│  │ Alumnos  │  │ Accesos  │  │ Notificaciones│  │ Auth/JWT  │ │
│  └──────────┘  └──────────┘  └──────────────┘  └────────────┘ │
└──────────────┬─────────────────────────────┬────────────────────┘
               │ JDBC                         │ HTTP
               ▼                              ▼
┌──────────────────────────┐    ┌─────────────────────────────────┐
│  SQLite (dev)            │    │  WHATSAPP BRIDGE — Node.js      │
│  PostgreSQL (prod)       │    │  whatsapp-web.js + Puppeteer    │
│  alumnos.db              │    │  http://localhost:3001          │
└──────────────────────────┘    └────────────────┬────────────────┘
                                                 │
                                                 ▼
                                    ┌────────────────────────┐
                                    │  WhatsApp Web (Meta)    │
                                    │  → Teléfonos de padres  │
                                    └────────────────────────┘
```

### 3.2 Flujo de registro de acceso + WhatsApp

```
Portería (Angular)
    → POST /api/accesos { alumnoId, tipo, observacion? }
        → AccesoService.registrar()
            → Guarda Acceso en BD
            → NotificacionService.notificarAcceso()
                → Construye mensaje según tipo
                → Por cada teléfono del alumno:
                    → Crea Notificacion (PENDIENTE)
                    → WhatsAppSender.enviarMensaje()
                        → BridgeWhatsAppSender (default)
                            → POST http://localhost:3001/send
                                → whatsapp-web.js → WhatsApp
                → Actualiza estado ENVIADO / FALLIDO
        → Retorna AccesoDTO con notificaciones
```

### 3.3 Patrones de diseño utilizados

- **Capas:** Controller → Service → Repository → Entity.
- **DTOs** separados de entidades JPA.
- **Strategy** para WhatsApp: `WhatsAppSender` con implementaciones `BridgeWhatsAppSender` y `BusinessApiWhatsAppSender`.
- **Filter chain** JWT stateless (sin sesiones servidor).
- **Global exception handler** con respuestas JSON uniformes (`ApiErrorDTO`).

---

## 4. Stack tecnológico

### 4.1 Backend

| Componente | Versión / detalle |
|---|---|
| Java | 21 |
| Spring Boot | 3.5.14 |
| Spring Security | JWT (JJWT 0.12.6) |
| Spring Data JPA | Hibernate |
| SQLite (dev) | sqlite-jdbc 3.45 |
| PostgreSQL (prod) | Driver incluido |
| Validación | Jakarta Bean Validation |
| Documentación API | springdoc-openapi 2.8.6 |
| Actuator | health, info |
| Build | Maven Wrapper (`mvnw`) |

### 4.2 Frontend

| Componente | Versión / detalle |
|---|---|
| Angular | 21.2 |
| TypeScript | 5.9 |
| RxJS | 7.8 |
| Standalone components | Sí |
| Signals | Sí (parcial) |
| Tests | Vitest 4 |
| Build | Angular CLI / `@angular/build` |

### 4.3 WhatsApp Bridge

| Componente | Versión / detalle |
|---|---|
| Node.js | 18+ (recom. 20) |
| Express | 4.21 |
| whatsapp-web.js | 1.26 |
| Puppeteer | (via whatsapp-web.js) |
| Google Chrome | Requerido en el host |

### 4.4 Infraestructura

| Herramienta | Uso |
|---|---|
| Docker + Docker Compose | Contenedores multi-servicio |
| Nginx | Frontend en producción (proxy `/api`) |
| GitHub Actions | CI en push/PR |
| PowerShell scripts | `start.ps1`, `stop.ps1` (Windows dev) |

---

## 5. Estructura del repositorio

```
Gestion_De_Estudiantes-main/
├── README.md                    # Guía rápida de instalación
├── start.ps1                    # Levantar 3 servicios (Windows)
├── stop.ps1                     # Detener servicios / liberar puertos
├── .env.example                 # Plantilla variables de entorno
├── docker-compose.yml           # Orquestación Docker
├── .github/workflows/ci.yml       # Pipeline CI
├── docs/
│   ├── INFORME_COMPLETO.md      # Este documento
│   └── PRODUCCION.md              # Guía de despliegue productivo
│
├── gestion-acceso-backend/      # API REST (Java/Spring)
│   ├── pom.xml
│   ├── Dockerfile
│   ├── mvnw / mvnw.cmd
│   ├── alumnos.db               # SQLite (generado en runtime)
│   └── src/main/java/com/colegio/gestionacceso/
│       ├── BackendApplication.java
│       ├── config/              # Security, JWT, OpenAPI, migraciones
│       ├── controller/          # REST controllers
│       ├── dto/                 # Objetos de transferencia
│       ├── exception/           # Excepciones y handler global
│       ├── model/               # Entidades JPA y enums
│       ├── repository/          # Spring Data JPA
│       └── service/             # Lógica de negocio + whatsapp/
│
├── colegio-gestion-acceso/      # SPA Angular
│   ├── package.json
│   ├── angular.json
│   ├── Dockerfile
│   ├── nginx.conf
│   └── src/app/
│       ├── app.routes.ts
│       ├── core/                # Auth, guards, layout, login
│       └── gestion-acceso/      # Páginas, servicios, modelos
│
└── whatsapp-bridge/             # Servicio Node WhatsApp
    ├── index.js
    ├── package.json
    ├── Dockerfile
    └── .wwebjs_auth/            # Sesión WhatsApp (runtime)
```

---

## 6. Backend — API Spring Boot

### 6.1 Punto de entrada

- Clase: `BackendApplication.java`
- Puerto: **8080**
- Perfiles: `default` (SQLite), `prod` (PostgreSQL)

### 6.2 Entidades JPA

#### Alumno (`alumnos`)

| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK autoincrement |
| codigo | String | Identificador en portería |
| nombre | String | |
| descripcion | String | Ej. grado/sección |
| dni | String | 8 dígitos numéricos |
| fechaRegistro | LocalDate | |
| telefonoPadre1 | String | Obligatorio, formato Perú |
| telefonoPadre2 | String | Opcional |

#### Acceso (`accesos`)

| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| alumno | ManyToOne → Alumno | FK alumno_id |
| tipo | TipoAcceso (enum) | Ver sección 6.3 |
| fechaHora | LocalDateTime | Momento del registro |
| registradoPor | String | Email del usuario JWT |
| observacion | String(500) | Motivo permiso / detalle tardanza |

#### Notificacion (`notificaciones`)

| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| acceso | ManyToOne → Acceso | FK acceso_id |
| telefono | String | Destino WhatsApp |
| mensaje | String(2000) | Texto enviado |
| estado | EstadoNotificacion | PENDIENTE / ENVIADO / FALLIDO |
| intentos | int | Contador de reintentos |
| error | String(1000) | Detalle si falló |
| fechaCreacion | LocalDateTime | |
| fechaEnvio | LocalDateTime | Nullable |

#### Usuario (`usuarios`)

| Campo | Tipo | Notas |
|---|---|---|
| id | Long | PK |
| email | String | UNIQUE |
| password | String | BCrypt |
| nombre | String | |
| rol | Rol | ADMIN / PERSONAL |
| activo | boolean | |

### 6.3 Enumeraciones

**TipoAcceso:**
- `ENTRADA` — Ingreso al colegio.
- `SALIDA` — Salida del colegio.
- `TARDANZA` — Llegada tarde (observación opcional en WhatsApp).
- `NO_ASISTIO` — Inasistencia sin permiso.
- `NO_ASISTIO_CON_PERMISO` — Inasistencia justificada (observación **obligatoria**).

**EstadoNotificacion:** `PENDIENTE`, `ENVIADO`, `FALLIDO`

**Rol:** `ADMIN`, `PERSONAL`

### 6.4 Controladores REST

| Controlador | Ruta base | Descripción |
|---|---|---|
| `RootController` | `/` | Info JSON de la API (público) |
| `AuthController` | `/api/auth` | Login |
| `AlumnoController` | `/api/alumnos` | CRUD + búsqueda por código |
| `AccesoController` | `/api/accesos` | Registro e historial |
| `DashboardController` | `/api/dashboard` | Resumen del día |
| `NotificacionController` | `/api/notificaciones` | Log y reintentos |
| `WhatsAppController` | `/api/whatsapp` | Estado y prueba |
| `UsuarioController` | `/api/usuarios` | Alta de usuarios (ADMIN) |

### 6.5 Servicios principales

| Servicio | Responsabilidad |
|---|---|
| `AuthService` | Login, creación usuarios, BCrypt |
| `AlumnoService` | CRUD, normalización teléfonos Perú (`51` + 9 dígitos), unicidad código/DNI |
| `AccesoService` | Registrar acceso, historial, conteos por tipo/día, validación permiso |
| `NotificacionService` | Mensajes WhatsApp, envío, log, reintentos |
| `DashboardService` | Agregación métricas + estado bridge |
| `WhatsAppService` | Consulta estado y envío de prueba |
| `WhatsAppBridgeClient` | Cliente HTTP al bridge Node |
| `BridgeWhatsAppSender` | Implementación bridge (default) |
| `BusinessApiWhatsAppSender` | Implementación Meta Cloud API |

### 6.6 Configuración especial

| Clase | Función |
|---|---|
| `SecurityConfig` | Reglas JWT, CORS, roles |
| `JwtAuthenticationFilter` | Parseo Bearer token |
| `JwtService` | Generación/validación JWT |
| `RestSecurityHandlers` | Respuestas JSON 401/403 |
| `DataInitializer` | Usuarios demo al arrancar |
| `AccesosSchemaMigration` | Migración SQLite (quita CHECK antiguo, agrega `observacion`) |
| `OpenApiConfig` | Swagger con esquema Bearer |
| `WebMvcConfig` | Redirección `/swagger-ui.html` |
| `RestTemplateConfig` | Timeouts HTTP bridge (5s / 15s) |

### 6.7 Plantillas de mensaje WhatsApp

Formato común:

```
Colegio - Gestión de Acceso

El alumno {nombre} ({codigo}) {acción}.
Registrado a las {HH:mm}.
[Motivo del permiso / Detalle tardanza si aplica]

Este es un mensaje automático del sistema de control de acceso.
```

| Tipo | Texto de acción |
|---|---|
| ENTRADA | ingresó al colegio |
| SALIDA | salió del colegio |
| TARDANZA | llegó tarde al colegio |
| NO_ASISTIO | NO ASISTIÓ al colegio el día de hoy (sin permiso registrado) |
| NO_ASISTIO_CON_PERMISO | NO ASISTIÓ al colegio el día de hoy (con permiso autorizado) |

---

## 7. Frontend — Angular

### 7.1 Rutas (`app.routes.ts`)

| Ruta | Componente | Guards |
|---|---|---|
| `/login` | LoginPage | — |
| `/dashboard` | DashboardPage | authGuard |
| `/porteria` | PorteriaPage | authGuard |
| `/alumnos` | AlumnoListPage | authGuard |
| `/alumnos/nuevo` | AlumnoFormPage | authGuard + adminGuard |
| `/alumnos/editar/:id` | AlumnoFormPage | authGuard + adminGuard |
| `/alumnos/:id` | AlumnoDetailPage | authGuard |
| `/accesos` | AccesoHistorialPage | authGuard |
| `/notificaciones` | NotificacionesPage | authGuard |
| `**` | → `/dashboard` | — |

Layout principal: `MainLayout` (sidebar con navegación).

### 7.2 Páginas y funcionalidad

| Página | Descripción |
|---|---|
| **Login** | Email + password → JWT en localStorage |
| **Dashboard** | Métricas del día, accesos recientes, estado WhatsApp |
| **Portería** | Buscar alumno por código; registrar todos los tipos de acceso; confirmación y preview WhatsApp para falta/tardanza/permiso |
| **Alumnos (lista)** | Tabla paginada con búsqueda |
| **Alumno (form)** | Crear/editar (solo ADMIN) |
| **Alumno (detalle)** | Datos + prueba WhatsApp |
| **Historial accesos** | Tabla paginada con badges por tipo |
| **Notificaciones** | Log WhatsApp + botón reintentar |

### 7.3 Servicios HTTP

| Servicio | Endpoint base |
|---|---|
| `AuthApiService` | `/api/auth` |
| `AlumnoService` | `/api/alumnos` |
| `AccesoService` | `/api/accesos` |
| `DashboardService` | `/api/dashboard` |
| `NotificacionService` | `/api/notificaciones` |
| `WhatsAppService` | `/api/whatsapp` |

**Environment dev:** `apiUrl = http://localhost:8080/api`  
**Environment prod:** `apiUrl = /api` (proxy Nginx)

### 7.4 Seguridad en frontend

- **`authInterceptor`:** Añade `Authorization: Bearer <token>` a todas las peticiones excepto login.
- **`errorInterceptor`:** En 401 → logout y redirección a `/login`.
- **`authGuard`:** Protege rutas autenticadas.
- **`adminGuard`:** Restringe formularios de alumnos a rol ADMIN.
- Sesión en `localStorage` bajo clave `colegio_auth`.

### 7.5 Utilidades

- `acceso-tipo.util.ts`: etiquetas, clases CSS badge, preview mensaje WhatsApp.

---

## 8. WhatsApp Bridge

### 8.1 Descripción

Servicio Node.js independiente que expone una API HTTP mínima y usa **whatsapp-web.js** para enviar mensajes como si fuera WhatsApp Web en un navegador headless (Chrome/Puppeteer).

### 8.2 Endpoints del bridge

| Método | Ruta | Body / respuesta |
|---|---|---|
| GET | `/status` | `{ ready, authenticated, chromeConfigured, message }` |
| POST | `/send` | Body: `{ "to": "51987654321", "message": "..." }` → `{ ok: true/false }` |

**Autenticación opcional:** header `X-API-Key` si `WHATSAPP_BRIDGE_API_KEY` está configurado.

### 8.3 Vinculación (QR)

1. Al iniciar, el bridge imprime un **código QR** en la consola.
2. Escanear con WhatsApp → **Dispositivos vinculados**.
3. La sesión se guarda en `.wwebjs_auth/` (no requiere QR en reinicios posteriores).
4. Estado `ready: true` en `/status` cuando está listo.

### 8.4 Requisitos

- **Google Chrome** instalado (auto-detectado en Windows/Mac/Linux).
- Variable `CHROME_PATH` opcional si Chrome no está en ruta estándar.
- Puerto **3001** libre.

### 8.5 Alternativa producción: WhatsApp Business API

Configurar en backend:

```properties
whatsapp.provider=business-api
whatsapp.business-api-url=https://graph.facebook.com/v21.0/<PHONE_NUMBER_ID>/messages
whatsapp.business-api-token=<ACCESS_TOKEN>
```

Implementación: `BusinessApiWhatsAppSender.java`. Ver `docs/PRODUCCION.md`.

---

## 9. Base de datos

### 9.1 Modo desarrollo — SQLite

- Archivo: `gestion-acceso-backend/alumnos.db`
- Config: `spring.jpa.hibernate.ddl-auto=update`
- Migración manual: `AccesosSchemaMigration` corrige restricciones CHECK heredadas y columna `observacion`.

### 9.2 Modo producción — PostgreSQL

- Perfil: `SPRING_PROFILES_ACTIVE=prod`
- Variables: `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, `DB_PASSWORD`
- Docker: servicio `postgres` con perfil `prod` en `docker-compose.yml`

### 9.3 Diagrama entidad-relación

```
┌─────────────┐       ┌─────────────┐       ┌─────────────────┐
│   usuarios  │       │   alumnos   │       │    accesos      │
│─────────────│       │─────────────│       │─────────────────│
│ id          │       │ id          │◄──────│ alumno_id (FK)  │
│ email (UK)  │       │ codigo      │       │ tipo            │
│ password    │       │ nombre      │       │ fecha_hora      │
│ rol         │       │ dni         │       │ registrado_por  │
│ activo      │       │ telefono_*  │       │ observacion     │
└─────────────┘       └─────────────┘       └────────┬────────┘
                                                       │
                                                       │ 1:N
                                                       ▼
                                              ┌─────────────────┐
                                              │ notificaciones  │
                                              │─────────────────│
                                              │ acceso_id (FK)  │
                                              │ telefono        │
                                              │ mensaje         │
                                              │ estado          │
                                              │ intentos, error │
                                              └─────────────────┘
```

### 9.4 Datos semilla

Creados automáticamente si no existen (`DataInitializer`):

| Email | Password | Rol |
|---|---|---|
| admin@colegio.edu | admin123 | ADMIN |
| personal@colegio.edu | personal123 | PERSONAL |

---

## 10. Seguridad y autenticación

### 10.1 Flujo JWT

```
POST /api/auth/login { email, password }
    → BCrypt verify
    → JwtService.generateToken(email, rol)
    → { token, email, nombre, rol }

Peticiones siguientes:
    Header: Authorization: Bearer <token>
    → JwtAuthenticationFilter
    → SecurityContext con ROLE_ADMIN o ROLE_PERSONAL
```

### 10.2 Configuración JWT

| Parámetro | Valor default (dev) |
|---|---|
| `app.jwt.secret` | Cadena de 32+ chars (cambiar en prod) |
| `app.jwt.expiration-ms` | 86400000 (24 horas) |
| Algoritmo | HMAC-SHA (JJWT) |

**No hay refresh token** — al expirar hay que volver a hacer login.

### 10.3 Matriz de permisos HTTP

| Recurso | ADMIN | PERSONAL | Público |
|---|---|---|---|
| POST /api/auth/login | ✓ | ✓ | ✓ |
| GET /, Swagger, health | ✓ | ✓ | ✓ |
| GET /api/alumnos, /api/accesos, etc. | ✓ | ✓ | ✗ |
| POST/PUT/DELETE /api/alumnos | ✓ | ✗ | ✗ |
| POST /api/usuarios | ✓ | ✗ | ✗ |
| POST /api/accesos (portería) | ✓ | ✓ | ✗ |

### 10.4 CORS

Orígenes permitidos en dev: `http://localhost:*`, `http://127.0.0.1:*`  
En producción: restringir al dominio real del colegio.

### 10.5 Respuestas de error

Formato `ApiErrorDTO`:

```json
{
  "timestamp": "2026-06-09T18:00:00",
  "status": 401,
  "error": "Unauthorized",
  "message": "Autenticación requerida...",
  "fieldErrors": { "dni": "El DNI debe tener 8 dígitos" }
}
```

---

## 11. Referencia completa de la API

**Base URL:** `http://localhost:8080`

### 11.1 Públicos (sin token)

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/` | Información de la API |
| POST | `/api/auth/login` | Obtener JWT |
| GET | `/actuator/health` | Health check |
| GET | `/swagger-ui/index.html` | Documentación Swagger |
| GET | `/v3/api-docs` | OpenAPI JSON |

### 11.2 Autenticación

**POST `/api/auth/login`**

Request:
```json
{ "email": "admin@colegio.edu", "password": "admin123" }
```

Response:
```json
{
  "token": "eyJhbGciOiJIUzI1NiIs...",
  "email": "admin@colegio.edu",
  "nombre": "Administrador",
  "rol": "ADMIN"
}
```

### 11.3 Alumnos — `/api/alumnos`

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| GET | `/api/alumnos?page=0&size=10&q=` | Auth | Listar paginado |
| GET | `/api/alumnos/{id}` | Auth | Por ID |
| GET | `/api/alumnos/codigo/{codigo}` | Auth | Por código (portería) |
| POST | `/api/alumnos` | ADMIN | Crear |
| PUT | `/api/alumnos/{id}` | ADMIN | Actualizar |
| DELETE | `/api/alumnos/{id}` | ADMIN | Eliminar |

Body ejemplo:
```json
{
  "codigo": "A-001",
  "nombre": "Juan Pérez",
  "descripcion": "5to grado",
  "dni": "12345678",
  "fechaRegistro": "2026-01-15",
  "telefonoPadre1": "987654321",
  "telefonoPadre2": "912345678"
}
```

### 11.4 Accesos — `/api/accesos`

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/accesos` | Registrar acceso + WhatsApp |
| GET | `/api/accesos?page=0&size=20&alumnoId=` | Historial paginado |
| GET | `/api/accesos/hoy` | Accesos del día |
| GET | `/api/accesos/{id}` | Detalle con notificaciones |

Body:
```json
{
  "alumnoId": 1,
  "tipo": "NO_ASISTIO_CON_PERMISO",
  "observacion": "Cita médica"
}
```

Tipos válidos: `ENTRADA`, `SALIDA`, `TARDANZA`, `NO_ASISTIO`, `NO_ASISTIO_CON_PERMISO`

### 11.5 Dashboard — `/api/dashboard`

**GET** — Retorna: total alumnos, accesos hoy, entradas, salidas, tardanzas, no asistió, con permiso, notificaciones enviadas/fallidas, estado WhatsApp.

### 11.6 Notificaciones — `/api/notificaciones`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/notificaciones?page=0&size=20` | Log paginado |
| POST | `/api/notificaciones/reintentar` | Reintentar PENDIENTE |

### 11.7 WhatsApp — `/api/whatsapp`

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/api/whatsapp/estado` | Estado del bridge/API |
| POST | `/api/whatsapp/prueba/{alumnoId}` | Mensaje de prueba a padres |

### 11.8 Usuarios — `/api/usuarios`

| Método | Ruta | Rol | Descripción |
|---|---|---|---|
| POST | `/api/usuarios` | ADMIN | Crear usuario |

### 11.9 Códigos HTTP

| Código | Significado |
|---|---|
| 200 / 201 | OK / Creado |
| 400 | Validación o regla de negocio |
| 401 | Sin token o token inválido |
| 403 | Sin permisos (rol insuficiente) |
| 404 | Recurso no encontrado |
| 500 | Error interno |

---

## 12. Flujos de negocio

### 12.1 Portería — entrada rápida

1. Personal busca código del alumno.
2. Clic en **Registrar ENTRADA** (un clic, sin confirmación extra).
3. Backend guarda acceso y envía WhatsApp inmediato.
4. Pantalla muestra resultado envío (X/Y enviados).

### 12.2 Portería — inasistencia con permiso

1. Buscar alumno.
2. Clic **No asistió (con permiso)**.
3. Escribir motivo obligatorio.
4. Ver preview del mensaje WhatsApp.
5. **Confirmar** → registro + envío.

### 12.3 Portería — inasistencia sin permiso

1. Buscar alumno.
2. Clic **No asistió**.
3. Panel de confirmación con preview.
4. **Confirmar — no asistió** → registro + envío.

### 12.4 Portería — tardanza

1. Buscar alumno.
2. Clic **Tardanza**.
3. Detalle opcional (ej. "20 min tarde").
4. Preview + confirmar.

### 12.5 Administración de alumnos

1. ADMIN entra a `/alumnos`.
2. Crear, editar o eliminar.
3. Desde detalle puede enviar **prueba WhatsApp**.

### 12.6 Reintento de notificaciones fallidas

1. Ir a `/notificaciones`.
2. Clic **Reintentar pendientes**.
3. Backend procesa cola `PENDIENTE` si bridge está `ready`.

---

## 13. Configuración y variables de entorno

### 13.1 Backend (`application.properties`)

| Propiedad | Default | Descripción |
|---|---|---|
| `spring.datasource.url` | `jdbc:sqlite:alumnos.db` | BD SQLite |
| `server.port` | 8080 | Puerto API |
| `app.jwt.secret` | (dev) | Secreto JWT |
| `app.jwt.expiration-ms` | 86400000 | Expiración token |
| `whatsapp.enabled` | true | Habilitar WhatsApp |
| `whatsapp.provider` | bridge | `bridge` o `business-api` |
| `whatsapp.bridge.url` | http://localhost:3001 | URL bridge |
| `whatsapp.bridge-api-key` | (vacío) | API key bridge |
| `whatsapp.max-retries` | 3 | Reintentos |

### 13.2 `.env.example` (Docker / prod)

```
JWT_SECRET
WHATSAPP_BRIDGE_API_KEY
WHATSAPP_BRIDGE_URL
SPRING_PROFILES_ACTIVE
DB_HOST, DB_PORT, DB_NAME, DB_USER, DB_PASSWORD
WHATSAPP_PROVIDER
WHATSAPP_BUSINESS_API_URL
WHATSAPP_BUSINESS_API_TOKEN
```

### 13.3 WhatsApp Bridge

| Variable | Default | Descripción |
|---|---|---|
| `PORT` | 3001 | Puerto HTTP |
| `WHATSAPP_BRIDGE_API_KEY` | — | Protección API |
| `CHROME_PATH` | auto | Ruta Chrome |

---

## 14. Instalación y ejecución

Ver también [README.md](../README.md) para comandos paso a paso.

### 14.1 Requisitos

- Java JDK **21**
- Node.js **18+**
- npm **9+**
- Google Chrome (bridge WhatsApp)

### 14.2 Instalación primera vez

```powershell
cd colegio-gestion-acceso && npm install && cd ..
cd whatsapp-bridge && npm install && cd ..
cd gestion-acceso-backend && .\mvnw.cmd -B compile && cd ..
```

### 14.3 Levantar todo (Windows)

```powershell
.\start.ps1
```

### 14.4 Detener todo

```powershell
.\stop.ps1
```

### 14.5 URLs de acceso

| Recurso | URL |
|---|---|
| App web | http://127.0.0.1:4200 |
| API info | http://localhost:8080/ |
| Swagger | http://localhost:8080/swagger-ui/index.html |
| Health | http://localhost:8080/actuator/health |
| Bridge status | http://localhost:3001/status |

---

## 15. Docker y despliegue

### 15.1 Servicios Docker Compose

| Servicio | Puerto host | Imagen / build |
|---|---|---|
| frontend | 4200 → 80 | Nginx + Angular build |
| backend | 8080 | Spring Boot JAR |
| whatsapp-bridge | 3001 | Node |
| postgres | 5432 | Solo perfil `prod` |

### 15.2 Comandos Docker

```bash
cp .env.example .env
docker compose up --build          # Dev (SQLite en volumen)
docker compose --profile prod up --build   # Con PostgreSQL
```

### 15.3 Nginx (frontend container)

- Sirve SPA estática.
- Proxy `/api/` → `backend:8080/api/`.
- Fallback `index.html` para rutas Angular.

### 15.4 Producción

Guía detallada: [PRODUCCION.md](./PRODUCCION.md)

Incluye: PostgreSQL, backups, HTTPS, WhatsApp Business API, checklist de seguridad, monitoreo.

---

## 16. CI/CD

### 16.1 GitHub Actions (`.github/workflows/ci.yml`)

**Trigger:** push/PR a `main` o `master`

| Job | Acciones |
|---|---|
| **backend** | Java 21 Temurin → `./mvnw -B verify` |
| **frontend** | Node 20 → `npm ci` → `npm run build` → `npm test -- --run` |

### 16.2 Artefactos generados

- Backend: JAR en `target/`
- Frontend: `dist/colegio-gestion-acceso/`

---

## 17. Pruebas

### 17.1 Backend

| Archivo | Tipo | Qué prueba |
|---|---|---|
| `BackendApplicationTests` | Integración | Contexto Spring carga |
| `AuthControllerIntegrationTest` | MockMvc | Login retorna token |
| `AccesoServiceTest` | Unitario | Error si alumno no existe |
| `AlumnoServiceTest` | Unitario | Normalización teléfono, unicidad |

```powershell
cd gestion-acceso-backend
.\mvnw.cmd test
```

### 17.2 Frontend

| Archivo | Qué prueba |
|---|---|
| `app.spec.ts` | Componente raíz |

```powershell
cd colegio-gestion-acceso
npm test
```

### 17.3 Cobertura actual

Pruebas básicas de humo y unidades críticas. **No hay** tests E2E completos ni cobertura exhaustiva de portería/WhatsApp.

---

## 18. Monitoreo y operación

### 18.1 Health checks

```bash
curl http://localhost:8080/actuator/health
curl http://localhost:3001/status
curl http://localhost:8080/api/whatsapp/estado   # requiere JWT
```

### 18.2 Logs

- Backend: stdout con patrón timestamp + nivel + logger.
- Bridge: consola Node (QR, ready, errores envío).
- Docker: `docker compose logs -f backend whatsapp-bridge`

### 18.3 Alertas recomendadas (producción)

- Backend health DOWN.
- Bridge `ready: false` > 5 min.
- Notificaciones FALLIDO > umbral diario.
- Espacio disco backups BD.

### 18.4 Backups

**SQLite (dev):**
```bash
cp gestion-acceso-backend/alumnos.db backup_$(date +%Y%m%d).db
```

**PostgreSQL (prod):**
```bash
pg_dump -h localhost -U colegio colegio_acceso > backup.sql
```

---

## 19. Limitaciones conocidas

| Limitación | Impacto | Mitigación |
|---|---|---|
| WhatsApp Web (bridge) no es API oficial | Puede desconectarse; no apto 100% prod | Usar Business API en prod |
| QR manual la primera vez | Requiere operador | Sesión persistente en `.wwebjs_auth` |
| Sin refresh token JWT | Re-login cada 24 h | Aceptable para uso interno |
| Entrada/Salida sin confirmación | Errores por doble clic | Mejora futura (ver §20) |
| API `/api/*` no usable desde barra del navegador | Solo JSON 401 sin token | Swagger / app Angular |
| Un bridge = un número WhatsApp | No escalar múltiples números en misma sesión | Una instancia por línea |
| SQLite en dev con migraciones manuales | CHECK constraints heredados | `AccesosSchemaMigration` al arrancar |
| Sin anulación de registros | Error humano irreversible vía UI | Mejora futura |
| CORS abierto en localhost | OK dev; inseguro prod | Restringir dominio |

---

## 20. Mejoras futuras recomendadas

Priorizadas según valor para el colegio:

| Prioridad | Mejora | Beneficio |
|---|---|---|
| Alta | Confirmación en Entrada/Salida | Menos clics accidentales |
| Alta | Validaciones contradictorias (ej. falta + entrada mismo día) | Coherencia de datos |
| Alta | Anular registro en 5–10 min | Corregir errores |
| Media | Cola WhatsApp con 2 min de gracia | Cancelar antes de enviar |
| Media | PIN personal para faltas | Seguridad extra |
| Media | Reportes PDF/Excel | Gestión administrativa |
| Baja | QR en carnet del alumno | Portería más rápida |
| Baja | App padres (solo consulta) | Transparencia familia |

---

## 21. Apéndices

### Apéndice A — Comandos rápidos

```powershell
# Instalar dependencias
cd colegio-gestion-acceso; npm install; cd ..
cd whatsapp-bridge; npm install; cd ..
cd gestion-acceso-backend; .\mvnw.cmd compile; cd ..

# Levantar / detener
.\start.ps1
.\stop.ps1

# Solo backend
cd gestion-acceso-backend
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
.\mvnw.cmd spring-boot:run

# Solo bridge
cd whatsapp-bridge
$env:PORT = "3001"; npm start

# Solo frontend
cd colegio-gestion-acceso
npx ng serve --host 127.0.0.1 --port 4200

# Tests
cd gestion-acceso-backend; .\mvnw.cmd test
cd colegio-gestion-acceso; npm test

# Docker
docker compose up --build
```

### Apéndice B — Puertos utilizados

| Puerto | Servicio |
|---|---|
| 4200 | Frontend Angular |
| 8080 | Backend Spring Boot |
| 3001 | WhatsApp Bridge |
| 5432 | PostgreSQL (Docker prod) |

### Apéndice C — Credenciales demo

| Email | Password | Rol |
|---|---|---|
| admin@colegio.edu | admin123 | ADMIN |
| personal@colegio.edu | personal123 | PERSONAL |

**Cambiar obligatoriamente en producción.**

### Apéndice D — Documentos relacionados

| Documento | Contenido |
|---|---|
| [README.md](../README.md) | Instalación rápida desde cero |
| [PRODUCCION.md](./PRODUCCION.md) | Despliegue productivo |
| Swagger UI | http://localhost:8080/swagger-ui/index.html |
| OpenAPI JSON | http://localhost:8080/v3/api-docs |

### Apéndice E — Historial de evolución del proyecto

El proyecto evolucionó desde un MVP de CRUD de alumnos con prueba WhatsApp manual hacia un sistema con:

1. **Fase 1 — Core:** accesos, notificaciones automáticas, portería.
2. **Fase 2 — Profesionalización:** JWT, roles, dashboard, Swagger, environments.
3. **Fase 3 — Calidad/Deploy:** tests, Docker, CI GitHub Actions.
4. **Fase 4 — Producción:** perfil PostgreSQL, Business API WhatsApp, docs prod.
5. **Extensiones recientes:** tardanza, inasistencia con/sin permiso, migración SQLite, raíz API JSON, handlers 401 JSON, confirmaciones portería.

---

*Fin del informe técnico completo.*
