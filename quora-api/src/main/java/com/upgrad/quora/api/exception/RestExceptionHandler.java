package com.upgrad.quora.api.exception;

import com.upgrad.quora.api.model.ErrorResponse;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.context.request.WebRequest;

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
}