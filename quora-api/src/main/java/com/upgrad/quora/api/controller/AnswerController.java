package com.upgrad.quora.api.controller;


import com.upgrad.quora.api.model.*;
import com.upgrad.quora.service.business.AnswerBusinessService;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@RestController
public class AnswerController {
    @Autowired
    private AnswerBusinessService answerBusinessService;

    /**
     * This method retrieves all the answers for a specific Question
     * after validating the user authorization token
     *
     * @param questionId    The UUID of the question for which answers are to be retrieved
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The question content and the list of all answers with respective uuid and answer content
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     * @throws InvalidQuestionException     If the Question with the uuid passed doesn't exist in DB
     */
    @RequestMapping(path = "/answer/all/{questionId}", method = RequestMethod.GET)
    public ResponseEntity<List<AnswerDetailsResponse>> getAllAnswersToQuestion(
            @PathVariable("questionId") final String questionId, @RequestHeader("authorization") final String authorization)
            throws AuthorizationFailedException, InvalidQuestionException {
        final List<Answer> allAnswersToQuestion = answerBusinessService.getAllAnswersToQuestion(questionId, authorization);
        List<AnswerDetailsResponse> answerDetailsResponseList = new ArrayList<>();
        for (Answer answer : allAnswersToQuestion) {
            AnswerDetailsResponse answerDetailsResponse = new AnswerDetailsResponse();
            answerDetailsResponse.id(answer.getUuid())
                    .questionContent(answer.getQuestion().getContent())
                    .answerContent(answer.getAns());
            answerDetailsResponseList.add(answerDetailsResponse);
        }
        return new ResponseEntity<List<AnswerDetailsResponse>>(answerDetailsResponseList, HttpStatus.OK);
    }
}
