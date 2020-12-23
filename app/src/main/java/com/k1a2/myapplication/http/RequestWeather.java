package com.k1a2.myapplication.http;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;

import com.k1a2.myapplication.MainActivity;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.BitSet;
import java.util.Calendar;

public class RequestWeather extends AsyncTask<String, String, Boolean> {

    private Context context = null;
    String urlLink = "";
    Bitmap bitmap = null;
    Double currentTemp = 0d;
    Double currentLike = 0d;
    Double humidity = 0d;
    Double wind = 0d;

    public RequestWeather(Context context) {
        this.context = context;
    }

    @Override
    protected Boolean doInBackground(String... strings) {
        String url = "http://api.openweathermap.org/data/2.5/weather?lat=37.7109&lon=126.7345&appid=f6d26c4aa2f2422325427bbfb57c40ba";
        URL urlCon = null;
        try {
            urlCon = new URL(url);
            HttpURLConnection conn = (HttpURLConnection) urlCon.openConnection();
            conn.setDoOutput(true);
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            conn.setRequestMethod("POST");
//                OutputStreamWriter osw = new OutputStreamWriter(conn.getOutputStream());
//                sendMsg = "id=njk"+"&pw=q";
//                osw.write(sendMsg);
//                osw.flush();
            if(conn.getResponseCode() == conn.HTTP_OK) {
                InputStreamReader tmp = new InputStreamReader(conn.getInputStream(), "UTF-8");
                BufferedReader reader = new BufferedReader(tmp);
                StringBuffer buffer = new StringBuffer();
                String str;
                while ((str = reader.readLine()) != null) {
                    buffer.append(str);
                }

                JSONObject jsonObject = new JSONObject(buffer.toString());
                JSONObject weathers = jsonObject.getJSONObject("main");
                JSONArray weatherIC = jsonObject.getJSONArray("weather");

                currentTemp = (weathers.getDouble("temp") - 273.15);
                Log.d("Current temp", currentTemp + "C");

                currentLike = weathers.getDouble("feels_like") - 273.15;
                Log.d("Current temp like", currentLike + "C");

                humidity = weathers.getDouble("humidity");

                wind = jsonObject.getJSONObject("wind").getDouble("speed");

                urlLink = "http://openweathermap.org/img/wn/" + weatherIC.getJSONObject(0).getString("icon") + "@2x.png";
                URL url1 = new URL(urlLink);
                HttpURLConnection conns = (HttpURLConnection)url1.openConnection();
                conns.setDoInput(true);
                conns.connect();

                InputStream is = conns.getInputStream();
                bitmap = BitmapFactory.decodeStream(is);


                return true;
            } else {
                Log.i("통신 결과", conn.getResponseCode()+"에러");
                return false;
            }


        } catch (Exception e) {
            e.printStackTrace();

            return false;
        }
    }

    @Override
    protected void onPostExecute(Boolean s) {
        if (s) {
            ((MainActivity) context).setWeatherInfo(bitmap, currentTemp, currentLike, humidity, wind);
        } else {

        }
    }
}
