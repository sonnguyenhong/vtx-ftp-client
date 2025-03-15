package com.example.constants;

public enum FTPConnectionMode {
    ACTIVE("ACTIVE"),
    PASSIVE("PASSIVE");

    private String mode;

    private FTPConnectionMode(String mode) {
        this.mode = mode;
    }

    public String getMode() {
        return this.mode;
    }
}
