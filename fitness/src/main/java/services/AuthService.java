package services;

import database.DatabaseConnection;
import database.UserDAO;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.Session;

import java.sql.*;

public class AuthService {

    private final UserDAO userDAO = new UserDAO();

    public boolean login(String email, String password) throws Exception {
        User user = userDAO.findByEmail(email);
        if (user == null) return false;
        if (!BCrypt.checkpw(password, user.getPassword())) return false;
        Session.setCurrentUser(user);
        return true;
    }

    public boolean register(String fullName, String email,
                            String password, String phone) throws Exception {
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(hashed);
        user.setPhone(phone);
        userDAO.save(user);
        return true;
    }
}