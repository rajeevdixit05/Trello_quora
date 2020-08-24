package com.upgrad.quora.api.controller;

import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.QuestionBusinessService;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class QuestionController {
    @Autowired
    private QuestionBusinessService questionBusinessService;

    /**
     * This method is used to create a new question
     * It uses Bearer token to validate the user
     *
     * @param questionRequest Contains all the attributes about the question
     * @param authorization   Holds the Bearer access token for authenticating the user
     * @return ResponseEntity with required question uuid and status
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    @RequestMapping(method = RequestMethod.POST, path = "/question/create", consumes = MediaType.APPLICATION_JSON_UTF8_VALUE, produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionResponse> createQuestion(final QuestionRequest questionRequest,
                                                           @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException {
        final Question question = new Question();
        question.setUuid(UUID.randomUUID().toString());
        question.setContent(questionRequest.getContent());
        Question createdQuestion = questionBusinessService.createNewQuestion(question, authorization);
        QuestionResponse questionResponse = new QuestionResponse();
        questionResponse.id(createdQuestion.getUuid()).status("QUESTION CREATED");
        return new ResponseEntity<QuestionResponse>(questionResponse, HttpStatus.CREATED);
    }

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
     * This method is used to delete the question.
     * Note, only the owner of the question or admin can delete the question
     *
     * @param questionId    It is uuid of the question to be deleted
     * @param authorization holds the Bearer access token for authenticating the user.
     * @return uuid of the deleted question and message 'QUESTION DELETED' in the JSON response with the corresponding HTTP status.
     * @throws AuthorizationFailedException if access token does not exit : if user has signed out : if non-owner tries to edit
     * @throws InvalidQuestionException     if question with uuid which is to be deleted does not exist in the database
     */
    @RequestMapping(method = RequestMethod.DELETE, path = "/question/delete/{questionId}", produces = MediaType.APPLICATION_JSON_UTF8_VALUE)
    public ResponseEntity<QuestionDeleteResponse> deleteQuestion(
            @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        String questionUUID = questionBusinessService.deleteQuestion(questionId, authorization);
        final QuestionDeleteResponse questionDeleteResponse = new QuestionDeleteResponse();
        questionDeleteResponse.id(questionUUID).status("QUESTION DELETED");
        return new ResponseEntity<QuestionDeleteResponse>(questionDeleteResponse, HttpStatus.OK);
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

    /**
     * This method is used to edit a question that has been posted by a user. Note, only the owner of the
     * question can edit the question.
     *
     * @param questionId    for the question which needs to be edited.
     * @param authorization holds the Bearer access token for authenticating the user.
     * @return uuid of the edited question and message 'QUESTION EDITED' in the JSON response with the corresponding HTTP status.
     * @throws AuthorizationFailedException : if access token does not exit : if user has signed out : if non-owner tries to edit
     * @throws InvalidQuestionException     : if question with uuid which is to be edited does not exist in the database
     */
    @RequestMapping(method = RequestMethod.PUT, path = "/question/edit/{questionId}")
    public ResponseEntity<QuestionEditResponse> editQuestionContent(
            @PathVariable("questionId") final String questionId,
            @RequestHeader("authorization") final String authorization,
            final QuestionEditRequest questionEditRequest)
            throws InvalidQuestionException, AuthorizationFailedException {
        final Question question = new Question();
        question.setContent(questionEditRequest.getContent());
        final Question editQuestionEntity = questionBusinessService.editQuestionContent(question, questionId, authorization);
        QuestionEditResponse questionEditResponse = new QuestionEditResponse().id(editQuestionEntity.getUuid()).status("QUESTION EDITED");
        return new ResponseEntity<QuestionEditResponse>(questionEditResponse, HttpStatus.OK);
    }
}
