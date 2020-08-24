package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.UserDeleteResponse;
import com.upgrad.quora.service.business.AdminBusinessService;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
public class AdminController {

    @Autowired
    private AdminBusinessService adminBusinessService;

    /**
     * This method is used to delete a user from db
     * Note,Only the admin can delete a user
     *
     * @param userId        Is the uuid of the user to be deleted from db
     * @param authorization Holds the access token generated at the time of signin and is used for authentication
     * @return uuid of the deleted user and message 'USER SUCCESSFULLY DELETED' in the JSON response with the corresponding HTTP status.
     * @throws AuthorizationFailedException if access token does not exit : if user has signed out : if non-admin tries to delete
     * @throws UserNotFoundException        if answer with uuid which is to be deleted does not exist in the database
     */

    @RequestMapping(method = RequestMethod.DELETE, path = "/admin/user/{userId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<UserDeleteResponse> userDelete(
            @PathVariable("userId") final String userId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, UserNotFoundException {

        String userUUID = adminBusinessService.deleteUser(userId, authorization);
        final UserDeleteResponse userDeleteResponse = new UserDeleteResponse();
        userDeleteResponse.id(userUUID).status("USER SUCCESSFULLY DELETED");
        return new ResponseEntity<UserDeleteResponse>(userDeleteResponse, HttpStatus.OK);
    }

}
