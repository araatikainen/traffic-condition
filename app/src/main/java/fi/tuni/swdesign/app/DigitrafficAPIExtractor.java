package fi.tuni.swdesign.app;

import java.io.*;
import java.net.HttpURLConnection;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import com.google.gson.*;

import javafx.util.Pair;

/**
 * Class for extracting data from Digitraffic API.

 */
public class DigitrafficAPIExtractor {

    /**
     * Get all the stations from the API.
     * @return ArrayList of station IDs.
     * @throws IOException if the connection fails.
     */
    public static ArrayList<String> getStations() throws IOException {
    
        // Set up the connection to the API endpoint
        URL url = new URL("https://tie.digitraffic.fi/api/weathercam/v1/stations");
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Set headers
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Digitraffic-User", "DT/Tester");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Check if the request was successful (status code 200)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Check if the response is encoded in GZIP
            String encoding = connection.getContentEncoding();
            BufferedReader reader;
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                // If GZIP, decode the content
                reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), StandardCharsets.UTF_8));
            } else {
                // If not GZIP, read the response normally
                reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), StandardCharsets.UTF_8));
            }

            StringBuilder response = new StringBuilder();
            String line;

            while ((line = reader.readLine()) != null) {
                response.append(line);
            }
            reader.close();

            // Parse the response and extract station IDs
            ArrayList<String> ids = new ArrayList<>();

            // Assuming the response is in JSON format
            Gson gson = new Gson();
            JsonArray stationsArray = gson.fromJson(response.toString(), JsonObject.class).getAsJsonArray("features");

            for (JsonElement station : stationsArray) {
                JsonObject stationObject = station.getAsJsonObject();
                ids.add(stationObject.getAsJsonObject("properties").get("id").getAsString());
            }

            return ids;
        } else {
            // Handle error cases
            return null;
        }
    }

    /**
     * Get a JsonObject from the API.
     * @param urlString
     * @return
     * @throws IOException
     */
    private static JsonObject getJsonObjectFromApi(String urlString) throws IOException {
        // Get a JSONObject of the data of one station from the API
        URL url = new URL(urlString);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");

        // Set headers
        connection.setRequestProperty("Accept-Encoding", "gzip");
        connection.setRequestProperty("Digitraffic-User", "DT/Tester");

        // Get the response code
        int responseCode = connection.getResponseCode();

        // Check if the request was successful (status code 200)
        if (responseCode == HttpURLConnection.HTTP_OK) {
            // Check if the response is encoded in GZIP
            String encoding = connection.getContentEncoding();
            if (encoding != null && encoding.equalsIgnoreCase("gzip")) {
                // If GZIP, decode the content
                BufferedReader reader = new BufferedReader(new InputStreamReader(new GZIPInputStream(connection.getInputStream()), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response using Gson with leniency
                Gson gson = new Gson();
                return gson.fromJson(response.toString(), JsonObject.class);
            } else {
                // If not GZIP, handle it accordingly (you can use your existing logic)
                BufferedReader reader = new BufferedReader(new InputStreamReader((connection.getInputStream()), StandardCharsets.UTF_8));
                StringBuilder response = new StringBuilder();
                String line;

                while ((line = reader.readLine()) != null) {
                    response.append(line);
                }
                reader.close();

                // Parse the response using Gson with leniency
                Gson gson = new Gson();
                return gson.fromJson(response.toString(), JsonObject.class);
            }

        } else {
            return null;
        }
    }

    /**
     * Get the data of one station with its ID from the API.
     * @param id
     * @return
     * @throws IOException
     */
    public static JsonObject getStationByID(String id) throws IOException {
        // Get the data of one station with its ID from the API.
        return getJsonObjectFromApi("https://tie.digitraffic.fi/api/weathercam/v1/stations/" + id);
    }

    /**
     * Get the weathercam sensors of one stations with its ID from the API.
     * @param id
     * @return
     * @throws IOException
     */
    public static JsonObject getSensorsByID(String id) throws IOException {

        return getJsonObjectFromApi("https://tie.digitraffic.fi/api/weathercam/v1/stations/" + id + "/data");
    }

    /**
     * Get the traffic sensors of one stations with its ID from the API.
     * @param id
     * @return
     * @throws IOException
     */
    public static JsonArray getTrafficSensorById(String id) throws IOException {
        
        JsonObject trafficData = getJsonObjectFromApi("https://tie.digitraffic.fi/api/tms/v1/stations/" + id + "/data");

        JsonArray trafficSensors = trafficData.get("sensorValues").getAsJsonArray();
        return trafficSensors;
    }   

    /**
     * Get all the weathercam stations of one city from the API.
     * @param city
     * @return
     * @throws IOException
     */
    public static ArrayList<Pair<String, String>> getStationsByCity(String city) throws IOException {
        
        // pair of station id (key) and city name (value)
        
        JsonObject stationsObject = getJsonObjectFromApi("https://tie.digitraffic.fi/api/weathercam/v1/stations");

        ArrayList<Pair<String, String>> stationsInCity = new ArrayList<>();
        
        for (JsonElement station : stationsObject.getAsJsonArray("features")) {
            JsonObject stationObject = station.getAsJsonObject();
            String stationCity = stationObject.getAsJsonObject("properties").get("name").getAsString();
            if (stationCity.contains(city)) {
                
                stationsInCity.add(new Pair<String, String>(stationObject.getAsJsonObject("properties").get("id").getAsString(), stationCity));
            }
        }
        return stationsInCity;
    }

    /**
     * Get the traffic data of one station with its name from the API.
     * @param stationName
     * @return
     * @throws IOException
     */
    public static JsonObject getTrafficDataByStationName(String stationName) throws IOException {
        // get traffic data from sensors by station name
        // fyi they have different ids than weathercam stations

        // there are not many weathercam stations that have traffic sensors
        // return traffic sensor from same city and highway if not found
        // return null if not found same highway

        String urlString = "https://tie.digitraffic.fi/api/tms/v1/stations";
        JsonObject stationsObject = getJsonObjectFromApi(urlString);

        String cityName = stationName.split("_")[0] + "_" + stationName.split("_")[1];

        JsonObject stationTmp = null;

        // find station with same name
        for (JsonElement station : stationsObject.getAsJsonArray("features")) {
            JsonObject stationObject = station.getAsJsonObject();
            String stationNameFromApi = stationObject.getAsJsonObject("properties").get("name").getAsString();

            if( stationNameFromApi.equals(stationName) ){
                    return stationObject;
                }

            if (stationNameFromApi.contains(cityName)) {
                stationTmp = stationObject;
            }
        }

        return stationTmp;
    }

}

