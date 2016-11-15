package ali.projecto;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.RemoteViews;
import android.widget.Toast;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;
import java.util.logging.Level;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

public class WidgetReceiver extends BroadcastReceiver {
    //=============================== Properties ===================================================
    double lat, lon;
    String xtemp, xcity, xweather, xweatherid, provider, cloud;
    Location loc;
    LocationManager mlocManager;
    boolean internet, location;
    //=============================== OnReceive Method =============================================
    @Override
    public void onReceive(Context context, Intent intent) {
        try {
            if (intent.getAction().equals("update")) {
                new WidgetWeather(context).execute();
            }
        } catch (Exception e){
            Log.e("WeatherApp", "onReceive", e);
        }
    }
    //======================================= AsyncTask ============================================
    public class WidgetWeather extends AsyncTask <Void, Void, String> {
        private RemoteViews views;
        private Context context;
        public WidgetWeather(Context ctx){
            // Now set context
            context = ctx;
            views = new RemoteViews(context.getPackageName(), R.layout.widget);
        }
        //=============================== Async - OnPre ============================================
        @Override
        protected void onPreExecute() {
            try {
            //views = new RemoteViews(context.getPackageName(), R.layout.widget);
            runAnimation();
            } catch (Exception e) {
                Log.e("WeatherApp", "onPre Exception", e);
            }
        }
        //=============================== Async - Background =======================================
        @Override
        protected String doInBackground(Void... params) {
            //protected String doInBackground(Context... params) {
            try {
                //context = params[0];
                //views = new RemoteViews(context.getPackageName(), R.layout.widget);
                //runAnimation();
                //stopAnimation();
                getLocation();
            } catch (Exception e) {
                Log.e("WeatherApp", "Background Exception", e);
            }
            return loadXML2();
        }
        //=============================== Async - OnPost ===========================================
        @Override
        protected void onPostExecute(String result) {
            //Toast toast = Toast.makeText(context, result, Toast.LENGTH_SHORT);
            //toast.show();
            try {
                if (result != null && result.length() >= 1) {
                    views.setTextViewText(R.id.widTemp, result);
                    views.setTextViewText(R.id.widCity, xcity);
                    String cloud = cloudInfo();
                    views.setTextViewText(R.id.widWeather, cloud);
                    DateFormat up = new SimpleDateFormat("hh:mm a", Locale.getDefault());
                    views.setTextViewText(R.id.widTimeStamp, up.format(Calendar.getInstance().getTime()));
                    views.setOnClickPendingIntent(R.id.rButton, Widget.buildButtonPendingIntent(context));
                    Widget.pushWidgetUpdate(context.getApplicationContext(), views);
                    if (!location) {
                        Toast toast = Toast.makeText(context, "Location Unvailable", Toast.LENGTH_LONG);
                        toast.show();
                    }
                }
                else {
                    Toast toast = Toast.makeText(context, "Network Unavailable", Toast.LENGTH_LONG);
                    toast.show();
                    /*new CountDownTimer(10000, 10000) {
                        public void onTick(long millisUntilFinished) {
                        }
                        public void onFinish() {
                            this.cancel();
                            if (!internet){
                                Toast toast = Toast.makeText(context, "Network Unavailable", Toast.LENGTH_LONG);
                                toast.show();
                            }
                            new WidgetWeather(context).execute();
                        }
                    }.start();*/
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "onPost Exception", e);
            } finally {
                stopAnimation();
            }
        }
        @Override
        protected void onCancelled() {
            super.onCancelled();
        }
        //=============================== Run Animation ============================================
        public void runAnimation() {
            try {
                views.setViewVisibility(R.id.rButton, View.GONE);
                views.setViewVisibility(R.id.pBar, View.VISIBLE);
                views.setOnClickPendingIntent(R.id.rButton, Widget.buildButtonPendingIntent(context));
                Widget.pushWidgetUpdate(context.getApplicationContext(), views);
            } catch (Exception e) {
                Log.e("WeatherApp", "Run Animation Exception", e);
            }
        }
        //=============================== Stop Animation ===========================================
        public void stopAnimation() {
            try {
                //new CountDownTimer(2500, 2500) {
                    //public void onTick(long millisUntilFinished) {
                    //}
                   // public void onFinish() {
                        views.setViewVisibility(R.id.rButton, View.VISIBLE);
                        views.setViewVisibility(R.id.pBar, View.GONE);
                        views.setOnClickPendingIntent(R.id.rButton, Widget.buildButtonPendingIntent(context));
                        Widget.pushWidgetUpdate(context.getApplicationContext(), views);
                    //}
                //}.start();
            } catch (Exception e) {
                Log.e("WeatherApp", "Stop Animation Exception", e);
            }
        }

        //=============================== Image & Weather Name =====================================
        public String cloudInfo() {
            try {
                String x = xweatherid;
                //=============================== Weather Image ====================================
                if (x.startsWith("2")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_storm);
                } else if (x.startsWith("3")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_drizzle);
                } else if (x.startsWith("5")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_rain);
                } else if (x.startsWith("6")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_snow);
                } else if (x.equals("800")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_sunny);
                } else if (x.startsWith("8")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_cloudy);
                } else if (x.equals("900") || x.equals("901") || x.equals("902") || x.equals("903") ||
                        x.equals("904") || x.equals("905") || x.equals("906")) {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_warning);
                } else {
                    views.setImageViewResource(R.id.widCondition, R.drawable.weather_sunny);
                }
            } catch (Exception e){
                Log.e("WeatherApp", "Weather Image Exception", e);
            }
            //=============================== Capitalize Weather Name ==============================
            StringBuilder sb = new StringBuilder();
            String[] words = xweather.split(" ");
            try {
                if (words[0].length() > 0) {
                    sb.append(Character.toUpperCase(words[0].charAt(0)) +
                            words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                    for (int i = 1; i < words.length; i++) {
                        sb.append(" ");
                        sb.append(Character.toUpperCase(words[i].charAt(0)) +
                                words[i].subSequence(1, words[i].length()).toString().toLowerCase());
                    }
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "Capitalize Weather Name Exception", e);
            }
                cloud = sb.toString();
                return cloud;
        }
        //======================================= Location =============================================
        public void getLocation() {
            try {
                mlocManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                criteria.setAltitudeRequired(false);
                criteria.setBearingRequired(false);
                criteria.setCostAllowed(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                provider = mlocManager.getBestProvider(criteria, true);
                if (provider != null) {
                    loc = mlocManager.getLastKnownLocation(provider);
                    lat = loc.getLatitude();
                    lon = loc.getLongitude();
                }
            } catch (Exception e) {
                Log.e("WeatherApp", "Get Location Exception", e);
            }
        }
        //======================================= Load XML =============================================
        public String loadXML2() {
            try {
                //=============================== Getting Data =========================================
                URL xmlUrl = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + lat +
                        "&lon=" + lon + "&APPID=a1916e5365462ceb65cfa9bb0606d1d8&units=metric&mode=xml");
                InputStream in = xmlUrl.openStream();
                Document doc = parse(in);
                doc.getDocumentElement().normalize();
                //================================ Getting Temperature =================================
                Node nNode = doc.getElementsByTagName("temperature").item(0);
                Element eElement = (Element) nNode;
                double d = Math.round(Double.parseDouble(eElement.getAttribute("value")));
                int dx = (int) d;
                xtemp = Integer.toString(dx) + "°";
                Node nNodec = doc.getElementsByTagName("city").item(0);
                Element eElementc = (Element) nNodec;
                xcity = eElementc.getAttribute("name");
                //================================ Getting Clouds ======================================
                Node nNodew = doc.getElementsByTagName("weather").item(0);
                Element eElementw = (Element) nNodew;
                xweather = eElementw.getAttribute("value");
                xweatherid = eElementw.getAttribute("number");
                internet = true;
                location = true;
                if (xcity.equals("Earth"))
                {
                    location=false;
                }
            }  catch (UnknownHostException h) {
                internet = false;
                Log.e("WeatherApp", "Network Unavailable", h);
            } catch (Exception e) {
                Log.e("WeatherApp", "Load XML Exception", e);
            }
            return xtemp;
        }
    }
    //======================================= Document Parsing =====================================
    public static Document parse (InputStream is) {
        Document ret = null;
        DocumentBuilderFactory domFactory;
        DocumentBuilder builder;
        try {
            domFactory = DocumentBuilderFactory.newInstance();
            domFactory.setValidating(false);
            domFactory.setNamespaceAware(false);
            builder = domFactory.newDocumentBuilder();

            ret = builder.parse(is);
        } catch (Exception e) {
            Log.e("WeatherApp", "Document Parse Exception", e);
        }
        return ret;
    }
}

