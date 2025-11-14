package com.example.qaassistant.configuration;

import com.example.qaassistant.controller.transfer.ErrorResponse;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.concurrent.TimeoutException;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = new ErrorResponse(
                "INTERNAL_ERROR",
                "Error interno del servidor",
                "Intenta nuevamente en unos momentos"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ErrorResponse> handleDataAccessException(DataAccessException ex) {
        ErrorResponse error = new ErrorResponse(
                "DATABASE_ERROR",
                "Error al acceder a la base de datos",
                "Verifica que la consulta sea válida"
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    @ExceptionHandler(TimeoutException.class)
    public ResponseEntity<ErrorResponse> handleTimeoutException(TimeoutException ex) {
        ErrorResponse error = new ErrorResponse(
                "TIMEOUT_ERROR",
                "La consulta está tardando demasiado tiempo",
                "Intenta con una consulta más específica o divide la consulta en partes más pequeñas"
        );
        return ResponseEntity.status(HttpStatus.REQUEST_TIMEOUT).body(error);
    }

}
