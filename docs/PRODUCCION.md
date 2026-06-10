# Guía de producción

## PostgreSQL

Active el perfil `prod`:

```properties
spring.profiles.active=prod
```

Variables de entorno:

```bash
DB_HOST=localhost
DB_PORT=5432
DB_NAME=colegio_acceso
DB_USER=colegio
DB_PASSWORD=<contraseña-segura>
```

Con Docker:

```bash
docker compose --profile prod up -d postgres
SPRING_PROFILES_ACTIVE=prod docker compose up backend
```

### Backups

Backup diario recomendado:

```bash
pg_dump -h localhost -U colegio colegio_acceso > backup_$(date +%Y%m%d).sql
```

Restauración:

```bash
psql -h localhost -U colegio colegio_acceso < backup_20260101.sql
```

Para SQLite (desarrollo):

```bash
cp alumnos.db alumnos_backup_$(date +%Y%m%d).db
```

## WhatsApp Business API

Para entornos productivos se recomienda la API oficial de Meta en lugar de `whatsapp-web.js`.

1. Cree una app en [Meta for Developers](https://developers.facebook.com/)
2. Configure WhatsApp Business API
3. En `application-prod.properties` o variables de entorno:

```properties
whatsapp.provider=business-api
whatsapp.business-api-url=https://graph.facebook.com/v21.0/<PHONE_NUMBER_ID>/messages
whatsapp.business-api-token=<ACCESS_TOKEN>
```

La implementación está en `BusinessApiWhatsAppSender.java` y se activa automáticamente con `whatsapp.provider=business-api`.

## HTTPS

En producción, coloque un reverse proxy (Nginx, Caddy o Traefik) delante del frontend y backend:

```nginx
server {
    listen 443 ssl http2;
    server_name colegio.ejemplo.edu;

    ssl_certificate     /etc/letsencrypt/live/colegio.ejemplo.edu/fullchain.pem;
    ssl_certificate_key /etc/letsencrypt/live/colegio.ejemplo.edu/privkey.pem;

    location / {
        proxy_pass http://frontend:80;
    }

    location /api/ {
        proxy_pass http://backend:8080/api/;
    }
}
```

Certificados gratuitos con [Let's Encrypt](https://letsencrypt.org/) y Certbot.

## Seguridad en producción

- Cambie `JWT_SECRET` por una clave aleatoria de al menos 32 caracteres
- Configure `WHATSAPP_BRIDGE_API_KEY` en bridge y backend
- Cambie contraseñas demo (`admin123`, `personal123`)
- Restrinja CORS en `SecurityConfig` a su dominio
- No exponga el puerto 3001 del bridge a Internet

## Monitoreo

### Spring Actuator

```bash
curl http://localhost:8080/actuator/health
```

### Logs

Los logs estructurados se emiten en stdout. En Docker:

```bash
docker compose logs -f backend
docker compose logs -f whatsapp-bridge
```

### Alertas recomendadas

- Health check del backend cada 1 min
- WhatsApp bridge desconectado (`/api/whatsapp/estado` → `listo: false`)
- Notificaciones fallidas > umbral en dashboard
- Espacio en disco para backups de BD

## Escalabilidad

- **Backend**: escalar horizontalmente detrás de load balancer; usar PostgreSQL compartido
- **WhatsApp bridge**: una instancia por número de WhatsApp; no escalar múltiples bridges con la misma sesión
- **Frontend**: CDN o Nginx estático; sin estado
