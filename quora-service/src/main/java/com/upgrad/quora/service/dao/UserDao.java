package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.User;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.PersistenceContext;

@Repository
public class UserDao {

    @PersistenceContext
    private EntityManager entityManager;

    /**
     * Persists the User Information in the Database table
     *
     * @param user The user data to be stored in DB
     * @return Updated User with generated id value
     */
    public User createUser(User user) {
        entityManager.persist(user);
        return user;
    }

    /**
     * Retreives the user record matching with the username passed
     *
     * @param username The username to match with the user record
     * @return The Found user matching with username, otherwise null
     */
    public User getUserByUserName(final String username) {
        try {
            return entityManager.createNamedQuery("userByUserName", User.class).setParameter("userName", username).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    /**
     * Retreives the user record matching with the username passed
     *
     * @param email The email to match with the user record
     * @return The Found user matching with username, otherwise null
     */
    public User getUserByEmail(final String email) {
        try {
            return entityManager.createNamedQuery("userByEmail", User.class).setParameter("email", email).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
}