package fi.tuni.swdesign.app;

import java.io.IOException;
import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import javafx.util.Pair;
import java.lang.Math;


/**
 * The model class of MVC implementation.
 */
public class Model {

    private ArrayList<Weather> weatherList;

    private digiTraffic traffic;
    private String emoji;

    /**
     * Constructor for model class.
     */
    public Model(){
        weatherList = new ArrayList<Weather>();
        traffic = new digiTraffic();
    }

    /**
     * Updates weather data in the model.
     * @param place
     * @throws IOException
     */
    public void updateWeather(String place) throws IOException {

        try{

            ArrayList<String> weatherdata = weatherApi.getWeatherPreFromApi(place);

            // weatherdata contains data in format hours:temperature:rain:windspeed (4 elements) with intervals of 1 hour
            // weatherdata has 36 hours of data
            // calculate avg temperature and rain for 3 hour intervals for the next 6*3hours = 18hours and update them to weatherList

            // current time
            DateFormat dateFormat = new SimpleDateFormat("HH:mm");
            Date date = new Date(System.currentTimeMillis());
            
            ArrayList<Weather> weatherListTemp = new ArrayList<Weather>();

            
            for(int i = 0; i < weatherdata.size()/(4*2); i++) {

                Weather tempWeather = new Weather();
            
                // add 3 hours to current time
                String time = dateFormat.format(date.getTime() + (i*3 * 60 * 60 * 1000));


                float temperatureValue = ((Float.parseFloat(weatherdata.get(i).split(":")[1]) +
                        Float.parseFloat(weatherdata.get(i + 1).split(":")[1])) +
                        Float.parseFloat(weatherdata.get(i + 2).split(":")[1])) / 3f;

                float rainValue = ((Float.parseFloat(weatherdata.get(i).split(":")[2]) +
                        Float.parseFloat(weatherdata.get(i + 1).split(":")[2])) +
                        Float.parseFloat(weatherdata.get(i + 2).split(":")[2])) / 3f;

                float windspeedValue = ((Float.parseFloat(weatherdata.get(i).split(":")[3]) +
                        Float.parseFloat(weatherdata.get(i + 1).split(":")[3])) +
                        Float.parseFloat(weatherdata.get(i + 2).split(":")[3])) / 3f;

                String temperature = String.format("%.1f", temperatureValue);
                String rain = String.format("%.1f", rainValue);
                String windspeed = String.format("%.1f", windspeedValue);

                tempWeather.setWeather(place, time, temperature, rain, windspeed);
                weatherListTemp.add(tempWeather);
                
            }
        
        this.weatherList = weatherListTemp;
        }
        catch (Exception e) {
            System.out.println(e);
        }


    }

    /**
     * Updates traffic data in the model.
     * @param station
     * @throws IOException
     */
    public void updateTrafficData(Pair<String, String> station) throws IOException {


        try{

            String stationName = station.getValue();
            
            JsonObject trafficCamObj = DigitrafficAPIExtractor.getStationByID(station.getKey());
            
            String trafficCam = trafficCamObj.getAsJsonObject("properties").get("presets").getAsJsonArray().get(0).getAsJsonObject().get("imageUrl").getAsString();
            
            JsonObject trafficData = DigitrafficAPIExtractor.getTrafficDataByStationName(stationName);
            

            if(trafficData == null){
                System.out.println("Traffic data not found");
                this.traffic.setTrafficData(trafficCam, stationName, "No data", "No data", "No data");
                return;
            }

            
            // get sensovalues from trafficData
            String trafficCars = ""; // id = 5054
            String avgSpeedCars = ""; // id=5158
            String avgSpeedKhm = ""; // id=5056

            JsonArray trafficSensors = DigitrafficAPIExtractor.getTrafficSensorById(trafficData.getAsJsonObject("properties").get("id").getAsString());
            
            for(JsonElement sensor : trafficSensors){
                if(sensor.getAsJsonObject().get("id").getAsString().equals("5054")){
                    trafficCars = String.format("%.0f", Float.parseFloat(sensor.getAsJsonObject().get("value").getAsString()));
                }
                else if(sensor.getAsJsonObject().get("id").getAsString().equals("5158")){
                    avgSpeedCars = String.format("%.0f", Float.parseFloat(sensor.getAsJsonObject().get("value").getAsString()));
                }
                else if(sensor.getAsJsonObject().get("id").getAsString().equals("5056")){
                    avgSpeedKhm = String.format("%.0f", Float.parseFloat(sensor.getAsJsonObject().get("value").getAsString()));
                }
            }  
            
            this.traffic.setTrafficData(trafficCam, stationName, trafficCars, avgSpeedCars, avgSpeedKhm);

        }
        catch (Exception e) {
            System.out.println(e);
        }


    }

    /**
     * Updates emoji data in the model.
     * Algorithm is provided by ChatGPT
     */
    public void updateEmoji() {

        Weather weather = this.weatherList.get(0);
                    
            
        // weights for each parameter
        double weightTemp = 0.2;
        double weightRain = 0.3;
        double weightWind = 0.3;
        double weightAvgSpeedCars = 2;
            
        // calculate weighted score for each parameter
        double temperature = Double.parseDouble(weather.getTemperature());
        double rain = Double.parseDouble(weather.getRain());
        double windSpeed = Double.parseDouble(weather.getWindpeed());

        double avgSpeedCars = 0.0;
        
        if(this.traffic.getAvgSpeedCars() != "No data"){
            avgSpeedCars = Double.parseDouble(this.traffic.getAvgSpeedCars());
        }
        else{
            avgSpeedCars = 100.0;
        }
                    
        // Considering very high or very low temperatures as bad
        double scoreTemp = temperature >= 25 || temperature <= -15 ? -weightTemp : Math.abs(temperature * weightTemp);
            
        // High rain is considered bad, no rain is good
        double scoreRain = rain >= 30 ? -weightRain : rain * weightRain;
            
        // High wind is considered bad, no wind is good
        double scoreWind = windSpeed >= 15 ? -weightWind : windSpeed * weightWind;
            
        // Lower average speed of cars is worse within the 0-100% range
        double normalizedAvgSpeedCars = avgSpeedCars / 100.0; // Normalize between 0 and 1
        double scoreAvgSpeedCars = normalizedAvgSpeedCars * weightAvgSpeedCars;
            
        // calculate total score
        double score = (scoreTemp + scoreRain + scoreWind) + scoreAvgSpeedCars;
            
        // Determine driving condition based on score thresholds
        
        if (score >= 2.5) {
            this.emoji = ":)";
        } else if (score >= 1.5) {
            this.emoji = ":|";
        } else {
            this.emoji = ":(";
        }

                
    }

    /**
     * Returns a list of stations in a city.
     * @param city
     * @return
     */
    public ArrayList<Pair<String, String>> getStationsByKey(String city){
        ArrayList<Pair<String, String>> cityList = new ArrayList<>();
        try {
            cityList = DigitrafficAPIExtractor.getStationsByCity(city);
        } catch (IOException e) {
            
            System.out.print("Retrieving stations by city failed.");
            e.printStackTrace();
        }
        return cityList;
    }

    /**
     * Returns a list of weather data.
     * @return
     */
    public ArrayList<Weather> getWeatherList() {
        return weatherList;
    }

    /**
     * Returns traffic data.
     * @return
     */
    public digiTraffic getTrafficData() {
        return traffic;
    }

    /**
     * Returns emoji data.
     * @return
     */
    public String getEmoji() {
        return emoji;
    }

    /**
     * Sets emoji data.
     * @param emoji
     */
    public void setEmoji(String emoji) {
        this.emoji = emoji;
    }

    /**
     * Class for weather data.
     * @param weatherList
     */
    public static class Weather {
       
        // ehk lisää windspeed
        private String place; 
        private String time; 
        private String temperature;
        private String rain;
        private String windpeed;

        /**
         * Constructor for weather class.
         * Sets all the parameters default value of "No data".
         */
        public Weather(){
            this.place = "No data";
            this.time = "No data";
            this.temperature = "No data";
            this.rain = "No data";
            this.windpeed = "No data";
        }

        /**
         * Sets weather data.
         * @param place
         * @param time
         * @param temperature
         * @param rain
         * @param windpeed
         */
        public void setWeather( String place, String time, String temperature, String rain, String windpeed){
            this.place = place;
            this.time = time;
            this.temperature = temperature;
            this.rain = rain;
            this.windpeed = windpeed;
        }

        /**
         * Returns weather data.
         * @return
         */
        public String getPlace() {
            return place;
        }

        /**
         * Returns weather data.
         * @return
         */
        public String getTime() {
            return time;
        }   

        /**
         * Returns weather data.
         * @return
         */
        public String getTemperature() {
            return temperature;
        }

        /**
         * Returns weather data.
         * @return
         */
        public String getRain() {
            return rain;
        }

        /**
         * Returns weather data.
         * @return
         */
        public String getWindpeed() {
            return windpeed;
        }
        
    }

    /**
     * Class for traffic data.
     */
    public static class digiTraffic {

       
        private String trafficCam; //jpg
        private String stationName; // name of the station example "vt3_Tampere_Hervanta"
        private String trafficCars; // kpl/h  id = 5054 and 5055
        private String avgSpeedCars; // % amount of speed limit, id=5158 and 5161
        private String avgSpeedKhm; // km/h for one hour, id=5056 and 5057
        
        /**
         * Constructor for traffic class.
         * Sets all the parameters default value of "No data".
         */
        public digiTraffic(){
            this.trafficCam = "No data";
            this.stationName = "No data";
            this.trafficCars = "No data";
            this.avgSpeedCars = "No data";
            this.avgSpeedKhm = "No data";
            
        }

        /**
         * Sets traffic data.
         * @param trafficCam
         * @param stationName
         * @param trafficCars
         * @param avgSpeedCars
         * @param avgSpeedKhm
         */
        public void setTrafficData(String trafficCam, String stationName, String trafficCars, String avgSpeedCars, String avgSpeedKhm) {
            this.trafficCam = trafficCam;
            this.stationName = stationName;
            this.trafficCars = trafficCars;
            this.avgSpeedCars = avgSpeedCars;
            this.avgSpeedKhm = avgSpeedKhm;
        }

        /**
         * Returns traffic data.
         * @return
         */
        public String getTrafficCam() {
            return trafficCam;
        }

        /**
         * Returns traffic data.
         * @return
         */
        public String getStationName() {
            return stationName;
        }

        /**
         * Returns traffic data.
         * @return
         */
        public String getTrafficCars() {
            return trafficCars;
        }

        /**
         * Returns traffic data.
         * @return
         */
        public String getAvgSpeedKhm() {
            return avgSpeedKhm;
        }

        /**
         * Returns traffic data.
         * @return
         */
        public String getAvgSpeedCars() {
            return avgSpeedCars;
        }

    }

}
