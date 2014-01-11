package blog.dao;

import com.mongodb.*;
import sun.misc.BASE64Encoder;

import java.io.UnsupportedEncodingException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random;

/**
 * author: erik
 */
public class UserDAO {
    private final DBCollection usersCollection;
    private Random random = new SecureRandom();

    public UserDAO(final DB moneyDB) {
        usersCollection = moneyDB.getCollection("users");
    }

    // validates that username is unique and insert into db
    public boolean addUser(String username, String password, String email, Boolean isAdmin) {

        String passwordHash = makePasswordHash(password, Integer.toString(random.nextInt()));

        BasicDBObject user = new BasicDBObject();

        user.append("username", username).append("password", passwordHash).append("isAdmin", isAdmin);

        if (email != null && !email.equals("")) {
            // the provided email address
            user.append("email", email);
        }

        try {
            usersCollection.insert(user);
            return true;
        } catch (MongoException.DuplicateKey e) {
            System.out.println("Username already in use: " + username);
            return false;
        }
    }

    public DBObject validateLogin(String username, String password) {
        DBObject user;

        user = usersCollection.findOne(new BasicDBObject("username", username));

        if (user == null) {
            System.out.println("User not in database");
            return null;
        }

        String hashedAndSalted = user.get("password").toString();

        String salt = hashedAndSalted.split(",")[1];

        if (!hashedAndSalted.equals(makePasswordHash(password, salt))) {
            System.out.println("Submitted password is not a match");
            return null;
        }

        return user;
    }


    private String makePasswordHash(String password, String salt) {
        try {
            String saltedAndHashed = password + "," + salt;
            MessageDigest digest = MessageDigest.getInstance("MD5");
            digest.update(saltedAndHashed.getBytes());
            BASE64Encoder encoder = new BASE64Encoder();
            byte hashedBytes[] = (new String(digest.digest(), "UTF-8")).getBytes();
            return encoder.encode(hashedBytes) + "," + salt;
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("MD5 is not available", e);
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException("UTF-8 unavailable?  Not a chance", e);
        }
    }


    public String getUserIdByUsername(String username) {


        return null;
    }

    public Boolean isAdminByUsername(String username) {
        DBObject query = new BasicDBObject("username", username);

        DBObject user = usersCollection.findOne(query);
        if (user == null) {
            return false;
        } else {
            Boolean isAdmin = (Boolean) user.get("isAdmin");
            return isAdmin;
        }
    }
}
