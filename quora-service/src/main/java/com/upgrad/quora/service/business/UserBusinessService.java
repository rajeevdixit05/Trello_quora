package com.upgrad.quora.service.business;

import com.upgrad.quora.service.common.GenericErrorCode;
import com.upgrad.quora.service.common.UnexpectedException;
import com.upgrad.quora.service.dao.UserDao;
import com.upgrad.quora.service.entity.User;
import com.upgrad.quora.service.entity.UserAuthEntity;
import com.upgrad.quora.service.exception.*;
import com.upgrad.quora.service.util.QuoraUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZonedDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Base64;

@Service
public class UserBusinessService {
    private static final Long EIGHT_HOURS_IN_MILLIS = 8 * 60 * 60 * 1000L;

    @Autowired
    private UserDao userDao;

    @Autowired
    private PasswordCryptographyProvider cryptographyProvider;
    /**
     * This method saves the registered user information to the Database
     * Encrypts the user password before storing in the DB
     * Checks if the existing user is trying to signup again by matching username/email
     * If so, throws error message as already username taken or already registered
     *
     * @param user The user information to be saved as part of signup
     * @return The persisted user details with the id value generated
     * @throws SignUpRestrictedException if the user details matches with the existing records
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public User signup(User user) throws SignUpRestrictedException {


        if (userDao.getUserByUserName(user.getUserName()) != null) {
            throw new SignUpRestrictedException("SGR-001", "Try any other Username, this Username has already been taken");
        }
        if (userDao.getUserByEmail(user.getEmail()) != null) {
            throw new SignUpRestrictedException("SGR-002", "This user has already been registered, try with any other emailId");
        }
        String password = user.getPassword();

        if (password != null) {
            String[] encryptedText = cryptographyProvider.encrypt(user.getPassword());
            user.setSalt(encryptedText[0]);
            user.setPassword(encryptedText[1]);
            user.setRole(QuoraUtil.NON_ADMIN_ROLE);
        }

        return userDao.createUser(user);
    }

    /**
     * This method takes the authorization string which is encoded username and password
     * If the username and password doesnot matches than it throws Authentication failed exception
     * If the username and password match than auth token is generated
     * If the input is illegal it  throws Unexpected Exception
     *
     * @param authorization holds the basic access token used for authentication
     * @return userAuthTokenEntity that conatins acess token and user UUID
     * @throws AuthenticationFailedException if the username doesnot exists or password doesnot match
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public UserAuthEntity signIn(String authorization) throws AuthenticationFailedException {
        //this will be used to decode the request header authorization
        try {
            byte[] decode = Base64.getDecoder().decode(authorization.split(QuoraUtil.BASIC_TOKEN)[1]);
            String decodedText = new String(decode);
            String[] decodedArray = decodedText.split(QuoraUtil.COLON);
            String username = decodedArray[0];
            String password = decodedArray[1];
            User user = userDao.getUserByUserName(username);
            if (user == null) {
                throw new AuthenticationFailedException("ATH-001", "This username does not exist");
            }

            final String encryptedPassword = cryptographyProvider.encrypt(password, user.getSalt());
            if (encryptedPassword.equals(user.getPassword())) {

                JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(encryptedPassword);
                UserAuthEntity userAuthTokenEntity = new UserAuthEntity();
                userAuthTokenEntity.setUser(user);
                final ZonedDateTime now = ZonedDateTime.now();
                final ZonedDateTime expiresAt = now.plusHours(8);
                userAuthTokenEntity.setAccessToken(jwtTokenProvider.generateToken(user.getUuid(), now, expiresAt));
                userAuthTokenEntity.setLoginAt(now);
                userAuthTokenEntity.setExpiresAt(expiresAt);
                userAuthTokenEntity.setUuid(user.getUuid());
                return userDao.createAuthToken(userAuthTokenEntity);

            } else {
                throw new AuthenticationFailedException("ATH-002", "Password failed");
            }

        } catch (ArrayIndexOutOfBoundsException | IllegalArgumentException ex) {
            GenericErrorCode genericErrorCode = GenericErrorCode.GEN_001;
            throw new UnexpectedException(genericErrorCode, ex);
        }
    }


    /**
     * This method validates the user session by making use of the access token
     * If it is expired or invalid, then throws back the exception asking the user to sign in
     * If the user session is active, then pulls the UUID of the user̥
     *
     * @param authorization holds the bearer access token for authenticating the user
     * @return uuid of the user
     * @throws SignOutRestrictedException if the access token is expired or user never signed in
     */
    @Transactional(propagation = Propagation.REQUIRED)
    public String getUserUUID(String authorization) throws SignOutRestrictedException {
        String[] bearerToken = authorization.split(QuoraUtil.BEARER_TOKEN);
        // If Bearer Token prefix is missed, ignore and just use the authorization text
        if (bearerToken != null && bearerToken.length > 1) {
            authorization = bearerToken[1];
        }
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);
        if (isUserSessionValid(userAuthEntity)) {
            userAuthEntity.setLogoutAt(ZonedDateTime.now());
            userDao.updateUserAuthEntity(userAuthEntity);
            return userAuthEntity.getUuid();
        }

        throw new SignOutRestrictedException("SGR-001", "User is not Signed in");
    }

    /**
     * This method checks if the user session is active based on the access token/logout at time
     *
     * @param userAuthEntity The authentication Entity object holding the information about user login and access token
     * @return true if the token exists in the DB and user is not logged out, false otherwise
     */
    public Boolean isUserSessionValid(UserAuthEntity userAuthEntity) {
        // userAuthEntity will be non null only if token exists in DB, and logoutAt null indicates user has not logged out yet
        return (userAuthEntity != null && userAuthEntity.getLogoutAt() == null);
    }

    /**
     * This method validates the authorization access token passed while accessing the apis after signing in
     * Handles the token both with/without Bearer prefix in the authorization token
     * Generic method used for different scenarios, so for ATHR-002 the message text will be used
     * from the dynamic value passed in
     *
     * @param authorization  holds the Bearer access token for authenticating the user
     * @param athr002Message The message text for different scenarios of ATHR-002 error code
     * @return The userAuthEntity based on the matched authorization
     * @throws AuthorizationFailedException if the token is not present in DB or user already logged out
     */
    public UserAuthEntity validateUserAuthentication(String authorization, String athr002Message)
            throws AuthorizationFailedException {
        String[] bearerToken = authorization.split(QuoraUtil.BEARER_TOKEN);
        // If Bearer Token prefix is missed, ignore and just use the authorization text
        if (bearerToken != null && bearerToken.length > 1) {
            authorization = bearerToken[1];
        }
        UserAuthEntity userAuthEntity = userDao.getUserAuthToken(authorization);
        // Token is not matched with the database records
        if (userAuthEntity == null) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }
        // Token matches, but the user has already logged out
        if (userAuthEntity.getLogoutAt() != null) {
            throw new AuthorizationFailedException("ATHR-002", athr002Message);
        }
        return userAuthEntity;
    }

    /**
     * This Method is used to get User Details from the database.
     *
     * @param userUuid      user id to get details of specific user.
     * @param authorization holds the Bearer access token for authenticating
     * @return the user profile if the conditions are satisfied
     * @throws AuthorizationFailedException If the access token provided by the user does not exist in the database,
     *                                      If the user has signed out
     * @throws UserNotFoundException        If the user with uuid whose profile is to be retrieved does not exist in the database
     */
    public User getUser(final String userUuid, final String authorization) throws AuthorizationFailedException, UserNotFoundException {
        UserAuthEntity userAuthEntity = validateUserAuthentication(authorization,
                "User is signed out.Sign in first to get user details");
        User user = userDao.getUserByUUID(userUuid);
        /**
         * If the user with uuid whose profile is to be retrieved does not exist
         * in the database, throw 'UserNotFoundException'
         */
        if (user == null) {
            throw new UserNotFoundException("USR-001", "User with entered uuid does not exist");
        }
        return user;
    }
}
