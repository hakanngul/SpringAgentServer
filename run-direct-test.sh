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

# example-test.json dosyasını oku
TEST_JSON=$(cat src/main/resources/example-test.json)

# TestRequest JSON'ını oluştur
TEST_REQUEST=$(cat <<EOF
{
  "test": $TEST_JSON,
  "async": false,
  "agentId": "$AGENT_ID"
}
EOF
)

# Testi doğrudan çalıştır
echo "Test çalıştırılıyor..."
curl -X POST "http://localhost:8080/api/tests/run" \
  -H "Content-Type: application/json" \
  -d "$TEST_REQUEST"

echo -e "\nTest başlatıldı."
