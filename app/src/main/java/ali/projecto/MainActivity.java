package ali.projecto;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.renderscript.Allocation;
import android.renderscript.Element;
import android.renderscript.RenderScript;
import android.renderscript.ScriptIntrinsicBlur;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.WindowManager;
import android.widget.ScrollView;


public class MainActivity extends ActionBarActivity implements SwipeRefreshLayout.OnRefreshListener {
    public Weather w1 = new Weather(this);
    private SwipeRefreshLayout mSwipeRefreshLayout;

    //=========================================== On Create ========================================
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*try {
            if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1){
                Bitmap bm = BitmapFactory.decodeResource(getResources(), R.drawable.background_cloudy);
                Bitmap v = BlurBuilder.blur(this, bm);
                BitmapDrawable vm = new BitmapDrawable(getResources(), v);

                ScrollView ll = (ScrollView) findViewById(R.id.scrollView);
                ll.setBackground(vm);
            }
        }catch (Exception e)
        {
            Log.e("WeatherApp", "Dim Error", e);
        }*/
        w1.start();
        mSwipeRefreshLayout = (SwipeRefreshLayout) findViewById(R.id.swipeRefreshLayout);
        mSwipeRefreshLayout.setColorSchemeResources(android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light);
        mSwipeRefreshLayout.setOnRefreshListener(this);
    }
    @Override
    public void onRefresh() {
        w1.start();
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                mSwipeRefreshLayout.setRefreshing(false);
            }
        }, 2000);
    }
    //=========================================== On Resume ========================================
    @Override protected void onResume() {
        super.onResume();
        w1.start();
    }
    //=========================================== On Pause ========================================
    @Override protected void onPause() {
        super.onPause();
    }

    //=========================================== Create Menu ======================================
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }
    //=========================================== Menu Functions ===================================
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                return true;
            case R.id.action_refresh:
                w1.start();
                return true;
            default:
                return false;
        }
    }
    private Bitmap downscaleBitmapUsingDensities(final int sampleSize,final int imageResId)
    {
        final BitmapFactory.Options bitmapOptions=new BitmapFactory.Options();
        bitmapOptions.inDensity=sampleSize;
        bitmapOptions.inTargetDensity=1;
        final Bitmap scaledBitmap=BitmapFactory.decodeResource(getResources(),imageResId,bitmapOptions);
        scaledBitmap.setDensity(Bitmap.DENSITY_NONE);
        return scaledBitmap;
    }
}