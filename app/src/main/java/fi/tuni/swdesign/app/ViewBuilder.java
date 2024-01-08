package fi.tuni.swdesign.app;

import java.io.IOException;
import javafx.fxml.FXMLLoader;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Region;
import javafx.util.Builder;

/**
 * Part of View implementation for MVC.
 * Simple class for building FXML environment.
 */
public class ViewBuilder implements Builder<Region>{


    public ViewBuilder(){
    }

    @Override
    public Region build() {
        FXMLLoader loader = new FXMLLoader(this.getClass().getResource("appGUI.fxml"));
        loader.setController(new FXMLController(null));
        try {
            return loader.load();
        } catch (IOException e) {
            System.out.println("*****Error, returning BorderPane()*****");
            return new BorderPane();
        }
    }
    
}
