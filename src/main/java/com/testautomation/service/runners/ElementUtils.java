package com.testautomation.service.runners;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.options.WaitForSelectorState;
import com.testautomation.model.enums.SelectorStrategy;
import com.testautomation.model.enums.WaitCondition;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class ElementUtils {
    private static final Logger logger = LoggerFactory.getLogger(ElementUtils.class);

    /**
     * Seçici oluştur
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @return Oluşturulan seçici
     */
    public static String getSelector(String target, String strategy) {
        if (target == null || target.isEmpty()) {
            throw new IllegalArgumentException("Hedef element belirtilmemiş");
        }

        SelectorStrategy selectorStrategy;
        try {
            if (strategy == null || strategy.isEmpty()) {
                selectorStrategy = SelectorStrategy.CSS;
            } else {
                selectorStrategy = SelectorStrategy.valueOf(strategy.toUpperCase());
            }
        } catch (IllegalArgumentException e) {
            logger.warn("Geçersiz seçici stratejisi: {}. CSS kullanılacak.", strategy);
            selectorStrategy = SelectorStrategy.CSS;
        }

        String selector;
        switch (selectorStrategy) {
            case ID:
                // Eğer hedef zaten # ile başlıyorsa, tekrar ekleme
                if (target.startsWith("#")) {
                    selector = target;
                } else {
                    selector = "#" + target;
                }
                break;
            case CLASS:
                // Eğer hedef zaten . ile başlıyorsa, tekrar ekleme
                if (target.startsWith(".")) {
                    selector = target;
                } else {
                    selector = "." + target;
                }
                break;
            case NAME:
                // Eğer hedef zaten [name='...'] formatındaysa, olduğu gibi kullan
                if (target.matches("\\[name=['\"](.*?)['\"]\\]")) {
                    selector = target;
                } else {
                    selector = "[name='" + target + "']";
                }
                break;
            case XPATH:
                // Eğer hedef zaten xpath= ile başlıyorsa, tekrar ekleme
                if (target.startsWith("xpath=")) {
                    selector = target;
                } else {
                    selector = "xpath=" + target;
                }
                break;
            case CSS:
            default:
                selector = target;
                break;
        }

        logger.debug("Oluşturulan seçici: {} (strateji: {})", selector, selectorStrategy);
        return selector;
    }

    /**
     * Elementi bul
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @return Bulunan element
     */
    public static Locator findElement(Page page, String target, String strategy) {
        if (target == null || target.isEmpty()) {
            throw new IllegalArgumentException("Hedef element belirtilmemiş");
        }

        String selector = getSelector(target, strategy);
        logger.debug("Element aranıyor: {}", selector);

        try {
            return page.locator(selector);
        } catch (Exception e) {
            logger.error("Element bulunurken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elementi bul ve tıkla
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void findAndClick(Page page, String target, String strategy) {
        logger.debug("Elemente tıklanıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).click();
            logger.debug("Elemente başarıyla tıklandı");
        } catch (Exception e) {
            logger.error("Elemente tıklarken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elementi bul ve metin yaz
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @param value Yazılacak metin
     */
    public static void findAndType(Page page, String target, String strategy, String value) {
        logger.debug("Elemente metin yazılıyor: {} (strateji: {}), değer: {}", target, strategy, value);
        try {
            findElement(page, target, strategy).fill(value);
            logger.debug("Elemente başarıyla metin yazıldı");
        } catch (Exception e) {
            logger.error("Elemente metin yazarken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elementi bul ve seç
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @param value Seçilecek değer
     */
    public static void findAndSelect(Page page, String target, String strategy, String value) {
        logger.debug("Elementten seçim yapılıyor: {} (strateji: {}), değer: {}", target, strategy, value);
        try {
            findElement(page, target, strategy).selectOption(value);
            logger.debug("Elementten başarıyla seçim yapıldı");
        } catch (Exception e) {
            logger.error("Elementten seçim yaparken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Element için bekle
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @param timeout Zaman aşımı
     */
    public static void waitForElement(Page page, String target, String strategy, int timeout) {
        logger.debug("Element için bekleniyor: {} (strateji: {}), timeout: {}", target, strategy, timeout);
        try {
            findElement(page, target, strategy).waitFor(new Locator.WaitForOptions().setTimeout(timeout));
            logger.debug("Element başarıyla beklendi");
        } catch (Exception e) {
            logger.error("Element beklenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Metni doğrula
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @param expectedText Beklenen metin
     */
    public static void verifyText(Page page, String target, String strategy, String expectedText) {
        logger.debug("Metin doğrulanıyor: {} (strateji: {}), beklenen: {}", target, strategy, expectedText);
        try {
            String actualText = findElement(page, target, strategy).textContent();
            if (!actualText.contains(expectedText)) {
                String errorMsg = "Metin doğrulama başarısız: Beklenen '" + expectedText + "', Alınan '" + actualText + "'";
                logger.error(errorMsg);
                throw new AssertionError(errorMsg);
            }
            logger.debug("Metin başarıyla doğrulandı");
        } catch (Exception e) {
            logger.error("Metin doğrulanırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * URL'i doğrula
     * @param page Playwright sayfası
     * @param expectedUrl Beklenen URL
     */
    public static void verifyUrl(Page page, String expectedUrl) {
        logger.debug("URL doğrulanıyor, beklenen: {}", expectedUrl);
        try {
            String currentUrl = page.url();
            if (!currentUrl.contains(expectedUrl)) {
                String errorMsg = "URL doğrulama başarısız: Beklenen '" + expectedUrl + "', Alınan '" + currentUrl + "'";
                logger.error(errorMsg);
                throw new AssertionError(errorMsg);
            }
            logger.debug("URL başarıyla doğrulandı");
        } catch (Exception e) {
            logger.error("URL doğrulanırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elemente hover yap
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void hover(Page page, String target, String strategy) {
        logger.debug("Elemente hover yapılıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).hover();
            logger.debug("Elemente başarıyla hover yapıldı");
        } catch (Exception e) {
            logger.error("Elemente hover yaparken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elemente çift tıkla
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void doubleClick(Page page, String target, String strategy) {
        logger.debug("Elemente çift tıklanıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).dblclick();
            logger.debug("Elemente başarıyla çift tıklandı");
        } catch (Exception e) {
            logger.error("Elemente çift tıklarken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elemente sağ tıkla
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void rightClick(Page page, String target, String strategy) {
        logger.debug("Elemente sağ tıklanıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).click(new Locator.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
            logger.debug("Elemente başarıyla sağ tıklandı");
        } catch (Exception e) {
            logger.error("Elemente sağ tıklarken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checkbox'ı işaretle
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void check(Page page, String target, String strategy) {
        logger.debug("Checkbox işaretleniyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).check();
            logger.debug("Checkbox başarıyla işaretlendi");
        } catch (Exception e) {
            logger.error("Checkbox işaretlenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Checkbox'ın işaretini kaldır
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void uncheck(Page page, String target, String strategy) {
        logger.debug("Checkbox'ın işareti kaldırılıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).uncheck();
            logger.debug("Checkbox'ın işareti başarıyla kaldırıldı");
        } catch (Exception e) {
            logger.error("Checkbox'ın işareti kaldırılırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elemente odaklan
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void focus(Page page, String target, String strategy) {
        logger.debug("Elemente odaklanılıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).focus();
            logger.debug("Elemente başarıyla odaklanıldı");
        } catch (Exception e) {
            logger.error("Elemente odaklanılırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Elementi görünür alana kaydır
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     */
    public static void scrollIntoView(Page page, String target, String strategy) {
        logger.debug("Element görünür alana kaydırılıyor: {} (strateji: {})", target, strategy);
        try {
            findElement(page, target, strategy).scrollIntoViewIfNeeded();
            logger.debug("Element başarıyla görünür alana kaydırıldı");
        } catch (Exception e) {
            logger.error("Element görünür alana kaydırılırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * JavaScript kodu çalıştır
     * @param page Playwright sayfası
     * @param script Çalıştırılacak JavaScript kodu
     * @return Çalıştırılan kodun sonucu
     */
    public static Object evaluate(Page page, String script) {
        logger.debug("JavaScript kodu çalıştırılıyor: {}", script);
        try {
            Object result = page.evaluate(script);
            logger.debug("JavaScript kodu başarıyla çalıştırıldı");
            return result;
        } catch (Exception e) {
            logger.error("JavaScript kodu çalıştırılırken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }

    /**
     * Belirli bir koşul için dinamik bekleme
     * @param page Playwright sayfası
     * @param target Hedef element
     * @param strategy Seçim stratejisi
     * @param condition Bekleme koşulu
     * @param value Koşul için değer (opsiyonel)
     * @param timeout Zaman aşımı (ms)
     */
    public static void waitForCondition(Page page, String target, String strategy, WaitCondition condition, String value, int timeout) {
        logger.debug("Koşul için bekleniyor: {} (strateji: {}), koşul: {}, değer: {}, timeout: {}",
                target, strategy, condition, value, timeout);

        try {
            String selector = getSelector(target, strategy);

            switch (condition) {
                case VISIBLE:
                    page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.VISIBLE)
                            .setTimeout(timeout));
                    break;
                case HIDDEN:
                    page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.HIDDEN)
                            .setTimeout(timeout));
                    break;
                case ENABLED:
                    // Playwright doğrudan enabled state'i desteklemediği için JavaScript ile kontrol ediyoruz
                    page.waitForFunction("() => !document.querySelector('" + selector.replace("'", "\\'") +
                            "').disabled", new Page.WaitForFunctionOptions().setTimeout(timeout));
                    break;
                case DISABLED:
                    // Playwright doğrudan disabled state'i desteklemediği için JavaScript ile kontrol ediyoruz
                    page.waitForFunction("() => document.querySelector('" + selector.replace("'", "\\'") +
                            "').disabled === true", new Page.WaitForFunctionOptions().setTimeout(timeout));
                    break;
                case PRESENT:
                    page.waitForSelector(selector, new Page.WaitForSelectorOptions()
                            .setState(WaitForSelectorState.ATTACHED)
                            .setTimeout(timeout));
                    break;
                case URL_CONTAINS:
                    if (value == null || value.isEmpty()) {
                        throw new IllegalArgumentException("URL_CONTAINS koşulu için değer gereklidir");
                    }
                    page.waitForURL("**" + value + "**", new Page.WaitForURLOptions().setTimeout(timeout));
                    break;
                case URL_EQUALS:
                    if (value == null || value.isEmpty()) {
                        throw new IllegalArgumentException("URL_EQUALS koşulu için değer gereklidir");
                    }
                    page.waitForURL(value, new Page.WaitForURLOptions().setTimeout(timeout));
                    break;
                case TEXT_CONTAINS:
                    if (value == null || value.isEmpty()) {
                        throw new IllegalArgumentException("TEXT_CONTAINS koşulu için değer gereklidir");
                    }
                    page.waitForFunction("() => document.querySelector('" + selector.replace("'", "\\'") +
                            "').textContent.includes('" + value.replace("'", "\\'") + "')",
                            new Page.WaitForFunctionOptions().setTimeout(timeout));
                    break;
                case PAGE_LOAD:
                    page.waitForLoadState(com.microsoft.playwright.options.LoadState.LOAD,
                            new Page.WaitForLoadStateOptions().setTimeout(timeout));
                    break;
                default:
                    throw new IllegalArgumentException("Desteklenmeyen bekleme koşulu: " + condition);
            }

            logger.debug("Koşul başarıyla beklendi");
        } catch (Exception e) {
            logger.error("Koşul beklenirken hata oluştu: {}", e.getMessage());
            throw e;
        }
    }
}
