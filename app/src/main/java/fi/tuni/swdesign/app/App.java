package fi.tuni.swdesign.app;

import java.io.IOException;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.stage.Stage;

/**
 * Hello world!
 *
 */
public class App extends Application
{

    public App() {

    }

    @Override
    public void start(Stage primaryStage) throws IOException {
        // FXMLLoader mainLoader = new FXMLLoader(
        //     this.getClass().getResource("appGUI.fxml"));
        // mainLoader.setController(new FXMLController());
        // Parent mainParent = mainLoader.load();

        Scene scene = new Scene(new Controller(null).getView());
        primaryStage.setScene(scene);

        primaryStage.setTitle("APP");
        primaryStage.setResizable(false);
        primaryStage.show();
    }

    public static void main( String[] args ) throws RuntimeException
    {
        launch();
    }
}
