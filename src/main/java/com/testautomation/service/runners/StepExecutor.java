package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.TestStep;
import com.testautomation.model.TestStepOptions;
import com.testautomation.model.enums.TestActionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StepExecutor {
    private static final Logger logger = LoggerFactory.getLogger(StepExecutor.class);
    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\$\\{([^}]+)\\}");

    public static String replaceVariables(String value, Map<String, Object> variables, Map<String, Object> dataSet) {
        if (value == null) {
            return null;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(value);
        StringBuffer sb = new StringBuffer();

        while (matcher.find()) {
            String variableName = matcher.group(1);
            Object replacement = null;

            // First check in variables
            if (variables != null && variables.containsKey(variableName)) {
                replacement = variables.get(variableName);
            }
            // Then check in dataSet
            else if (dataSet != null && dataSet.containsKey(variableName)) {
                replacement = dataSet.get(variableName);
            }

            if (replacement != null) {
                matcher.appendReplacement(sb, replacement.toString().replace("$", "\\$"));
            } else {
                matcher.appendReplacement(sb, matcher.group(0));
            }
        }

        matcher.appendTail(sb);
        return sb.toString();
    }

    private static TestActionType getActionType(String action) {
        try {
            return TestActionType.fromString(action.toLowerCase());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Unsupported action type: " + action);
        }
    }

    /**
     * Test adımını çalıştır
     * @param page Playwright sayfası
     * @param step Test adımı
     * @param variables Değişkenler
     * @param dataSet Veri seti
     */
    public static void executeStep(
        Page page,
        TestStep step,
        Map<String, Object> variables,
        Map<String, Object> dataSet
    )
    {
        String action = step.getAction();
        String target = replaceVariables(step.getTarget(), variables, dataSet);
        String strategy = step.getStrategy();
        String value = replaceVariables(step.getValue(), variables, dataSet);

        TestActionType actionType = getActionType(action);
        TestStepOptions options = step.getStepOptions();

        logger.info("Adım çalıştırılıyor: {} - Hedef: {}, Strateji: {}, Değer: {}",
                action, target, strategy, value);

        try {
            switch (actionType) {
                case NAVIGATE:
                    logger.debug("URL'ye gidiliyor: {}", target);
                    page.navigate(target);
                    logger.debug("URL'ye başarıyla gidildi");
                    break;
                case CLICK:
                    if (options != null && options.isForce()) {
                        logger.debug("Elemente zorla tıklanıyor: {} (strateji: {})", target, strategy);
                        ElementUtils.findElement(page, target, strategy).click(
                            new com.microsoft.playwright.Locator.ClickOptions().setForce(true)
                        );
                    } else {
                        ElementUtils.findAndClick(page, target, strategy);
                    }
                    break;
                case TYPE:
                    ElementUtils.findAndType(page, target, strategy, value);
                    break;
                case WAIT:
                    int waitTime = value != null ? Integer.parseInt(value) : 1000;
                    logger.debug("Bekleniyor: {} ms", waitTime);
                    page.waitForTimeout(waitTime);
                    logger.debug("Bekleme tamamlandı");
                    break;
                case WAIT_FOR_NAVIGATION:
                    logger.debug("Navigasyon için bekleniyor");
                    // Boş bir Runnable ile çağıralım
                    page.waitForNavigation(() -> {});
                    logger.debug("Navigasyon tamamlandı");
                    break;
                case WAIT_FOR_ELEMENT:
                case WAIT_FOR_SELECTOR:
                    int timeout = value != null ? Integer.parseInt(value) : 30000;
                    ElementUtils.waitForElement(page, target, strategy, timeout);
                    break;
                case PRESS_ENTER:
                    logger.debug("Enter tuşuna basılıyor");
                    page.keyboard().press("Enter");
                    logger.debug("Enter tuşuna basıldı");
                    break;
                case PRESS:
                    logger.debug("Tuşa basılıyor: {}", value);
                    page.keyboard().press(value);
                    logger.debug("Tuşa basıldı");
                    break;
                case SELECT:
                    ElementUtils.findAndSelect(page, target, strategy, value);
                    break;
                case VERIFY_URL:
                    ElementUtils.verifyUrl(page, target);
                    break;
                case VERIFY_TEXT:
                    ElementUtils.verifyText(page, target, strategy, value);
                    break;
                case TAKE_SCREENSHOT:
                case SCREENSHOT:
                    // Screenshot is handled by TestStepExecutor
                    logger.debug("Ekran görüntüsü alınıyor (TestStepExecutor tarafından işlenecek)");
                    break;
                case SET_VARIABLE:
                    if (target != null && value != null) {
                        logger.debug("Değişken ayarlanıyor: {} = {}", target, value);
                        variables.put(target, value);
                        logger.debug("Değişken başarıyla ayarlandı");
                    }
                    break;
                case GET_TEXT:
                    if (target != null && value != null) {
                        logger.debug("Metin alınıyor: {} (strateji: {})", target, strategy);
                        String text = ElementUtils.findElement(page, target, strategy).textContent();
                        variables.put(value, text);
                        logger.debug("Metin başarıyla alındı: {}", text);
                    }
                    break;
                case GET_ATTRIBUTE:
                    if (target != null && value != null && step.getAdditionalProperties().containsKey("attribute")) {
                        String attributeName = (String) step.getAdditionalProperties().get("attribute");
                        logger.debug("Özellik alınıyor: {} (strateji: {}), özellik: {}", target, strategy, attributeName);
                        String attributeValue = ElementUtils.findElement(page, target, strategy).getAttribute(attributeName);
                        variables.put(value, attributeValue);
                        logger.debug("Özellik başarıyla alındı: {}", attributeValue);
                    }
                    break;
                case FILL:
                    ElementUtils.findAndType(page, target, strategy, value);
                    break;
                case CHECK:
                    ElementUtils.check(page, target, strategy);
                    break;
                case UNCHECK:
                    ElementUtils.uncheck(page, target, strategy);
                    break;
                case EXPECT:
                    ElementUtils.verifyText(page, target, strategy, value);
                    break;
                case HOVER:
                    ElementUtils.hover(page, target, strategy);
                    break;
                case DOUBLE_CLICK:
                    ElementUtils.doubleClick(page, target, strategy);
                    break;
                case RIGHT_CLICK:
                    ElementUtils.rightClick(page, target, strategy);
                    break;
                case FOCUS:
                    ElementUtils.focus(page, target, strategy);
                    break;
                case SCROLL_INTO_VIEW:
                    ElementUtils.scrollIntoView(page, target, strategy);
                    break;
                case EVALUATE:
                    ElementUtils.evaluate(page, value);
                    break;
                default:
                    String errorMsg = "Desteklenmeyen aksiyon türü: " + action;
                    logger.error(errorMsg);
                    throw new IllegalArgumentException(errorMsg);
            }
            logger.info("Adım başarıyla tamamlandı: {}", action);
        } catch (Exception e) {
            logger.error("Adım çalıştırılırken hata oluştu: {} - {}", action, e.getMessage());
            throw e;
        }
    }
}
