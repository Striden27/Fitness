package database;

import models.User;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class UserDAO {

    public User findByEmail(String email) throws Exception {
        String sql = "SELECT * FROM users WHERE email = ?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, email);
        ResultSet rs = st.executeQuery();
        User user = null;
        if (rs.next()) user = mapUser(rs);
        rs.close(); st.close(); conn.close();
        return user;
    }

    public List<User> getAllUsers() throws Exception {
        String sql = "SELECT * FROM users WHERE role = 'USER' ORDER BY full_name";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        List<User> list = new ArrayList<>();
        while (rs.next()) list.add(mapUser(rs));
        rs.close(); st.close(); conn.close();
        return list;
    }

    public void save(User user) throws Exception {
        String sql = "INSERT INTO users (full_name, email, password, phone, birth_date, role) " +
                "VALUES (?, ?, ?, ?, ?, 'USER')";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, user.getFullName());
        st.setString(2, user.getEmail());
        st.setString(3, user.getPassword());
        st.setString(4, user.getPhone());
        st.setDate(5, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void update(User user) throws Exception {
        String sql = "UPDATE users SET full_name=?, phone=?, birth_date=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, user.getFullName());
        st.setString(2, user.getPhone());
        st.setDate(3, user.getBirthDate() != null ? Date.valueOf(user.getBirthDate()) : null);
        st.setInt(4, user.getId());
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void updatePassword(int userId, String hashedPassword) throws Exception {
        String sql = "UPDATE users SET password=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, hashedPassword);
        st.setInt(2, userId);
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void assignMembership(int userId, int membershipId,
                                 java.time.LocalDate start,
                                 java.time.LocalDate end) throws Exception {
        String sql = "UPDATE users SET membership_id=?, membership_start=?, membership_end=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, membershipId);
        st.setDate(2, Date.valueOf(start));
        st.setDate(3, Date.valueOf(end));
        st.setInt(4, userId);
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void delete(int userId) throws Exception {
        String sql = "DELETE FROM users WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, userId);
        st.executeUpdate();
        st.close(); conn.close();
    }

    private User mapUser(ResultSet rs) throws Exception {
        User u = new User();
        u.setId(rs.getInt("id"));
        u.setFullName(rs.getString("full_name"));
        u.setEmail(rs.getString("email"));
        u.setPassword(rs.getString("password"));
        u.setPhone(rs.getString("phone"));
        u.setRole(rs.getString("role"));
        Date bd = rs.getDate("birth_date");
        if (bd != null) u.setBirthDate(bd.toLocalDate());
        int mid = rs.getInt("membership_id");
        if (!rs.wasNull()) u.setMembershipId(mid);
        Date ms = rs.getDate("membership_start");
        if (ms != null) u.setMembershipStart(ms.toLocalDate());
        Date me = rs.getDate("membership_end");
        if (me != null) u.setMembershipEnd(me.toLocalDate());
        return u;
    }
}