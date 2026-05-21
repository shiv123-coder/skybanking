# 🚀 Deployment Guide: GitHub & Render

This guide provides step-by-step instructions for securely pushing your **SkyBanking** project to GitHub and deploying it to **Render.com**.

---

## 🛡️ Pre-Deployment Security Audit

Before pushing to a public repository, ensure the following:

1.  **Environment Variables**: 
    - The `.env` file is already in `.gitignore`. **NEVER** remove it from `.gitignore`.
    - Ensure `.env.example` contains only placeholder values (e.g., `your_db_password_here`).
2.  **No Hardcoded Secrets**: 
    - A scan has been performed, and no API keys or passwords were found in the `.java` or `.jsp` files.
3.  **Clean Repository**: 
    - Ensure folders like `target/`, `.idea/`, `.vscode/`, and `out/` are ignored (already handled in `.gitignore`).

---

## 🐙 Part 1: Push to GitHub

1.  **Initialize Git** (if not already done):
    ```bash
    git init
    ```
2.  **Add Files**:
    ```bash
    git add .
    ```
3.  **Commit**:
    ```bash
    git commit -m "Initial commit: SkyBanking System with Docker support"
    ```
4.  **Create Repository on GitHub**:
    - Go to [github.com/new](https://github.com/new).
    - Create a new repository named `skybanking`.
5.  **Link and Push**:
    ```bash
    git remote add origin https://github.com/YOUR_USERNAME/skybanking.git
    git branch -M main
    git push -u origin main
    ```

---

## ☁️ Part 2: Deploy to Render.com

Render is an excellent platform for deploying Java applications via Docker.

### 1. Database Setup (PostgreSQL)
1.  Log in to [Render Dashboard](https://dashboard.render.com/).
2.  Click **New +** > **PostgreSQL**.
3.  Name it `skybank-db`.
4.  Once created, copy the **Internal Database URL** (for Render services) or **External Database URL** (for local testing).

### 2. Deploy Web Service
1.  Click **New +** > **Web Service**.
2.  Connect your GitHub repository.
3.  **Name**: `skybanking-app`.
4.  **Runtime**: Select **Docker**.
5.  **Instance Type**: `Free` (or higher if needed).

### 3. Configure Environment Variables
In the **Environment** tab of your Render service, add the following variables:

| Key | Value | Required |
| :--- | :--- | :---: |
| `PORT` | `10000` (Render sets this automatically) | ✅ |
| `DB_URL` | *Your Render Internal Database URL* | ✅ |
| `DB_USER` | *Your Render DB Username* | ✅ |
| `DB_PASSWORD` | *Your Render DB Password* | ✅ |
| `DB_POOL_SIZE` | `10` | Optional |
| `SMTP_EMAIL` | *Your Gmail/Email* | ✅ |
| `SMTP_PASSWORD` | *Your Gmail App Password* | ✅ |
| `SMTP_HOST` | `smtp.gmail.com` | Optional |
| `SMTP_PORT` | `587` | Optional |
| `STRIPE_SECRET_KEY` | `sk_test_...` | ✅ |
| `STRIPE_PUBLISHABLE_KEY` | `pk_test_...` | Optional |
| `STRIPE_WEBHOOK_SECRET` | `whsec_...` | Optional |
| `ADMIN_PASSWORD` | *Your admin password* | ✅ |
| `APP_SECRET_KEY` | *HMAC secret for QR codes* | Optional |
| `APP_ENV` | `production` | Optional |
| `ENABLE_PRELOADER` | `true` | Optional |

> **Note**: Render automatically injects the `PORT` environment variable. The Dockerfile defaults to `10000` if not set.

### 4. Database Schema Migration
Since this project uses a standard SQL schema, you need to import it into your Render database:
1.  Install `psql` locally if you haven't.
2.  Run the following command using your **External Database URL**:
    ```bash
    psql "YOUR_EXTERNAL_DB_URL" -f database/skybanking_schema_pg.sql
    ```

---

## 🐳 Part 3: Local Docker Testing

Before deploying to Render, test the Docker image locally:

### Build
```bash
docker build -t skybanking .
```

### Run (simulating Render environment)
```bash
docker run -p 10000:10000 \
  -e PORT=10000 \
  -e DB_URL=jdbc:postgresql://host.docker.internal:5432/skybank \
  -e DB_USER=postgres \
  -e DB_PASSWORD=your_password \
  -e SMTP_EMAIL=your_email@gmail.com \
  -e SMTP_PASSWORD=your_app_password \
  -e STRIPE_SECRET_KEY=sk_test_xxx \
  -e ADMIN_PASSWORD=your_admin_password \
  skybanking
```

### Verify
```bash
# Check port binding
curl http://localhost:10000/

# Expected startup logs:
# ✅ PORT=10000
# ✅ Tomcat connector bound to 0.0.0.0:10000
# ✅ DB_URL set (host: ...)
```

### Troubleshooting
| Symptom | Cause | Fix |
| :--- | :--- | :--- |
| `port out of range:-1` | PORT env var missing or invalid | Ensure `PORT=10000` is set |
| Render timeout (no port detected) | Tomcat not binding to `0.0.0.0` | Dockerfile handles this automatically |
| DB connection failed | Wrong DB_URL or credentials | Verify env vars in Render dashboard |
| Email not sending | SMTP credentials missing | Set SMTP_EMAIL and SMTP_PASSWORD |

---

## 🛠️ Maintenance & Monitoring

- **Logs**: View real-time logs in the Render Dashboard under the **Logs** tab.
- **Scaling**: If the app becomes slow, consider upgrading from the `Free` tier to `Starter`.
- **Health Checks**: Render automatically monitors the health of your Docker container.

---
**SkyBanking Deployment Team** | *Secure, Scalable, Reliable*
