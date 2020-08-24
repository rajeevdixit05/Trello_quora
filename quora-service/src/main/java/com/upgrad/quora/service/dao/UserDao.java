package com.upgrad.quora.service.dao;

import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
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
     * This method is added to persist the authData in database
     *
     * @param userAuthEntity Contains user information who has signed in and the access token
     * @return The userAuthEntity that is saved in data base
     */
    public UserAuthEntity createAuthToken(final UserAuthEntity userAuthEntity) {
        entityManager.persist(userAuthEntity);
        return userAuthEntity;
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
     * Retrieves the user record matching with the username passed
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

    /**
     * Retrieves the user auth record matched with the access token passed
     * The access token is the one generated at the time of login
     *
     * @param accessToken The Security accessToken generated at the time of Sign in
     * @return The UserAuthEntity record matched with the accessToken
     */
    public UserAuthEntity getUserAuthToken(final String accessToken) {
        try {
            return entityManager.createNamedQuery("userAuthByAccessToken", UserAuthEntity.class).setParameter("accessToken", accessToken).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }

    /**
     * Updates the User Auth Entity, like logout times or tokens to the Database
     *
     * @param updatedUserAuthEntity The Entity object to be updated in the Database
     */
    public void updateUserAuthEntity(final UserAuthEntity updatedUserAuthEntity) {
        entityManager.merge(updatedUserAuthEntity);
    }
    /**
     * Retrieves the user detail matched with the userId passed
     * @param userUUID Id of the user
     * @return matched userID detail
     */
    public User getUserByUUID(String userUUID) {
        try {
            return entityManager.createNamedQuery("userByUUID", User.class).setParameter("uuid", userUUID).getSingleResult();
        } catch (NoResultException nre) {
            return null;
        }
    }
    /**
     * This method is used to delete a user from db
     *
     * @param user Is User that needed to be deleted from db
     */
    public void deleteUser(User user) {
        entityManager.remove(user);
    }
}