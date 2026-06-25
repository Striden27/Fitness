package models;

import java.time.LocalDate;
import java.time.LocalTime;

public class Training {

    private int id;
    private String title;
    private String trainerName;
    private LocalDate trainingDate;
    private LocalTime trainingTime;
    private int capacity;
    private String description;
    private int bookedCount; // сколько уже записалось — для отображения

    public Training() {}

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getTrainerName() { return trainerName; }
    public void setTrainerName(String trainerName) { this.trainerName = trainerName; }

    public LocalDate getTrainingDate() { return trainingDate; }
    public void setTrainingDate(LocalDate trainingDate) { this.trainingDate = trainingDate; }

    public LocalTime getTrainingTime() { return trainingTime; }
    public void setTrainingTime(LocalTime trainingTime) { this.trainingTime = trainingTime; }

    public int getCapacity() { return capacity; }
    public void setCapacity(int capacity) { this.capacity = capacity; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public int getBookedCount() { return bookedCount; }
    public void setBookedCount(int bookedCount) { this.bookedCount = bookedCount; }

    public String getAvailability() {
        int free = capacity - bookedCount;
        return free + " / " + capacity;
    }
}