package database;

import models.Booking;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookingDAO {

    public void book(int userId, int trainingId) throws Exception {
        Connection conn = DatabaseConnection.getConnection();
        conn.setAutoCommit(false);
        try {
            // Проверяем не записан ли уже
            PreparedStatement check = conn.prepareStatement(
                    "SELECT id FROM bookings WHERE user_id=? AND training_id=? AND status='ACTIVE'");
            check.setInt(1, userId);
            check.setInt(2, trainingId);
            ResultSet rs = check.executeQuery();
            if (rs.next()) throw new Exception("Вы уже записаны на эту тренировку");

            // Проверяем есть ли места
            PreparedStatement cap = conn.prepareStatement(
                    "SELECT capacity, COUNT(b.id) AS cnt FROM trainings t " +
                            "LEFT JOIN bookings b ON b.training_id = t.id AND b.status='ACTIVE' " +
                            "WHERE t.id=? GROUP BY t.capacity");
            cap.setInt(1, trainingId);
            ResultSet rsCap = cap.executeQuery();
            if (rsCap.next() && rsCap.getInt("cnt") >= rsCap.getInt("capacity")) {
                throw new Exception("Мест нет — тренировка заполнена");
            }

            PreparedStatement st = conn.prepareStatement(
                    "INSERT INTO bookings (user_id, training_id) VALUES (?,?)");
            st.setInt(1, userId);
            st.setInt(2, trainingId);
            st.executeUpdate();
            conn.commit();
        } catch (Exception e) {
            conn.rollback();
            throw e;
        } finally {
            conn.setAutoCommit(true);
            conn.close();
        }
    }

    public void cancel(int bookingId) throws Exception {
        String sql = "UPDATE bookings SET status='CANCELLED' WHERE id=?";
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, bookingId);
        st.executeUpdate();
        st.close(); conn.close();
    }

    public List<Booking> getActiveByUser(int userId) throws Exception {
        String sql = """
            SELECT b.id, b.user_id, b.training_id, b.booked_at, b.status,
                   t.title, t.trainer_name, t.training_date, t.training_time
            FROM bookings b
            JOIN trainings t ON b.training_id = t.id
            WHERE b.user_id=? AND b.status='ACTIVE'
            ORDER BY t.training_date, t.training_time
        """;
        Connection conn = DatabaseConnection.getConnection();
        PreparedStatement st = conn.prepareStatement(sql);
        st.setInt(1, userId);
        ResultSet rs = st.executeQuery();
        List<Booking> list = new ArrayList<>();
        while (rs.next()) {
            Booking b = new Booking();
            b.setId(rs.getInt("id"));
            b.setUserId(rs.getInt("user_id"));
            b.setTrainingId(rs.getInt("training_id"));
            b.setTrainingTitle(rs.getString("title"));
            b.setTrainerName(rs.getString("trainer_name"));
            b.setTrainingDate(rs.getDate("training_date").toLocalDate());
            b.setTrainingTime(rs.getTime("training_time").toLocalTime());
            b.setStatus(rs.getString("status"));
            list.add(b);
        }
        rs.close(); st.close(); conn.close();
        return list;
    }
}