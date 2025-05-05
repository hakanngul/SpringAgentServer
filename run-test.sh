#!/bin/bash

# Otomatik agent yönetimi kullanılıyor, agent kaydı yapmaya gerek yok
echo "Otomatik agent yönetimi kullanılıyor..."

# Agent havuzunun durumunu kontrol et
echo "Agent havuzu durumu kontrol ediliyor..."
POOL_STATUS=$(curl -s -X GET http://localhost:8080/api/agents/pool/status -H "Content-Type: application/json")

# JSON işleme için jq kullanılabilir, ancak varsayılan olarak yüklü olmayabilir
# Bu nedenle basit bir kontrol yapalım
if [ -z "$POOL_STATUS" ]; then
    echo "Agent havuzu durumu alınamadı. Sunucu çalışıyor mu?"
    TOTAL_AGENTS="?"
    IDLE_AGENTS="?"
else
    # Basit bir grep ile değerleri çıkarmaya çalışalım
    TOTAL_AGENTS=$(echo $POOL_STATUS | grep -o '"totalAgents":[0-9]*' | grep -o '[0-9]*')
    IDLE_AGENTS=$(echo $POOL_STATUS | grep -o '"idleAgents":[0-9]*' | grep -o '[0-9]*')

    # Değerler boşsa varsayılan değerler ata
    if [ -z "$TOTAL_AGENTS" ]; then TOTAL_AGENTS="?"; fi
    if [ -z "$IDLE_AGENTS" ]; then IDLE_AGENTS="?"; fi
fi

echo "Toplam agent sayısı: $TOTAL_AGENTS"
echo "Boşta agent sayısı: $IDLE_AGENTS"

# Boşta agent kontrolü (sadece sayısal değer ise)
if [[ "$IDLE_AGENTS" =~ ^[0-9]+$ ]] && [ "$IDLE_AGENTS" -eq "0" ]; then
    echo "Boşta agent yok, test otomatik olarak kuyruğa alınacak..."
fi

# Test dosyasını gönder ve test ID'sini al
echo "Test oluşturuluyor..."
TEST_RESPONSE=$(curl -s -X POST http://localhost:8080/api/tests -H "Content-Type: application/json" -d @src/main/resources/example-test.json)
TEST_ID=$(echo $TEST_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)

if [ -z "$TEST_ID" ]; then
    echo "Test ID alınamadı. Test oluşturulamadı."
    exit 1
fi

echo "Test ID: $TEST_ID"

# Testi çalıştır (otomatik agent atama ile)
echo "Test çalıştırılıyor..."
curl -X POST "http://localhost:8080/api/tests/$TEST_ID/run-auto" -H "Content-Type: application/json"

echo -e "\nTest başlatıldı. Sonuçları kontrol etmek için:"
echo "curl -X GET http://localhost:8080/api/tests/$TEST_ID -H \"Content-Type: application/json\""

# Test kuyruğu durumunu kontrol et
echo -e "\nTest kuyruğu durumu kontrol ediliyor..."
curl -s -X GET http://localhost:8080/api/tests/queue/status -H "Content-Type: application/json"
