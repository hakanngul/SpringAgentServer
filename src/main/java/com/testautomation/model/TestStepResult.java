package com.testautomation.model;

import lombok.Data;

@Data
public class TestStepResult {
    private int index;
    private String action;
    private String description;
    private boolean success;
    private String error;
    private String screenshot;
    private long duration;
    private String startTime;
    private String endTime;
}
