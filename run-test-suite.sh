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
    local status_code=$(curl -s -o /dev/null -w "%{http_code}" http://localhost:8080/api/test-suites)

    if [[ "$status_code" == "200" ]]; then
        print_success "Sunucu çalışıyor (HTTP $status_code)"
        return 0
    else
        print_error "Sunucu çalışmıyor veya erişilemiyor (HTTP $status_code)"
        return 1
    fi
}

function list_test_suites() {
    print_info "Mevcut Test Suite'ler listeleniyor..."
    local response=$(curl -s -X GET http://localhost:8080/api/test-suites -H "Content-Type: application/json")

    if [ -z "$response" ]; then
        print_error "Test Suite listesi alınamadı."
        return 1
    fi

    if [ "$JQ_AVAILABLE" = true ]; then
        # jq ile JSON işleme
        local count=$(echo $response | jq '. | length')

        if [ "$count" -eq 0 ]; then
            print_warning "Hiç Test Suite bulunamadı."
            return 1
        fi

        echo -e "\n${BLUE}Mevcut Test Suite'ler:${NC}"
        echo -e "${YELLOW}No  ID                                     İsim                                    Durum${NC}"
        echo "-----------------------------------------------------------------------------------------"

        for ((i=0; i<$count; i++)); do
            local id=$(echo $response | jq -r ".[$i].id")
            local name=$(echo $response | jq -r ".[$i].name")
            local status=$(echo $response | jq -r ".[$i].status")
            printf "%-3d %-40s %-40s %-10s\n" $((i+1)) "$id" "$name" "$status"
        done

        echo "-----------------------------------------------------------------------------------------"
        echo -e "Toplam: ${GREEN}$count${NC} Test Suite\n"

        # Kullanıcıdan seçim iste
        echo -n "Çalıştırmak istediğiniz Test Suite numarasını girin (1-$count): "
        read selection

        # Seçimi doğrula
        if ! [[ "$selection" =~ ^[0-9]+$ ]] || [ "$selection" -lt 1 ] || [ "$selection" -gt "$count" ]; then
            print_error "Geçersiz seçim: $selection"
            return 1
        fi

        # Seçilen Test Suite'in ID'sini al
        SUITE_ID=$(echo $response | jq -r ".[$(($selection-1))].id")
        SUITE_NAME=$(echo $response | jq -r ".[$(($selection-1))].name")

        print_success "Seçilen Test Suite: $SUITE_NAME ($SUITE_ID)"
        return 0
    else
        # Basit grep ile işleme (jq yoksa)
        echo -e "\n${BLUE}Mevcut Test Suite'ler:${NC}"
        echo $response
        echo -e "\n${YELLOW}Not:${NC} jq yüklü olmadığı için JSON formatında gösteriliyor. Daha iyi görüntüleme için jq yükleyin."

        # Kullanıcıdan ID iste
        echo -n "Çalıştırmak istediğiniz Test Suite ID'sini girin: "
        read SUITE_ID

        if [ -z "$SUITE_ID" ]; then
            print_error "Geçersiz ID."
            return 1
        fi

        return 0
    fi
}

function create_test_suite() {
    print_info "Yeni Test Suite oluşturuluyor..."

    # Örnek Test Suite dosyasını kontrol et
    local example_file="src/main/resources/example-test-suite.json"
    if [ ! -f "$example_file" ]; then
        print_error "Örnek Test Suite dosyası bulunamadı: $example_file"
        return 1
    fi

    # Test Suite oluştur
    local response=$(curl -s -X POST http://localhost:8080/api/test-suites -H "Content-Type: application/json" -d @"$example_file")

    if [ -z "$response" ]; then
        print_error "Test Suite oluşturulamadı."
        return 1
    fi

    if [ "$JQ_AVAILABLE" = true ]; then
        SUITE_ID=$(echo $response | jq -r '.id // ""')
        SUITE_NAME=$(echo $response | jq -r '.name // ""')
    else
        SUITE_ID=$(echo $response | grep -o '"id":"[^"]*' | cut -d'"' -f4)
        SUITE_NAME=$(echo $response | grep -o '"name":"[^"]*' | cut -d'"' -f4)
    fi

    if [ -z "$SUITE_ID" ]; then
        print_error "Test Suite ID alınamadı. Test Suite oluşturulamadı."
        print_error "Sunucu yanıtı: $response"
        return 1
    fi

    print_success "Test Suite oluşturuldu: $SUITE_NAME ($SUITE_ID)"
    return 0
}

function show_help() {
    echo -e "${BLUE}Test Suite Çalıştırma Aracı${NC}"
    echo "Kullanım: $0 [seçenekler]"
    echo ""
    echo "Seçenekler:"
    echo "  -h, --help                 Bu yardım mesajını göster"
    echo "  -i, --id <suite_id>        Çalıştırılacak Test Suite ID"
    echo "  -l, --list                 Mevcut Test Suite'leri listele ve seç"
    echo "  -c, --create               Yeni bir Test Suite oluştur ve çalıştır"
    echo "  -w, --wait                 Test Suite tamamlanana kadar bekle ve sonuçları göster"
    echo "  -t, --timeout <saniye>     Bekleme zaman aşımı (varsayılan: 300 saniye)"
    echo "  -v, --verbose              Ayrıntılı çıktı göster"
    echo ""
    exit 0
}

# Varsayılan değerler
SUITE_ID=""
WAIT_FOR_RESULT=false
TIMEOUT=300
VERBOSE=false
LIST_SUITES=false
CREATE_SUITE=false

# Parametreleri işle
while [[ $# -gt 0 ]]; do
    case $1 in
        -h|--help)
            show_help
            ;;
        -i|--id)
            SUITE_ID="$2"
            shift 2
            ;;
        -l|--list)
            LIST_SUITES=true
            shift
            ;;
        -c|--create)
            CREATE_SUITE=true
            shift
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

# Test Suite ID belirleme
if [ -z "$SUITE_ID" ]; then
    if [ "$CREATE_SUITE" = true ]; then
        # Yeni Test Suite oluştur
        if ! create_test_suite; then
            print_error "Test Suite oluşturulamadı."
            exit 1
        fi
    elif [ "$LIST_SUITES" = true ]; then
        # Mevcut Test Suite'leri listele ve seç
        if ! list_test_suites; then
            print_error "Test Suite seçilemedi."
            exit 1
        fi
    else
        # Hiçbir seçenek belirtilmemişse, kullanıcıya sor
        echo -e "\n${BLUE}Test Suite seçenekleri:${NC}"
        echo "1. Mevcut Test Suite'leri listele ve seç"
        echo "2. Yeni bir Test Suite oluştur ve çalıştır"
        echo -n "Seçiminiz (1-2): "
        read choice

        case $choice in
            1)
                if ! list_test_suites; then
                    print_error "Test Suite seçilemedi."
                    exit 1
                fi
                ;;
            2)
                if ! create_test_suite; then
                    print_error "Test Suite oluşturulamadı."
                    exit 1
                fi
                ;;
            *)
                print_error "Geçersiz seçim: $choice"
                exit 1
                ;;
        esac
    fi
fi

# Test Suite'i kontrol et
print_info "Test Suite kontrol ediliyor: $SUITE_ID"
SUITE_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/test-suites/$SUITE_ID" -H "Content-Type: application/json")

if [ "$JQ_AVAILABLE" = true ]; then
    SUITE_NAME=$(echo $SUITE_RESPONSE | jq -r '.name // ""')
else
    SUITE_NAME=$(echo $SUITE_RESPONSE | grep -o '"name":"[^"]*' | cut -d'"' -f4)
fi

if [ -z "$SUITE_NAME" ]; then
    print_error "Test Suite bulunamadı: $SUITE_ID"
    exit 1
fi

print_info "Test Suite bulundu: $SUITE_NAME ($SUITE_ID)"

# Test Suite'i çalıştır
print_info "Test Suite çalıştırılıyor..."
RUN_RESPONSE=$(curl -s -X POST "http://localhost:8080/api/test-suites/$SUITE_ID/run" -H "Content-Type: application/json")

if [ "$VERBOSE" = true ]; then
    print_info "Çalıştırma yanıtı: $RUN_RESPONSE"
fi

print_success "Test Suite başlatıldı."

# Test Suite sonuçlarını bekle
if [ "$WAIT_FOR_RESULT" = true ]; then
    print_info "Test Suite sonuçları bekleniyor (maksimum $TIMEOUT saniye)..."

    for ((i=1; i<=$TIMEOUT; i++)); do
        SUITE_STATUS=$(curl -s -X GET "http://localhost:8080/api/test-suites/$SUITE_ID" -H "Content-Type: application/json")

        if [ "$JQ_AVAILABLE" = true ]; then
            STATUS=$(echo $SUITE_STATUS | jq -r '.status // ""')
        else
            STATUS=$(echo $SUITE_STATUS | grep -o '"status":"[^"]*' | cut -d'"' -f4)
        fi

        if [[ "$STATUS" == "COMPLETED" || "$STATUS" == "FAILED" || "$STATUS" == "CANCELLED" ]]; then
            print_success "Test Suite tamamlandı. Durum: $STATUS"

            # Test Suite sonuçlarını getir
            RESULTS_RESPONSE=$(curl -s -X GET "http://localhost:8080/api/test-suites/$SUITE_ID/results" -H "Content-Type: application/json")

            if [ "$JQ_AVAILABLE" = true ]; then
                if [ "$VERBOSE" = true ]; then
                    echo $RESULTS_RESPONSE | jq
                else
                    # İlk sonucu al (en son oluşturulan)
                    RESULT_ID=$(echo $RESULTS_RESPONSE | jq -r '.[0].id // ""')
                    if [ -n "$RESULT_ID" ]; then
                        RESULT=$(curl -s -X GET "http://localhost:8080/api/test-suites/results/$RESULT_ID" -H "Content-Type: application/json")
                        echo $RESULT | jq
                    else
                        print_warning "Test Suite sonucu bulunamadı"
                    fi
                fi
            else
                echo $RESULTS_RESPONSE
            fi

            break
        elif [[ "$STATUS" == "RUNNING" ]]; then
            print_info "Test Suite çalışıyor... ($i/$TIMEOUT)"
        elif [[ "$STATUS" == "QUEUED" ]]; then
            print_info "Test Suite kuyrukta bekliyor... ($i/$TIMEOUT)"
        else
            print_warning "Bilinmeyen Test Suite durumu: $STATUS ($i/$TIMEOUT)"
        fi

        if [ $i -eq $TIMEOUT ]; then
            print_warning "Zaman aşımı: Test Suite hala tamamlanmadı."
            break
        fi

        sleep 1
    done
else
    echo -e "\nTest Suite sonuçlarını kontrol etmek için:"
    echo "curl -X GET http://localhost:8080/api/test-suites/$SUITE_ID -H \"Content-Type: application/json\""
    echo -e "\nTest Suite sonuçlarını getirmek için:"
    echo "curl -X GET http://localhost:8080/api/test-suites/$SUITE_ID/results -H \"Content-Type: application/json\""
fi
