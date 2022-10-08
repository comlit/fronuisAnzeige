package com.lit.fronuis;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.drawable.DrawableCompat;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.github.mikephil.charting.data.Entry;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MainActivity extends AppCompatActivity
{
    //WebView webView;
    WebView webWeather;
    FloatingActionButton fab;
    Runnable runnable;
    APIHelper api;
    Handler handler;
    Map<String, String> data;
    ProgressBar  batt3;
    TextView grid, prod, cons, batt;
    TextView tvProd, tvCons, tvBatt, tvGrid;
    int barlength = 348;
    //SnakeView snakeViewPV, snakeViewCONS;
    ArrayList<Integer> congraph, pvgraph;
    int maxgraph = 1;
    ImageView battic;
    CustomGraph custom;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN);
        //webView = new WebView(MainActivity.this);
        setContentView(R.layout.activity_main);
        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(MainActivity.this, Radio.class);
                startActivity(intent);
            }
        });
        //webView = findViewById(R.id.web);
        webWeather = findViewById(R.id.webv);

        prod = findViewById(R.id.pvState2);
        grid = findViewById(R.id.gridState2);
        cons = findViewById(R.id.consState2);
        batt = findViewById(R.id.battState2);
        batt3 = findViewById(R.id.battState);

        tvProd = findViewById(R.id.tVprod);
        tvGrid = findViewById(R.id.tVgrid);
        tvCons = findViewById(R.id.tVcons);
        tvBatt = findViewById(R.id.tVbatt);

        battic = findViewById(R.id.battIcon);
        custom = findViewById(R.id.graph);

        webWeather.getSettings().setJavaScriptEnabled(true);
        webWeather.setWebViewClient(new WebViewClient());
        webWeather.loadUrl("file:///android_asset/weather.html");
        //setContentView(R.layout.activity_main);
        //hideSystemUI();
        //webv = findViewById(R.id.webv);
        WebViewClient client = new WebViewClient();
        //webView.setWebViewClient(client);
        //webView.getSettings().setJavaScriptEnabled(true);
        //webView.getSettings().setDomStorageEnabled(true);
        String newUA= "Mozilla/5.0 (X11; U; Linux i686; en-US; rv:1.9.0.4) Gecko/20100101 Firefox/4.0";
        //webView.getSettings().setUserAgentString(newUA);
        //webView.loadUrl("file:///android_asset/froni.html");
        handler = new Handler();
        api = new APIHelper();

        //graphing

        //snakeViewPV = findViewById(R.id.snake);
        //snakeViewPV.setMinValue(0);

        //snakeViewCONS = findViewById(R.id.snake2);
        //snakeViewCONS.setMinValue(0);

        //snakeViewPV.setMaximumNumberOfValues(300);
        //snakeViewCONS.setMaximumNumberOfValues(300);

        //get from shared

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        String pvString = sharedPref.getString("pv123", "100,110,120,130,140,150,160,");
        String consString = sharedPref.getString("cons123", "130,150,180,130,100,80,130,");

        pvgraph = new ArrayList<>();
        congraph = new ArrayList<>();

        String[] pvArr = pvString.split(",");
        for(String x: pvArr)
            pvgraph.add(Integer.parseInt(x));
        String[] consArr = consString.split(",");
        for(String x: consArr)
            congraph.add(Integer.parseInt(x));

        //System.out.println("PV: " + pvgraph.toString() + "Cons: " + congraph.toString());


        //LineChart chart = (LineChart) findViewById(R.id.chart);
        List<Entry> entries = new ArrayList<Entry>();
        int cou = 0;
        for(int i: pvgraph)
        {
            entries.add(new Entry(cou,i));
            cou++;
        }


        custom.init(pvgraph, congraph);
    }

    public void startHandler()
    {
        handler.postDelayed(runnable = new Runnable() {
            @Override
            public void run() {
                startNetThread();
                if(data != null)
                {
                    String perc = prettyNumber(data.get("batt"));
                    if(!perc.equals("NaN"))
                        perc = perc.substring(0, perc.length() - 2) + " %";

                    barlength = (int) (batt3.getWidth() - convertDpToPixel(16.0F,MainActivity.this));


                    // PV and Consumption Bars
                    prod.getLayoutParams().width = (int)(Double.parseDouble(data.get("prod"))*(barlength/7400.0));
                    prod.requestLayout();

                    cons.getLayoutParams().width = (int)((Math.atan((Math.abs(Double.parseDouble(data.get("cons")))-1000.0)/1000.0)*146.5+115.5)*(barlength/346.0));
                    cons.requestLayout();

                    custom.change((int) Math.abs(Double.parseDouble(data.get("cons"))));

                    // Batt and grid bars
                    double battval = Double.parseDouble(data.get("battuse"));
                    double gridval = Double.parseDouble(data.get("grid"));

                    //System.out.println(battval + " " + gridval);
                    if(battval < -10) {
                        Drawable buttonDrawable = batt.getBackground();
                        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                        DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.greenlight));
                        batt.setBackground(buttonDrawable);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) batt.getLayoutParams();
                        params.addRule(RelativeLayout.RIGHT_OF, R.id.battmiddleleft);
                        params.removeRule(RelativeLayout.LEFT_OF);
                        params.width = (int) Math.abs(Math.atan(battval/650.0)*120.0);
                        batt.setLayoutParams(params);
                        //batt.setText(Integer.toString((int) Math.abs(battval)));
                    } else if(battval > 10) {
                        Drawable buttonDrawable = batt.getBackground();
                        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                        DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.redlight));
                        batt.setBackground(buttonDrawable);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) batt.getLayoutParams();
                        params.addRule(RelativeLayout.LEFT_OF, R.id.battmiddleright);
                        params.removeRule(RelativeLayout.RIGHT_OF);
                        //batt.setText(Integer.toString((int) Math.abs(battval)));
                        params.width = (int) Math.abs(Math.atan(battval/650.0)*120.0);
                        batt.setLayoutParams(params);
                    } else {
                        batt.setText("");
                        batt.getLayoutParams().width = 1;
                        batt.requestLayout();
                    }


                    int soc = (int) Double.parseDouble(data.get("batt"));
                    if(battval < -20)
                    {
                        //wird geladen
                        if(soc < 13)
                            battic.setImageResource(R.drawable.ic_battery_charging_25);
                        else if(soc < 37)
                            battic.setImageResource(R.drawable.ic_battery_charging_25);
                        else if(soc < 62)
                            battic.setImageResource(R.drawable.ic_battery_charging_50);
                        else if(soc < 87)
                            battic.setImageResource(R.drawable.ic_battery_charging_75);
                        else
                            battic.setImageResource(R.drawable.ic_battery_charging_100);

                    } else
                    {
                        if(soc < 13)
                            battic.setImageResource(R.drawable.ic_battery_0);
                        else if(soc < 37)
                            battic.setImageResource(R.drawable.ic_battery_25);
                        else if(soc < 62)
                            battic.setImageResource(R.drawable.ic_battery_50);
                        else if(soc < 87)
                            battic.setImageResource(R.drawable.ic_battery_75);
                        else
                            battic.setImageResource(R.drawable.ic_battery_100);
                    }

                    if(gridval < -10) {
                        Drawable buttonDrawable = grid.getBackground();
                        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                        DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.greenlight));
                        grid.setBackground(buttonDrawable);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) grid.getLayoutParams();
                        params.addRule(RelativeLayout.RIGHT_OF, R.id.gridmiddleleft);
                        params.removeRule(RelativeLayout.LEFT_OF);
                        params.width = (int) Math.abs(Math.atan(gridval/650.0)*120.0);
                        grid.setLayoutParams(params);
                    } else if(gridval > 10) {
                        Drawable buttonDrawable = grid.getBackground();
                        buttonDrawable = DrawableCompat.wrap(buttonDrawable);
                        DrawableCompat.setTint(buttonDrawable, getResources().getColor(R.color.redlight));
                        grid.setBackground(buttonDrawable);

                        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) grid.getLayoutParams();
                        params.addRule(RelativeLayout.LEFT_OF, R.id.gridmiddleright);
                        params.removeRule(RelativeLayout.RIGHT_OF);
                        params.width = (int) Math.abs(Math.atan(gridval/650.0)*120.0);
                        grid.setLayoutParams(params);
                    } else {
                        grid.getLayoutParams().width = 1;
                        grid.requestLayout();
                    }


                    //Textviews
                    tvBatt.setText(perc);
                    tvProd.setText(prettyNumber(data.get("prod")));
                    tvGrid.setText(prettyNumber(data.get("grid")));
                    tvCons.setText(prettyNumber(data.get("cons")));

                    //8,4 kw peak

                    //TODO: proper graphing / background worker for data collection

                    //graphing

                    //System.out.println("PV: " + pvgraph.toString() + "Cons: " + congraph.toString() + "Max: " + maxgraph);

                    int pv = Math.abs((int) Math.abs(Double.parseDouble(data.get("prod"))));
                    int cons = Math.abs((int) Math.abs(Double.parseDouble(data.get("cons"))));

                    congraph.add(cons);
                    if(congraph.size()>3000)
                        congraph.remove(0);

                    pvgraph.add(pv);
                    if(pvgraph.size()>3000)
                        pvgraph.remove(0);


                    //custom.init(pvgraph, congraph);
                }

                //webView.scrollBy(0, 205);
                handler.postDelayed(runnable, 3000);
            }
        }, 100);
    }

    public String prettyNumber(String str)
    {
        String re = "NaN";
        if(str != null && str.length() != 0){
            int val = (int) Double.parseDouble(str);
            val = Math.abs(val);
            if(val > 999)
            {
                double val2 = val/1000.0;
                val2 = Math.abs(val2);
                DecimalFormat df = new DecimalFormat("#.##");
                re = df.format(val2);
                re = re + " KW";
            }
            else
            {
                val = Math.abs(val);
                re = Integer.toString(val);
                re = re + " W";
            }

        }

        return re;
    }

    public void stopHandler()
    {
        handler.removeCallbacks(runnable);
    }

    public void startNetThread()
    {
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try  {
                    data = api.getData();
                } catch (Exception e) {
                    e.printStackTrace();
                    data = null;
                }
            }
        });
        thread.start();
    }
    @Override
    protected void onResume()
    {
        super.onResume();
        webWeather.reload();
        startHandler();

        //setContentView(webView);
        //hideSystemUI();
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopHandler();
        String conString = "";
        String pvString = "";

        for(Integer i: pvgraph)
            pvString = pvString + i + ",";
        for(Integer j: congraph)
            conString = conString + j + ",";

        SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPref.edit();
        editor.putString("cons123", conString);
        editor.putString("pv123", pvString);
        editor.apply();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    public static float convertDpToPixel(float dp, Context context){
        return dp * ((float) context.getResources().getDisplayMetrics().densityDpi / DisplayMetrics.DENSITY_DEFAULT);
    }
}