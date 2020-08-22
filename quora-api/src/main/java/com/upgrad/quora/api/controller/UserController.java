package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.SignoutResponse;
import com.upgrad.quora.api.model.SignupUserRequest;
import com.upgrad.quora.api.model.SignupUserResponse;
import com.upgrad.quora.service.business.UserBusinessService;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.exception.SignOutRestrictedException;
import com.upgrad.quora.service.exception.SignUpRestrictedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/")
public class UserController {
    @Autowired
    private UserBusinessService userBusinessService;

    /**
     * This method registers a user with all the details provided and handles the
     * Scenario when user provides empty or invalid username/email and throws an error message
     *
     * @param signupUserRequest Holds all the details keyed in by the user at the time of Signup
     * @return UUID of the registered user for further login
     * @throws SignUpRestrictedException if the user provides invalid username/email
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signup", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignupUserResponse> signUp(final SignupUserRequest signupUserRequest) throws SignUpRestrictedException {
        final User user = new User();
        user.setUuid(UUID.randomUUID().toString());
        user.setFirstName(signupUserRequest.getFirstName());
        user.setLastName(signupUserRequest.getLastName());
        user.setUserName(signupUserRequest.getUserName());
        user.setEmail(signupUserRequest.getEmailAddress());
        user.setPassword(signupUserRequest.getPassword());
        user.setCountry(signupUserRequest.getCountry());
        user.setAboutMe(signupUserRequest.getAboutMe());
        user.setDob(signupUserRequest.getDob());
        user.setContactNumber(signupUserRequest.getContactNumber());
        final User createdUser = userBusinessService.signup(user);
        SignupUserResponse signupUserResponse = new SignupUserResponse();
        signupUserResponse.id(createdUser.getUuid()).status("USER SUCCESSFULLY REGISTERED");
        return new ResponseEntity<SignupUserResponse>(signupUserResponse, HttpStatus.CREATED);
    }

    /**
     * This method signs out the user from the application if his session is still active.
     * If not, throws an error message stating the user is not logged in before to signout.
     *
     * @param authorization Holds the access token generated at the time of signin and is used for authentication
     * @return UUID of the use̥r and a message stating Sign Out Successful
     * @throws SignOutRestrictedException when the user session is inactive or he never signed in before
     */
    @RequestMapping(method = RequestMethod.POST, path = "/user/signout", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<SignoutResponse> signOut(@RequestHeader("authorization") final String authorization) throws SignOutRestrictedException {
        String uuid = userBusinessService.getUserUUID(authorization);
        SignoutResponse signoutResponse = new SignoutResponse();
        signoutResponse.setId(uuid);
        signoutResponse.setMessage("SIGNED OUT SUCCESSFULLY");
        return new ResponseEntity<SignoutResponse>(signoutResponse, HttpStatus.OK);
    }
}