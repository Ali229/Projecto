package ali.projecto;

import android.app.Activity;
import android.content.Context;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.RotateAnimation;
import android.widget.ImageView;
import android.widget.TextView;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.net.UnknownHostException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathFactory;

public class Weather {
    //======================================= Properties ===========================================
    double lat, lon;
    String xtemp, xmaxtemp, xmintemp, xcity, xweather, provider, xweatherid;
    Location loc;
    LocationManager mlocManager;
    boolean internet = true;
    Context context;
    //======================================= Constructors =========================================
    public Weather(){
        //Default Constructor
    }
    public Weather(Context context){
        this.context=context;
    }
    //======================================= Start Method =========================================
    public void start() {
        new MyAsyncTask().execute();
    }
    //======================================= AsyncTask ============================================
    public class MyAsyncTask extends AsyncTask<Void, Void, String> {
        //=============================== Async - OnPre ============================================
        @Override
        protected void onPreExecute() {
            runAnimation();
            getDate();
            getLocation();
        }
        //=============================== Async - Background =======================================
        @Override
        protected String doInBackground(Void... params) {
            return loadXML();
        }
        //=============================== Async - OnPost ===========================================
        @Override
        protected void onPostExecute(String result) {
            StringBuilder sb = cloudInfo();
            setTemp(sb, result);
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
        }
        catch (Exception ex) {
            System.err.println("unable to load XML: " + ex);
        }
        return ret;
    }
    //======================================= Changing Picture =====================================
    public void changePicture() {
        try {
            ImageView conView = (ImageView) ((Activity) context).findViewById(R.id.conditionView);
            String x = xweatherid;
            //=============================== Weather Image ====================================
            if (x.startsWith("2")) {
                conView.setImageResource(R.drawable.weather_storm);
            } else if (x.startsWith("3")) {
                conView.setImageResource(R.drawable.weather_drizzle);
            } else if (x.startsWith("5")) {
                conView.setImageResource(R.drawable.weather_rain);
            } else if (x.startsWith("6")) {
                conView.setImageResource(R.drawable.weather_snow);
            } else if (x.equals("800")) {
                conView.setImageResource(R.drawable.weather_sunny);
            } else if (x.startsWith("8")) {
                conView.setImageResource(R.drawable.weather_cloudy);
            } else if (x.equals("900") || x.equals("901") || x.equals("902") || x.equals("903") ||
                    x.equals("904") || x.equals("905") || x.equals("906")) {
                conView.setImageResource(R.drawable.weather_warning);
            } else {
                conView.setImageResource(R.drawable.weather_sunny);
            }
        } catch (Exception e){
            e.printStackTrace();
        }
    }
    //======================================= Load Animation =======================================
    public void runAnimation(){
        try {
            RotateAnimation rn = new RotateAnimation(0, 1080, Animation.RELATIVE_TO_SELF, 0.5f, Animation.RELATIVE_TO_SELF, 0.5f);
            rn.setDuration(3000);
            ImageView i2 = (ImageView) ((Activity) context).findViewById(R.id.loadingView);
            i2.setAnimation(rn);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    //======================================= Setting Date =========================================
    public void getDate() {
        try {
            TextView date = (TextView) ((Activity)context).findViewById(R.id.date);
            date.setText(new SimpleDateFormat("EEEE, dd").format(Calendar.getInstance().getTime()));

        } catch (Exception e) {
            e.printStackTrace();
        }
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
            else{
                //This is what you need:
                //mlocManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 3000, 0, this);
                //mlocManager.requestLocationUpdates(provider, 1000, 0, this);
            }
        } catch (Exception e) {
            Log.e("WeatherApp", "Get Location Exception", e);
        }
    }
    //======================================= Load XML =============================================
    public String loadXML() {
        try {
            internet = true;
            //=============================== Getting Data =====================================
            URL xmlUrl = new URL("http://api.openweathermap.org/data/2.5/weather?lat=" + lat + "&lon=" + lon + "&APPID=a1916e5365462ceb65cfa9bb0606d1d8&units=metric&mode=xml");
            URL xmlUrl2 = new URL("http://api.openweathermap.org/data/2.5/forecast/daily?lat=" + lat + "&lon=" + lon + "&APPID=a1916e5365462ceb65cfa9bb0606d1d8&mode=xml&units=metric&cnt=16");
            InputStream in = xmlUrl.openStream();
            Document doc = parse(in);
            doc.getDocumentElement().normalize();
            InputStream inm = xmlUrl2.openStream();
            Document docm = parse(inm);
            docm.getDocumentElement().normalize();
            //================================ Getting Temperature =============================
            Node nNode = doc.getElementsByTagName("temperature").item(0);
            Element eElement = (Element) nNode;
            double d = Math.round(Double.parseDouble(eElement.getAttribute("value")));
            int dx = (int) d;
            xtemp = Integer.toString(dx) + "°";
            //=============================== Getting current max/min Temperature ==============
            XPath xPath = XPathFactory.newInstance().newXPath();
            String timeStamp = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            String expression = "//time[@day='" + timeStamp + "']/temperature";
            try {
                NodeList nodeList = (NodeList) xPath.compile(expression).evaluate(docm, XPathConstants.NODESET);
                Node nNodem = nodeList.item(0);
                if (nNodem.getNodeType() == Node.ELEMENT_NODE) {
                    Element eElementm = (Element) nNodem;
                    double dmax = Math.round(Double.parseDouble(eElementm.getAttribute("max")));
                    int dxmax = (int) dmax;
                    xmaxtemp = Integer.toString(dxmax);
                    double dmin = Math.round(Double.parseDouble(eElementm.getAttribute("min")));
                    int dxmin = (int) dmin;
                    xmintemp = Integer.toString(dxmin);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            //================================ Getting Clouds ======================================
            Node nNodew = doc.getElementsByTagName("weather").item(0);
            Element eElementw = (Element) nNodew;
            xweather = eElementw.getAttribute("value");
            xweatherid = eElementw.getAttribute("number");
            //================================ Getting City ========================================
            Node nNodec = doc.getElementsByTagName("city").item(0);
            Element eElementc = (Element) nNodec;
            xcity = eElementc.getAttribute("name");
        } catch (UnknownHostException s) {
            internet = false;
        } catch (IOException i) {
            System.out.println("IO Exception error!");
        } catch (Exception ex) {
            ex.printStackTrace();
        }
        return xtemp;
    }
    //======================================= Getting Cloud Info In Title ==========================
    public StringBuilder cloudInfo() {
        StringBuilder sb = new StringBuilder();
        try {
            String[] words = xweather.split(" ");
            if (words[0].length() > 0)
            {
                sb.append(Character.toUpperCase(words[0].charAt(0)) +
                        words[0].subSequence(1, words[0].length()).toString().toLowerCase());
                for (int i = 1; i < words.length; i++)
                {
                    sb.append(" ");
                    sb.append(Character.toUpperCase(words[i].charAt(0)) +
                            words[i].subSequence(1, words[i].length()).toString().toLowerCase());
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sb;
    }
    //======================================= Display Each Value ===================================
    public void setTemp(StringBuilder sb, String result) {
        try {
            TextView temp = (TextView) ((Activity)context).findViewById(R.id.temp);
            TextView minmax = (TextView) ((Activity)context).findViewById(R.id.minmax);
            TextView weather = (TextView) ((Activity)context).findViewById(R.id.weather);
            TextView city = (TextView) ((Activity)context).findViewById(R.id.city);
            TextView timeStamp = (TextView) ((Activity)context).findViewById(R.id.timeStamp);
            if (!internet)
            {
                temp.setText("Oops...");
                weather.setText("Check internet connection!");
                city.setText("");
                timeStamp.setText("");
                minmax.setText("");
                new CountDownTimer(5000, 5000) {
                    public void onTick(long millisUntilFinished) {
                    }
                    public void onFinish() {
                        new MyAsyncTask().execute();
                    }
                }.start();
            }
            else
            {
                temp.setText(result);
                minmax.setText("↑" + xmaxtemp + "     " + xmintemp + "↓");
                weather.setText(sb);
                city.setText(xcity);
                DateFormat up = new SimpleDateFormat("hh:mm a");
                timeStamp.setText("Updated: " + up.format(Calendar.getInstance().getTime()));
                if (city.getText().toString().equals("Earth") || city.getText().toString().equals("")) {
                    try {
                        timeStamp.setText("Waiting for location");
                        new CountDownTimer(5000, 5000) {
                            public void onTick(long millisUntilFinished) {
                            }
                            public void onFinish() {
                                GPSTracker gps;
                                gps = new GPSTracker(context);
                                if (gps.canGetLocation()) {
                                    lat = gps.getLatitude();
                                    lon = gps.getLongitude();
                                } else {
                                    gps.showSettingsAlert();
                                }
                                new MyAsyncTask().execute();
                            }
                        }.start();
                    }
                    catch (Exception ex){
                        ex.printStackTrace();
                    }
                }
                changePicture();
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
}
