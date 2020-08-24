package com.upgrad.quora.service.business;

import com.upgrad.quora.service.dao.QuestionDao;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.Question;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import com.upgrad.quora.service.exception.InvalidQuestionException;
import com.upgrad.quora.service.exception.UserNotFoundException;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.util.List;

@Service
public class QuestionBusinessService {

    @Autowired
    private UserDao userDao;

    @Autowired
    private QuestionDao questionDao;

    @Autowired
    private UserBusinessService userBusinessService;


    /**
     * This method pulls all the question details from the database after validating the user authorization token
     * If the token is not valid, throws an Authorization failure
     *
     * @param authorization holds the Bearer access token for authenticating the user
     * @return All the Questions added in the application present in the Database
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    public List<Question> getAllQuestions(String authorization) throws AuthorizationFailedException {
        userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to get all questions");
        return questionDao.getAllQuestions();
    }

    /**
     * This method fetches all the questions posted by a particular user after
     * validating the authorization token is valid
     * If token is invalid or user is logged out then appropriate error message
     * is thrown back to the client
     * Same applies when the userId itself doesn't match with any user in DB
     *
     * @param userUUID      The user UUID whose questions have to be retrieved
     * @param authorization holds the Bearer access token for authenticating the user
     * @return The list of all questions posted by the user matched with userId
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     * @throws UserNotFoundException        If no user id with that UUID exists in DB
     */
    public List<Question> getAllQuestionsByUser(String userUUID, String authorization) throws AuthorizationFailedException, UserNotFoundException {
        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,"User is signed out.Sign in first to post a question");
        final User user = userDao.getUserByUUID(userUUID);
        // No user matched with the UUID
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid whose question details are to be seen does not exist");
        }
        return questionDao.findQuestionByUserId(user.getId());
    }
    /**
     * This method first validate the user calling the validate method is UserDao
     * than this method stores the question in database if user is validated successfully
     *
     * @param question      this is question object that needed to be stored in database
     * @param authorization holds the Bearer access token for authenticating the user
     * @return the newly created question after saving in database
     * @throws AuthorizationFailedException If the token is not present in DB or user already logged out
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Question createNewQuestion(Question question, String authorization) throws AuthorizationFailedException {

        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to post a question");
        question.setDate(ZonedDateTime.now());
        question.setUser(userAuthEntity.getUser());
        Question createdQuestion = questionDao.createQuestion(question);
        return createdQuestion;

    }

    /**
     * This method is used to edit question content :
     * checks for all the conditions and provides necessary response messages
     *
     * @param question      entity
     * @param questionId    for the question which needs to be edited
     * @param authorization holds the Bearer access token for authenticating
     * @return updates the question as per the questionId provided
     * @throws AuthorizationFailedException if access token does not exit, if user has signed out, if non-owner tries to edit
     * @throws InvalidQuestionException     if question with uuid which is to be edited does not exist in the database
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public Question editQuestionContent(final Question question, final String questionId, final String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to edit the question");
        Question questionEntity = questionDao.getQuestionByUUID(questionId);
        // If the question with uuid which is to be edited does not exist in the database, throw 'InvalidQuestionException'
        if (questionEntity == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        } else {
            // if the user who is not the owner of the question tries to edit the question throw "AuthorizationFailedException"
            if (questionEntity.getUser().getId() != userAuthEntity.getUser().getId()) {
                throw new AuthorizationFailedException("ATHR-003", "Only the question owner can edit the question");
            }
        }
        questionEntity.setContent(question.getContent());
        return questionDao.updateQuestion(questionEntity);
    }

    /**
     * This method is used to delete question
     * checks for all the conditions and provides necessary response messages
     *
     * @param questionId    or the question which needs to be deleted
     * @param authorization holds the Bearer access token for authenticating
     * @return the uuid of the question that is deleted
     * @throws AuthorizationFailedException if access token does not exit, if user has signed out, if non-owner tries to delete
     * @throws InvalidQuestionException     if question with uuid which is to be edited does not exist in the database
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String deleteQuestion(String questionId, String authorization) throws AuthorizationFailedException, InvalidQuestionException {
        final UserAuthEntity userAuthEntity = userBusinessService.validateUserAuthentication(authorization,
                "User is signed out.Sign in first to delete a question");
        Question question = questionDao.getQuestionByUUID(questionId);
        if (question == null) {
            throw new InvalidQuestionException("QUES-001", "Entered question uuid does not exist");
        }
        if (QuoraUtil.ADMIN_ROLE.equalsIgnoreCase(userAuthEntity.getUser().getRole()) || question.getUser().getId() == userAuthEntity.getUser().getId()) {
            questionDao.deleteQuestion(question);
            return question.getUuid();
        }
        throw new AuthorizationFailedException("ATHR-003", "Only the question owner or admin can delete the question");
    }
}
