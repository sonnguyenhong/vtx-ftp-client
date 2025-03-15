package com.example.constants;

public enum FTPResponseCode {
    COMMAND_SUCCESS("125"),
    OPENING_DATA_CONNECTION("150"),
    PORT_COMMAND_SUCCESS("200"),
    DIRECTORY_SEND_OK("226"),
    ENTERING_PASSIVE_MODE("227"),
    LOGGED_IN("230"),
    FILE_ACTION_OK("250"),
    PATHNAME_CREATED("257"),
    REQUIRES_PASSWORD("331"),
    NOT_LOGGED_IN("530"),
    FILE_UNAVAILABLE("550");
    
    private String code;

    private FTPResponseCode(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
