package com.example.jon.weatherapp;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.DateFormat;
import java.util.Date;
import java.util.Locale;

public class Function {

    private static final String OPEN_WEATHER_URL = "http://api.openweathermap.org/data/2.5/weather?lat=%s&lon=%s&units=metric";

    private static final String OPEN_WEATHER_API = "make-sure-you-change-to-specific-key";

    //using hexadecimals to specify the icon that I am going to use for the user interface
    //'assets/fonts' folder in ttf format
    public static String setWeatherIcon(int actualId, long sunrise, long sunset){
        int id = actualId / 100;
        String icon = " ";
        if(actualId == 800){
            long currentTime = new Date().getTime();
            if(currentTime>=sunrise && currentTime<sunset) {
                icon = "&#xf00d;";
            } else {
                icon = "&#xf02e;";
            }
        } else {
            switch(id) {
                case 2 : icon = "&#xf01e;";
                    break;
                case 3 : icon = "&#xf01c;";
                    break;
                case 7 : icon = "&#xf014;";
                    break;
                case 8 : icon = "&#xf013;";
                    break;
                case 6 : icon = "&#xf01b;";
                    break;
                case 5 : icon = "&#xf019;";
                    break;
            }
        }
        return icon;
    }


    //setting up interface for abstract class
    public interface AsyncResponse {

        void processFinish(String output1, String output2, String output3, String output4, String output5, String output6, String output7, String output8);
    }




    //setting up threads for UI
    public static class placeIdTask extends AsyncTask<String, Void, JSONObject>{

        //callback interface
        public AsyncResponse delegate = null;

        //assigning callback interface with constructor
        public placeIdTask(AsyncResponse asyncResponse){
            delegate = asyncResponse;
        }

        @Override
        protected JSONObject doInBackground(String... params){

            //initiating jsonWeather variable and assigning null
            JSONObject jsonWeather = null;
            try{
                //calling method and creating 2 strings for weather information
                jsonWeather = getWeatherJSON(params[0], params[1]);
            }
            //catching any errors with json information
            catch (Exception e){
                Log.d("Error", "Cannot process JSON results", e);
            }


            return jsonWeather;
        }

        //converting json object from the API into strings and renaming them
        @Override
        protected void onPostExecute(JSONObject json){
            try{
                if(json != null){
                    JSONObject details = json.getJSONArray("weather").getJSONObject(0);
                    JSONObject main = json.getJSONObject("main");
                    DateFormat df = DateFormat.getDateTimeInstance();


                    String city = json.getString("name").toUpperCase(Locale.US) + ", " + json.getJSONObject("sys").getString("country");
                    String description = details.getString("description").toUpperCase(Locale.US);
                    String temperature = String.format("%.2f", main.getDouble("temp"))+ "°";
                    String humidity = main.getString("humidity") + "%";
                    String pressure = main.getString("pressure") + " hPa";
                    //multiplying by 1000 in order to convert into milliseconds
                    String updatedOn = df.format(new Date(json.getLong("dt")*1000));
                    String iconText = setWeatherIcon(details.getInt("id"),
                            json.getJSONObject("sys").getLong("sunrise") * 1000,
                            json.getJSONObject("sys").getLong("sunset") * 1000);

                    //setting up instance variables
                    delegate.processFinish(city, description, temperature, humidity, pressure, updatedOn, iconText, ""+ (json.getJSONObject("sys").getLong("sunrise") * 1000));

                }
            }
            catch (JSONException e){
                //Log.e(LOG_TAG, "Cannot process JSON results", e);
            }



        }
    }


    public static JSONObject getWeatherJSON(String latitude, String longitude){
        //using try-catch method just incase it returns any errors with connection
        try{
            //initializing  objects to locate the server & send API key
            //and using latitude/longitude in order to establish connection
            URL url = new URL(String.format(OPEN_WEATHER_URL, latitude, longitude));
            HttpURLConnection connect = (HttpURLConnection)url.openConnection();

            //used to add a header to the connection
            connect.addRequestProperty("x-api-key", OPEN_WEATHER_API);

            //setting up reader for the response from the API connection
            BufferedReader read = new BufferedReader(new InputStreamReader(connect.getInputStream()));

            //initializing json variable to be able to append and insert methods
            StringBuffer json = new StringBuffer(1024);
            String temp = " ";

            //saving the response from the API and appending to save in string format
            while((temp = read.readLine()) != null)
                json.append(temp).append("\n");
            //closing connection to openweathermap.org API response
            read.close();

            JSONObject info = new JSONObject(json.toString());

            //checking for the 'cod' that it is anything but 200 for accuracy
            if(info.getInt("cod") != 200){

                return null;
            }
            //if everthing was successful, then it will return weather information with
            //a JSON object
            return info;
            //ignoring any exceptions "empty catch clause"
            }
        catch(Exception e){

            return null;
        }
    }
}
