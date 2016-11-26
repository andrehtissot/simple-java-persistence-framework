package com.andretissot.java.model;

import com.andretissot.java.dao.Model;
import java.math.BigInteger;
import java.security.MessageDigest;

/**
 * @author André Augusto Tissot
 */
public class User extends Model {
    public final static int ADMIN = 1;
    public final static int USER = 0;
    private static User currentUser = null;

    @Override
    public String getTableName() {
        return "user";
    }

    @Override
    public String[] getColumnNames() {
        String[] columnNames = {"id", "login", "password", "profile"};
        return columnNames;
    }

    @Override
    public String[][] validatesPresenceOf() {
        String[][] columnNames = {{"login", "Login should be filled"},
            {"password", "Password should be filled"},
            {"profile", "Profile should be filled"}};
        return columnNames;
    }

    public static String generateMD5(String password) throws Exception {
        MessageDigest md5 = MessageDigest.getInstance("MD5");
        md5.update(password.getBytes());
        BigInteger hash = new BigInteger(1, md5.digest());
        return hash.toString(16);
    }

    public static boolean login(String login, String password) throws Exception {
        User user = new User();
        login = login.replaceAll("[\'\"]", "");
        password = password.replaceAll("[\'\"]", "");
        Model userFound = user.first("login = \"" + login + "\" AND senha = \""
                + generateMD5(password) + "\"");
        if (userFound == null) {
            currentUser = null;
            return false;
        }
        currentUser = (User) userFound;
        return true;
    }

    public static void logout() {
        currentUser = null;
    }

    public static boolean isLoggedIn() {
        return !userProtected || currentUser != null;
    }

    public static boolean currentUserIsAdmin() {
        return !userProtected || (currentUser != null
                && currentUser.get("perfil").equals(ADMIN));
    }
}