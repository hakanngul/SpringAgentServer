package com.testautomation.model.enums;

/**
 * Enum representing the types of browsers supported by Playwright
 */
public enum BrowserType {
    CHROMIUM("chromium"),
    FIREFOX("firefox"),
    WEBKIT("webkit");
    
    private final String value;
    
    BrowserType(String value) {
        this.value = value;
    }
    
    public String getValue() {
        return value;
    }
    
    public static BrowserType fromString(String text) {
        for (BrowserType browserType : BrowserType.values()) {
            if (browserType.value.equalsIgnoreCase(text)) {
                return browserType;
            }
        }
        throw new IllegalArgumentException("No browser type with value " + text + " found");
    }
}
