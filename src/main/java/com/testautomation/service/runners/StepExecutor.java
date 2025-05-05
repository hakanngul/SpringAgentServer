package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import com.testautomation.model.TestStep;
import com.testautomation.model.enums.TestActionType;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class StepExecutor {

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

    public static void executeStep(
        Page page,
        TestStep step,
        int stepIndex,
        int totalSteps,
        Map<String, Object> variables,
        Map<String, Object> dataSet
    ) throws Exception {
        String action = step.getAction();
        String target = replaceVariables(step.getTarget(), variables, dataSet);
        String strategy = step.getStrategy();
        String value = replaceVariables(step.getValue(), variables, dataSet);

        TestActionType actionType = getActionType(action);

        switch (actionType) {
            case NAVIGATE:
                page.navigate(target);
                break;
            case CLICK:
                ElementUtils.findAndClick(page, target, strategy);
                break;
            case TYPE:
                ElementUtils.findAndType(page, target, strategy, value);
                break;
            case WAIT:
                int waitTime = value != null ? Integer.parseInt(value) : 1000;
                page.waitForTimeout(waitTime);
                break;
            case WAIT_FOR_NAVIGATION:
                page.waitForNavigation(() -> {});
                break;
            case WAIT_FOR_ELEMENT:
            case WAIT_FOR_SELECTOR:
                int timeout = value != null ? Integer.parseInt(value) : 30000;
                ElementUtils.waitForElement(page, target, strategy, timeout);
                break;
            case PRESS_ENTER:
                page.keyboard().press("Enter");
                break;
            case PRESS:
                page.keyboard().press(value);
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
                // Screenshot is handled by TestStepExecutor
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
                throw new IllegalArgumentException("Unsupported action type: " + action);
        }
    }
}
