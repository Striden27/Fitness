package controllers;

import database.BookingDAO;
import database.MembershipDAO;
import database.TrainingDAO;
import database.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import models.Booking;
import models.Membership;
import models.Training;
import models.User;
import org.mindrot.jbcrypt.BCrypt;
import utils.SceneManager;
import utils.Session;

import java.util.List;

public class MainController {

    // Профиль
    @FXML private Label userLabel;
    @FXML private TextField profileName;
    @FXML private Label profileEmail;
    @FXML private TextField profilePhone;
    @FXML private PasswordField oldPasswordField;
    @FXML private PasswordField newPasswordField;
    @FXML private Label membershipLabel;
    @FXML private Label membershipDatesLabel;

    // Расписание
    @FXML private TextField searchField;
    @FXML private TableView<Training> trainingsTable;
    @FXML private TableColumn<Training, String> tTitleCol;
    @FXML private TableColumn<Training, String> tTrainerCol;
    @FXML private TableColumn<Training, String> tDateCol;
    @FXML private TableColumn<Training, String> tTimeCol;
    @FXML private TableColumn<Training, String> tSlotsCol;
    @FXML private TableColumn<Training, String> tDescCol;

    // Мои записи
    @FXML private TableView<Booking> bookingsTable;
    @FXML private TableColumn<Booking, String> bTitleCol;
    @FXML private TableColumn<Booking, String> bTrainerCol;
    @FXML private TableColumn<Booking, String> bDateCol;
    @FXML private TableColumn<Booking, String> bTimeCol;
    @FXML private TableColumn<Booking, String> bStatusCol;

    private final TrainingDAO trainingDAO  = new TrainingDAO();
    private final BookingDAO  bookingDAO   = new BookingDAO();
    private final UserDAO     userDAO      = new UserDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();

    @FXML
    public void initialize() {
        User user = Session.getCurrentUser();
        userLabel.setText("👤  " + user.getFullName());

        // Профиль
        profileName.setText(user.getFullName());
        profileEmail.setText(user.getEmail());
        profilePhone.setText(user.getPhone() != null ? user.getPhone() : "");

        // Абонемент
        loadMembershipInfo(user);

        // Колонки расписания
        tTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        tTrainerCol.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        tDateCol.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        tTimeCol.setCellValueFactory(new PropertyValueFactory<>("trainingTime"));
        tSlotsCol.setCellValueFactory(new PropertyValueFactory<>("availability"));
        tDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));

        // Колонки записей
        bTitleCol.setCellValueFactory(new PropertyValueFactory<>("trainingTitle"));
        bTrainerCol.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        bDateCol.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        bTimeCol.setCellValueFactory(new PropertyValueFactory<>("trainingTime"));
        bStatusCol.setCellValueFactory(new PropertyValueFactory<>("status"));

        loadTrainings();
    }

    private void loadMembershipInfo(User user) {
        if (user.getMembershipId() == null) {
            membershipLabel.setText("Абонемент не оформлен");
            membershipDatesLabel.setText("");
            return;
        }
        try {
            Membership m = membershipDAO.findById(user.getMembershipId());
            if (m != null) {
                membershipLabel.setText(m.getName() + " — " + m.getPrice() + " ₽");
                membershipDatesLabel.setText(
                        "Действует: " + user.getMembershipStart() +
                                " → " + user.getMembershipEnd());
            }
        } catch (Exception e) {
            membershipLabel.setText("Ошибка загрузки абонемента");
        }
    }

    @FXML
    public void loadTrainings() {
        try {
            List<Training> list = trainingDAO.getAll();
            trainingsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка загрузки расписания: " + e.getMessage());
        }
    }

    @FXML
    private void searchTrainings() {
        String keyword = searchField.getText().trim();
        if (keyword.isEmpty()) { loadTrainings(); return; }
        try {
            List<Training> list = trainingDAO.searchByTitle(keyword);
            trainingsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка поиска: " + e.getMessage());
        }
    }

    @FXML
    private void onTrainingsTab() {
        loadTrainings();
    }

    @FXML
    private void bookTraining() {
        Training selected = trainingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите тренировку"); return; }
        try {
            bookingDAO.book(Session.getCurrentUser().getId(), selected.getId());
            loadTrainings();
            showInfo("Вы успешно записаны на «" + selected.getTitle() + "»!");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void onBookingsTab() {
        try {
            List<Booking> list = bookingDAO.getActiveByUser(Session.getCurrentUser().getId());
            bookingsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка загрузки записей: " + e.getMessage());
        }
    }

    @FXML
    private void cancelBooking() {
        Booking selected = bookingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите запись для отмены"); return; }
        try {
            bookingDAO.cancel(selected.getId());
            onBookingsTab();
            loadTrainings();
            showInfo("Запись отменена.");
        } catch (Exception e) {
            showError(e.getMessage());
        }
    }

    @FXML
    private void saveProfile() {
        String name  = profileName.getText().trim();
        String phone = profilePhone.getText().trim();
        if (name.isEmpty()) { showError("Имя не может быть пустым"); return; }
        try {
            User user = Session.getCurrentUser();
            user.setFullName(name);
            user.setPhone(phone);
            userDAO.update(user);
            userLabel.setText("👤  " + name);
            showInfo("Профиль обновлён!");
        } catch (Exception e) {
            showError("Ошибка сохранения: " + e.getMessage());
        }
    }

    @FXML
    private void changePassword() {
        String oldPass = oldPasswordField.getText();
        String newPass = newPasswordField.getText();

        if (oldPass.isEmpty() || newPass.isEmpty()) {
            showError("Заполните оба поля пароля");
            return;
        }
        if (!BCrypt.checkpw(oldPass, Session.getCurrentUser().getPassword())) {
            showError("Текущий пароль введён неверно");
            return;
        }
        if (newPass.length() < 8 || !newPass.matches(".*[a-zA-Z].*") || !newPass.matches(".*[0-9].*")) {
            showError("Новый пароль: минимум 8 символов, буква и цифра");
            return;
        }
        try {
            String hashed = BCrypt.hashpw(newPass, BCrypt.gensalt());
            userDAO.updatePassword(Session.getCurrentUser().getId(), hashed);
            Session.getCurrentUser().setPassword(hashed);
            oldPasswordField.clear();
            newPasswordField.clear();
            showInfo("Пароль успешно изменён!");
        } catch (Exception e) {
            showError("Ошибка смены пароля: " + e.getMessage());
        }
    }

    @FXML
    private void logout() {
        Session.clear();
        SceneManager.switchScene("/views/login.fxml");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }

    private void showInfo(String msg) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("Успешно");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}