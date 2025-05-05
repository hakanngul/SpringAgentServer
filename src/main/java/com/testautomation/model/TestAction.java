package com.testautomation.model;

/**
 * Enum representing the types of actions that can be performed in a test step
 */
public enum TestAction {
    NAVIGATE("navigate", "Navigate to a URL"),
    CLICK("click", "Click on an element"),
    FILL("fill", "Fill a form field"),
    TYPE("type", "Type text into an element"),
    PRESS("press", "Press a key on the keyboard"),
    CHECK("check", "Check a checkbox"),
    UNCHECK("uncheck", "Uncheck a checkbox"),
    SELECT("select", "Select an option from a dropdown"),
    WAIT("wait", "Wait for a specified time"),
    WAIT_FOR_SELECTOR("waitforselector", "Wait for an element to be visible"),
    WAIT_FOR_NAVIGATION("waitfornavigation", "Wait for navigation to complete"),
    EXPECT("expect", "Assert that an element contains specific text"),
    VERIFY_TEXT("verifytext", "Verify text content"),
    VERIFY_URL("verifyurl", "Verify the current URL"),
    SCREENSHOT("screenshot", "Take a screenshot"),
    HOVER("hover", "Hover over an element"),
    DOUBLE_CLICK("doubleclick", "Double-click on an element"),
    RIGHT_CLICK("rightclick", "Right-click on an element"),
    DRAG_AND_DROP("draganddrop", "Drag and drop an element"),
    FOCUS("focus", "Focus on an element"),
    SCROLL_INTO_VIEW("scrollintoview", "Scroll an element into view"),
    EVALUATE("evaluate", "Evaluate JavaScript in the browser context"),
    SET_VARIABLE("setvariable", "Set a variable in the test context"),
    GET_TEXT("gettext", "Get text from an element and store it in a variable"),
    GET_ATTRIBUTE("getattribute", "Get an attribute from an element and store it in a variable");
    
    private final String value;
    private final String description;
    
    TestAction(String value, String description) {
        this.value = value;
        this.description = description;
    }
    
    public String getValue() {
        return value;
    }
    
    public String getDescription() {
        return description;
    }
    
    public static TestAction fromString(String text) {
        for (TestAction action : TestAction.values()) {
            if (action.value.equalsIgnoreCase(text)) {
                return action;
            }
        }
        throw new IllegalArgumentException("No action with value " + text + " found");
    }
}
