package database;

import models.Training;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class TrainingDAO {

    public List<Training> getAll() throws Exception {
        String sql = """
            SELECT t.*, COUNT(b.id) AS booked_count
            FROM trainings t
            LEFT JOIN bookings b ON b.training_id = t.id AND b.status = 'ACTIVE'
            WHERE t.training_date >= CURRENT_DATE
            GROUP BY t.id
            ORDER BY t.training_date, t.training_time
        """;
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        ResultSet rs = st.executeQuery();
        List<Training> list = new ArrayList<>();
        while (rs.next()) list.add(mapTraining(rs));
        rs.close(); st.close(); conn.close();
        return list;
    }

    public List<Training> searchByTitle(String keyword) throws Exception {
        String sql = """
            SELECT t.*, COUNT(b.id) AS booked_count
            FROM trainings t
            LEFT JOIN bookings b ON b.training_id = t.id AND b.status = 'ACTIVE'
            WHERE t.training_date >= CURRENT_DATE
              AND LOWER(t.title) LIKE LOWER(?)
            GROUP BY t.id
            ORDER BY t.training_date, t.training_time
        """;
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, "%" + keyword + "%");
        ResultSet rs = st.executeQuery();
        List<Training> list = new ArrayList<>();
        while (rs.next()) list.add(mapTraining(rs));
        rs.close(); st.close(); conn.close();
        return list;
    }

    public void save(Training t) throws Exception {
        String sql = "INSERT INTO trainings (title, trainer_name, training_date, training_time, capacity, description) " +
                "VALUES (?,?,?,?,?,?)";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, t.getTitle());
        st.setString(2, t.getTrainerName());
        st.setDate(3, Date.valueOf(t.getTrainingDate()));
        st.setTime(4, Time.valueOf(t.getTrainingTime()));
        st.setInt(5, t.getCapacity());
        st.setString(6, t.getDescription());
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void update(Training t) throws Exception {
        String sql = "UPDATE trainings SET title=?, trainer_name=?, training_date=?, " +
                "training_time=?, capacity=?, description=? WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setString(1, t.getTitle());
        st.setString(2, t.getTrainerName());
        st.setDate(3, Date.valueOf(t.getTrainingDate()));
        st.setTime(4, Time.valueOf(t.getTrainingTime()));
        st.setInt(5, t.getCapacity());
        st.setString(6, t.getDescription());
        st.setInt(7, t.getId());
        st.executeUpdate();
        st.close(); conn.close();
    }

    public void delete(int id) throws Exception {
        String sql = "DELETE FROM trainings WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, id);
        st.executeUpdate();
        st.close(); conn.close();
    }

    private Training mapTraining(ResultSet rs) throws Exception {
        Training t = new Training();
        t.setId(rs.getInt("id"));
        t.setTitle(rs.getString("title"));
        t.setTrainerName(rs.getString("trainer_name"));
        t.setTrainingDate(rs.getDate("training_date").toLocalDate());
        t.setTrainingTime(rs.getTime("training_time").toLocalTime());
        t.setCapacity(rs.getInt("capacity"));
        t.setDescription(rs.getString("description"));
        t.setBookedCount(rs.getInt("booked_count"));
        return t;
    }
}