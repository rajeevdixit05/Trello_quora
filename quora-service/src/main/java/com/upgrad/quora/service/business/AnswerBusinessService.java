package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.AnswerDao;
import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.entity.Answer;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AnswerNotFoundException;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

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
     * This method is used to create answer for questions asked by users
     *
     * @param answer        for the particular question
     * @param questionId    for the question which needs to be answered
     * @param authorization holds the Bearer access token for authenticating
     * @return creates the answer for particular question by Id
     * @throws AuthorizationFailedException If the access token provided by the user does not exist
     *                                      in the database, If the user has signed out
     * @throws InvalidQuestionException     If the question uuid entered by the user whose answer
     *                                      is to be posted does not exist in the database
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer createAnswer(final Answer answer, final String questionId, final String authorization) throws
            AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to post an answer");

        Question questionEntity = questionDao.getQuestionByUUID(questionId);
        if (questionEntity == null) {
                /*If the question uuid entered by the user whose answer
              is to be posted does not exist in the database, throw
              "InvalidQuestionException"
            */
            throw new InvalidQuestionException("QUES-001", "The question entered is invalid");
        }
        answer.setQuestion(questionEntity);
        answer.setUser(userAuthEntity.getUser());
        return answerDao.createAnswer(answer);
    }
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

    /**
     * This method validates Authorization for the user and returns the Id of the deleted answer
     *
     * @param answerId      UUid for particular answer
     * @param authorization holds the Bearer access token for authenticating
     * @return returns id of the answer
     * @throws AuthorizationFailedException If the access token provided by the user does not exist in the database,
     *                                      If the user has signed out, if the user who is not the owner of the answer
     *                                      or the role of the user is not‘admin’ and tries to delete the answer
     * @throws AnswerNotFoundException      If the answer with uuid which is to be deleted does not exist in the database
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteAnswer(String answerId, String authorization)
            throws AuthorizationFailedException, InvalidQuestionException, AnswerNotFoundException {
        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to delete an answer");

        Answer answer = answerDao.getAnswerByUUID(answerId);
        // If the answer with uuid which is to be deleted does not exist in the database
        if (answer == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        }
        if (QuoraUtil.ADMIN_ROLE.equalsIgnoreCase(userAuthEntity.getUser().getRole())
                || answer.getUser().getId() == userAuthEntity.getUser().getId()) {
            answerDao.deleteAnswer(answer);
            return answer.getUuid();
        }
        /*
         * Only the answer owner or admin can delete the answer. Therefore, if the user who is not the owner of the answer or the role of the user is ‘nonadmin’
         * and tries to delete the answer throw "AuthorizationFailedException"
         */
        throw new AuthorizationFailedException("ATHR-003", "Only the answer owner or admin can delete the answer");
    }

    /**
     * This method is used to edit answer content
     * checks for all the conditions and provides necessary response messages
     *
     * @param answer        entity that needed to be updated
     * @param answerId      Is the uuid of the answer that needed to be edited
     * @param authorization holds the Bearer access token for authenticating
     * @return the answer after updating the content
     * @throws AuthorizationFailedException if access token does not exit, if user has signed out, if non-owner tries to edit
     * @throws AnswerNotFoundException      if answer with uuid which is to be edited does not exist in the database
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Answer editAnswerContent(final Answer answer, final String answerId, final String authorization)
            throws AuthorizationFailedException, AnswerNotFoundException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to edit an answer");
        Answer answerEntity = answerDao.getAnswerByUUID(answerId);
        // If the answer with uuid which is to be edited does not exist in the database, throw 'AnswerNotFoundException'
        if (answerEntity == null) {
            throw new AnswerNotFoundException("ANS-001", "Entered answer uuid does not exist");
        } else {
            // if the user who is not the owner of the answer tries to edit the answer throw "AuthorizationFailedException"
            if (answerEntity.getUser().getId() != userAuthEntity.getUser().getId()) {
                throw new AuthorizationFailedException("ATHR-003", "Only the answer owner can edit the answer");
            }
        }
        answerEntity.setAns(answer.getAns());
        return answerDao.updateAnswerContent(answerEntity);
    }
}
