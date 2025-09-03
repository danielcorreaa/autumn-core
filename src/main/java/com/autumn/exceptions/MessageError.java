package com.autumn.exceptions;

public class MessageError {
    private final String uri;
    private final int status;
    private final String message;
    private final String causeBy;

    public MessageError(String uri, int status, String message, String causeBy) {
        this.uri = uri;
        this.status = status;
        this.message = message;
        this.causeBy = causeBy;
    }

    public String getUri() {
        return uri;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    public String getCauseBy() {
        return causeBy;
    }
}
