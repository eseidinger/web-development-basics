#!/bin/bash

# Quick setup script for Task Board development environment

set -e

echo "🚀 Starting Task Board development environment..."
echo ""

# Check if Docker is running
if ! docker info > /dev/null 2>&1; then
  echo "❌ Docker is not running. Please start Docker and try again."
  exit 1
fi

# Start all services
echo "📦 Starting Docker services..."
docker compose up -d

# Wait for services
echo "⏳ Waiting for services to be ready..."
sleep 5

echo ""
echo "✅ Development environment is ready!"
echo ""
echo "📍 Access points:"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Prometheus: http://localhost:9090"
echo "   - Jaeger: http://localhost:16686"
echo "   - PostgreSQL: localhost:5432 (taskboard/taskboard_dev_password)"
echo ""
echo "🔐 OIDC provider:"
echo "   - Issuer: https://keycloak.eseidinger.de/realms/taskboard-dev"
echo "   - Client ID: taskboard"
echo ""
echo "📚 Next steps:"
echo "   1. Start your Spring Boot backend"
echo "   2. Ensure .env points to the hosted OIDC issuer"
echo "   3. Build and run: mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'"
