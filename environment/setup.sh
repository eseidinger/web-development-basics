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

# Check Keycloak health
echo "🔑 Checking Keycloak health..."
if docker compose exec -T keycloak curl -s http://localhost:8080/health/ready > /dev/null 2>&1; then
  echo "✅ Keycloak is ready"
else
  echo "⏳ Keycloak is starting up, this may take a minute..."
  for i in {1..30}; do
    if docker compose exec -T keycloak curl -s http://localhost:8080/health/ready > /dev/null 2>&1; then
      echo "✅ Keycloak is ready"
      break
    fi
    sleep 2
  done
fi

# Initialize Keycloak realm
echo ""
echo "🔧 Initializing Keycloak realm..."
docker compose run --rm keycloak-init

echo ""
echo "✅ Development environment is ready!"
echo ""
echo "📍 Access points:"
echo "   - Keycloak Admin: http://localhost:8090/admin (admin/admin)"
echo "   - Grafana: http://localhost:3000 (admin/admin)"
echo "   - Prometheus: http://localhost:9090"
echo "   - Jaeger: http://localhost:16686"
echo "   - PostgreSQL: localhost:5432 (taskboard/taskboard_dev_password)"
echo ""
echo "🧪 Test users (password: password):"
echo "   - alice@taskboard.local"
echo "   - bob@taskboard.local"
echo "   - charlie@taskboard.local"
echo ""
echo "📚 Next steps:"
echo "   1. Start your Spring Boot backend"
echo "   2. Configure application-dev.yml with OIDC endpoints"
echo "   3. Build and run: mvn spring-boot:run -Dspring-boot.run.arguments='--spring.profiles.active=dev'"
