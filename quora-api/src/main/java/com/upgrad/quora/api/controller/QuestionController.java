package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.QuestionDetailsResponse;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

@RestController
public class QuestionController {
    @Autowired
    private QuestionBusinessService questionBusinessService;
    /**
     * This method validates the user session and if active pulls all the questions from the database
     * Populates the uuid and content of each question posted earlier in the application and sends in the response
     * If session token is invalid, then throws the error message of Authorization failure
     *
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The List of question details(uuid, question content) present in the database
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestions(@RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        final List<Question> allQuestions = questionBusinessService.getAllQuestions(authorization);
        return getQuestionDetailsResponse(allQuestions);
    }

    /**
     * This method fetches all the questions posted by a particular user matched
     * by the userId from the DB
     * Validates authorization token and throws error accordingly based on whether
     * the user is signed out or an invalid token is passed
     * If the userId doesn't match with any of the users in DB, then an error message
     * is thrown saying user doesn't exist.
     *
     * @param userId        The user UUID whose questions have to be retrieved
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The list of all questions posted by the user matched with userId
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     * @throws UserNotFoundException        If no user id with that UUID exists in DB
     */
    @RequestMapping(method = RequestMethod.GET, path = "/question/all/{userId}")
    public ResponseEntity<List<QuestionDetailsResponse>> getAllQuestionsByUser(
            @PathVariable("userId") final String userId, @RequestHeader final String authorization)
            throws AuthorizationFailedException, UserNotFoundException {
        List<Question> allQuestionsByUser = questionBusinessService.getAllQuestionsByUser(userId, authorization);
        return getQuestionDetailsResponse(allQuestionsByUser);
    }
    /**
     * This method takes the list of question as input and populates the corresponding response objects
     * setting the uuid and the content of each question. Add the Http Response code so that this method
     * return value can be used to return in the corresponding request mapped methods
     *
     * @param allQuestions The List of Questions retrieved from the Database to populate the responses
     * @return ResponseEntity with the required question details populated and the HTTP Status added
     */
    private ResponseEntity<List<QuestionDetailsResponse>> getQuestionDetailsResponse(List<Question> allQuestions) {
        List<QuestionDetailsResponse> allQuesDetailsResponse = new ArrayList<>();
        for (Question question : allQuestions) {
            QuestionDetailsResponse questionDetailsResponse = new QuestionDetailsResponse();
            questionDetailsResponse.id(question.getUuid()).content(question.getContent());
            allQuesDetailsResponse.add(questionDetailsResponse);
        }
        return new ResponseEntity<List<QuestionDetailsResponse>>(allQuesDetailsResponse, HttpStatus.OK);
    }
}
