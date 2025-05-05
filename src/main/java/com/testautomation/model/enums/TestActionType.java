package com.testautomation.model.enums;

import lombok.Getter;

@Getter
public enum TestActionType {
    NAVIGATE("navigate", "Navigate to a URL"),
    CLICK("click", "Click on an element"),
    TYPE("type", "Type text into an element"),
    WAIT("wait", "Wait for a specified time"),
    WAIT_FOR_NAVIGATION("waitfornavigation", "Wait for navigation to complete"),
    WAIT_FOR_ELEMENT("waitforelement", "Wait for an element to be visible"),
    PRESS_ENTER("pressenter", "Press the Enter key"),
    SELECT("select", "Select an option from a dropdown"),
    VERIFY_URL("verifyurl", "Verify the current URL"),
    VERIFY_TEXT("verifytext", "Verify text content"),
    TAKE_SCREENSHOT("screenshot", "Take a screenshot"),
    FILL("fill", "Fill a form field"),
    CHECK("check", "Check a checkbox"),
    UNCHECK("uncheck", "Uncheck a checkbox"),
    WAIT_FOR_SELECTOR("waitforselector", "Wait for an element to be visible"),
    EXPECT("expect", "Assert that an element contains specific text"),
    HOVER("hover", "Hover over an element"),
    PRESS("press", "Press a key on the keyboard"),
    DOUBLE_CLICK("doubleclick", "Double-click on an element"),
    RIGHT_CLICK("rightclick", "Right-click on an element"),
    DRAG_AND_DROP("draganddrop", "Drag and drop an element"),
    FOCUS("focus", "Focus on an element"),
    SCROLL_INTO_VIEW("scrollintoview", "Scroll an element into view"),
    EVALUATE("evaluate", "Evaluate JavaScript in the browser context");
    
    private final String value;
    private final String description;
    
    TestActionType(String value, String description) {
        this.value = value;
        this.description = description;
    }

    public static TestActionType fromString(String text) {
        for (TestActionType action : TestActionType.values()) {
            if (action.value.equalsIgnoreCase(text)) {
                return action;
            }
        }
        throw new IllegalArgumentException("No constant with value " + text + " found");
    }
}
