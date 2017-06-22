package mkawa.okhttp;

import mkawa.okhttp.contactActivity.sendShitParams;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.PercentFormatter;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class TradeShare extends Activity
{

    public static final MediaType FORM_DATA_TYPE = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");

    //input element ids found from the live form page
    public static final String NAME_KEY="entry.468624322";
    public static final String TOKEN_KEY="entry.1339641009";
    public static final String DRINK_POINT_KEY="entry.1674081600";
    public static final String OZ_POINT_KEY="entry.1995814502";
    public static final String ABV_POINT_KEY="entry.1922509005";
    public static final String IBU_POINT_KEY="entry.444340806";
    public static final String TEAM_KEY = "entry.2118900696";
    public static final String NTOKEN_KEY = "entry.1190458816";

    protected static String clickToken = "";
    protected static float clickVal;
    public static String URL = "https://docs.google.com/forms/d/1xVzwPQyQdasuy2wSjJnQ1v6Vn9o_I9czcCHN42-9qYc/formResponse";
    public static String playerName = "";
    public static String playerTeam = "";
    protected static String drinkPointsAdd;
    protected static String ozPointsAdd;
    protected static String abvPointsAdd;
    protected static String ibuPointsAdd;
    protected static float keepStock = 0;
    protected static float shares = 0;
    protected static int drinkPoints;
    protected static int ozPoints;
    protected static int abvPoints;
    protected static int ibuPoints;
    protected static float playerDrinkPoints;
    protected static float playerOzPoints;
    protected static float playerAbvPoints;
    protected static float playerIbuPoints;
    protected static float defaultDrinkPoints;
    protected static float defaultOzPoints;
    protected static float defaultAbvPoints;
    protected static float defaultIbuPoints;
    protected static int nInstances;
    protected static float teamDrinkPct;
    protected static float teamOzPct;
    protected static float teamAbvPct;
    protected static float teamIbuPct;



    private Button tradeButton;
    private TextView stockLeft;
    private TextView drinkCounter;
    private TextView ozCounter;
    private TextView abvCounter;
    private TextView ibuCounter;
    private DecoView tokenDeco;
    private int stockSeriesIndex;
    private float maxCatPoints = 0;
    private int maxPoints;
    private double maxMarketVal;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trade_share);

        initializeVariables();

        FetchSettings settings = FetchSettings.findById(FetchSettings.class, 1);
        maxCatPoints = settings.catgPoints;

        //set trade button to unClickable by default
        tradeButton.setClickable(false);
        final int button = getResources().getIdentifier("inactive_button", "drawable", getPackageName());
        tradeButton.setBackgroundResource(button);

        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE",MODE_PRIVATE);
        playerName = sharedPreferences.getString("USERNAME","");
        playerTeam = sharedPreferences.getString("TEAM", "");

        //RecyclerView recyclerView = (RecyclerView) findViewById(R.id.recyclerViewTrade);
        ImageView mainTokenPic = (ImageView) findViewById(R.id.mainTokenView);

        //retrieve information from beerSearch.java
        Bundle extras = getIntent().getExtras();

        if (extras != null)
        {
            clickToken = extras.getString("tokenType").replaceAll("\"", "");
            clickVal = extras.getFloat("curVal");
        }
        int token = getResources().getIdentifier(clickToken, "mipmap", getPackageName());
        mainTokenPic.setImageResource(token);


        //determine # of shares
        List<TokenPurse> tokens;

        long nTokens = TokenPurse.count(TokenPurse.class,null,null);
        tokens = TokenPurse.listAll(TokenPurse.class);
        for (int i = 0; i < nTokens; i++)
        {
            if(tokens.get(i).tokenName.equals(clickToken))
            {
                shares =  (tokens.get(i).tokenShare);
                break;
            }
        }

        //determine max number of points player can get
        maxMarketVal = shares * clickVal;
        maxPoints = (int)Math.floor(shares * clickVal);
        keepStock = (float)Math.floor((maxMarketVal / clickVal)*10)/10;


        System.out.println(shares);
        tokenArc();
        try
        {
            callUserStats(playerName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    } //END ON CREATE

    public void executeTrade (View v)
    {
        //code here to submit token shares
        drinkPointsAdd = drinkCounter.getText().toString();
        ozPointsAdd = ozCounter.getText().toString();
        abvPointsAdd = abvCounter.getText().toString();
        ibuPointsAdd = ibuCounter.getText().toString();

        //Create an object for PostDataTask AsyncTask
        contactActivity.sendShitParams params = new contactActivity().new sendShitParams(true,
                null,
                playerName,
                null,
                URL,
                null,
                null,
                null,
                null,
                clickToken,
                drinkPointsAdd,
                ozPointsAdd,
                abvPointsAdd,
                ibuPointsAdd,
                keepStock,
                playerTeam);

        //Create an object for PostDataTask AsyncTask
        PostDataTask postDataTask = new PostDataTask();

        postDataTask.execute(params);
    }


    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<sendShitParams, Void, Boolean>
    {

        @Override
        protected Boolean doInBackground(sendShitParams... params)
        {
            Boolean result = params[0].test;
            String name = params[0].playerName;
            String postBody="";
            String URL = params[0].postUrl;
            String tradeToken = params[0].tradeToken;
            String drinkP = params[0].drinkPoints;
            String ozP = params[0].ozPoints;
            String abvP = params[0].abvPoints;
            String ibuP = params[0].ibuPoints;
            String team = params[0].teamName;

            int nTokens = Integer.valueOf(drinkP) + Integer.valueOf(ozP) + Integer.valueOf(abvP) + Integer.valueOf(ibuP);

            try
            {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = NAME_KEY+"=" + URLEncoder.encode(name,"UTF-8") +
                        "&" + TOKEN_KEY + "=" + URLEncoder.encode(tradeToken,"UTF-8") +
                        //"&" + DRINK_POINT_KEY + "=" + URLEncoder.encode(drinkP,"UTF-8") +
                        //"&" + OZ_POINT_KEY + "=" + URLEncoder.encode(ozP,"UTF-8") +
                        //"&" + ABV_POINT_KEY + "=" + URLEncoder.encode(abvP,"UTF-8") +
                        //"&" + IBU_POINT_KEY + "=" + URLEncoder.encode(ibuP,"UTF-8") +
                        "&" + TEAM_KEY + "=" + URLEncoder.encode(team,"UTF-8") +
                        "&" + NTOKEN_KEY + "=" + URLEncoder.encode(String.valueOf(nTokens),"UTF-8");

            }
            catch (UnsupportedEncodingException ex)
            {
                result = false;
            }

            try
            {
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(URL)
                        .post(body)
                        .build();
                //Send the request
                Response response = client.newCall(request).execute();
            }
            catch (IOException exception)
            {
                result = false;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Boolean w)
        {
            //Print Success or failure message accordingly
            final Boolean test = w;

            runOnUiThread(new Runnable()
            {
                @Override
                public void run()
                {
                    Toast.makeText(getApplicationContext(),test?"Trade Executed":"There was some error in sending message. Please try again after some time.",Toast.LENGTH_LONG).show();

                    List<TokenPurse> tokens;
                    long nTokens = TokenPurse.count(TokenPurse.class,null,null);
                    tokens = TokenPurse.listAll(TokenPurse.class);
                    for (int i = 0; i < nTokens; i++)
                    {
                        if(tokens.get(i).tokenName.equals(clickToken))
                        {
                            tokens.get(i).tokenShare = keepStock;
                            tokens.get(i).save();
                            break;
                        }
                    }

                    Intent allDone = new Intent(getApplicationContext(), Drop.class);

                    setNumInstances(Integer.valueOf(drinkPointsAdd),Integer.valueOf(ozPointsAdd), Integer.valueOf(abvPointsAdd), Integer.valueOf(ibuPointsAdd));

                    startActivity(allDone);
                    finish();
                }
            });
        }
    }

    private void initializeVariables()
    {
        tradeButton = (Button) findViewById(R.id.tradeButton);
        drinkCounter = (TextView) findViewById(R.id.drinkCounter);
        ozCounter = (TextView) findViewById(R.id.ozCounter);
        abvCounter = (TextView) findViewById(R.id.abvCounter);
        ibuCounter = (TextView) findViewById(R.id.ibuCounter);
        stockLeft = (TextView) findViewById(R.id.stockLeft);
        TextView stockLeftTV = (TextView) findViewById(R.id.stockLeftTV);
        TextView headerView = (TextView) findViewById(R.id.tradeheader);

        tokenDeco = (DecoView) findViewById(R.id.dynamicArcChart);

        drinkCounter.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ozCounter.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        abvCounter.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ibuCounter.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        stockLeft.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        stockLeftTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        headerView.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));


        drinkCounter.setText("0");
        ozCounter.setText("0");
        abvCounter.setText("0");
        ibuCounter.setText("0");
    }

    public void resetVal(View v)
    {
        playerDrinkPoints = defaultDrinkPoints;
        playerOzPoints = defaultOzPoints;
        playerAbvPoints = defaultAbvPoints;
        playerIbuPoints = defaultIbuPoints;

        tradeButton.setClickable(false);

        keepStock = shares;

        drinkCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray));
        ozCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray));
        abvCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray));
        ibuCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray));

        drinkCounter.setText("0");
        ozCounter.setText("0");
        abvCounter.setText("0");
        ibuCounter.setText("0");

        tokenDeco.addEvent(new DecoEvent.Builder(shares).setIndex(stockSeriesIndex).build());
    }

    public void increaseVal(View v)
    {
        if(keepStock * clickVal >= 1)
        {
            switch (v.getId())
            {
                case R.id.drinkClick:
                    if (playerDrinkPoints < maxCatPoints)
                    {
                        drinkCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod));
                        drinkPoints = Integer.valueOf(drinkCounter.getText().toString().replaceAll("\"", "")) + 1;
                        drinkCounter.setText(String.valueOf(drinkPoints));
                        playerDrinkPoints = playerDrinkPoints + 1;
                        keepStock  = (float)Math.floor(((keepStock*clickVal - 1) / clickVal)*10)/10;
                    }
                    break;
                case R.id.ozClick:
                    if (playerOzPoints < maxCatPoints)
                    {
                        ozCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod));
                        ozPoints = Integer.valueOf(ozCounter.getText().toString().replaceAll("\"", "")) + 1;
                        playerOzPoints = playerOzPoints + 1;
                        ozCounter.setText(String.valueOf(ozPoints));
                        keepStock  = (float)Math.floor(((keepStock*clickVal - 1) / clickVal)*10)/10;
                    }
                    break;
                case R.id.abvClick:
                    if (playerAbvPoints < maxCatPoints)
                    {
                        abvCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod));
                        abvPoints = Integer.valueOf(abvCounter.getText().toString().replaceAll("\"", "")) + 1;
                        playerAbvPoints = playerAbvPoints + 1;
                        abvCounter.setText(String.valueOf(abvPoints));
                        keepStock  = (float)Math.floor(((keepStock*clickVal - 1) / clickVal)*10)/10;
                    }
                    break;
                case R.id.ibuClick:
                    if (playerIbuPoints < maxCatPoints) {
                        ibuCounter.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod));
                        ibuPoints = Integer.valueOf(ibuCounter.getText().toString().replaceAll("\"", "")) + 1;
                        ibuCounter.setText(String.valueOf(ibuPoints));
                        playerIbuPoints = playerIbuPoints + 1;
                        keepStock  = (float)Math.floor(((keepStock*clickVal - 1) / clickVal)*10)/10;
                    }
                    break;
            }

            tokenDeco.addEvent(new DecoEvent.Builder(keepStock).setIndex(stockSeriesIndex).build());

            tradeButton.setClickable(true);
        }
    }

    private void tokenArc()
    {
        // CREATE BACKGROUND TRACK
        tokenDeco.addSeries(new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray))
                .setRange(0, shares, shares)
                .setInitialVisibility(false)
                .setLineWidth(32f)
                .build());

        //CREATE DATA SERIES TRACK
        SeriesItem stockSeries;
        stockSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.Yellow))
                .setRange(0, shares, 0)
                .setLineWidth(32f)
                .setSpinDuration(1000)
                .setInterpolator(new LinearInterpolator())
                .build();

        stockSeriesIndex = tokenDeco.addSeries(stockSeries);

        tokenDeco.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(500)
                .setDuration(500)
                .build());

        tokenDeco.addEvent(new DecoEvent.Builder(shares).setIndex(stockSeriesIndex).setDelay(500).build());

        //SET TOKEN LISTENER
        stockSeries.addArcSeriesItemListener(new SeriesItem.SeriesItemListener()
        {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition)
            {
                stockLeft.setText((String.format(Locale.getDefault(),"%.1f",currentPosition)));
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete)
            {
            }
        });
    }

    public void printSomeBar(float drinkPct, float ozPct, float abvPct, float ibuPct)
    {
        //ADD VALUES TO CHART
        ArrayList<BarEntry> entries = new ArrayList<>();
        entries.add(new BarEntry(1,drinkPct*100));
        entries.add(new BarEntry(2,ozPct*100));
        entries.add(new BarEntry(3,abvPct*100));
        entries.add(new BarEntry(4,ibuPct*100));

        //CREATE DATASET
        BarDataSet dataSet = new BarDataSet(entries,"points");

        int[] colors = new int[4];
        colors[0] = ContextCompat.getColor(getApplicationContext(),R.color.GoldSpades);
        colors[1] = ContextCompat.getColor(getApplicationContext(),R.color.BlueDiamonds);
        colors[2] = ContextCompat.getColor(getApplicationContext(),R.color.BlackHearts);
        colors[3] = ContextCompat.getColor(getApplicationContext(),R.color.CreamClubs);

        dataSet.setColors(colors);

        BarChart pointsLeaderBoard = new BarChart(getApplicationContext());

        BarData data = new BarData(dataSet);
        data.setValueFormatter(new PercentFormatter(new DecimalFormat("###")));
        data.setValueTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        data.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.WhiteSmoke));
        data.setBarWidth(0.75f); // set the width of each bar
        data.setValueTextSize(8f);

        pointsLeaderBoard.setData(data);
        pointsLeaderBoard.setFitBars(false);
        pointsLeaderBoard.animateY(1000);
        pointsLeaderBoard.setDescription("");
        pointsLeaderBoard.setDescriptionColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        pointsLeaderBoard.setDescriptionTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        pointsLeaderBoard.setDescriptionTextSize(25f);
        pointsLeaderBoard.getXAxis().setDrawGridLines(false);
        pointsLeaderBoard.getAxisRight().setEnabled(false);
        pointsLeaderBoard.invalidate(); // refresh

        XAxis xAxis = pointsLeaderBoard.getXAxis();
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        xAxis.setDrawAxisLine(true);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinValue(0f);
        xAxis.setAxisMaxValue(5f);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        xAxis.setDrawLabels(false);
        xAxis.setLabelCount(4, true);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(-90);


        YAxis yAxis = pointsLeaderBoard.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
        yAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        yAxis.setAxisMinValue(0);
        yAxis.setAxisMaxValue(110f);
        yAxis.setTextSize(15f);
        yAxis.setGranularity(1f);
        yAxis.setGridColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGoldenrod));
        yAxis.setValueFormatter(new PercentFormatter(new DecimalFormat("###")));

        //define layout parameters for charts
        RelativeLayout.LayoutParams primaryChartParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

        //define layout for chart to be applied and apply
        RelativeLayout primary = (RelativeLayout) findViewById(R.id.pointContainer);
        primary.removeAllViews();
        primary.addView(pointsLeaderBoard,primaryChartParams);
        pointsLeaderBoard.invalidate(); // refresh
    }


    public void callUserStats(final String userName) throws Exception {
        Request request = new Request.Builder()
                .url("https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/5/public/full?alt=json")
                .build();

        //Set up Web Client for data source
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                if (!response.isSuccessful()) throw new IOException("unexpected code " + response);

                //get Web response and store into variable
                final String responseData = response.body().string();

                //Parse JSON
                JsonParser jsonParser = new JsonParser();
                JsonElement elem = jsonParser.parse(responseData);

                //Set JsonElement to JsonArray for looping
                JsonArray elemArr = elem.getAsJsonObject().get("feed").getAsJsonObject().get("entry").getAsJsonArray();

                //Set up ArrayList Containers
                final ArrayList<PlayerGroup> sortCont = new ArrayList<>();

                //Iterate to get all results and fill ArrayList Containers at "data" level
                for (JsonElement sheetsResponse : elemArr) {

                    PlayerGroup group = new PlayerGroup();

                    //Player Name Fill Container
                    JsonElement nameField = sheetsResponse.getAsJsonObject().get("gsx$team").getAsJsonObject().get("$t");
                    group.player = nameField.toString().replaceAll("\"", "");

                    //Ounces Fill Container
                    JsonElement ozField = sheetsResponse.getAsJsonObject().get("gsx$totaloz").getAsJsonObject().get("$t");
                    group.oz = ozField.getAsFloat();

                    //Drink # Fill Container
                    JsonElement drinkField = sheetsResponse.getAsJsonObject().get("gsx$totaldrinks").getAsJsonObject().get("$t");
                    group.drinks = drinkField.getAsFloat();

                    //IBU Fill Container
                    JsonElement ibuField = sheetsResponse.getAsJsonObject().get("gsx$totalibu").getAsJsonObject().get("$t");
                    group.ibu = ibuField.getAsFloat();

                    //ABVoz Fill Container
                    JsonElement abvField = sheetsResponse.getAsJsonObject().get("gsx$totalabvoz").getAsJsonObject().get("$t");
                    group.abv = abvField.getAsFloat();

                    //Drink Points accumulated
                    JsonElement drkPts = sheetsResponse.getAsJsonObject().get("gsx$drinkpoint").getAsJsonObject().get("$t");
                    group.drinkPts = drkPts.getAsFloat();

                    //Ounce Points accumulated
                    JsonElement ozPts = sheetsResponse.getAsJsonObject().get("gsx$ozpoint").getAsJsonObject().get("$t");
                    group.ozPts = ozPts.getAsFloat();

                    //ABV Points accumulated
                    JsonElement abvPts = sheetsResponse.getAsJsonObject().get("gsx$abvpoint").getAsJsonObject().get("$t");
                    group.abvPts = abvPts.getAsFloat();

                    //IBU Points accumulated
                    JsonElement ibuPts = sheetsResponse.getAsJsonObject().get("gsx$ibupoint").getAsJsonObject().get("$t");
                    group.ibuPts = ibuPts.getAsFloat();

                    //add to master container
                    sortCont.add(group);

                }
                //extract user info out of container
                final TeamStats team = new TeamStats();
                for (int i = 0; i < sortCont.size(); i++) {
                    if (sortCont.get(i).player.equals(playerTeam.toUpperCase())) {
                        team.setTeamDrinks(sortCont.get(i).drinks);
                        team.setTeamOz(sortCont.get(i).oz);
                        team.setTeamAbv(sortCont.get(i).abv);
                        team.setTeamIbu(sortCont.get(i).ibu);
                        team.setTeamDrinkTokens(sortCont.get(i).drinkPts);
                        team.setTeamOzTokens(sortCont.get(i).ozPts);
                        team.setTeamAbvTokens(sortCont.get(i).abvPts);
                        team.setTeamIbuTokens(sortCont.get(i).ibuPts);
                    }
                }

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
                        float catgPoints = settings.catgPoints;
                        teamDrinkPct = team.getTeamDrinkPoints()/catgPoints;
                        teamOzPct = team.getTeamOzPoints()/catgPoints;
                        teamAbvPct = team.getTeamAbvPoints()/catgPoints;
                        teamIbuPct = team.getTeamIbuPoints()/catgPoints;
                        printSomeBar(teamDrinkPct, teamOzPct, teamAbvPct, teamIbuPct);

                    }
                });

            }
        });
    }

    public static void setNumInstances(int d, int o, int a, int i)
    {
        nInstances = d + o + a + i;
    }
    public static int getNumInstances()
    {
        return nInstances;
    }
    public static int getDrinkTokens() { return Integer.valueOf(drinkPointsAdd);}
    public static int getOzTokens() { return Integer.valueOf(ozPointsAdd);}
    public static int getAbvTokens() { return Integer.valueOf(abvPointsAdd);}
    public static int getIbuTokens() { return Integer.valueOf(ibuPointsAdd);}
}
