# Deployment Health Check

Server: `193.40.157.230`, user: `ubuntu`, deploy path: `~/droonidest-deploy`
Domain: `team-08.ddns.net`

## SSH into the server

```bash
ssh ubuntu@193.40.157.230
cd ~/droonidest-deploy
```

## Check container status

```bash
docker compose -f docker-compose.prod.yaml ps
```

All 4 services (`frontend`, `backend`, `postgres`, `minio`) should show `Up` / `healthy`.

## View logs

All services:

```bash
docker compose -f docker-compose.prod.yaml logs
```

Single service:

```bash
docker compose -f docker-compose.prod.yaml logs backend
docker compose -f docker-compose.prod.yaml logs frontend
docker compose -f docker-compose.prod.yaml logs postgres
docker compose -f docker-compose.prod.yaml logs minio
```

Follow logs live (Ctrl+C to stop):

```bash
docker compose -f docker-compose.prod.yaml logs -f
```

## Restart a service

```bash
docker compose -f docker-compose.prod.yaml restart backend
```

## Restart everything

```bash
docker compose -f docker-compose.prod.yaml down
docker compose -f docker-compose.prod.yaml up -d
```

## Full rebuild (no cache)

```bash
docker compose -f docker-compose.prod.yaml down
docker compose -f docker-compose.prod.yaml build --no-cache
docker compose -f docker-compose.prod.yaml up -d
```

## Shell into a running container

```bash
docker compose -f docker-compose.prod.yaml exec backend sh
docker compose -f docker-compose.prod.yaml exec frontend sh
```

## Check disk usage

```bash
docker system df
```

## Clean up unused images

```bash
docker image prune -f
```

## Add HTTPS with Certbot

Stop the frontend so port 80 is free for Certbot's standalone mode:

```bash
docker compose -f docker-compose.prod.yaml down
```

Install Certbot and obtain a certificate:

```bash
sudo apt install certbot -y
sudo certbot certonly --standalone -d team-08.ddns.net
```

Copy certificates into the project's nginx/ssl directory:

```bash
sudo mkdir -p ~/droonidest-deploy/nginx/ssl
sudo cp /etc/letsencrypt/live/team-08.ddns.net/{fullchain,privkey,chain}.pem \
        ~/droonidest-deploy/nginx/ssl/
sudo chmod 644 ~/droonidest-deploy/nginx/ssl/*.pem
```

Start everything back up:

```bash
docker compose -f docker-compose.prod.yaml up -d
```

The site should now be available at `https://team-08.ddns.net`. HTTP requests on port 80 are automatically redirected to HTTPS.

### Certificate renewal

Let's Encrypt certificates expire every 90 days. After auto-renewal, copy the new certificates and restart the frontend:

```bash
sudo certbot renew
sudo cp /etc/letsencrypt/live/team-08.ddns.net/{fullchain,privkey,chain}.pem \
        ~/droonidest-deploy/nginx/ssl/
sudo chmod 644 ~/droonidest-deploy/nginx/ssl/*.pem
cd ~/droonidest-deploy && docker compose -f docker-compose.prod.yaml restart frontend
```
