#!/bin/bash

set -e

echo "=========================================="
echo "  EC2 Setup for Spring DevOps Demo"
echo "=========================================="

# Detect OS
if [ -f /etc/os-release ]; then
    . /etc/os-release
    OS=$ID
fi

# ── Install Java 17 ──────────────────────────────────────────
echo "[1/4] Installing Java 17..."
if [ "$OS" = "ubuntu" ] || [ "$OS" = "debian" ]; then
    apt-get update -y
    apt-get install -y openjdk-17-jdk curl
elif [ "$OS" = "amzn" ] || [ "$OS" = "rhel" ] || [ "$OS" = "centos" ]; then
    yum update -y
    yum install -y java-17-amazon-corretto curl
fi

java -version
echo "✅ Java installed"

# ── Create app directory ─────────────────────────────────────
echo "[2/4] Creating app directory..."
mkdir -p /opt/spring-devops-demo
# Adjust 'ec2-user' to your SSH user (ubuntu for Ubuntu AMI)
chown -R ec2-user:ec2-user /opt/spring-devops-demo 2>/dev/null || \
chown -R ubuntu:ubuntu /opt/spring-devops-demo 2>/dev/null || true
echo "✅ Directory /opt/spring-devops-demo created"

# ── Configure firewall (allow port 8080) ─────────────────────
echo "[3/4] Configuring firewall..."
# Note: Also open port 8080 in your EC2 Security Group in AWS Console!
if command -v ufw &>/dev/null; then
    ufw allow 8080/tcp
    echo "✅ UFW: port 8080 opened"
elif command -v firewall-cmd &>/dev/null; then
    firewall-cmd --permanent --add-port=8080/tcp
    firewall-cmd --reload
    echo "✅ firewalld: port 8080 opened"
else
    echo "⚠️  No firewall found — ensure Security Group allows port 8080"
fi

# ── Create systemd service (optional, for auto-restart) ──────
echo "[4/4] Creating systemd service..."
cat > /etc/systemd/system/spring-devops-demo.service << 'SERVICE'
[Unit]
Description=Spring DevOps Demo App
After=network.target

[Service]
Type=simple
User=ec2-user
WorkingDirectory=/opt/spring-devops-demo
ExecStart=/usr/bin/java -jar /opt/spring-devops-demo/spring-devops-demo.jar
Restart=on-failure
RestartSec=10
StandardOutput=journal
StandardError=journal

[Install]
WantedBy=multi-user.target
SERVICE

systemctl daemon-reload
echo "✅ Systemd service created (spring-devops-demo)"

echo ""
echo "=========================================="
echo "  Setup Complete!"
echo "=========================================="
echo ""
echo "NEXT STEPS:"
echo "  1. Add these GitHub Secrets in your repo settings:"
echo "     EC2_HOST    → your EC2 public IP or DNS"
echo "     EC2_USER    → ec2-user (Amazon Linux) or ubuntu (Ubuntu)"
echo "     EC2_SSH_KEY → content of your .pem private key"
echo ""
echo "  2. Open port 8080 in your EC2 Security Group (AWS Console)"
echo ""
echo "  3. Push to 'main' branch → pipeline auto-deploys!"
echo ""
echo "  4. Visit: http://<EC2_HOST>:8080"
echo "  5. Health: http://<EC2_HOST>:8080/actuator/health"
echo ""
