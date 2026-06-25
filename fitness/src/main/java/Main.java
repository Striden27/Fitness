import javafx.application.Application;
import javafx.stage.Stage;
import utils.SceneManager;

public class Main extends Application {

    @Override
    public void start(Stage stage) {
        stage.setTitle("Фитнес-клуб");
        stage.setWidth(900);
        stage.setHeight(600);
        stage.setResizable(true);
        SceneManager.setStage(stage);
        SceneManager.switchScene("/views/login.fxml");
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}