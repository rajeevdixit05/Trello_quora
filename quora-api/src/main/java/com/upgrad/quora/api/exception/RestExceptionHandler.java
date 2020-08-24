package com.upgrad.quora.api.exception;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.service.common.GenericErrorCode;
import com.upgrad.quora.service.common.UnexpectedException;
import com.upgrad.quora.service.exception.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.Set;

@ControllerAdvice
public class RestExceptionHandler {
    /**
     * Global Exception handler for Sign up Failures
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The Sign up Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */

    @ExceptionHandler(SignUpRestrictedException.class)
    public ResponseEntity<ErrorResponse> signUpRestrictedException(SignUpRestrictedException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.CONFLICT
        );
    }

    /**
     * Global Exception handler for Sign out Failures
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The Sign out Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(SignOutRestrictedException.class)
    public ResponseEntity<ErrorResponse> signOutRestrictedException(SignOutRestrictedException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Global Exception handler for all Authorization failures, based on authorization token
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The Authorization Failure Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(AuthorizationFailedException.class)
    public ResponseEntity<ErrorResponse> authorizationFailedException(AuthorizationFailedException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.FORBIDDEN
        );
    }

    /**
     * Global Exception handler for authentication failure
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The Authentication Exception occured in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(AuthenticationFailedException.class)
    public ResponseEntity<ErrorResponse> authenticationFailedException(AuthenticationFailedException exe, WebRequest request) {

        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.UNAUTHORIZED
        );
    }

    /**
     * Global Exception handler for Invalid Question failures
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The InvalidQuestionException Failure Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(InvalidQuestionException.class)
    public ResponseEntity<ErrorResponse> invalidQuestionException(InvalidQuestionException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.NOT_FOUND
        );
    }

    /**
     * Global Exception handler for Invalid Answer failure
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The AnswerNotFoundException Failure Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(AnswerNotFoundException.class)
    public ResponseEntity<ErrorResponse> answerNotFoundException(AnswerNotFoundException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.NOT_FOUND
        );
    }

    /**
     * Global Exception handler for Invalid User failures
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The UserNotFoundException Failure Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<ErrorResponse> userNotFoundException(UserNotFoundException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getCode()).message(exe.getErrorMessage()), HttpStatus.NOT_FOUND
        );
    }

    /**
     * Global Exception handler for Unexpected Exceptions
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param exe     The UnexpectedException Failure Exception occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(UnexpectedException.class)
    public ResponseEntity<ErrorResponse> unexpectedException(UnexpectedException exe, WebRequest request) {
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(exe.getErrorCode().toString()).message(exe.getMessage()), HttpStatus.INTERNAL_SERVER_ERROR
        );

    }

    /**
     * Global Exception handler for ConstraintViolation Exceptions
     * Handles the exception and sends back the user/client a user friendly message along with HTTP Status code
     *
     * @param ex      The ConstraintViolationException Failures occurred in the application
     * @param request The web request information if any to be used while framing the response
     * @return The Error Response consisting of the Http status code and an error message
     */
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> constraintViolationException(ConstraintViolationException ex, WebRequest request) {
        StringBuilder sb = new StringBuilder();
        Set<ConstraintViolation<?>> violations = ex.getConstraintViolations();
        if (violations != null) {
            for (ConstraintViolation violation : violations) {
                sb.append(violation.getPropertyPath()).append(" ").append(violation.getMessage()).append(" ");
            }
        }
        return new ResponseEntity<ErrorResponse>(
                new ErrorResponse().code(GenericErrorCode.GEN_001.getCode()).message(sb.toString()),
                HttpStatus.INTERNAL_SERVER_ERROR
        );
    }
}