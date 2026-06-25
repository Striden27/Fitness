package database;

import models.Membership;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class MembershipDAO {

    public List<Membership> getAll() throws Exception {
        String sql = "SELECT * FROM memberships ORDER BY price";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        List<Membership> list = new ArrayList<>();
        while (rs.next()) list.add(mapMembership(rs));
        rs.close(); st.close(); conn.close();
        return list;
    }

    public Membership findById(int id) throws Exception {
        String sql = "SELECT * FROM memberships WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, id);
        ResultSet rs = st.executeQuery();
        Membership m = null;
        if (rs.next()) m = mapMembership(rs);
        rs.close(); st.close(); conn.close();
        return m;
    }

    public void save(Membership m) throws Exception {
        String sql = "INSERT INTO memberships (name, description, duration_days, price) VALUES (?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, m.getName());
        st.setString(2, m.getDescription());
        st.setInt(3, m.getDurationDays());
        st.setBigDecimal(4, m.getPrice());
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void update(Membership m) throws Exception {
        String sql = "UPDATE memberships SET name=?, description=?, duration_days=?, price=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, m.getName());
        st.setString(2, m.getDescription());
        st.setInt(3, m.getDurationDays());
        st.setBigDecimal(4, m.getPrice());
        st.setInt(5, m.getId());
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM memberships WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, id);
        st.executeUpdate();
        st.close(); conn.close();
    }

    private Membership mapMembership(ResultSet rs) throws Exception {
        Membership m = new Membership();
        m.setId(rs.getInt("id"));
        m.setName(rs.getString("name"));
        m.setDescription(rs.getString("description"));
        m.setDurationDays(rs.getInt("duration_days"));
        m.setPrice(rs.getBigDecimal("price"));
        return m;
    }
}