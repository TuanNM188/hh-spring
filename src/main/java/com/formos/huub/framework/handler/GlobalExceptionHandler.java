/**
 * ***************************************************
 * * Description :
 * * File        : GlobalExceptionHandler
 * * Author      : Hung Tran
 * * Date        : Dec 21, 2022
 * ***************************************************
 **/
package com.formos.huub.framework.handler;

import static java.util.stream.Collectors.toList;

import com.formos.huub.framework.exception.*;
import com.formos.huub.framework.handler.model.FieldError;
import com.formos.huub.framework.handler.model.ResponseData;
import com.formos.huub.framework.message.MessageHelper;
import com.formos.huub.framework.message.model.Message;
import com.formos.huub.framework.service.exception.AmazonClientException;
import com.formos.huub.framework.service.exception.AmazonServiceException;
import com.formos.huub.framework.support.ResponseSupport;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import java.io.IOException;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletionException;
import java.util.stream.StreamSupport;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;
import org.springframework.validation.BindException;
import org.springframework.validation.BindingResult;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.ServletRequestBindingException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
@Component
@Slf4j
public class GlobalExceptionHandler {

    private Logger logErr = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ResponseSupport responseSupport;

    public GlobalExceptionHandler(ResponseSupport responseSupport) {
        this.responseSupport = responseSupport;
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ResponseData> handleAuthenticationException(AuthenticationException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.UNAUTHORIZED, message);
    }

    @ExceptionHandler(InsufficientAuthenticationException.class)
    public ResponseEntity<ResponseData> handleInsufficientAuthenticationException(InsufficientAuthenticationException exception) {
        logErr.error(exception.getMessage());
        return responseSupport.failed(HttpStatus.UNAUTHORIZED, MessageHelper.getMessage(Message.Keys.E0003));
    }

    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ResponseData> handleRuntimeException(RuntimeException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, MessageHelper.getMessage(exception.getMessage()));
    }

    @ExceptionHandler(IOException.class)
    public ResponseEntity<ResponseData> handleIOException(IOException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, MessageHelper.getMessage(exception.getMessage()));
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ResponseData> handleHttpRequestMethodNotSupportedException(HttpRequestMethodNotSupportedException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setId(Message.Keys.I0002);
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.METHOD_NOT_ALLOWED, message);
    }

    @ExceptionHandler(ServletRequestBindingException.class)
    public ResponseEntity<ResponseData> handleServletRequestBindingException(ServletRequestBindingException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setId(Message.Keys.I0002);
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ResponseData> handleAccessDeniedException(AccessDeniedException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.FORBIDDEN, MessageHelper.getMessage(Message.Keys.E0006));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ResponseData> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    public ResponseEntity<ResponseData> handleHttpMediaTypeNotSupportedException(HttpMediaTypeNotSupportedException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ResponseData> handleException(Exception exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, MessageHelper.getMessage(exception.getMessage()));
    }

    @ExceptionHandler(BindException.class)
    public ResponseEntity<ResponseData> handleBindException(BindException ex) {
        logErr.error(ex.getMessage(), ex);
        BindingResult result = ex.getBindingResult();
        List<FieldError> filedErrs = result
            .getFieldErrors()
            .stream()
            .map(
                f ->
                    new FieldError(
                        f.getField(),
                        StringUtils.isNotBlank(f.getDefaultMessage())
                            ? MessageHelper.getMessage(f.getDefaultMessage(), f.getField())
                            : f.getCode()
                    )
            )
            .collect(toList());

        if (filedErrs.isEmpty()) {
            var msg = result.getAllErrors().get(0).getDefaultMessage();
            Message message = new Message();
            message.setContent(msg);
            return responseSupport.failed(HttpStatus.BAD_REQUEST, message);
        }

        return responseSupport.failed(
            HttpStatus.BAD_REQUEST,
            MessageHelper.getMessage(Message.Keys.E0008, filedErrs.get(0).getField()),
            filedErrs
        );
    }

    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ResponseData> handleBadRequestException(BadRequestException exception) {
        logErr.error(exception.getMsg().getContent(), exception);
        return responseSupport.failed(HttpStatus.BAD_REQUEST, exception.getMsg());
    }

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ResponseData> handleNotFoundException(NotFoundException exception) {
        logErr.error(exception.getMsg().getContent(), exception);
        return responseSupport.failed(HttpStatus.NOT_FOUND, exception.getMsg());
    }

    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ResponseData> handleBadCredentialsException(BadCredentialsException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.UNAUTHORIZED, MessageHelper.getMessage(Message.Keys.E0009));
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ResponseData> handleConstraintViolationException(ConstraintViolationException exception) {
        logErr.error(exception.getMessage(), exception);
        Message msg = new Message();
        msg.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.BAD_REQUEST, msg, buildValidationErrors(exception.getConstraintViolations()));
    }

    @ExceptionHandler(MultipartException.class)
    public ResponseEntity<ResponseData> handleMultipartException(MultipartException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.BAD_REQUEST, MessageHelper.getMessage(Message.Keys.E0029));
    }

    @ExceptionHandler(AmazonServiceException.class)
    public ResponseEntity<ResponseData> handleAmazonServiceException(AmazonServiceException exception) {
        logErr.error(exception.getMessage(), exception);
        var msg = MessageHelper.getMessageWithCode("com.formos.huub.exception.AmazonServiceException");
        Message message = new Message();
        message.setContent(msg);
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(AmazonClientException.class)
    public ResponseEntity<ResponseData> handleAmazonClientException(AmazonClientException exception) {
        logErr.error(exception.getMessage(), exception);
        var msg = MessageHelper.getMessageWithCode("com.formos.huub.exception.AmazonClientException");
        Message message = new Message();
        message.setContent(msg);
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(CompletionException.class)
    public ResponseEntity<ResponseData> handleCompletionException(CompletionException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.BAD_REQUEST, message);
    }

    @ExceptionHandler(TokenRefreshException.class)
    public ResponseEntity<ResponseData> handleTokenRefreshException(TokenRefreshException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.UNAUTHORIZED, exception.getMsg());
    }

    @ExceptionHandler(TokenException.class)
    public ResponseEntity<ResponseData> handleTokenException(TokenException exception) {
        logErr.error(exception.getMessage(), exception);
        return responseSupport.failed(HttpStatus.UNAUTHORIZED, MessageHelper.getMessage(exception.getMessage()));
    }

    @ExceptionHandler(ActiveCampaignException.class)
    public ResponseEntity<ResponseData> handleActiveCampaignException(ActiveCampaignException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setId(Message.Keys.E0001);
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(SocialConnectException.class)
    public ResponseEntity<ResponseData> handleSocialConnectException(SocialConnectException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setId(Message.Keys.E0001);
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    @ExceptionHandler(SurveyProcessingException.class)
    public ResponseEntity<ResponseData> handleSurveyProcessingException(SurveyProcessingException exception) {
        logErr.error(exception.getMessage(), exception);
        Message message = new Message();
        message.setId(Message.Keys.E0001);
        message.setContent(exception.getMessage());
        return responseSupport.failed(HttpStatus.INTERNAL_SERVER_ERROR, message);
    }

    /**
     * Build list of FieldError from set of ConstraintViolation
     *
     * @param violations Set<ConstraintViolation<?>> - Set
     * of parameterized ConstraintViolations
     * @return List<FieldError> - list of validation errors
     */
    private List<FieldError> buildValidationErrors(Set<ConstraintViolation<?>> violations) {
        return violations
            .stream()
            .map(
                violation ->
                    FieldError.builder()
                        .field(
                            StreamSupport.stream(violation.getPropertyPath().spliterator(), false)
                                .reduce((first, second) -> second)
                                .orElse(null)
                                .toString()
                        )
                        .message(violation.getMessage())
                        .build()
            )
            .collect(toList());
    }
}
