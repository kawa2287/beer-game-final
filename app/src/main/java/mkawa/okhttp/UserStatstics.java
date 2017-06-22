package mkawa.okhttp;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.os.Bundle;
import android.support.annotation.FloatRange;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.SeekBar;
import android.widget.TextView;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ObjectPool;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.lang.reflect.Array;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class UserStatstics extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_statstics);

        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE",MODE_PRIVATE);
        final String playerUserName = sharedPreferences.getString("USERNAME","");
        final String query = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/2/public/full?alt=json";

        //Method for retrieving Data on worker thread
        try
        {
            requestUserStats(query, playerUserName);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

    }//END ON CREATE

    void requestUserStats(String url, final String userName) throws Exception
    {
        Request request = new Request.Builder()
                .url(url)
                .build();

        //Set up Web Client for data source
        OkHttpClient client = new OkHttpClient();

        client.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException
            {
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
                for (JsonElement sheetsResponse : elemArr)
                {
                    PlayerGroup group = new PlayerGroup();

                    //Player Name Fill Container
                    JsonElement nameField = sheetsResponse.getAsJsonObject().get("gsx$name").getAsJsonObject().get("$t");
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

                    //Tokens Submitted
                    JsonElement tokensSubmitted = sheetsResponse.getAsJsonObject().get("gsx$totaltokenssubmitted").getAsJsonObject().get("$t");
                    group.totalTokensSubmitted = tokensSubmitted.getAsFloat();

                    //add to master container
                    sortCont.add(group);

                }
                //extract user info out of container
                final PlayerStats user = new PlayerStats();
                for (int i = 0; i < sortCont.size();i++){
                    if(sortCont.get(i).player.equals(userName.toUpperCase()))
                    {
                        user.setHeader("STATS");
                        user.setDrinks(sortCont.get(i).drinks);
                        user.setOz(sortCont.get(i).oz);
                        user.setAbv(sortCont.get(i).abv);
                        user.setIbu(sortCont.get(i).ibu);
                        user.setDrinkTokens(sortCont.get(i).drinkPts);
                        user.setOzTokens(sortCont.get(i).ozPts);
                        user.setAbvTokens(sortCont.get(i).abvPts);
                        user.setIbuTokens(sortCont.get(i).ibuPts);
                        user.setTokenSubmitted(sortCont.get(i).totalTokensSubmitted);
                    }
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        //set values to other players for rankings
                        ArrayList<PlayerStats> players = new ArrayList<>();
                        for (int i = 0; i < sortCont.size(); i++)
                        {
                            if(!sortCont.get(i).player.equals("")){
                                PlayerStats player = new PlayerStats();
                                player.setHeader("STATS");
                                player.setName(sortCont.get(i).player);
                                player.setDrinks(sortCont.get(i).drinks);
                                player.setOz(sortCont.get(i).oz);
                                player.setAbv(sortCont.get(i).abv);
                                player.setIbu(sortCont.get(i).ibu);
                                player.setDrinkTokens(sortCont.get(i).drinkPts);
                                player.setOzTokens(sortCont.get(i).ozPts);
                                player.setAbvTokens(sortCont.get(i).abvPts);
                                player.setIbuTokens(sortCont.get(i).ibuPts);
                                player.setTokenSubmitted(sortCont.get(i).totalTokensSubmitted);
                                players.add(player);
                            }
                        }

                        for (int i = 0; i < players.size(); i++)
                        {
                            players.get(i).setPlayers(players);
                        }

                        user.setPlayers(players);
                        user.setName(userName);
                        System.out.println(user.getDrinkRank());

                        ArrayList<String> statNames = new ArrayList<>();
                        initializeStatNames(statNames);

                        ArrayList<Object> statValues = new ArrayList<>();
                        initializeStatValues(statValues, user);

                        ArrayList<Object> rankValues = new ArrayList<>();
                        initializeRankList(rankValues, user);

                        ArrayList<StatLineItem> statLineItems = new ArrayList<>();
                        initializeStatLineItems(statLineItems,statNames,statValues,rankValues);

                        LinearLayout statLinearLayout = (LinearLayout) findViewById(R.id.userStatsLayout);
                        LinearLayout.LayoutParams statLinearLayoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, 0, 1);

                        for (int i = 0; i < statLineItems.size(); i++)
                        {
                            statLinearLayout.addView(statLineItems.get(i).mainLinearLayout,statLinearLayoutParams);
                        }

                        float numberOfTokens = TokenPurse.count(TokenPurse.class,null,null);
                        printTokenArc(numberOfTokens);

                        //System.out.print("print this first to make work for some reason" + players.get(0).getDrinkRank());
                        for (int i = 0; i < rankValues.size(); i++)
                        {
                            System.out.println(statNames.get(i) + " = " + statValues.get(i) + " rank = " + rankValues.get(i));
                        }

                    }
                });
            }
        });
    }

    private class StatLineItem
    {
        TextView statName;
        TextView statValue;
        TextView rank;
        LinearLayout mainLinearLayout;

    }

    private void initializeStatNames (ArrayList<String> statNameList)
    {
        statNameList.add("STAT");

        statNameList.add("TOTAL POINTS");
        statNameList.add("TOTAL DRINKS");
        statNameList.add("TOTAL OZ");
        statNameList.add("TOTAL ABV OZ");
        statNameList.add("TOTAL IBU");

        statNameList.add("RPPD");
        statNameList.add("PPT");
        statNameList.add("OPPD");

        statNameList.add("TOKENS SUBMITTED");

        statNameList.add("ABV/DRINK");
        statNameList.add("IBU/DRINK");
        statNameList.add("OZ/DRINK");
    }

    private void initializeStatValues (ArrayList<Object> statValues, PlayerStats playerStats)
    {
        statValues.add("VALUE");

        statValues.add(playerStats.getTotalPoints());
        statValues.add(playerStats.getDrinks());
        statValues.add(playerStats.getOz());
        statValues.add(playerStats.getAbv());
        statValues.add(playerStats.getIbu());

        statValues.add(playerStats.getRPPD());
        statValues.add(playerStats.getPPT());
        statValues.add(playerStats.getOPPD());

        statValues.add(playerStats.getTokenSubmitted());

        statValues.add(playerStats.getAvgABV());
        statValues.add(playerStats.getIbuPerDrink());
        statValues.add(playerStats.getOzPerDrink());
    }

    private void initializeRankList (ArrayList<Object> rank, PlayerStats playerStats)
    {
        rank.add("RANK");

        rank.add(playerStats.getTotalPointsRank());
        rank.add(playerStats.getDrinkRank());
        rank.add(playerStats.getOzRank());
        rank.add(playerStats.getAbvRank());
        rank.add(playerStats.getIbuRank());

        rank.add(playerStats.getRPPD_rank());
        rank.add(playerStats.getPPT_rank());
        rank.add(playerStats.getOPPD_rank());

        rank.add(playerStats.getTokensSubmittedRank());

        rank.add(playerStats.getAbvPerDrinkRank());
        rank.add(playerStats.getIbuPerDrinkRank());
        rank.add(playerStats.getOzPerDrinkRank());
    }

    private void initializeStatLineItems (ArrayList<StatLineItem> statLineItemList ,ArrayList<String> statNames, ArrayList<Object> statValues, ArrayList<Object> rankValues)
    {
        for (int i = 0; i < statNames.size(); i++)
        {
            //SET FORMATTERS
            DecimalFormat decimalFormatter = new DecimalFormat("#0.00");

            statLineItemList.add(new StatLineItem());
            statLineItemList.get(i).statName = new TextView(getApplicationContext());
            statLineItemList.get(i).statValue = new TextView(getApplicationContext());
            statLineItemList.get(i).rank = new TextView(getApplicationContext());

            statLineItemList.get(i).statName.setText(statNames.get(i));
            if(statValues.get(i).getClass().equals(Float.class))
            {
                statLineItemList.get(i).statValue.setText(String.valueOf(decimalFormatter.format(statValues.get(i))));
            }
            else
            {
                statLineItemList.get(i).statValue.setText(String.valueOf(statValues.get(i)));
            }

            statLineItemList.get(i).rank.setText(String.valueOf(rankValues.get(i)));

            statLineItemList.get(i).statName.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
            statLineItemList.get(i).statValue.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));
            statLineItemList.get(i).rank.setTypeface(EasyFonts.robotoRegular(getApplicationContext()));

            statLineItemList.get(i).statName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.NavajoWhite));
            statLineItemList.get(i).statValue.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
            statLineItemList.get(i).rank.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod));

            if (i == 0){
                statLineItemList.get(i).statName.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
                statLineItemList.get(i).statValue.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
                statLineItemList.get(i).rank.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));

                statLineItemList.get(i).statName.setTextSize(20);
                statLineItemList.get(i).statValue.setTextSize(20);
                statLineItemList.get(i).rank.setTextSize(20);

                statLineItemList.get(i).statName.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
                statLineItemList.get(i).statValue.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
                statLineItemList.get(i).rank.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
            }

            statLineItemList.get(i).statName.setGravity(Gravity.CENTER);
            statLineItemList.get(i).statValue.setGravity(Gravity.CENTER);
            statLineItemList.get(i).rank.setGravity(Gravity.CENTER);

            statLineItemList.get(i).mainLinearLayout = new LinearLayout(getApplicationContext());
            LinearLayout.LayoutParams mainLayoutParams = new LinearLayout.LayoutParams(0, LinearLayout.LayoutParams.MATCH_PARENT, 1);

            statLineItemList.get(i).mainLinearLayout.addView(statLineItemList.get(i).statName,mainLayoutParams);
            statLineItemList.get(i).mainLinearLayout.addView(statLineItemList.get(i).statValue,mainLayoutParams);
            statLineItemList.get(i).mainLinearLayout.addView(statLineItemList.get(i).rank,mainLayoutParams);
        }
    }

    private void printTokenArc(float tokensCollected)
    {
        DecoView tokenArc = (DecoView) findViewById(R.id.tokensCollectedChart);
        final TextView tokensCollectedPercentage = (TextView) findViewById(R.id.tokensCollectedPercentage);
        tokensCollectedPercentage.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        int tokenSeriesIndex;

        FetchSettings settings = FetchSettings.findById(FetchSettings.class, 1);
        float maxTokens = settings.nTokens;

        // CREATE BACKGROUND TRACK
        tokenArc.addSeries(new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray))
                .setRange(0, maxTokens, maxTokens)
                .setInitialVisibility(false)
                .setLineWidth(32f)
                .build());

        //CREATE DATA SERIES TRACK
        final SeriesItem tokenSeries;

        tokenSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.DodgerBlue))
                .setRange(0, maxTokens,0)
                .setLineWidth(32f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .build();

        tokenSeriesIndex = tokenArc.addSeries(tokenSeries);

        tokenArc.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(1500)
                .build());

        tokenArc.addEvent(new DecoEvent.Builder(tokensCollected).setIndex(tokenSeriesIndex).setDelay(2500).build());

        final String format = "%.0f%%";

        tokenSeries.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                if (format.contains("%%")) {
                    float percentFilled = ((currentPosition - tokenSeries.getMinValue()) / (tokenSeries.getMaxValue() - tokenSeries.getMinValue()));
                    tokensCollectedPercentage.setText(String.format(format, percentFilled * 100f)+ System.lineSeparator() + "TOKENS"+ System.lineSeparator() + "COLLECTED");
                } else {
                    tokensCollectedPercentage.setText(String.format(format, currentPosition)+ System.lineSeparator() + "TOKENS"+ System.lineSeparator() + "COLLECTED");
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

    }
}
