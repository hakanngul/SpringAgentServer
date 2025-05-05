package com.testautomation.model;

import com.testautomation.model.enums.BrowserType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Represents browser configuration options for a test
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BrowserOptions {
    
    /**
     * Browser type: chromium, firefox, webkit
     */
    @Builder.Default
    private String browserType = "chromium";
    
    /**
     * Whether to run the browser in headless mode
     */
    @Builder.Default
    private boolean headless = false;
    
    /**
     * Browser viewport width
     */
    @Builder.Default
    private int viewportWidth = 1280;
    
    /**
     * Browser viewport height
     */
    @Builder.Default
    private int viewportHeight = 720;
    
    /**
     * Custom user agent string
     */
    private String userAgent;
    
    /**
     * Default timeout in milliseconds
     */
    @Builder.Default
    private int timeout = 30000;
    
    /**
     * Whether to ignore HTTPS errors
     */
    @Builder.Default
    private boolean ignoreHttpsErrors = false;
    
    /**
     * Whether to record video of the test
     */
    @Builder.Default
    private boolean recordVideo = false;
    
    /**
     * Path to save videos (if recording is enabled)
     */
    private String videoPath;
}
