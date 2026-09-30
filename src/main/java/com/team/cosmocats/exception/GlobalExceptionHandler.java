package com.team.cosmocats.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    @ExceptionHandler(ProductNotFoundException.class)
    public ProblemDetail handleProductNotFound(ProductNotFoundException e) {
        return createProblem(
                HttpStatus.NOT_FOUND,
                "Product Not Found",
                e.getMessage());
    }
    
    @ExceptionHandler(CategoryNotFoundException.class)
    public ProblemDetail handleCategoryNotFound(CategoryNotFoundException e) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid category",
                e.getMessage()
        );
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ProblemDetail handleInvalidRequestBody(MethodArgumentNotValidException e) {
        var detail = e.getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));
        
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                detail
        );
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    public ProblemDetail handleInvalidMethodArguments(
            HandlerMethodValidationException exception
    ) {
        var detail = exception.getAllErrors()
                .stream()
                .map(error -> Objects.requireNonNullElse(
                        error.getDefaultMessage(),
                        "Invalid value"
                ))
                .collect(Collectors.joining("; "));

        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                detail
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ProblemDetail handleUnreadableRequestBody(
            HttpMessageNotReadableException exception
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid request body",
                "Request body is missing or contains malformed JSON"
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ProblemDetail handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        return createProblem(
                HttpStatus.BAD_REQUEST,
                "Invalid parameter",
                "Parameter '%s' has an invalid value"
                        .formatted(exception.getName())
        );
    }
    
    private ProblemDetail createProblem(HttpStatus status, String title, String detail) {
        var problem = ProblemDetail.forStatusAndDetail(status, detail);
        problem.setTitle(title);
        return problem;
    }
}
