package sk.mlobb.authserver.rest.advice;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import sk.mlobb.authserver.model.error.ErrorResponse;
import sk.mlobb.authserver.model.exception.ConflictException;
import sk.mlobb.authserver.model.exception.InvalidRequestException;
import sk.mlobb.authserver.model.exception.NoContentException;
import sk.mlobb.authserver.model.exception.NotFoundException;
import sk.mlobb.authserver.model.exception.UnauthorizedException;

import javax.servlet.http.HttpServletRequest;

@Slf4j
@RestControllerAdvice
public class ErrorMapper {

    private static final String ERROR_MESSAGE = "Error message: {}";

    @ExceptionHandler(UnauthorizedException.class)
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    public ErrorResponse handleUnauthorizedException(HttpServletRequest request, UnauthorizedException exception) {
        log.info(ERROR_MESSAGE, exception.getMessage());
        return ErrorResponse.builder().info(exception.getMessage()).build();
    }

    @ExceptionHandler(InvalidRequestException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ErrorResponse handleInvalidRequestException(HttpServletRequest request, InvalidRequestException exception) {
        log.info(ERROR_MESSAGE, exception.getMessage());
        return ErrorResponse.builder().info(exception.getMessage()).build();
    }

    @ExceptionHandler(NotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public ErrorResponse handleNotFoundException(NotFoundException exception) {
        log.info(ERROR_MESSAGE, exception.getMessage());
        return ErrorResponse.builder().info(exception.getMessage()).build();
    }

    @ExceptionHandler(ConflictException.class)
    @ResponseStatus(HttpStatus.CONFLICT)
    public ErrorResponse handleDuplicateException(HttpServletRequest request, ConflictException exception) {
        log.info(ERROR_MESSAGE, exception.getMessage());
        return ErrorResponse.builder().info(exception.getMessage()).build();
    }

    @ExceptionHandler(NoContentException.class)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ErrorResponse handleDuplicateException(HttpServletRequest request, NoContentException exception) {
        log.info(ERROR_MESSAGE, exception.getMessage());
        return ErrorResponse.builder().info(exception.getMessage()).build();
    }
}