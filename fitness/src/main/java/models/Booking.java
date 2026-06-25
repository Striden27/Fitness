package models;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Booking {

    private int id;
    private int userId;
    private int trainingId;
    private String trainingTitle;
    private String trainerName;
    private LocalDate trainingDate;
    private LocalTime trainingTime;
    private LocalDateTime bookedAt;
    private String status;

    public Booking() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getUserId() { return userId; }
    public void setUserId(int userId) { this.userId = userId; }

    public int getTrainingId() { return trainingId; }
    public void setTrainingId(int trainingId) { this.trainingId = trainingId; }

    public String getTrainingTitle() { return trainingTitle; }
    public void setTrainingTitle(String trainingTitle) { this.trainingTitle = trainingTitle; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public LocalTime getTrainingTime() { return trainingTime; }
    public void setTrainingTime(LocalTime trainingTime) { this.trainingTime = trainingTime; }

    public LocalDateTime getBookedAt() { return bookedAt; }
    public void setBookedAt(LocalDateTime bookedAt) { this.bookedAt = bookedAt; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}