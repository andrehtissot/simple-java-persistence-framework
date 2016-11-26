package com.andretissot.java.model;

import com.andretissot.java.dao.Model;

/**
 * @author André Augusto Tissot
 */
public class Person extends Model {
    @Override
    public String getTableName() {
        return "people";
    }

    @Override
    public String[] getColumnNames() {
        String[] columnNames = {"id", "user_id", "name", "email", "code",
            "status"};
        return columnNames;
    }
    
    @Override
    public String[][] validatesPresenceOf() {
        String[][] columnNames = {{"name", "Name should be filled"},
            {"email", "Email should be filled"},
            {"code", "Code should be filled"}};
        return columnNames;
    }

    public User user() throws Exception {
        Object user_id = get("user_id");
        if (user_id != null && ((Integer) user_id) > 0) {
            User user = new User();
            user = (User) user.findById(user_id);
            if (user != null) {
                return user;
            }
        }
        return new User();
    }

    @Override
    public String toString() {
        return getString("code") + ": " + getString("name");
    }
}
