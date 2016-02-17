package com.festivent.hardeep.festivent;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class Eventbrite {

    private static final String TAG_EVENTS = "events";
    private static final String TAG_NAME = "name";
    private static final String TAG_TEXT = "text";
    private static final String TAG_DESCRIPTION = "description";
    private static final String TAG_URL = "url";
    private static final String TAG_START = "start";
    private static final String TAG_LOCAL = "local";
    private static final String TAG_END = "end";
    private static final String TAG_LOGO = "logo";


    public static JSONObject data;
    public static ArrayList<EventInfo> eventbriteEvents = new ArrayList<EventInfo>();
    public static JSONArray eventbriteJSONArray = null;

    public static String getData(String latitude, String longitude, String distance, String page, int increment) {
        String link = "https://www.eventbriteapi.com/v3/events/search/?" +
                "location.within=" + distance +
                "&location.latitude=" + latitude +
                "&location.longitude=" + longitude +
                "&start_date.range_start=" + getEventBriteDate() +
                "Z&page=" + page +
                "&start_date.range_end=" + getEventBriteDateIncrement(increment) +
                "Z&token=PZDYIE3MVNSM5Z6EI3B6";
        try {
            URL url = new URL(link);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            conn.setRequestProperty("accept", "application/json");
            conn.connect();

            BufferedReader br = new BufferedReader(
                    new InputStreamReader(conn.getInputStream()));
            String output;
            String result = "";
            while ((output = br.readLine()) != null) {
                result = result + output;
            }
            conn.disconnect();
            return result;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public static ArrayList<EventInfo> getDateArray(String apiData) {
        eventbriteEvents.clear();
        try {
            data = new JSONObject(apiData);
            if(data != null) {
                //Get JSON Array node
                eventbriteJSONArray = data.getJSONArray(TAG_EVENTS);

                //loop through each event
                for (int i = 0; i < eventbriteJSONArray.length(); i++) {
                    JSONObject e = eventbriteJSONArray.getJSONObject(i);

                    //get name object from  list
                    JSONObject name = e.getJSONObject(TAG_NAME);
                    String eventName = name.getString(TAG_TEXT);

                    JSONObject description = e.getJSONObject(TAG_DESCRIPTION);
                    String desc = description.getString(TAG_TEXT);
                    String url = e.getString(TAG_URL);
                    if(desc == null) {
                        desc = "No Description";
                    }

                    if(url == null) {
                        url = "Website not provided";
                    }

                    //get startTime object
                    JSONObject start = e.getJSONObject(TAG_START);
                    String startTime = start.getString(TAG_LOCAL);

                    //get endTime Object
                    JSONObject end = e.getJSONObject(TAG_END);
                    String endTime = end.getString(TAG_LOCAL);

                    //get logo object
                    String imageUrl;
                    if (!e.isNull("logo")) {
                        JSONObject logo = e.getJSONObject(TAG_LOGO);
                        imageUrl = logo.getString(TAG_URL);
                    } else {
                        imageUrl = null;
                    }

                    if(!checkDuplicate(eventbriteEvents, eventName)) {
                        eventbriteEvents.add(new EventInfo(eventName, desc, startTime, endTime, url, imageUrl, 0.0, 0.0, "EventBrite"));
                    }
                }
            }
        } catch (Exception e) {
            Log.e("eventbrite_error", e.toString());
        }
        return eventbriteEvents;
    }

    public static boolean checkDuplicate(ArrayList<EventInfo> list,  String title) {
        for(EventInfo eventInfo: list) {
            if (eventInfo.getEventName().equals(title)) {
                return true;
            }
        }
        return  false;
    }

    public static String getEventBriteDate() {
        String result = "";
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        result += sdf.format(new Date());
        return result;
    }

    public static String getEventBriteDateIncrement(int increment) {
        String result = "";
        String current = getEventBriteDate();
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.US);
        Calendar c = Calendar.getInstance();
        try {
            c.setTime(sdf.parse(current));
            c.add(Calendar.DATE, increment);
            result += sdf.format(c.getTime());
        } catch (ParseException e) {
            e.printStackTrace();
        }
        return result;
    }
}