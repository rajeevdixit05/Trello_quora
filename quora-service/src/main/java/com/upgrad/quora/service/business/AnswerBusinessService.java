package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnswerBusinessService {

    @Autowired
    private AnswerDao answerDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserBusinessService userBusinessService;
    /**
     * This method fetches all the answers posted to a Specific question referred by questionId
     * after validating the authorization token
     *
     * @param questionId    The UUID of the question for which answers are to be retrieved
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The list of all answers posted for a specific question
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     * @throws InvalidQuestionException     If the Question with the uuid passed doesn't exist in DB
     */
    public List<Answer> getAllAnswersToQuestion(String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to get the answers");
        final Question question = questionDao.getQuestionByUUID(questionId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "The question with entered uuid whose details are to be seen does not exist");
        }
        return answerDao.getAllAnswersByQuestionId(question.getId());
    }
}
