# Colegio — Gestión de Acceso

Sistema de control de acceso escolar con notificaciones automáticas por WhatsApp a los padres.

## Arquitectura

| Servicio | Carpeta | Tecnología | Puerto |
|---|---|---|---|
| Frontend | `colegio-gestion-acceso` | Angular 21 | **4200** |
| Backend API | `gestion-acceso-backend` | Spring Boot 3.5 + Java 21 | **8080** |
| WhatsApp Bridge | `whatsapp-bridge` | Node.js + whatsapp-web.js | **3001** |

---

## Requisitos previos

Instala esto **antes** de levantar el proyecto:

| Herramienta | Versión mínima | Verificar |
|---|---|---|
| **Java JDK** | 21 | `java -version` |
| **Node.js** | 18+ (recom. 20+) | `node -v` |
| **npm** | 9+ | `npm -v` |
| **Google Chrome** | Instalado | Necesario para el bridge de WhatsApp |

### Instalar Java 21 en Windows (si no lo tienes)

```powershell
winget install Microsoft.OpenJDK.21
```

Configura `JAVA_HOME` (ajusta la ruta si es distinta):

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
```

---

## Levantar desde cero — Windows (recomendado)

Abre **PowerShell** en la raíz del proyecto (donde está este README).

### 1. Clonar / descomprimir el proyecto

```powershell
cd "C:\ruta\a\Gestion_De_Estudiantes-main"
```

### 2. Instalar dependencias (primera vez)

```powershell
# Frontend Angular
cd colegio-gestion-acceso
npm install
cd ..

# WhatsApp Bridge
cd whatsapp-bridge
npm install
cd ..

# Backend (Maven descarga dependencias al primer run; opcional compilar antes)
cd gestion-acceso-backend
.\mvnw.cmd -B compile
cd ..
```

### 3. Levantar los 3 servicios con un solo comando

**Desde el Explorador de Windows (recomendado):** doble clic en **`Iniciar Servicios.bat`**

O desde PowerShell en la **raíz del proyecto**:

```powershell
.\start.ps1
```

Se abren **3 ventanas de PowerShell**:
- Backend (8080)
- WhatsApp Bridge (3001) — escanea el QR aquí
- Frontend (4200)

### 4. Detener todo

**Desde el Explorador de Windows:** doble clic en **`Detener Servicios.bat`**

O desde PowerShell:

```powershell
.\stop.ps1
```

---

## Levantar manualmente (terminal por servicio)

Útil si prefieres controlar cada servicio por separado.

### Terminal 1 — Backend

```powershell
cd gestion-acceso-backend
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
.\mvnw.cmd spring-boot:run
```

Linux / macOS:

```bash
cd gestion-acceso-backend
./mvnw spring-boot:run
```

Espera el mensaje `Started BackendApplication`. API en http://localhost:8080

### Terminal 2 — WhatsApp Bridge

```powershell
cd whatsapp-bridge
npm install          # solo la primera vez
$env:PORT = "3001"
npm start
```

Linux / macOS:

```bash
cd whatsapp-bridge
npm install
PORT=3001 npm start
```

Escanea el **QR** que aparece en la consola: WhatsApp → **Dispositivos vinculados** → Vincular.

Estado del bridge: http://localhost:3001/status  
QR en el navegador (si no aparece en consola): http://localhost:3001/qr

### Terminal 3 — Frontend

```powershell
cd colegio-gestion-acceso
npm install          # solo la primera vez
npx ng serve --host 127.0.0.1 --port 4200
```

Linux / macOS:

```bash
cd colegio-gestion-acceso
npm install
npx ng serve --host 127.0.0.1 --port 4200
```

App en http://127.0.0.1:4200

---

## Acceso a la aplicación

| Recurso | URL |
|---|---|
| **App web** | http://127.0.0.1:4200 |
| **API (info)** | http://localhost:8080/ |
| **Swagger** | http://localhost:8080/swagger-ui/index.html |
| **Health check** | http://localhost:8080/actuator/health |
| **WhatsApp status** | http://localhost:3001/status |

### Usuarios demo

| Email | Contraseña | Rol |
|---|---|---|
| `admin@colegio.edu` | `admin123` | ADMIN |
| `personal@colegio.edu` | `personal123` | PERSONAL |

---

## Funcionalidades

- Login JWT con roles **ADMIN** y **PERSONAL**
- CRUD de alumnos (DNI y teléfono Perú)
- **Portería**: entrada, salida, tardanza, no asistió, no asistió con permiso
- WhatsApp automático al registrar (padre 1 y padre 2)
- Historial de accesos, dashboard y log de notificaciones
- Swagger / OpenAPI documentado

---

## API — resumen

Todas las rutas `/api/*` (excepto login) requieren:

```http
Authorization: Bearer <token>
```

Obtener token:

```http
POST http://localhost:8080/api/auth/login
Content-Type: application/json

{"email":"admin@colegio.edu","password":"admin123"}
```

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/auth/login` | Autenticación (público) |
| GET | `/api/alumnos` | Listar alumnos |
| GET | `/api/alumnos/codigo/{codigo}` | Buscar por código |
| POST | `/api/accesos` | Registrar acceso / asistencia |
| GET | `/api/accesos` | Historial |
| GET | `/api/dashboard` | Resumen del día |
| GET | `/api/notificaciones` | Log WhatsApp |
| GET | `/api/whatsapp/estado` | Estado del bridge |

Tipos de acceso: `ENTRADA`, `SALIDA`, `TARDANZA`, `NO_ASISTIO`, `NO_ASISTIO_CON_PERMISO`

> **Nota:** Abrir `http://localhost:8080/api/alumnos` en el navegador sin token devuelve JSON 401. Usa Swagger, Postman o la app Angular.

---

## Variables de entorno (opcional)

Copia el ejemplo si usas Docker o producción:

```powershell
copy .env.example .env
```

Variables principales: `JWT_SECRET`, `WHATSAPP_BRIDGE_URL`, `SPRING_PROFILES_ACTIVE`, `WHATSAPP_PROVIDER`.

En desarrollo local **no es obligatorio** crear `.env`; el backend usa SQLite (`alumnos.db`) y configuración por defecto.

---

## Docker Compose (alternativa)

```bash
cp .env.example .env
docker compose up --build
```

- Frontend: http://localhost:4200  
- Backend: http://localhost:8080  

PostgreSQL (producción):

```bash
docker compose --profile prod up --build
```

Ver [docs/PRODUCCION.md](docs/PRODUCCION.md) para despliegue completo.

---

## Tests

```powershell
# Backend
cd gestion-acceso-backend
.\mvnw.cmd test

# Frontend
cd colegio-gestion-acceso
npm test
```

---

## Solución de problemas

### Puerto en uso (`EADDRINUSE`)

```powershell
.\stop.ps1
.\start.ps1
```

O revisar manualmente:

```powershell
Get-NetTCPConnection -LocalPort 8080,3001,4200 -State Listen
```

### `JAVA_HOME` no definido

```powershell
$env:JAVA_HOME = "C:\Program Files\Microsoft\jdk-21.0.11.10-hotspot"
java -version
```

### WhatsApp no muestra QR

1. Verifica que **Google Chrome** esté instalado.
2. Abre el QR en el navegador: http://localhost:3001/qr
3. Detén el bridge y borra sesión antigua (fuerza QR nuevo):

```powershell
.\Detener Servicios.bat
Remove-Item -Recurse -Force whatsapp-bridge\.wwebjs_auth -ErrorAction SilentlyContinue
Remove-Item -Recurse -Force whatsapp-bridge\.wwebjs_cache -ErrorAction SilentlyContinue
.\Iniciar Servicios.bat
```

4. Revisa la ventana **WHATSAPP BRIDGE - puerto 3001** (el bridge reintenta solo si falla la conexión).

### Error al registrar inasistencia / tardanza

Reinicia el backend para aplicar migraciones de base de datos:

```powershell
.\stop.ps1
.\start.ps1
```

### Swagger no carga

Usa: http://localhost:8080/swagger-ui/index.html  
(no http://localhost:8080/swagger-ui.html si da error en versiones antiguas)

---

## Estructura del proyecto

```
├── colegio-gestion-acceso/   # Frontend Angular
├── gestion-acceso-backend/   # API Spring Boot + SQLite
├── whatsapp-bridge/          # Puente WhatsApp Web
├── Iniciar Servicios.bat     # Levantar todo (doble clic)
├── Detener Servicios.bat     # Detener todo (doble clic)
├── start.ps1                 # Levantar todo (PowerShell)
├── stop.ps1                  # Detener todo (PowerShell)
├── docker-compose.yml
├── .env.example
└── docs/PRODUCCION.md
```

---

## Documentación adicional

- [docs/INFORME_COMPLETO.md](docs/INFORME_COMPLETO.md) — Informe técnico completo del proyecto
- [docs/PRODUCCION.md](docs/PRODUCCION.md) — PostgreSQL, WhatsApp Business API, HTTPS, backups
