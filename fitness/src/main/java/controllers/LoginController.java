package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.AuthService;
import utils.SceneManager;
import utils.Session;

public class LoginController {

    @FXML private TextField emailField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    private void login() {
        String email = emailField.getText().trim();
        String password = passwordField.getText();

        if (email.isEmpty() || password.isEmpty()) {
            showError("Заполните все поля");
            return;
        }

        try {
            boolean success = authService.login(email, password);
            if (success) {
                if (Session.isAdmin()) {
                    SceneManager.switchScene("/views/admin.fxml");
                } else {
                    SceneManager.switchScene("/views/main.fxml");
                }
            } else {
                showError("Неверный email или пароль");
            }
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка подключения к базе данных");
        }
    }

    @FXML
    private void goToRegister() {
        SceneManager.switchScene("/views/register.fxml");
    }

    private void showError(String msg) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Ошибка");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}