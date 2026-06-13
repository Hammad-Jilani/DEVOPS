# Spring MVC DevOps Demo — CI/CD with GitHub Actions → AWS EC2

A production-style Spring Boot MVC application with a full CI/CD pipeline.

## 🏗️ Stack
- **Backend**: Spring Boot 3.2 + Spring MVC + Thymeleaf
- **CI/CD**: GitHub Actions
- **Cloud**: AWS EC2
- **Java**: 17

## 🔄 Pipeline Flow

```
Push to main
     │
     ▼
┌─────────────┐
│  Build Job  │  mvn clean package + tests
└──────┬──────┘
       │
       ▼
┌─────────────┐
│ Deploy Job  │  SCP JAR → EC2 → start app → health check
└─────────────┘
```

## 🚀 Setup Instructions

### 1. Launch an EC2 Instance
- AMI: Amazon Linux 2023 or Ubuntu 22.04
- Instance type: t2.micro (free tier)
- Security Group: open ports **22** (SSH) and **8080** (HTTP)

### 2. Run Setup Script on EC2
```bash
scp -i your-key.pem scripts/setup-ec2.sh ec2-user@<EC2_IP>:~/
ssh -i your-key.pem ec2-user@<EC2_IP>
chmod +x setup-ec2.sh && sudo ./setup-ec2.sh
```

### 3. Add GitHub Secrets
Go to your repo → **Settings → Secrets and Variables → Actions**

| Secret Name   | Value                              |
|---------------|------------------------------------|
| `EC2_HOST`    | Your EC2 public IP or DNS          |
| `EC2_USER`    | `ec2-user` or `ubuntu`             |
| `EC2_SSH_KEY` | Contents of your `.pem` private key |

### 4. Push to Deploy
```bash
git push origin main
```
GitHub Actions will build, test, and deploy automatically!

## 📡 Endpoints
| URL | Description |
|-----|-------------|
| `http://<EC2>:8080/` | Main UI |
| `http://<EC2>:8080/actuator/health` | Health check |
| `http://<EC2>:8080/api/tasks` | REST API |
| `http://<EC2>:8080/api/status` | App status |

## 🧪 Run Locally
```bash
mvn clean spring-boot:run
# Visit http://localhost:8080
```
