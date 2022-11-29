package ru.yandex.practicum.filmorate.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

public class ValidationException extends RuntimeException {
    public ValidationException(String message) {
        super(message);
    }
}
