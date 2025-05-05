#!/bin/bash

# Renk tanımlamaları
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[0;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Fonksiyonlar
function print_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

function print_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

function print_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

function print_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

function check_server() {
    print_info "Sunucu durumu kontrol ediliyor..."
    local status_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/agents/pool/status)

    if [[ "$status_code" == "200" ]]; then
        print_success "Sunucu çalışıyor (HTTP $status_code)"
        return 0
    else
        print_error "Sunucu çalışmıyor veya erişilemiyor (HTTP $status_code)"
        return 1
    fi
}

function show_help() {
    echo -e "${BLUE}Test Çalıştırma Aracı${NC}"
    echo "Kullanım: $0 [seçenekler]"
    echo ""
    echo "Seçenekler:"
    echo "  -h, --help                 Bu yardım mesajını göster"
    echo "  -f, --file <dosya_yolu>    Kullanılacak test dosyası (varsayılan: src/main/resources/example-test.json)"
    echo "  -w, --wait                 Test tamamlanana kadar bekle ve sonuçları göster"
    echo "  -t, --timeout <saniye>     Bekleme zaman aşımı (varsayılan: 60 saniye)"
    echo "  -v, --verbose              Ayrıntılı çıktı göster"
    echo ""
    exit 0
}

# Varsayılan değerler
TEST_FILE="src/main/resources/example-test.json"
WAIT_FOR_RESULT=false
TIMEOUT=60
VERBOSE=false

# Parametreleri işle
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -f|--file)
            TEST_FILE="$2"
            shift 2
            ;;
        -w|--wait)
            WAIT_FOR_RESULT=true
            shift
            ;;
        -t|--timeout)
            TIMEOUT="$2"
            shift 2
            ;;
        -v|--verbose)
            VERBOSE=true
            shift
            ;;
        *)
            print_error "Bilinmeyen parametre: $1"
            show_help
            ;;
    esac
done

# Dosya kontrolü
if [ ! -f "$TEST_FILE" ]; then
    print_error "Test dosyası bulunamadı: $TEST_FILE"
    exit 1
fi

# jq kontrolü
JQ_AVAILABLE=false
if command -v jq &> /dev/null; then
    JQ_AVAILABLE=true
    if [ "$VERBOSE" = true ]; then
        print_info "jq bulundu, JSON işleme için kullanılacak"
    fi
else
    print_warning "jq bulunamadı, basit JSON işleme kullanılacak"
    print_warning "Daha iyi JSON işleme için: 'brew install jq' veya 'apt-get install jq'"
fi

# Sunucu kontrolü
if ! check_server; then
    print_error "Sunucu erişilebilir değil. Lütfen sunucunun çalıştığından emin olun."
    exit 1
fi

# Otomatik agent yönetimi kullanılıyor, agent kaydı yapmaya gerek yok
print_info "Otomatik agent yönetimi kullanılıyor..."

# Agent havuzunun durumunu kontrol et
print_info "Agent havuzu durumu kontrol ediliyor..."
POOL_STATUS=$(curl -s -X GET http://localhost:8080/api/agents/pool/status -H "Content-Type: application/json")

# JSON işleme
if [ -z "$POOL_STATUS" ]; then
    print_error "Agent havuzu durumu alınamadı. Sunucu çalışıyor mu?"
    TOTAL_AGENTS="?"
    IDLE_AGENTS="?"
else
    if [ "$JQ_AVAILABLE" = true ]; then
        # jq ile JSON işleme
        TOTAL_AGENTS=$(echo $POOL_STATUS | jq -r '.totalAgents // "?"')
        IDLE_AGENTS=$(echo $POOL_STATUS | jq -r '.idleAgents // "?"')
    else
        # Basit grep ile değerleri çıkarma
        TOTAL_AGENTS=$(echo $POOL_STATUS | grep -o '"totalAgents":[0-9]*' | grep -o '[0-9]*')
        IDLE_AGENTS=$(echo $POOL_STATUS | grep -o '"idleAgents":[0-9]*' | grep -o '[0-9]*')

        # Değerler boşsa varsayılan değerler ata
        if [ -z "$TOTAL_AGENTS" ]; then TOTAL_AGENTS="?"; fi
        if [ -z "$IDLE_AGENTS" ]; then IDLE_AGENTS="?"; fi
    fi
fi

print_info "Toplam agent sayısı: $TOTAL_AGENTS"
print_info "Boşta agent sayısı: $IDLE_AGENTS"

# Boşta agent kontrolü (sadece sayısal değer ise)
if [[ "$IDLE_AGENTS" =~ ^[0-9]+$ ]] && [ "$IDLE_AGENTS" -eq "0" ]; then
    print_warning "Boşta agent yok, test otomatik olarak kuyruğa alınacak..."
fi

# Test dosyasını gönder ve test ID'sini al
print_info "Test oluşturuluyor: $TEST_FILE"
TEST_RESPONSE=$(curl -s -X POST http://localhost:8080/api/tests -H "Content-Type: application/json" -d @"$TEST_FILE")

if [ "$JQ_AVAILABLE" = true ]; then
    TEST_ID=$(echo $TEST_RESPONSE | jq -r '.id // ""')
else
    TEST_ID=$(echo $TEST_RESPONSE | grep -o '"id":"[^"]*' | cut -d'"' -f4)
fi

if [ -z "$TEST_ID" ]; then
    print_error "Test ID alınamadı. Test oluşturulamadı."
    print_error "Sunucu yanıtı: $TEST_RESPONSE"
    exit 1
fi

print_success "Test oluşturuldu. Test ID: $TEST_ID"

# Testi çalıştır (otomatik agent atama ile)
print_info "Test çalıştırılıyor..."
RUN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/tests/$TEST_ID/run-auto" -H "Content-Type: application/json")

if [ "$VERBOSE" = true ]; then
    print_info "Çalıştırma yanıtı: $RUN_RESPONSE"
fi

print_success "Test başlatıldı."

# Test kuyruğu durumunu kontrol et
print_info "Test kuyruğu durumu kontrol ediliyor..."
QUEUE_STATUS=$(curl -s -X GET http://localhost:8080/api/tests/queue/status -H "Content-Type: application/json")

if [ "$JQ_AVAILABLE" = true ]; then
    QUEUE_LENGTH=$(echo $QUEUE_STATUS | jq -r '.length // "?"')
    print_info "Kuyruk uzunluğu: $QUEUE_LENGTH"

    if [ "$VERBOSE" = true ]; then
        echo $QUEUE_STATUS | jq
    fi
else
    echo $QUEUE_STATUS
fi

# Test sonuçlarını bekle
if [ "$WAIT_FOR_RESULT" = true ]; then
    print_info "Test sonuçları bekleniyor (maksimum $TIMEOUT saniye)..."

    for ((i=1; i<=$TIMEOUT; i++)); do
        TEST_STATUS=$(curl -s -X GET "http://localhost:8080/api/tests/$TEST_ID" -H "Content-Type: application/json")

        if [ "$JQ_AVAILABLE" = true ]; then
            STATUS=$(echo $TEST_STATUS | jq -r '.status // ""')
        else
            STATUS=$(echo $TEST_STATUS | grep -o '"status":"[^"]*' | cut -d'"' -f4)
        fi

        if [[ "$STATUS" == "COMPLETED" || "$STATUS" == "FAILED" || "$STATUS" == "CANCELLED" ]]; then
            print_success "Test tamamlandı. Durum: $STATUS"

            if [ "$JQ_AVAILABLE" = true ]; then
                echo $TEST_STATUS | jq
            else
                echo $TEST_STATUS
            fi

            break
        elif [[ "$STATUS" == "RUNNING" ]]; then
            print_info "Test çalışıyor... ($i/$TIMEOUT)"
        elif [[ "$STATUS" == "QUEUED" ]]; then
            print_info "Test kuyrukta bekliyor... ($i/$TIMEOUT)"
        else
            print_warning "Bilinmeyen test durumu: $STATUS ($i/$TIMEOUT)"
        fi

        if [ $i -eq $TIMEOUT ]; then
            print_warning "Zaman aşımı: Test hala tamamlanmadı."
            break
        fi

        sleep 1
    done
else
    echo -e "\nTest sonuçlarını kontrol etmek için:"
    echo "curl -X GET http://localhost:8080/api/tests/$TEST_ID -H \"Content-Type: application/json\""
fi
