package com.testautomation.service.runners;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import com.testautomation.model.enums.SelectorStrategy;
import org.springframework.stereotype.Component;

@Component
public class ElementUtils {
    
    public static Locator findElement(Page page, String target, String strategy) {
        SelectorStrategy selectorStrategy;
        try {
            selectorStrategy = SelectorStrategy.valueOf(strategy.toUpperCase());
        } catch (IllegalArgumentException e) {
            selectorStrategy = SelectorStrategy.CSS;
        }
        
        String selector;
        switch (selectorStrategy) {
            case ID:
                selector = "#" + target;
                break;
            case CLASS:
                selector = "." + target;
                break;
            case NAME:
                selector = "[name='" + target + "']";
                break;
            case XPATH:
                selector = target; // Playwright uses xpath=//div for xpath
                return page.locator("xpath=" + target);
            case CSS:
            default:
                selector = target;
                break;
        }
        
        return page.locator(selector);
    }
    
    public static void findAndClick(Page page, String target, String strategy) {
        findElement(page, target, strategy).click();
    }
    
    public static void findAndType(Page page, String target, String strategy, String value) {
        findElement(page, target, strategy).fill(value);
    }
    
    public static void findAndSelect(Page page, String target, String strategy, String value) {
        findElement(page, target, strategy).selectOption(value);
    }
    
    public static void waitForElement(Page page, String target, String strategy, int timeout) {
        findElement(page, target, strategy).waitFor(new Locator.WaitForOptions().setTimeout(timeout));
    }
    
    public static void verifyText(Page page, String target, String strategy, String expectedText) {
        String actualText = findElement(page, target, strategy).textContent();
        if (!actualText.contains(expectedText)) {
            throw new AssertionError("Expected text '" + expectedText + "' not found in '" + actualText + "'");
        }
    }
    
    public static void verifyUrl(Page page, String expectedUrl) {
        String currentUrl = page.url();
        if (!currentUrl.contains(expectedUrl)) {
            throw new AssertionError("Expected URL to contain '" + expectedUrl + "', but got '" + currentUrl + "'");
        }
    }
    
    public static void hover(Page page, String target, String strategy) {
        findElement(page, target, strategy).hover();
    }
    
    public static void doubleClick(Page page, String target, String strategy) {
        findElement(page, target, strategy).dblclick();
    }
    
    public static void rightClick(Page page, String target, String strategy) {
        findElement(page, target, strategy).click(new Locator.ClickOptions().setButton(com.microsoft.playwright.options.MouseButton.RIGHT));
    }
    
    public static void check(Page page, String target, String strategy) {
        findElement(page, target, strategy).check();
    }
    
    public static void uncheck(Page page, String target, String strategy) {
        findElement(page, target, strategy).uncheck();
    }
    
    public static void focus(Page page, String target, String strategy) {
        findElement(page, target, strategy).focus();
    }
    
    public static void scrollIntoView(Page page, String target, String strategy) {
        findElement(page, target, strategy).scrollIntoViewIfNeeded();
    }
    
    public static Object evaluate(Page page, String script) {
        return page.evaluate(script);
    }
}
