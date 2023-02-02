package org.example.app.exceptions;

import java.io.FileNotFoundException;

public class BookShelfDownloadException extends FileNotFoundException {
    private final String message;

    public BookShelfDownloadException(String message) {
        this.message = message;
    }

    @Override
    public String getMessage() {
        return message;
    }
}
