package com.testautomation.service.runners;

import com.microsoft.playwright.Page;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Component
public class ScreenshotUtils {
    
    public static String takeScreenshot(
        Page page,
        String testName,
        int stepIndex,
        String description,
        String screenshotsDir
    ) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(screenshotsDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Create a unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");
            String sanitizedDescription = description != null ? 
                description.replaceAll("[^a-zA-Z0-9]", "_") : "step";
            
            String filename = String.format("%s/%s_%s_%d_%s.png", 
                screenshotsDir, 
                sanitizedTestName, 
                timestamp, 
                stepIndex, 
                sanitizedDescription);
            
            // Take screenshot
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)).setFullPage(true));
            
            return filename;
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
    
    public static String takeFullPageScreenshot(
        Page page,
        String testName,
        String description,
        String screenshotsDir
    ) {
        try {
            // Create directory if it doesn't exist
            File directory = new File(screenshotsDir);
            if (!directory.exists()) {
                directory.mkdirs();
            }
            
            // Create a unique filename
            String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String sanitizedTestName = testName.replaceAll("[^a-zA-Z0-9]", "_");
            String sanitizedDescription = description != null ? 
                description.replaceAll("[^a-zA-Z0-9]", "_") : "fullpage";
            
            String filename = String.format("%s/%s_%s_%s.png", 
                screenshotsDir, 
                sanitizedTestName, 
                timestamp, 
                sanitizedDescription);
            
            // Take screenshot
            page.screenshot(new Page.ScreenshotOptions().setPath(Paths.get(filename)).setFullPage(true));
            
            return filename;
        } catch (Exception e) {
            System.err.println("Failed to take screenshot: " + e.getMessage());
            return null;
        }
    }
}
