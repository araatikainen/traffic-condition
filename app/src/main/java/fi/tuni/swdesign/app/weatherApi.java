package fi.tuni.swdesign.app;

import java.util.ArrayList;
import java.io.IOException;
import java.io.StringReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

/**
 * Fetches weather data from the Finnish Meteorological Institute's open data API
 * 
 */

public class weatherApi {

    /*
     * Example formation for location and time 
     *  String place = "Tampere";
        String startTime = "2023-10-24T00:00:00Z";
        String endTime = "2023-10-24T23:59:59Z";
        Returns data in format hours:temperature:rain (mm)
     */
    
    private static String baseUrl = "http://opendata.fmi.fi/wfs";

    // prediction meaning forecast
    private static String predQueryId = "fmi::forecast::harmonie::surface::point::multipointcoverage";

    // past obersevations meaning data from past
    private static String obsQueryId = "fmi::observations::weather::multipointcoverage";

    // time parameters to get data in 3 hour intervals
    // parameters for temperature and rain
    private static String timeParam = "&timestep=180";
    private static String parameters = "&parameters=temperature,r_1h";

    /**
         * Gets weather observations from the API
         * @param city: city name
         * @param startTime: start time for the data
         * @param endTime: end time for the data
         * @return weatherObs: ArrayList of weather observations in format hours:temperature:rain (mm)
         */
    public static ArrayList<String> getWeatherObsFromApi(String place, String startTime, String endTime) throws IOException{
        
        ArrayList<String> weatherObs = new ArrayList<String>();
        HttpClient client = HttpClient.newHttpClient();

        String queryUrl = baseUrl + "?service=WFS&version=2.0.0&request=getFeature&storedquery_id=" + obsQueryId
                + "&place=" + place + timeParam+ "&starttime=" + startTime + "&endtime=" + endTime+parameters;

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryUrl))
                .GET()
                .build();

        try {

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // create a new DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // create a new document from input stream
            Document doc = builder.parse(new InputSource(new StringReader(response.body())));

            // Find and print the text content of gml:doubleOrNilReasonTupleList
            NodeList doubleOrNilReasonTupleList = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList");
            String data = "";

            if (doubleOrNilReasonTupleList.getLength() > 0) {
                Element doubleOrNilReasonTupleListElement = (Element) doubleOrNilReasonTupleList.item(0);
                data = doubleOrNilReasonTupleListElement.getTextContent();
                
            }
            String [] dataArray = data.trim().split("\\s+");
            

            // get time between start and end time with 3 hour intervals
            // start and end time in format 00:00:00
            String startTimeHour = startTime.split("T")[1].split(":")[0];
            String endTimeHour = endTime.split("T")[1].split(":")[0];

            // days between start and end time
            int daysBetween = Integer.parseInt(endTime.split("T")[0].split("-")[2]) - Integer.parseInt(startTime.split("T")[0].split("-")[2]) + 1;
            

            // Weather data is in 3 hour intervals
            // save only the data that is between start and end time
            
            int timeBetween = daysBetween * (Integer.parseInt(endTimeHour) - Integer.parseInt(startTimeHour));
            ArrayList<String> intervals = new ArrayList<String>();
            for(int i = 0; i < timeBetween; i++){
                if(i % 3 == 0){
                    intervals.add(Integer.toString(i));
                }
                
            }
            System.out.println(intervals.toString());
            
            // document always has even number of elements
            // save in String time:temperature:rain

            for(int i = 0; i < dataArray.length; i++){
                if(i % 2 == 0){
                    String temp = dataArray[i] + ":" + dataArray[i+1];
                    weatherObs.add(temp);
                }
                
            }


            if(weatherObs.size() == intervals.size()){
                for(int i = 0; i < weatherObs.size(); i++){
                    weatherObs.set(i, intervals.get(i) + ":" + weatherObs.get(i));
                }
            }
            
        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
        return weatherObs;
    }
    
    /**
         * Gets weather predictions from the API
         * default for 36 hours with hour intervals
         * @param place: city name
         * 
         * @return weatherPre: ArrayList of weather predictions in format hours:temperature:rain:windspeed
         */
    public static ArrayList<String> getWeatherPreFromApi(String place) throws IOException{
        
        String parameters = "&parameters=temperature,precipitation1h,windspeedms";
        ArrayList<String> weatherObs = new ArrayList<String>();
        HttpClient client = HttpClient.newHttpClient();

        String queryUrl = baseUrl + "?service=WFS&version=2.0.0&request=getFeature&storedquery_id=" + predQueryId
                + "&place=" + place + parameters;


        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(queryUrl))
                .GET()
                .build();
        
        try{
            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

            // create a new DocumentBuilderFactory
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            DocumentBuilder builder = factory.newDocumentBuilder();

            // create a new document from input stream
            Document doc = builder.parse(new InputSource(new StringReader(response.body())));

            // Find and print the text content of gml:doubleOrNilReasonTupleList
            NodeList doubleOrNilReasonTupleList = doc.getElementsByTagName("gml:doubleOrNilReasonTupleList");
            String data = "";

            if (doubleOrNilReasonTupleList.getLength() > 0) {
                Element doubleOrNilReasonTupleListElement = (Element) doubleOrNilReasonTupleList.item(0);
                data = doubleOrNilReasonTupleListElement.getTextContent();
                
            }
            String [] dataArray = data.trim().split("\\s+");
            
            // document always has even number of elements
            // save in String time:temperature:rain::windspeed
            int hourIndex = 0;
            for(int i = 0; i < dataArray.length; i++){
                if(i % 3 == 0){
                    String temp = Integer.toString(hourIndex) + ":" + dataArray[i] + ":" + dataArray[i+1]+ ":" + dataArray[i+2];
                    weatherObs.add(temp);
                    hourIndex++;
                }
                
            }


        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }


        return weatherObs;
    }
    
}
