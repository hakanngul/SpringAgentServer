#!/bin/bash

# Agent kaydı yap ve agent ID'sini al
echo "Agent kaydı yapılıyor..."
AGENT_RESPONSE=$(curl -s -X POST http://localhost:8080/api/agents/register -H "Content-Type: application/json")
AGENT_ID=$(echo $AGENT_RESPONSE | grep -o '"agentId":"[^"]*' | cut -d'"' -f4)

if [ -z "$AGENT_ID" ]; then
    echo "Agent ID alınamadı. Sunucu çalışıyor mu?"
    exit 1
fi

echo "Agent ID: $AGENT_ID"

# Test dosyasını gönder ve test ID'sini al
echo "Test oluşturuluyor..."
TEST_RESPONSE=$(curl -s -X POST http://localhost:8080/api/tests -H "Content-Type: application/json" -d @src/main/resources/example-test.json)
TEST_ID=$(echo $TEST_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ -z "$TEST_ID" ]; then
    echo "Test ID alınamadı. Test oluşturulamadı."
    exit 1
fi

echo "Test ID: $TEST_ID"

# Testi çalıştır
echo "Test çalıştırılıyor..."
curl -X POST "http://localhost:8080/api/tests/$TEST_ID/run?agentId=$AGENT_ID" -H "Content-Type: application/json"

echo -e "\nTest başlatıldı. Sonuçları kontrol etmek için:"
echo "curl -X GET http://localhost:8080/api/tests/$TEST_ID -H \"Content-Type: application/json\""
