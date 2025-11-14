#!/bin/bash
# Test script para diagnosticar problema de chunked encoding

BASE_URL="http://localhost:8080/api/v1"

echo "=== Teste 1: Health Check (sem auth) ==="
curl -v "$BASE_URL/health" 2>&1 | grep -E "(Transfer-Encoding|Content-Length|HTTP/)"
echo ""

echo "=== Teste 2: Login para obter token ==="
TOKEN_RESPONSE=$(curl -s -X POST "$BASE_URL/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
    "email": "ademar@email.com",
    "password": "presidente123"
  }')

TOKEN=$(echo "$TOKEN_RESPONSE" | grep -o '"token":"[^"]*' | cut -d'"' -f4)
echo "Token obtido: ${TOKEN:0:50}..."
echo ""

echo "=== Teste 3: GET /services (verificar encoding) ==="
curl -v -X GET "$BASE_URL/services?page=0&size=5" \
  -H "Authorization: Bearer $TOKEN" 2>&1 | grep -E "(Transfer-Encoding|Content-Length|HTTP/)"
echo ""

echo "=== Teste 4: POST /appointments/book (com horário FUTURO) ==="
# Calcula horário 48h no futuro para garantir que não está no passado
FUTURE_TIME=$(date -u -d "+48 hours" +"%Y-%m-%dT14:30:00Z" 2>/dev/null || date -u -v+48H +"%Y-%m-%dT14:30:00Z")

echo "Tentando agendar para: $FUTURE_TIME"

curl -v -X POST "$BASE_URL/appointments/book" \
  -H "Content-Type: application/json" \
  -H "Authorization: Bearer $TOKEN" \
  -d "{
    \"clientId\": 23,
    \"barberId\": 2,
    \"serviceId\": 3,
    \"startTime\": \"$FUTURE_TIME\",
    \"tz\": \"America/Sao_Paulo\"
  }" 2>&1 | grep -E "(Transfer-Encoding|Content-Length|HTTP/|error|Error)"

echo ""
echo "=== Análise ==="
echo "✅ Se vir 'Content-Length' -> Resposta OK (sem chunking)"
echo "❌ Se vir 'Transfer-Encoding: chunked' -> Problema pode ocorrer"
echo "🔍 Verifique também se há erro 400 (horário no passado)"

