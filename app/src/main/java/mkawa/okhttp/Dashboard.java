package mkawa.okhttp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.PointF;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextClock;
import android.widget.TextView;

import com.github.mikephil.charting.data.CandleData;
import com.google.api.services.sheets.v4.model.NumberFormat;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.hookedonplay.decoviewlib.DecoView;
import com.hookedonplay.decoviewlib.charts.SeriesItem;
import com.hookedonplay.decoviewlib.events.DecoEvent;
import com.vstechlab.easyfonts.EasyFonts;

import org.andengine.entity.text.Text;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Dashboard extends Activity {
    private String playerName;
    private String playerTeam;
    private int textColor;
    private Drawable backgroundDrawable;


    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);


        //retrieve settings
        try
        {
            FetchSettings getSettings = new FetchSettings();
            getSettings.requestSettings();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

        //handle app startup for first time users
        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE",MODE_PRIVATE);
        if(sharedPreferences.getString("USERNAME",null) == null){
            Intent startUserNameActivity = new Intent(getApplicationContext(), UserNameEntry.class);
            startActivity(startUserNameActivity);
        }
        else if (sharedPreferences.getString("TEAM",null) == null)
        {
            Intent teamSelect = new Intent(getApplicationContext(), SelectTeam.class);
            startActivity(teamSelect);
        }
        else
        {
            playerName = sharedPreferences.getString("USERNAME","");
            playerTeam = sharedPreferences.getString("TEAM","");

            final RelativeLayout dashboardButtonLayout = (RelativeLayout) findViewById(R.id.dashboardButtons);

            dashboardButtonLayout.post(new Runnable()
            {
                public void run()
                {
                    int h = dashboardButtonLayout.getHeight();
                    ImageView settings = (ImageView) findViewById(R.id.action_settings);
                    ImageView leaderBoard = (ImageView) findViewById(R.id.leaderBoard);
                    ImageView market = (ImageView) findViewById(R.id.market);
                    ImageView beerSearch = (ImageView) findViewById(R.id.searchBeer);
                    ImageView userStats = (ImageView) findViewById(R.id.userStats);
                    ImageView tokens = (ImageView) findViewById(R.id.tokens);

                    for(int i = h; i == 0; i--)
                    {
                        if(i % 3 == 0)
                        {
                            h = i;
                            break;
                        }
                    }

                    settings.requestLayout();
                    settings.getLayoutParams().height = h/3;
                    //settings.setMinimumHeight(h/3);
                    leaderBoard.requestLayout();
                    leaderBoard.getLayoutParams().height = h/3;
                    //leaderBoard.setMinimumHeight(h/3);
                    market.requestLayout();
                    market.getLayoutParams().height = h/3;
                    //market.setMinimumHeight(h/3);
                    beerSearch.requestLayout();
                    beerSearch.getLayoutParams().height = h/3;
                    //beerSearch.setMinimumHeight(h/3);
                    userStats.requestLayout();
                    userStats.getLayoutParams().height = h/3;
                    //userStats.setMinimumHeight(h/3);
                    tokens.requestLayout();
                    tokens.getLayoutParams().height = h/3;
                    //tokens.setMinimumHeight(h/3);

                }
            });


            try
            {
                callTeamStats(playerTeam);
            }
            catch (Exception e)
            {
                e.printStackTrace();
            }

            initializeVariables();
        }


    }//END ONCREATE

    private void initializeVariables()
    {
        TextView teamName = (TextView) findViewById(R.id.teamNameText);
        teamName.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        teamName.setText(playerTeam);

        ImageView teamLogo = (ImageView) findViewById(R.id.teamLogo);
        RelativeLayout teamLayout = (RelativeLayout) findViewById(R.id.teamLayout);
        TextView userNameTV = (TextView) findViewById(R.id.playerNameText);

        switch(playerTeam)
        {
            case "RATCHET RATS":
                teamLogo.setImageResource(R.mipmap.rats);
                textColor = ContextCompat.getColor(getApplicationContext(),R.color.RatsDark);
                backgroundDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.rats);
                break;
            case "LIT LEMURS":
                teamLogo.setImageResource(R.mipmap.lemurs);
                textColor = ContextCompat.getColor(getApplicationContext(),R.color.LemursLight);
                backgroundDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.lemurs);
                break;
            case "FADED FOXES":
                teamLogo.setImageResource(R.mipmap.foxes);
                textColor = ContextCompat.getColor(getApplicationContext(),R.color.FoxesLight);
                backgroundDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.foxes);
                break;
            case "SLIZARD LIZARDS":
                teamLogo.setImageResource(R.mipmap.lizards);
                textColor = ContextCompat.getColor(getApplicationContext(),R.color.LizardsLight);
                backgroundDrawable = ContextCompat.getDrawable(getApplicationContext(), R.drawable.lizards);
                break;
        }

        teamName.setTextColor(textColor);
        teamLayout.setBackground(backgroundDrawable);
        userNameTV.setText(playerName);
        userNameTV.setTextColor(textColor);
        userNameTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));

    }

    public void selectDestination(View v)
    {
        Intent activitySelect;
        switch(v.getId())
        {
            case R.id.action_settings:
                activitySelect = new Intent(getApplicationContext(), settings.class);
                break;
            case R.id.market:
                activitySelect = new Intent(getApplicationContext(), Market.class);
                break;
            case R.id.userStats:
                activitySelect = new Intent(getApplicationContext(), UserStatstics.class);
                break;
            case R.id.leaderBoard:
                activitySelect = new Intent(getApplicationContext(), LeaderBoard.class);
                break;
            case R.id.searchBeer:
                activitySelect = new Intent(getApplicationContext(), beerSearch.class);
                break;
            case R.id.tokens:
                activitySelect = new Intent(getApplicationContext(), CandleStickTokens.class);
                break;
            default:
                throw new RuntimeException("Unknown button ID");
        }

        startActivity(activitySelect);
    }

    private class TeamData
    {
        String teamName;
        int picName;
        float RPPD;
        float PPT;
        float OPPD;
        float percentVictory;
    }

    private class TeamTextViews
    {
        String teamName;
        TextView RPPD_TV;
        TextView PPT_TV;
        TextView OPPD_TV;
        TextView percentVictory_TV;
    }

    private void setTeamStats(ArrayList<TeamStats> teamStatsArrayList)
    {
        //CREATE LIST OF TEAM NAMES
        ArrayList<String> teamNames = new ArrayList<>();
        teamNames.add("SLIZARD LIZARDS");
        teamNames.add("RATCHET RATS");
        teamNames.add("LIT LEMURS");
        teamNames.add("FADED FOXES");

        //CREATE STATIC LIST TO POPULATE
        ArrayList<TeamData> populatedList = new ArrayList<>();
        for (int i = 0; i < teamNames.size(); i++)
        {
            populatedList.add(new TeamData());
            populatedList.get(i).teamName = teamNames.get(i);

            for (int x = 0; x < teamStatsArrayList.size(); x++)
            {
                if (teamStatsArrayList.get(x).getTeamName().equals(populatedList.get(i).teamName))
                {
                    populatedList.get(i).RPPD = teamStatsArrayList.get(x).getTeamRPPD();
                    populatedList.get(i).PPT = teamStatsArrayList.get(x).getTeamPPT();
                    populatedList.get(i).OPPD = teamStatsArrayList.get(x).getTeamOPPD();
                    populatedList.get(i).percentVictory = teamStatsArrayList.get(x).getTeamVictoryPorgress();
                    switch (teamStatsArrayList.get(x).getTeamName())
                    {
                        case "SLIZARD LIZARDS":
                            populatedList.get(i).picName = R.mipmap.lizards;
                            break;
                        case "RATCHET RATS":
                            populatedList.get(i).picName = R.mipmap.rats;
                            break;
                        case "FADED FOXES":
                            populatedList.get(i).picName = R.mipmap.foxes;
                            break;
                        case "LIT LEMURS":
                            populatedList.get(i).picName = R.mipmap.lemurs;
                            break;
                    }
                }
            }
        }

        int textHeight = 15;

        //SORT TEAMS BY TOTAL POINTS
        Comparator<TeamData> pointsComp = new Comparator<TeamData>()
        {
            @Override
            public int compare(TeamData x1, TeamData x2)
            {
                return Double.compare(x1.percentVictory, x2.percentVictory);
            }
        };
        Collections.sort(populatedList, Collections.reverseOrder(pointsComp));

        //SET FORMATTERS
        DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
        DecimalFormat percentageFormatter = new DecimalFormat("##%");


        //SET TEXTVIEWS
        ArrayList<TeamTextViews> teamTextViews = new ArrayList<>();
        for (int i = 0; i < populatedList.size(); i++) {
            teamTextViews.add(new TeamTextViews());
        }

        TextView RPPD = (TextView) findViewById(R.id.RPPD);
        teamTextViews.get(0).RPPD_TV = (TextView) findViewById(R.id.rank1RPPD);
        teamTextViews.get(1).RPPD_TV = (TextView) findViewById(R.id.rank2RPPD);
        teamTextViews.get(2).RPPD_TV  = (TextView) findViewById(R.id.rank3RPPD);
        teamTextViews.get(3).RPPD_TV  = (TextView) findViewById(R.id.rank4RPPD);

        TextView PPT = (TextView) findViewById(R.id.PPT);
        teamTextViews.get(0).PPT_TV = (TextView) findViewById(R.id.rank1PPT);
        teamTextViews.get(1).PPT_TV  = (TextView) findViewById(R.id.rank2PPT);
        teamTextViews.get(2).PPT_TV = (TextView) findViewById(R.id.rank3PPT);
        teamTextViews.get(3).PPT_TV = (TextView) findViewById(R.id.rank4PPT);

        TextView OPPD = (TextView) findViewById(R.id.OPPD);
        teamTextViews.get(0).OPPD_TV  = (TextView) findViewById(R.id.rank1OPPD);
        teamTextViews.get(1).OPPD_TV = (TextView) findViewById(R.id.rank2OPPD);
        teamTextViews.get(2).OPPD_TV= (TextView) findViewById(R.id.rank3OPPD);
        teamTextViews.get(3).OPPD_TV= (TextView) findViewById(R.id.rank4OPPD);

        TextView percentComplete = (TextView) findViewById(R.id.percentComplete);
        teamTextViews.get(0).percentVictory_TV = (TextView) findViewById(R.id.rank1percentComplete);
        teamTextViews.get(1).percentVictory_TV= (TextView) findViewById(R.id.rank2percentComplete);
        teamTextViews.get(2).percentVictory_TV= (TextView) findViewById(R.id.rank3percentComplete);
        teamTextViews.get(3).percentVictory_TV = (TextView) findViewById(R.id.rank4percentComplete);

        for (int i = 0; i < populatedList.size(); i++)
        {
            teamTextViews.get(i).RPPD_TV.setText(String.valueOf(decimalFormatter.format(populatedList.get(i).RPPD)));
            teamTextViews.get(i).RPPD_TV.setTypeface(EasyFonts.robotoThin(getApplicationContext()));
            teamTextViews.get(i).RPPD_TV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.WhiteSmoke));
            teamTextViews.get(i).RPPD_TV.setTextSize(textHeight);

            teamTextViews.get(i).OPPD_TV.setText(String.valueOf(decimalFormatter.format(populatedList.get(i).OPPD)));
            teamTextViews.get(i).OPPD_TV.setTypeface(EasyFonts.robotoThin(getApplicationContext()));
            teamTextViews.get(i).OPPD_TV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.WhiteSmoke));
            teamTextViews.get(i).OPPD_TV.setTextSize(textHeight);

            teamTextViews.get(i).PPT_TV.setText(String.valueOf(decimalFormatter.format(populatedList.get(i).PPT)));
            teamTextViews.get(i).PPT_TV.setTypeface(EasyFonts.robotoThin(getApplicationContext()));
            teamTextViews.get(i).PPT_TV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.WhiteSmoke));
            teamTextViews.get(i).PPT_TV.setTextSize(textHeight);

            teamTextViews.get(i).percentVictory_TV.setText(String.valueOf(percentageFormatter.format(populatedList.get(i).percentVictory)));
            teamTextViews.get(i).percentVictory_TV.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
            teamTextViews.get(i).percentVictory_TV.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
            teamTextViews.get(i).percentVictory_TV.setTextSize(textHeight);
        }

        TextView teamHeader = (TextView) findViewById(R.id.teamHeader);
        ImageView team1pic = (ImageView) findViewById(R.id.rank1image);
        ImageView team2pic = (ImageView) findViewById(R.id.rank2image);
        ImageView team3pic = (ImageView) findViewById(R.id.rank3image);
        ImageView team4pic = (ImageView) findViewById(R.id.rank4image);

        teamHeader.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        RPPD.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        PPT.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        OPPD.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        percentComplete.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));

        teamHeader.setText("TEAM");
        RPPD.setText("RPPD");
        PPT.setText("PPT");
        OPPD.setText("OPPD");
        percentComplete.setText("% WIN");

        //SET IMAGEVIEWS
        team1pic.setImageResource(populatedList.get(0).picName);
        team2pic.setImageResource(populatedList.get(1).picName);
        team3pic.setImageResource(populatedList.get(2).picName);
        team4pic.setImageResource(populatedList.get(3).picName);
    }

    private void printTeamArc(float drinkPoints, float ozPoints, float abvPoints, float ibuPoints)
    {
        DecoView teamArc = (DecoView) findViewById(R.id.teamChart);
        final TextView mainTeamPercentage = (TextView) findViewById(R.id.mainTeamPercentageComplete);
        mainTeamPercentage.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        int drinkSeriesIndex;
        int ozSeriesIndex;
        int abvSeriesIndex;
        int ibuSeriesIndex;
        int totalSeriesIndex;

        FetchSettings settings = FetchSettings.findById(FetchSettings.class, 1);
        float maxPoints = settings.catgPoints;
        float totalPoints = drinkPoints + ozPoints + abvPoints + ibuPoints;

        // CREATE BACKGROUND TRACK
        teamArc.addSeries(new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.SlateGray))
                .setRange(0, maxPoints, maxPoints)
                .setInitialVisibility(false)
                .setLineWidth(128f)
                .setInset(new PointF(48f,48f))
                .build());

        //CREATE DATA SERIES TRACK
        SeriesItem drinkSeries;
        SeriesItem ozSeries;
        SeriesItem abvSeries;
        SeriesItem ibuSeries;
        final SeriesItem totalSeries;

        drinkSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.GoldSpades))
                .setRange(0, maxPoints,0)
                .setLineWidth(32f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .build();
        ozSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.BlueDiamonds))
                .setRange(0, maxPoints,0)
                .setLineWidth(32f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .setInset(new PointF(32f,32f))
                .build();
        abvSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.BlackHearts))
                .setRange(0, maxPoints, 0)
                .setLineWidth(32f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .setInset(new PointF(64f,64f))
                .build();
        ibuSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.CreamClubs))
                .setRange(0, maxPoints, 0)
                .setLineWidth(32f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .setInset(new PointF(96f,96f))
                .build();
        totalSeries = new SeriesItem.Builder(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke))
                .setRange(0, maxPoints*4, 0)
                .setLineWidth(1f)
                .setSpinDuration(2000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .setCapRounded(false)
                .setInset(new PointF(128,128f))
                .build();

        drinkSeriesIndex = teamArc.addSeries(drinkSeries);
        ozSeriesIndex = teamArc.addSeries(ozSeries);
        abvSeriesIndex = teamArc.addSeries(abvSeries);
        ibuSeriesIndex = teamArc.addSeries(ibuSeries);
        totalSeriesIndex = teamArc.addSeries(totalSeries);

        teamArc.addEvent(new DecoEvent.Builder(DecoEvent.EventType.EVENT_SHOW, true)
                .setDelay(0)
                .setDuration(1500)
                .build());

        teamArc.addEvent(new DecoEvent.Builder(drinkPoints).setIndex(drinkSeriesIndex).setDelay(2500).build());
        teamArc.addEvent(new DecoEvent.Builder(ozPoints).setIndex(ozSeriesIndex).setDelay(2750).build());
        teamArc.addEvent(new DecoEvent.Builder(abvPoints).setIndex(abvSeriesIndex).setDelay(3000).build());
        teamArc.addEvent(new DecoEvent.Builder(ibuPoints).setIndex(ibuSeriesIndex).setDelay(3250).build());
        teamArc.addEvent(new DecoEvent.Builder(totalPoints).setIndex(totalSeriesIndex).setDelay(2500).build());

        final String format = "%.0f%%";

        totalSeries.addArcSeriesItemListener(new SeriesItem.SeriesItemListener() {
            @Override
            public void onSeriesItemAnimationProgress(float percentComplete, float currentPosition) {
                if (format.contains("%%")) {
                    float percentFilled = ((currentPosition - totalSeries.getMinValue()) / (totalSeries.getMaxValue() - totalSeries.getMinValue()));
                    mainTeamPercentage.setText(String.format(format, percentFilled * 100f));
                } else {
                    mainTeamPercentage.setText(String.format(format, currentPosition));
                }
            }

            @Override
            public void onSeriesItemDisplayProgress(float percentComplete) {

            }
        });

    }

    public void callTeamStats(final String teamName) throws Exception
    {
        Request request = new Request.Builder()
                .url("https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/5/public/full?alt=json")
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

                    //Tokens Submitted
                    JsonElement tokensSubmitted = sheetsResponse.getAsJsonObject().get("gsx$totaltokenssubmitted").getAsJsonObject().get("$t");
                    group.totalTokensSubmitted = tokensSubmitted.getAsFloat();

                    //add to master container
                    sortCont.add(group);
                }

                //set values to other players for rankings
                final ArrayList<TeamStats> teams = new ArrayList<>();
                for (int i = 0; i < sortCont.size(); i++){
                    System.out.println(sortCont.get(i).player);
                    if(!sortCont.get(i).player.equals("")){
                        TeamStats player = new TeamStats();
                        player.setTeamName(sortCont.get(i).player);
                        player.setTeamDrinks(sortCont.get(i).drinks);
                        player.setTeamOz(sortCont.get(i).oz);
                        player.setTeamAbv(sortCont.get(i).abv);
                        player.setTeamIbu(sortCont.get(i).ibu);
                        player.setTeamDrinkTokens(sortCont.get(i).drinkPts);
                        player.setTeamOzTokens(sortCont.get(i).ozPts);
                        player.setTeamAbvTokens(sortCont.get(i).abvPts);
                        player.setTeamIbuTokens(sortCont.get(i).ibuPts);
                        player.setTeamTokensSubmitted(sortCont.get(i).totalTokensSubmitted);
                        teams.add(player);
                    }
                }

                //extract user info out of container
                final TeamStats team = new TeamStats();
                for (int i = 0; i < sortCont.size(); i++)
                {
                    if (sortCont.get(i).player.equals(teamName.toUpperCase()))
                    {
                        team.setTeamDrinks(sortCont.get(i).drinks);
                        team.setTeamOz(sortCont.get(i).oz);
                        team.setTeamAbv(sortCont.get(i).abv);
                        team.setTeamIbu(sortCont.get(i).ibu);
                        team.setTeamDrinkTokens(sortCont.get(i).drinkPts);
                        team.setTeamOzTokens(sortCont.get(i).ozPts);
                        team.setTeamAbvTokens(sortCont.get(i).abvPts);
                        team.setTeamIbuTokens(sortCont.get(i).ibuPts);
                        team.setTeamTokensSubmitted(sortCont.get(i).totalTokensSubmitted);
                    }
                }

                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        float teamDrinkPoints = team.getTeamDrinkPoints();
                        float teamOzPoints = team.getTeamOzPoints();
                        float teamAbvPoints = team.getTeamAbvPoints();
                        float teamIbuPoints = team.getTeamIbuPoints();
                        printTeamArc(teamDrinkPoints, teamOzPoints, teamAbvPoints, teamIbuPoints);
                        setTeamStats(teams);
                    }
                });
            }
        });
    }
}
