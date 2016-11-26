package com.andretissot.java.dao;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import com.andretissot.java.model.User;

/**
 * @author André Augusto Tissot
 */
public abstract class Model {
    public abstract String getTableName();
    protected static boolean userProtected = false;
    protected HashMap<String, Object> attributes = new HashMap<String, Object>();
    private ArrayList<String> errorMessages = new ArrayList<String>();
    public abstract String[] getColumnNames();

    public Object getId() {
        return attributes.get(getIdColumnName());
    }

    public String[][] validatesPresenceOf() {
        return new String[0][0];
    }

    public String[][] validatesDateFormatOf() {
        return new String[0][0];
    }

    public String getIdColumnName() {
        return "id";
    }

    public String getPrimaryKeyCondition() {
        return getIdColumnName() + " = " + getId();
    }

    public Model() {
        for (String column : getColumnNames())
            attributes.put(column, null);
    }

    public boolean loadAttributes() throws Exception {
        String sql = "SELECT * FROM " + getTableName() + " WHERE " + getPrimaryKeyCondition() + " LIMIT 1";
        boolean reloaded = false;
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        if (reloaded = rs.next())
            for (String column : getColumnNames())
                attributes.put(column, rs.getObject(column));
        rs.close();
        stmt.close();
        return reloaded;
    }

    public boolean newRecord() throws Exception {
        if (this.getId() == null || !this.valid())
            return false;
        String sql = "SELECT " + getIdColumnName() + " FROM " + getTableName() + " WHERE " + getPrimaryKeyCondition() + " LIMIT 1";
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        boolean newRecord = !rs.next();
        rs.close();
        stmt.close();
        return newRecord;
    }

    private String prepareValueToQuery(Object value) {
        if (value instanceof String) {
            return "\"" + value + "\"";
        } else if (value instanceof Date) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return "'" + sdf.format((Date) value) + "'";
        }
        return String.valueOf(value);
    }

    private boolean create() throws Exception {
        if (!this.valid())
            return false;
        boolean saved = false, notFirst = false;
        StringBuilder sql = new StringBuilder("INSERT INTO " + getTableName() + " (");
        for (String column : getColumnNames()) {
            if (notFirst)
                sql.append(",");
            sql.append(column);
            notFirst = true;
        }
        sql.append(") VALUES (");
        notFirst = false;
        for (String column : getColumnNames()) {
            if (notFirst)
                sql.append(",");
            if (column == getIdColumnName()) {
                sql.append("default");
            } else {
                sql.append(prepareValueToQuery(attributes.get(column)));
            }
            notFirst = true;
        }
        sql.append(")");
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql.toString());
        stmt.executeUpdate();
        ResultSet rs = stmt.executeQuery("SELECT LAST_INSERT_ID() AS " + getIdColumnName());
        if (saved = rs.next())
            attributes.put(getIdColumnName(), rs.getObject(getIdColumnName()));
        rs.close();
        stmt.close();
        return true;
    }

    private boolean update() throws Exception {
        if (!this.valid())
            return false;
        boolean notFirst = false;
        StringBuilder sql = new StringBuilder("UPDATE " + getTableName() + " SET ");
        for (String column : getColumnNames()) {
            if (notFirst)
                sql.append(",");
            sql.append(column).append("=").append(prepareValueToQuery(attributes.get(column)));
            notFirst = true;
        }
        sql.append(" WHERE ").append(getPrimaryKeyCondition());
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql.toString());
        stmt.executeUpdate();
        stmt.close();
        return true;
    }

    public boolean save() throws Exception {
        if (!User.isLoggedIn())
            throw new Exception("User not logged in");
        if (newRecord())
            return this.create();
        return this.update();
    }

    public boolean delete() throws Exception {
        if (!User.isLoggedIn())
            throw new Exception("User not logged in");
        String sql = new StringBuilder("DELETE FROM ").append(getTableName())
                .append(" WHERE ").append(getPrimaryKeyCondition()).toString();
        PreparedStatement stmt = ConnectionFactory.getConnection()
                .prepareStatement(sql);
        stmt.executeUpdate();
        stmt.close();
        return true;
    }

    public boolean valid() {
        ArrayList<String> messages = new ArrayList<String>();
        for (String[] attributeData : validatesPresenceOf()) {
            String attributeName = attributeData[0];
            Object value = attributes.get(attributeName);
            if (value == null || (value instanceof String
                    && String.valueOf(value).length() == 0))
                messages.add(attributeData[1]);
        }
        for (String[] attributeData : validatesDateFormatOf()) {
            String attributeName = attributeData[0];
            if (!(attributes.get(attributeName) instanceof Date))
                messages.add(attributeData[1]);
        }
        if (messages.isEmpty())
            return true;
        this.errorMessages = messages;
        return false;
    }

    public ArrayList<String> getErrorMessages() {
        return this.errorMessages;
    }

    public Model set(String attributeName, Object value, boolean dontChangeType) {
        if (dontChangeType) {
            attributes.put(attributeName, value);
        } else {
            set(attributeName, value);
        }
        return this;
    }

    public Model set(String attributeName, Object value) {
        if (value instanceof String) {
            SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
            try {
                Date dateValue = dateformat.parse(String.valueOf(value));
                if (dateformat.format(dateValue).equals(value)) {
                    attributes.put(attributeName, dateValue);
                } else {
                    attributes.put(attributeName, value);
                }
            } catch (ParseException ex) {
                attributes.put(attributeName, value);
            }
        } else {
            attributes.put(attributeName, value);
        }
        return this;
    }

    public Object get(String attributeName) {
        return attributes.get(attributeName);
    }

    public String getString(String attributeName) {
        Object value = attributes.get(attributeName);
        if (value == null)
            return "";
        if (value instanceof Date) {
            SimpleDateFormat dateformat = new SimpleDateFormat("dd/MM/yyyy");
            return dateformat.format((Date) value);
        }
        return String.valueOf(value);
    }

    public int getInt(String attributeName) {
        String value = String.valueOf(attributes.get(attributeName));
        return Integer.parseInt(value);
    }

    public ArrayList<Model> all() throws Exception {
        return find(null);
    }

    public Model findById(Object id) throws Exception {
        return this.first(this.getIdColumnName() + " = " + id);
    }

    public Model first(String condition) throws Exception {
        ArrayList<Model> found = this.find(condition + " LIMIT 1");
        if (found.isEmpty())
            return null;
        return found.get(0);
    }

    public ArrayList<Model> find(String condition) throws Exception {
        ArrayList<Model> results = new ArrayList<Model>();
        String sql = "SELECT * FROM " + getTableName();
        if (condition != null && !condition.isEmpty())
            sql += " WHERE " + condition;
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        Class upperClass = getClass();
        Model model;
        while (rs.next()) {
            model = (Model) upperClass.newInstance();
            for (String column : getColumnNames())
                model.set(column, rs.getObject(column));
            results.add(model);
        }
        rs.close();
        stmt.close();
        return results;
    }

    public ArrayList<String[]> executeSql(String sql, String[] resultColumns) throws Exception {
        ArrayList<String[]> results = new ArrayList<String[]>();
        PreparedStatement stmt = ConnectionFactory.getConnection().prepareStatement(sql);
        ResultSet rs = stmt.executeQuery();
        String[] result;
        while (rs.next()) {
            result = new String[resultColumns.length];
            int idx = 0;
            for (String column : getColumnNames())
                result[++idx] = rs.getString(column);
            results.add(result);
        }
        rs.close();
        stmt.close();
        return results;
    }

    public void copyNonBlankAttributesTo(Model model) {
        for (Map.Entry entry : attributes.entrySet()) {
            if ((entry.getValue() != null && (!(entry.getValue() instanceof String)
                    || !entry.getValue().equals("")))
                    && !entry.getKey().equals(getIdColumnName())
                    && model.attributes.containsKey("" + entry.getKey())) {
                model.attributes.put("" + entry.getKey(), entry.getValue());
            }
        }
    }
}
