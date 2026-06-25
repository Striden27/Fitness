package controllers;

import database.MembershipDAO;
import database.TrainingDAO;
import database.UserDAO;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.layout.GridPane;
import models.Membership;
import models.Training;
import models.User;
import utils.SceneManager;
import utils.Session;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public class AdminController {

    @FXML private TableView<User> usersTable;
    @FXML private TableColumn<User, String> uNameCol;
    @FXML private TableColumn<User, String> uEmailCol;
    @FXML private TableColumn<User, String> uPhoneCol;
    @FXML private TableColumn<User, String> uMemberCol;
    @FXML private TableColumn<User, String> uMemberEndCol;

    @FXML private TableView<Training> adminTrainingsTable;
    @FXML private TableColumn<Training, String>  atTitleCol;
    @FXML private TableColumn<Training, String>  atTrainerCol;
    @FXML private TableColumn<Training, String>  atDateCol;
    @FXML private TableColumn<Training, String>  atTimeCol;
    @FXML private TableColumn<Training, Integer> atCapCol;
    @FXML private TableColumn<Training, Integer> atSlotsCol;

    @FXML private TableView<Membership> membershipsTable;
    @FXML private TableColumn<Membership, String>  mNameCol;
    @FXML private TableColumn<Membership, String>  mDescCol;
    @FXML private TableColumn<Membership, Integer> mDaysCol;
    @FXML private TableColumn<Membership, String>  mPriceCol;

    private final UserDAO       userDAO       = new UserDAO();
    private final TrainingDAO   trainingDAO   = new TrainingDAO();
    private final MembershipDAO membershipDAO = new MembershipDAO();

    @FXML
    public void initialize() {
        uNameCol.setCellValueFactory(new PropertyValueFactory<>("fullName"));
        uEmailCol.setCellValueFactory(new PropertyValueFactory<>("email"));
        uPhoneCol.setCellValueFactory(new PropertyValueFactory<>("phone"));
        uMemberEndCol.setCellValueFactory(new PropertyValueFactory<>("membershipEnd"));

        uMemberCol.setCellFactory(col -> new TableCell<User, String>() {
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || getTableRow() == null || getTableRow().getItem() == null) {
                    setText(null);
                } else {
                    User u = getTableRow().getItem();
                    if (u.getMembershipId() == null) {
                        setText("—");
                    } else {
                        try {
                            Membership m = membershipDAO.findById(u.getMembershipId());
                            setText(m != null ? m.getName() : "—");
                        } catch (Exception e) {
                            setText("—");
                        }
                    }
                }
            }
        });

        atTitleCol.setCellValueFactory(new PropertyValueFactory<>("title"));
        atTrainerCol.setCellValueFactory(new PropertyValueFactory<>("trainerName"));
        atDateCol.setCellValueFactory(new PropertyValueFactory<>("trainingDate"));
        atTimeCol.setCellValueFactory(new PropertyValueFactory<>("trainingTime"));
        atCapCol.setCellValueFactory(new PropertyValueFactory<>("capacity"));
        atSlotsCol.setCellValueFactory(new PropertyValueFactory<>("bookedCount"));

        mNameCol.setCellValueFactory(new PropertyValueFactory<>("name"));
        mDescCol.setCellValueFactory(new PropertyValueFactory<>("description"));
        mDaysCol.setCellValueFactory(new PropertyValueFactory<>("durationDays"));
        mPriceCol.setCellValueFactory(new PropertyValueFactory<>("price"));

        loadUsers();
    }

    // ===== ПОЛЬЗОВАТЕЛИ =====

    private void loadUsers() {
        try {
            List<User> list = userDAO.getAllUsers();
            usersTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка загрузки пользователей: " + e.getMessage());
        }
    }

    @FXML
    private void assignMembership() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите пользователя"); return; }

        try {
            List<Membership> memberships = membershipDAO.getAll();
            List<String> choices = memberships.stream()
                    .map(m -> m.getName() + " (" + m.getDurationDays() + " дн.) — " + m.getPrice() + " ₽")
                    .toList();

            ChoiceDialog<String> dialog = new ChoiceDialog<>(choices.get(0), choices);
            dialog.setTitle("Назначить абонемент");
            dialog.setHeaderText("Пользователь: " + selected.getFullName());
            dialog.setContentText("Выберите абонемент:");

            Optional<String> result = dialog.showAndWait();
            result.ifPresent(choiceStr -> {
                int index = choices.indexOf(choiceStr);
                Membership m = memberships.get(index);
                try {
                    LocalDate start = LocalDate.now();
                    LocalDate end   = start.plusDays(m.getDurationDays());
                    userDAO.assignMembership(selected.getId(), m.getId(), start, end);
                    loadUsers();
                    showInfo("Абонемент «" + m.getName() + "» назначен до " + end);
                } catch (Exception e) {
                    showError("Ошибка назначения: " + e.getMessage());
                }
            });
        } catch (Exception e) {
            showError("Ошибка: " + e.getMessage());
        }
    }

    @FXML
    private void deleteUser() {
        User selected = usersTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите пользователя"); return; }

        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Подтверждение");
        confirm.setContentText("Удалить пользователя «" + selected.getFullName() + "»?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    userDAO.delete(selected.getId());
                    loadUsers();
                    showInfo("Пользователь удалён.");
                } catch (Exception e) {
                    showError("Ошибка удаления: " + e.getMessage());
                }
            }
        });
    }

    // ===== ТРЕНИРОВКИ =====

    @FXML
    private void onTrainingsAdminTab() {
        try {
            List<Training> list = trainingDAO.getAll();
            adminTrainingsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка загрузки тренировок: " + e.getMessage());
        }
    }

    @FXML
    private void addTraining() {
        Dialog<Training> dialog = buildTrainingDialog(null);
        dialog.showAndWait().ifPresent(t -> {
            try {
                trainingDAO.save(t);
                onTrainingsAdminTab();
                showInfo("Тренировка добавлена!");
            } catch (Exception e) {
                showError("Ошибка: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editTraining() {
        Training selected = adminTrainingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите тренировку"); return; }
        Dialog<Training> dialog = buildTrainingDialog(selected);
        dialog.showAndWait().ifPresent(t -> {
            try {
                t.setId(selected.getId());
                trainingDAO.update(t);
                onTrainingsAdminTab();
                showInfo("Тренировка обновлена!");
            } catch (Exception e) {
                showError("Ошибка: " + e.getMessage());
            }
        });
    }

    @FXML
    private void deleteTraining() {
        Training selected = adminTrainingsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите тренировку"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Удалить тренировку «" + selected.getTitle() + "»?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    trainingDAO.delete(selected.getId());
                    onTrainingsAdminTab();
                    showInfo("Тренировка удалена.");
                } catch (Exception e) {
                    showError("Ошибка: " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Training> buildTrainingDialog(Training existing) {
        Dialog<Training> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить тренировку" : "Редактировать тренировку");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 10;");

        TextField  titleF   = new TextField(existing != null ? existing.getTitle() : "");
        TextField  trainerF = new TextField(existing != null ? existing.getTrainerName() : "");
        DatePicker dateF    = new DatePicker(existing != null ? existing.getTrainingDate() : LocalDate.now().plusDays(1));
        TextField  timeF    = new TextField(existing != null ? existing.getTrainingTime().toString() : "10:00");
        TextField  capF     = new TextField(existing != null ? String.valueOf(existing.getCapacity()) : "10");
        TextField  descF    = new TextField(existing != null ? existing.getDescription() : "");

        titleF.setPrefWidth(220);
        trainerF.setPrefWidth(220);
        timeF.setPrefWidth(220);
        capF.setPrefWidth(220);
        descF.setPrefWidth(220);

        grid.add(new Label("Название:"),      0, 0); grid.add(titleF,   1, 0);
        grid.add(new Label("Тренер:"),        0, 1); grid.add(trainerF, 1, 1);
        grid.add(new Label("Дата:"),          0, 2); grid.add(dateF,    1, 2);
        grid.add(new Label("Время (HH:MM):"), 0, 3); grid.add(timeF,    1, 3);
        grid.add(new Label("Мест:"),          0, 4); grid.add(capF,     1, 4);
        grid.add(new Label("Описание:"),      0, 5); grid.add(descF,    1, 5);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                Training t = new Training();
                t.setTitle(titleF.getText().trim());
                t.setTrainerName(trainerF.getText().trim());
                t.setTrainingDate(dateF.getValue());
                t.setTrainingTime(LocalTime.parse(timeF.getText().trim()));
                t.setCapacity(Integer.parseInt(capF.getText().trim()));
                t.setDescription(descF.getText().trim());
                return t;
            } catch (Exception e) {
                showError("Проверьте данные. Время в формате HH:MM");
                return null;
            }
        });
        return dialog;
    }

    // ===== АБОНЕМЕНТЫ =====

    @FXML
    private void onMembershipsTab() {
        try {
            List<Membership> list = membershipDAO.getAll();
            membershipsTable.setItems(FXCollections.observableArrayList(list));
        } catch (Exception e) {
            showError("Ошибка загрузки абонементов: " + e.getMessage());
        }
    }

    @FXML
    private void addMembership() {
        Dialog<Membership> dialog = buildMembershipDialog(null);
        dialog.showAndWait().ifPresent(m -> {
            try {
                membershipDAO.save(m);
                onMembershipsTab();
                showInfo("Абонемент добавлен!");
            } catch (Exception e) {
                showError("Ошибка: " + e.getMessage());
            }
        });
    }

    @FXML
    private void editMembership() {
        Membership selected = membershipsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите абонемент"); return; }
        Dialog<Membership> dialog = buildMembershipDialog(selected);
        dialog.showAndWait().ifPresent(m -> {
            try {
                m.setId(selected.getId());
                membershipDAO.update(m);
                onMembershipsTab();
                showInfo("Абонемент обновлён!");
            } catch (Exception e) {
                showError("Ошибка: " + e.getMessage());
            }
        });
    }

    @FXML
    private void deleteMembership() {
        Membership selected = membershipsTable.getSelectionModel().getSelectedItem();
        if (selected == null) { showError("Выберите абонемент"); return; }
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setContentText("Удалить абонемент «" + selected.getName() + "»?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                try {
                    membershipDAO.delete(selected.getId());
                    onMembershipsTab();
                    showInfo("Абонемент удалён.");
                } catch (Exception e) {
                    showError("Ошибка удаления: " + e.getMessage());
                }
            }
        });
    }

    private Dialog<Membership> buildMembershipDialog(Membership existing) {
        Dialog<Membership> dialog = new Dialog<>();
        dialog.setTitle(existing == null ? "Добавить абонемент" : "Редактировать абонемент");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setStyle("-fx-padding: 10;");

        TextField nameF  = new TextField(existing != null ? existing.getName() : "");
        TextField descF  = new TextField(existing != null ? existing.getDescription() : "");
        TextField daysF  = new TextField(existing != null ? String.valueOf(existing.getDurationDays()) : "30");
        TextField priceF = new TextField(existing != null ? existing.getPrice().toString() : "");

        nameF.setPrefWidth(220);
        descF.setPrefWidth(220);
        daysF.setPrefWidth(220);
        priceF.setPrefWidth(220);

        grid.add(new Label("Название:"), 0, 0); grid.add(nameF,  1, 0);
        grid.add(new Label("Описание:"), 0, 1); grid.add(descF,  1, 1);
        grid.add(new Label("Дней:"),     0, 2); grid.add(daysF,  1, 2);
        grid.add(new Label("Цена (₽):"), 0, 3); grid.add(priceF, 1, 3);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                Membership m = new Membership();
                m.setName(nameF.getText().trim());
                m.setDescription(descF.getText().trim());
                m.setDurationDays(Integer.parseInt(daysF.getText().trim()));
                m.setPrice(new java.math.BigDecimal(priceF.getText().trim()));
                return m;
            } catch (Exception e) {
                showError("Проверьте введённые данные");
                return null;
            }
        });
        return dialog;
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