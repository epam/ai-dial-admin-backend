package com.epam.aidial.cfg.web.handler;

import com.epam.aidial.cfg.exception.EntityAlreadyExistsException;
import com.epam.aidial.cfg.exception.EntityNotFoundException;
import com.epam.aidial.cfg.exception.FolderAlreadyExistsException;
import com.epam.aidial.cfg.exception.FolderNotFoundException;
import com.epam.aidial.cfg.exception.NotModifiedException;
import com.epam.aidial.cfg.exception.OptimisticLockConflictException;
import com.epam.aidial.cfg.exception.ResourceNotFoundException;
import com.epam.aidial.cfg.exception.ResourcePreconditionFailedException;
import com.epam.aidial.cfg.exception.VersionMismatchException;
import feign.FeignException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestValueException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.servlet.resource.NoResourceFoundException;

/**
 * Created by Aliaksei Kurnosau on 9/9/24.
 */
@ControllerAdvice
@Slf4j
public class DefaultExceptionHandler {

    @ResponseBody
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler(VersionMismatchException.class)
    public ErrorView handleMismatchError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.PRECONDITION_FAILED, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(EntityAlreadyExistsException.class)
    public ErrorView handleEntityAlreadyExistsError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler({EntityNotFoundException.class, NoResourceFoundException.class,
            ResourceNotFoundException.class})
    public ErrorView handleEntityNotFoundError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.METHOD_NOT_ALLOWED)
    @ExceptionHandler({HttpRequestMethodNotSupportedException.class})
    public ErrorView handleMethodNotAllowedError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.METHOD_NOT_ALLOWED, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.UNAUTHORIZED)
    @ExceptionHandler(AccessDeniedException.class)
    public ErrorView handleAuthorizationException(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.UNAUTHORIZED, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ErrorView handleWrongJsonError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MissingRequestValueException.class)
    public ErrorView handleMissingRequestValueError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(ConstraintViolationException.class)
    public ErrorView handleConstraintViolationError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(Exception.class)
    public ErrorView handleGeneralError(HttpServletRequest req, Exception ex) {
        log.error("[{}] Request: {} raised ", req.getMethod(), req.getServletPath(), ex);

        return new ErrorView(req, HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ErrorView handleValidationExceptions(
            HttpServletRequest req,
            MethodArgumentNotValidException ex) {
        StringBuffer message = new StringBuffer();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            message
                    .append("Field [")
                    .append(((FieldError) error).getField())
                    .append("]: ")
                    .append(error.getDefaultMessage())
                    .append("\n");
        });
        return new ErrorView(req, HttpStatus.BAD_REQUEST, message.toString());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(IllegalArgumentException.class)
    public ErrorView handleIllegalArgumentError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    @ResponseBody
    @ExceptionHandler(FeignException.FeignClientException.class)
    public ErrorView handleFeignClientError(FeignException.FeignClientException clientException,
                                            HttpServletRequest req) {
        final HttpStatus httpStatus = HttpStatus.resolve(clientException.status());
        String message = clientException.contentUTF8();
        return new ErrorView(req, httpStatus == null ? HttpStatus.INTERNAL_SERVER_ERROR : httpStatus, message);
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.CONFLICT)
    @ExceptionHandler(FolderAlreadyExistsException.class)
    public ErrorView handleFolderAlreadyExistsError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.CONFLICT, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(FolderNotFoundException.class)
    public ErrorView handleFolderNotFoundError(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.NOT_FOUND, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.PRECONDITION_FAILED)
    @ExceptionHandler({OptimisticLockConflictException.class, ResourcePreconditionFailedException.class})
    public ErrorView handlePreconditionFailedException(HttpServletRequest req, Exception ex) {
        return new ErrorView(req, HttpStatus.PRECONDITION_FAILED, ex.getMessage());
    }

    @ResponseBody
    @ResponseStatus(HttpStatus.NOT_MODIFIED)
    @ExceptionHandler(NotModifiedException.class)
    public ResponseEntity<Void> handleNotModifiedException(NotModifiedException ex,
                                                           HttpServletRequest req) {
        return ResponseEntity.status(HttpStatus.NOT_MODIFIED).eTag(ex.getEtag()).build();
    }

}
