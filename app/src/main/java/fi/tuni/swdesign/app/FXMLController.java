package fi.tuni.swdesign.app;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.ResourceBundle;

import fi.tuni.swdesign.app.Model.Weather;
import fi.tuni.swdesign.app.Model.digiTraffic;
import javafx.application.Platform;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Button;
import javafx.scene.control.ChoiceBox;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.util.Pair;


/*
 * Part of View implementation for MVC.
 * Handles all user inputs from UI through FXML-implementation.
 */
public class FXMLController implements Initializable {

    private Controller controller;

    @FXML
    private Button quitBtn = new Button();

    @FXML
    private Button searchBtn = new Button();

    @FXML
    private TextField searchBar = new TextField();

    @FXML
    private ChoiceBox<String> resultsMenu = new ChoiceBox<>();

    @FXML
    private Button resultsBtn = new Button();

    @FXML
    private Label stationLabel = new Label();

    @FXML
    private Label emojiLabel = new Label();

    @FXML
    private ImageView trafficCam = new ImageView();

    @FXML
    private ListView<String> trafficList = new ListView<>();

    @FXML 
    private ListView<String> weatherListView = new ListView<>();

    /**
     * Constructor for FXMLController class.
     * @param controller current instance of controller.
     */
    public FXMLController(Controller controller){
        this.controller =  new Controller(this);
    }   

    /**
     * Overrides the initialize method from the Initializable interface.
     * This method is called automatically when the associated FXML file is loaded.
     * Does any necessary preparations for UI; sets values etc.
     *
     * @param location   The URL of the FXML file. Can be used to determine the file's location,
     *                   but is often not explicitly used in the implementation.
     * @param resources  The ResourceBundle containing localized resources for the FXML file.
     *                   It provides access to localized strings and other resources.
     */
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        // Implementation here.
    }

    /**
     * Associated with FXML-quit button.
     * Exits the program when pressed.
     */
    @FXML
    private void quitProgram() {
        Platform.exit();
    }

    /**
     * Associated with the searchBtn -element in FXML.
     * Initiates searches in controller based on SearchBar contents.
     */
    @FXML
    private void searchStation() {
        String searchKey = searchBar.getText();
        String formatKey = searchKey.substring(0,1).toUpperCase() + searchKey.substring(1).toLowerCase();
        System.out.println(formatKey);

        controller.searchStations(formatKey);
    }

    /**
     * Adds the result of cities search to resultsMenu.
     * @param resultList an arraylist of cities found in search.
     */
    public void displaySearchResults(ArrayList<Pair<String, String>> resultList){
        ObservableList<String> menuItems = resultsMenu.getItems();
        menuItems.clear();
        // Inserts results of search to resultsMenu in UI.
        for (int i = 0; i < resultList.size(); i++){
            Pair<String, String> station = resultList.get(i);
            menuItems.add(station.getValue() + ": " + station.getKey());
        }
    }

    /**
     * Associated with resultsBtn.
     * Sends the information in  about the user selection in resultsMenu
     * to controller to initiate the display of information.
     * @throws IOException if updateWeatherData() or updateTrafficData() fails.
     */
    @FXML
    private void displayInformation() throws IOException{
        
        String station = resultsMenu.getValue();

        // Finds the name of the city from selected value:
        int startIndex = station.indexOf('_');
        int endIndex = station.indexOf('_', startIndex + 1);
        String place = "null";

        if (startIndex != -1 && endIndex != -1 && startIndex < endIndex) {
            place = station.substring(startIndex + 1, endIndex);
        } else {
            // Handle the case where there are no or only one underscore
            System.out.println("No text between underscores");
        }
        System.out.println(place);
        
        // Send the data to controller for handling weather information.
        controller.updateWeatherData(place);
        
        String stationName = station.substring(0, station.length()-8);
        String stationID = station.substring(station.length()-6);

        // Make a pair of station data and send it to controller for handling traffic information.
        Pair<String, String> stationData = new Pair<>(stationID, stationName);
        controller.updateStationData(stationData);

        // Sends a request to controller to update emoji.
        controller.updateEmoji();
    }

    /**
     * Adds necessary information about traffic conditions to FXML elements.
     * @param station a digiTraffic object containing information about desired station.
     */
    public void displayTrafficInformation(digiTraffic station){

        stationLabel.setText(station.getStationName());
        String url = station.getTrafficCam();
        System.out.println(url);
        trafficCam.setImage(new Image(url));

        // Listview Information:
        ObservableList<String> listItems = trafficList.getItems();
        listItems.clear();

        listItems.add(String.format("%-35s %s km/h", "Keskimääränopeus: ", station.getAvgSpeedKhm()));
        listItems.add(String.format("%-35s %s %%", "Keskimääränopeus / nopeusrajoitus: ", station.getAvgSpeedCars()));
        listItems.add(String.format("%-35s %s kpl", "Autojen lkm / tunti: ", station.getTrafficCars()));
    }

    /**
     * Adds becessary information about weather conditions to FXML elements,
     * @param weatherList an arraylist containing weather information of the next few hours.
     */
    public void displayWeatherInformation(ArrayList<Weather> weatherList){
        weatherListView.getItems().clear();

        ObservableList<String> listItems = weatherListView.getItems();
        String header = String.format("%-13s %-13s %-13s %-13s", 
                                     "Kellonaika",
                                     "Lämpötila",
                                     "Sademäärä",
                                     "Tuulennopeus");
        listItems.add(header);

        // Iterates through all weather-elements and adds information to ListView.
        for (Weather weatherInfo : weatherList){
            String weatherInfoString = String.format("%-13s %-13s %-13s %-13s",
                                                     weatherInfo.getTime() + " h",
                                                     weatherInfo.getTemperature() + " °C",
                                                     weatherInfo.getRain() + " mm",
                                                     weatherInfo.getWindpeed() + " m/s");
            listItems.add(weatherInfoString);
        }

    }
    
    /**
     * Adds necessary information to emojiLabel demonstrating weather coditions,
     * @param emoji a string, either ":)", ":|" or ":(" depending on weather conditions.
     */
    public void displayEmoji(String emoji){
        emojiLabel.setText(emoji);
    }

}
