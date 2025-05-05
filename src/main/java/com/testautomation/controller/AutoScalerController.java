package com.testautomation.controller;

import com.testautomation.service.core.AutoScalerService;
import com.testautomation.service.core.AutoScalerService.AutoScalerOptions;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * AutoScalerController
 * Otomatik ölçeklendirme API'leri
 */
@RestController
@RequestMapping("/api/autoscaler")
@RequiredArgsConstructor
@Tag(name = "Otomatik Ölçeklendirme", description = "Otomatik ölçeklendirme yönetimi API'leri")
public class AutoScalerController {
    private final AutoScalerService autoScalerService;

    /**
     * Otomatik ölçeklendirme durumunu al
     * @return Otomatik ölçeklendirme seçenekleri
     */
    @Operation(summary = "Otomatik ölçeklendirme durumunu al", description = "Otomatik ölçeklendirme seçeneklerini döndürür")
    @GetMapping("/status")
    public ResponseEntity<AutoScalerOptions> getStatus() {
        return ResponseEntity.ok(autoScalerService.getOptions());
    }

    /**
     * Otomatik ölçeklendirmeyi etkinleştir/devre dışı bırak
     * @param enabled Etkin mi?
     * @return Otomatik ölçeklendirme seçenekleri
     */
    @Operation(summary = "Otomatik ölçeklendirmeyi etkinleştir/devre dışı bırak", description = "Otomatik ölçeklendirmeyi etkinleştirir veya devre dışı bırakır")
    @PostMapping("/enable")
    public ResponseEntity<AutoScalerOptions> setEnabled(@RequestParam boolean enabled) {
        autoScalerService.setEnabled(enabled);
        return ResponseEntity.ok(autoScalerService.getOptions());
    }

    /**
     * Otomatik ölçeklendirme seçeneklerini güncelle
     * @param options Yeni seçenekler
     * @return Güncellenmiş otomatik ölçeklendirme seçenekleri
     */
    @Operation(summary = "Otomatik ölçeklendirme seçeneklerini güncelle", description = "Otomatik ölçeklendirme seçeneklerini günceller")
    @PostMapping("/options")
    public ResponseEntity<AutoScalerOptions> updateOptions(@RequestBody AutoScalerOptions options) {
        autoScalerService.updateOptions(options);
        return ResponseEntity.ok(autoScalerService.getOptions());
    }

    /**
     * Manuel ölçeklendirme kontrolü yap
     * @return İşlem sonucu
     */
    @Operation(summary = "Manuel ölçeklendirme kontrolü yap", description = "Manuel olarak ölçeklendirme kontrolü yapar")
    @PostMapping("/check")
    public ResponseEntity<String> manualCheck() {
        autoScalerService.checkAndScale();
        return ResponseEntity.ok("Ölçeklendirme kontrolü yapıldı");
    }
}
