package fi.tuni.swdesign.app;

import java.io.IOException;
import javafx.scene.layout.Region;
import javafx.util.Pair;

/**
 * The controller class of MVC implementation.
 */
public class Controller {
    private final ViewBuilder ViewBuilder;
    private Model model = new Model();
    private FXMLController FXMLController;
    
    /**
     * Constructor for controller class.
     * @param view current instance of FXMLController.
     */
    public Controller(FXMLController view){
        this.ViewBuilder = new ViewBuilder(/*model.getWeatherList(), model.getTrafficData()*/);
        this.FXMLController = view;
    }

    /**
     * Sends information collected from the UI to Model.
     * Retirieves information from Model and updates UI elements accordingly.
     * @param stationData infromation about station in pair element (ID: name).
     * @throws IOException if updateTrafficData() fails.
     */
    public void updateStationData(Pair<String, String> stationData) throws IOException{
        model.updateTrafficData(stationData);
        FXMLController.displayTrafficInformation(model.getTrafficData());
    }

    /**
     * 
     * @param name
     * @throws IOException
     */
    public void updateWeatherData(String name) throws IOException{
        model.updateWeather(name);
        FXMLController.displayWeatherInformation(model.getWeatherList());
    }

    /**
     * Updates the emoji data in the model and displays the updated emoji in the UI.
     */
    public void searchStations(String searchKey){
        FXMLController.displaySearchResults(model.getStationsByKey(searchKey));
    }

    /**
     * Updates the emoji data in the model and displays the updated emoji in the UI.
     */
    public void updateEmoji(){
        model.updateEmoji();
        FXMLController.displayEmoji(model.getEmoji());
    }

    /**
     * Retrieves and returns the view as a JavaFX Region by utilizing the ViewBuilder.
     *
     * @return The JavaFX Region representing the built view.
     */
    public Region getView(){
        return ViewBuilder.build();
    }
}
