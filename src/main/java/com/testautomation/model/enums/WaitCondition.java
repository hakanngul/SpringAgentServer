package com.testautomation.model.enums;

/**
 * Bekleme koşulları için enum
 */
public enum WaitCondition {
    VISIBLE("visible", "Element görünür olana kadar bekle"),
    HIDDEN("hidden", "Element gizlenene kadar bekle"),
    ENABLED("enabled", "Element etkinleşene kadar bekle"),
    DISABLED("disabled", "Element devre dışı kalana kadar bekle"),
    PRESENT("present", "Element DOM'da var olana kadar bekle"),
    URL_CONTAINS("urlcontains", "URL belirli bir metni içerene kadar bekle"),
    URL_EQUALS("urlequals", "URL belirli bir değere eşit olana kadar bekle"),
    TEXT_CONTAINS("textcontains", "Element belirli bir metni içerene kadar bekle"),
    PAGE_LOAD("pageload", "Sayfa yüklenene kadar bekle");

    private final String value;
    private final String description;

    WaitCondition(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public String getValue() {
        return value;
    }

    public String getDescription() {
        return description;
    }

    public static WaitCondition fromString(String value) {
        if (value == null) {
            return null;
        }

        for (WaitCondition condition : WaitCondition.values()) {
            if (condition.value.equalsIgnoreCase(value)) {
                return condition;
            }
        }

        throw new IllegalArgumentException("Bilinmeyen bekleme koşulu: " + value);
    }
}
