package controllers;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import services.AuthService;
import utils.SceneManager;

public class RegisterController {

    @FXML private TextField fullNameField;
    @FXML private TextField emailField;
    @FXML private TextField phoneField;
    @FXML private PasswordField passwordField;

    private final AuthService authService = new AuthService();

    @FXML
    private void register() {
        String fullName = fullNameField.getText().trim();
        String email    = emailField.getText().trim();
        String phone    = phoneField.getText().trim();
        String password = passwordField.getText();

        String emailError = validateEmail(email);
        if (emailError != null) { showError(emailError); return; }

        String passwordError = validatePassword(password);
        if (passwordError != null) { showError(passwordError); return; }

        if (fullName.isEmpty()) { showError("Введите полное имя"); return; }

        try {
            authService.register(fullName, email, password, phone);
            showInfo("Регистрация выполнена! Войдите в систему.");
            SceneManager.switchScene("/views/login.fxml");
        } catch (Exception e) {
            e.printStackTrace();
            showError("Ошибка регистрации. Возможно, email уже занят.");
        }
    }

    private String validateEmail(String email) {
        if (email == null || email.isEmpty())
            return "Email не может быть пустым.";
        if (!email.matches("^[\\w.+-]+@(gmail\\.com|yandex\\.ru)$"))
            return "Email должен быть формата @gmail.com или @yandex.ru";
        return null;
    }

    private String validatePassword(String password) {
        if (password == null || password.length() < 8)
            return "Пароль должен содержать не менее 8 символов.";
        if (!password.matches(".*[a-zA-Z].*"))
            return "Пароль должен содержать хотя бы одну английскую букву.";
        if (!password.matches(".*[0-9].*"))
            return "Пароль должен содержать хотя бы одну цифру.";
        return null;
    }

    @FXML
    private void goToLogin() {
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