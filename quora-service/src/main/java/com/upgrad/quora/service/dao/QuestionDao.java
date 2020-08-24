package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.Question;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;
import java.util.List;

@Repository
public class QuestionDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Retrieves all the questions present in the Database question table and returns as a list
     *
     * @return The list of questions present in the question table
     */
    public List<Question> getAllQuestions() {
        final List<Question> allQuestions = entityManager.createQuery("select q from Question q", Question.class).getResultList();
        return allQuestions;
    }


    /**
     * Retrieves all the questions posted by a user matched with the userId field
     * Here the userId is the Id attribute in User Entity
     *
     * @param userId The user id Id attribute of User Entity to pull the questions posted by that user
     * @return The list of all questions posted by the matched user
     */
    public List<Question> findQuestionByUserId(Integer userId) {
        return entityManager.createNamedQuery("questionByUserId", Question.class).setParameter("userId", userId).getResultList();
    }

    /**
     * Retrieves question present in database by ID
     *
     * @return The question present in the question table
     */
    public Question getQuestionByUUID(String questionUUID) {
        try {
            return entityManager.createNamedQuery("questionByUUID", Question.class).setParameter("uuid", questionUUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

}

