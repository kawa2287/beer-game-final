package mkawa.okhttp;


import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.charts.HorizontalBarChart;
import com.github.mikephil.charting.charts.PieChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.PieData;
import com.github.mikephil.charting.data.PieDataSet;
import com.github.mikephil.charting.data.PieEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.github.mikephil.charting.renderer.PieChartRenderer;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import javax.xml.transform.Templates;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class LeaderBoard extends Activity {

    protected static int pointSelection = 0;
    private ArrayList<PlayerStats> contestants = new ArrayList<>();
    private String playerUserName;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_leader_board);


        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE",MODE_PRIVATE);
        playerUserName = sharedPreferences.getString("USERNAME","");
        final String query = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/2/public/full?alt=json";

        //Method for retrieving Data on worker thread
        try {
            requestLeaderboardData(query, playerUserName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //set radiogroup on checkedlistener
        RadioGroup radioGroup = (RadioGroup) findViewById(R.id.radioGroup);
        final RadioButton totalButton = (RadioButton) findViewById(R.id.totalSelect);
        final RadioButton drinkButton = (RadioButton) findViewById(R.id.drinkSelect);
        final RadioButton ozButton = (RadioButton) findViewById(R.id.ozSelect);
        final RadioButton abvButton = (RadioButton) findViewById(R.id.abvSelect);
        final RadioButton ibuButton = (RadioButton) findViewById(R.id.ibuSelect);

        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup radioGroup, int checkedId) {

                if (checkedId == R.id.totalSelect){
                    pointSelection = 0;
                    drinkButton.setChecked(false);
                    abvButton.setChecked(false);
                    ozButton.setChecked(false);
                    ibuButton.setChecked(false);
                    toastTest();

                } else if (checkedId == R.id.drinkSelect){
                    pointSelection = 1;
                    abvButton.setChecked(false);
                    ozButton.setChecked(false);
                    ibuButton.setChecked(false);
                    totalButton.setChecked(false);
                    toastTest();
                } else if (checkedId == R.id.ozSelect){
                    pointSelection = 2;
                    abvButton.setChecked(false);
                    drinkButton.setChecked(false);
                    ibuButton.setChecked(false);
                    totalButton.setChecked(false);
                    toastTest();
                } else if (checkedId == R.id.abvSelect){
                    pointSelection = 3;
                    drinkButton.setChecked(false);
                    ozButton.setChecked(false);
                    ibuButton.setChecked(false);
                    totalButton.setChecked(false);
                    toastTest();
                } else {
                    pointSelection = 4;
                    abvButton.setChecked(false);
                    ozButton.setChecked(false);
                    totalButton.setChecked(false);
                    drinkButton.setChecked(false);
                    toastTest();
                }
            }
        });

    }//END ON CREATE

    public void toastTest(){
        switch(pointSelection){
            case 0:
                System.out.println("point 0 selected");
                printLeaderBoard(contestants, playerUserName);
                return;
            case 1:
                System.out.println("point 1 selected");
                printSomePie(contestants, playerUserName, "drinks");
                printSomeBar(contestants, playerUserName, "drinks");
                return;
            case 2:
                System.out.println("point 2 selected");
                printSomePie(contestants, playerUserName, "oz");
                printSomeBar(contestants, playerUserName, "oz");
                return;
            case 3:
                System.out.println("point 3 selected");
                printSomePie(contestants, playerUserName, "abv");
                printSomeBar(contestants, playerUserName, "abv");
                return;
            case 4:
                System.out.println("point 4 selected");
                printSomePie(contestants, playerUserName, "ibu");
                printSomeBar(contestants, playerUserName, "ibu");
                return;
            default:
        }
    }

    void requestLeaderboardData(String url, final String userName) throws Exception{
        Request request = new Request.Builder()
                .url(url)
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

                    //add to master container
                    sortCont.add(group);

                }

                //set values to other players for rankings
                final ArrayList<PlayerStats> players = new ArrayList<>();
                for (int i = 0; i < sortCont.size(); i++){
                    System.out.println(sortCont.get(i).player);
                    if(!sortCont.get(i).player.equals("")){
                        PlayerStats player = new PlayerStats();
                        player.setName(sortCont.get(i).player);
                        player.setDrinks(sortCont.get(i).drinks);
                        player.setOz(sortCont.get(i).oz);
                        player.setAbv(sortCont.get(i).abv);
                        player.setIbu(sortCont.get(i).ibu);
                        player.setDrinkTokens(sortCont.get(i).drinkPts);
                        player.setOzTokens(sortCont.get(i).ozPts);
                        player.setAbvTokens(sortCont.get(i).abvPts);
                        player.setIbuTokens(sortCont.get(i).ibuPts);
                        players.add(player);
                    }
                }

                //Sort players based on Total Points
                Comparator<PlayerStats> pointsComp = new Comparator<PlayerStats>() {
                    @Override
                    public int compare(PlayerStats x1, PlayerStats x2) {
                        return Double.compare(
                                x1.getDrinkPts()+x1.getOzPts()+x1.getAbvPts()+x1.getIbuPts(),
                                x2.getDrinkPts()+x2.getOzPts()+x2.getAbvPts()+x2.getIbuPts());
                    }
                };
                Collections.sort(players, pointsComp);


                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        contestants = players;
                        toastTest();
                    }
                });
            }
        });
    }

    public void printLeaderBoard(ArrayList<PlayerStats> players, String userName){
        //add values to chart in groups
        ArrayList<BarEntry> entries = new ArrayList<>();
        float maxFloat = 1;

        for (int i = 0; i < players.size(); i++) {
            entries.add(new BarEntry(i+1 , players.get(i).getDrinkPts() + players.get(i).getOzPts() + players.get(i).getAbvPts() + players.get(i).getIbuPts()));
            maxFloat = maxFloat + 1;
        }

        //create dataset
        BarDataSet dataSet = new BarDataSet(entries,"points");
        int[] colors = new int[(int)maxFloat];
        for (int i = 0; i < maxFloat-1; i ++){
            if(userName.toUpperCase().equals(players.get(i).getName())){
                colors[i] = ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod);
            } else {
                colors[i] = ContextCompat.getColor(getApplicationContext(),R.color.SlateGray);
            }
        }

        dataSet.setColors(colors);

        //setup Leader Board
        HorizontalBarChart pointsLeaderBoard = new HorizontalBarChart(getApplicationContext());

        //define chart data
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new MyValueFormatter());
        data.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.SlateGray));
        data.setBarWidth(0.50f); // set the width of each bar
        data.setValueTextSize(25f);

        pointsLeaderBoard.setData(data);
        pointsLeaderBoard.setFitBars(false);
        pointsLeaderBoard.animateY(3000);
        pointsLeaderBoard.setDescription("TOTAL POINTS");
        pointsLeaderBoard.setDescriptionColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        pointsLeaderBoard.setDescriptionTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        pointsLeaderBoard.setDescriptionTextSize(25f);
        pointsLeaderBoard.getXAxis().setDrawGridLines(false);
        pointsLeaderBoard.getAxisRight().setEnabled(false);
        pointsLeaderBoard.invalidate(); // refresh

        //define labels
        final ArrayList<String> labels = new ArrayList<>();
        labels.add("");
        for (int i = 0; i < players.size(); i++){
            labels.add(players.get(i).getName());
        }
        labels.add("");

        //set up formatting for labels
        String[] names = new String[labels.size()];
        names = labels.toArray(names);
        final String[] nameLabels = names;

        AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return nameLabels[(int)value];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        XAxis xAxis = pointsLeaderBoard.getXAxis();
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinValue(0f);
        xAxis.setAxisMaxValue(maxFloat);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        xAxis.setLabelCount(nameLabels.length, true);
        xAxis.setTextSize(15f);


        YAxis yAxis = pointsLeaderBoard.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
        yAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        yAxis.setTextSize(15f);
        yAxis.setGridColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGoldenrod));

        //define layout parameters for charts
        LinearLayout.LayoutParams primaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 1f);

        LinearLayout.LayoutParams secondaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0f);

        //define layout for chart to be applied and apply
        LinearLayout secondary = (LinearLayout) findViewById(R.id.secondaryChart);
        secondary.removeAllViews();
        secondary.setLayoutParams(secondaryChartParams);
        LinearLayout primary = (LinearLayout) findViewById(R.id.primaryChart);
        primary.removeAllViews();
        primary.addView(pointsLeaderBoard,primaryChartParams);
        pointsLeaderBoard.invalidate(); // refresh
    }

    public void printSomePie(ArrayList<PlayerStats> players, String userName, String dataType){
        //add values to chart in groups
        ArrayList<PieEntry> entries = new ArrayList<>();
        float maxFloat = 1;
        float chartTotal = 0;

        for (int i = 0; i < players.size(); i++) {
            switch(dataType){
                case "drinks":
                    entries.add(new PieEntry(players.get(i).getDrinks(), players.get(i).getName()));
                    chartTotal = chartTotal + players.get(i).getDrinks();
                    break;
                case "oz":
                    entries.add(new PieEntry(players.get(i).getOz(), players.get(i).getName()));
                    chartTotal = chartTotal + players.get(i).getOz();
                    break;
                case "abv":
                    entries.add(new PieEntry(players.get(i).getAbv(), players.get(i).getName()));
                    chartTotal = chartTotal + players.get(i).getAbv();
                    break;
                case "ibu":
                    entries.add(new PieEntry(players.get(i).getIbu(), players.get(i).getName()));
                    chartTotal = chartTotal + players.get(i).getIbu();
                    break;
                default:
                    entries.add(new PieEntry(1f,"error"));
            }

            maxFloat = maxFloat + 1;
        }


        //create dataset
        PieDataSet dataSet = new PieDataSet(entries,"Totals");

        int[] colors = new int[(int)maxFloat];
        for (int i = 0; i < maxFloat-1; i ++){
            if(userName.toUpperCase().equals(players.get(i).getName())){
                colors[i] = ContextCompat.getColor(getApplicationContext(),R.color.Gold);
            } else {
                colors[i] = ColorTemplate.getColorWithAlphaComponent(ColorTemplate.PASTEL_COLORS[(i+1)%5],125);

            }
        }

        dataSet.setColors(colors);
        //determine slice space
        dataSet.setYValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setXValuePosition(PieDataSet.ValuePosition.OUTSIDE_SLICE);
        dataSet.setValueLineColor(ContextCompat.getColor(getApplicationContext(), R.color.SlateGray));
        dataSet.setValueTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        dataSet.setSliceSpace(20);
        dataSet.setValueFormatter(new MyValueFormatter());
        PieData data = new PieData(dataSet);
        data.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        data.setValueTextSize(15f);


        PieChart pieChart = new PieChart(getApplicationContext());
        pieChart.setData(data);

        switch (dataType){
            case "drinks":
                pieChart.setDescription("");
                pieChart.animateY(1500, Easing.EasingOption.EaseOutCubic);
                pieChart.spin(2000, 0, -90f, Easing.EasingOption.EaseInOutQuad);
                pieChart.setCenterText("TOTAL DRINKS");
                break;
            case "oz":
                pieChart.setDescription("");
                pieChart.animateY(1000, Easing.EasingOption.EaseOutQuad);
                pieChart.spin(1500, 0, 45f, Easing.EasingOption.EaseOutCirc);
                pieChart.setCenterText("TOTAL OZ");
                break;
            case "abv":
                pieChart.setDescription("");
                pieChart.animateY(1000, Easing.EasingOption.Linear);
                pieChart.spin(1000, 0, -360f, Easing.EasingOption.Linear);
                pieChart.setCenterText("TOTAL ABVoz");
                break;
            case "ibu":
                pieChart.setDescription("");
                pieChart.animateY(2000, Easing.EasingOption.EaseOutBounce);
                pieChart.spin(2000, 0, -360f, Easing.EasingOption.EaseOutBounce);
                pieChart.setCenterText("TOTAL IBU");
                break;
            default:
                entries.add(new PieEntry(1f,"error"));
        }
        pieChart.setDescriptionColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
        pieChart.setCenterTextTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        pieChart.setCenterTextSize(30f);
        pieChart.setCenterTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
        pieChart.invalidate();

        pieChart.setDrawEntryLabels(true);
        pieChart.setEntryLabelTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        pieChart.setEntryLabelColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
        pieChart.setExtraOffsets(20f,20f,20f,20f);
        pieChart.setHoleColor(ContextCompat.getColor(getApplicationContext(),R.color.DarkSlateGray));





        //define layout parameters for charts
        LinearLayout.LayoutParams primaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.4f);
        LinearLayout.LayoutParams secondaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.6f);

        //define layout for chart to be applied and apply
        LinearLayout secondary = (LinearLayout) findViewById(R.id.secondaryChart);
        secondary.removeAllViews();
        secondary.setLayoutParams(secondaryChartParams);
        LinearLayout primary = (LinearLayout) findViewById(R.id.primaryChart);
        primary.removeAllViews();
        primary.addView(pieChart,primaryChartParams);
        pieChart.invalidate(); // refresh

    }

    public void printSomeBar(ArrayList<PlayerStats> players, String userName, String dataType){
        //add values to chart in groups
        ArrayList<BarEntry> entries = new ArrayList<>();
        int maxFloat = 1;
        TextView chartDescription = (TextView) findViewById(R.id.secondaryChartHeader);
        chartDescription.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));
        chartDescription.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        chartDescription.setTextSize(20f);

        for (int i = 0; i < players.size(); i++) {
            switch(dataType){
                case "drinks":
                    entries.add(new BarEntry(i + 1, players.get(i).getRPPD()));
                    maxFloat = maxFloat + 1;
                    chartDescription.setText("PTS/DRINK");
                    break;
                case "oz":
                    entries.add(new BarEntry(i + 1, players.get(i).getOzPerDrink()));
                    maxFloat = maxFloat + 1;
                    chartDescription.setText("AVG OZ/DRINK");
                    break;
                case "abv":
                    entries.add(new BarEntry(i + 1, players.get(i).getAvgABV()));
                    maxFloat = maxFloat + 1;
                    chartDescription.setText("AVG ABV/DRINK");
                    break;
                case "ibu":
                    entries.add(new BarEntry(i + 1, players.get(i).getIbuPerDrink()));
                    maxFloat = maxFloat + 1;
                    chartDescription.setText("AVG IBU/DRINK");
                    break;
                default:
                    entries.add(new BarEntry(0f,0f));
            }

            //maxFloat = maxFloat + 1;
        }
        //create dataSet
        BarDataSet dataSet = new BarDataSet(entries,"points");
        int[] colors = new int[maxFloat];
        for (int i = 0; i < maxFloat-1; i ++){
            if(userName.toUpperCase().equals(players.get(i).getName())){
                colors[i] = ContextCompat.getColor(getApplicationContext(),R.color.Goldenrod);
            } else {
                colors[i] = ContextCompat.getColor(getApplicationContext(),R.color.SlateGray);
            }
        }

        dataSet.setColors(colors);

        //setup Leader Board
        BarChart pointsLeaderBoard = new BarChart(getApplicationContext());

        //define chart data
        BarData data = new BarData(dataSet);
        data.setValueFormatter(new decimalValueFormatter());
        data.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.WhiteSmoke));
        data.setBarWidth(0.50f); // set the width of each bar
        data.setValueTextSize(10f);

        pointsLeaderBoard.setData(data);
        pointsLeaderBoard.setFitBars(false);
        pointsLeaderBoard.animateY(3000);
        pointsLeaderBoard.setDescription("");
        pointsLeaderBoard.setDescriptionColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        pointsLeaderBoard.setDescriptionTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        //pointsLeaderBoard.setDescriptionPosition(maxFloat/2,dataSet.getYMax());
        pointsLeaderBoard.setDescriptionTextSize(25f);
        pointsLeaderBoard.getXAxis().setDrawGridLines(false);
        pointsLeaderBoard.getAxisRight().setEnabled(false);
        pointsLeaderBoard.invalidate(); // refresh

        //define labels
        final ArrayList<String> labels = new ArrayList<>();
        labels.add("");
        for (int i = 0; i < players.size(); i++){
            labels.add(players.get(i).getName());
        }
        labels.add("");

        //set up formatting for labels
        String[] names = new String[labels.size()];
        names = labels.toArray(names);
        final String[] nameLabels = names;

        AxisValueFormatter formatter = new AxisValueFormatter() {
            @Override
            public String getFormattedValue(float value, AxisBase axis) {
                return nameLabels[(int)value];
            }

            @Override
            public int getDecimalDigits() {
                return 0;
            }
        };

        XAxis xAxis = pointsLeaderBoard.getXAxis();
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawAxisLine(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinValue(0f);
        xAxis.setAxisMaxValue(maxFloat);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        xAxis.setLabelCount(nameLabels.length, true);
        xAxis.setTextSize(10f);
        xAxis.setLabelRotationAngle(-90);


        YAxis yAxis = pointsLeaderBoard.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
        yAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        yAxis.setTextSize(15f);
        yAxis.setGridColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGoldenrod));

        //define layout parameters for charts
        LinearLayout.LayoutParams primaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.5f);

        LinearLayout.LayoutParams secondaryChartParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, 0, 0.5f);

        //define layout for chart to be applied and apply
        LinearLayout secondary = (LinearLayout) findViewById(R.id.secondaryChart);
        secondary.removeAllViews();
        secondary.addView(pointsLeaderBoard,secondaryChartParams);
        //LinearLayout primary = (LinearLayout) findViewById(R.id.primaryChart);
        //primary.removeAllViews();
        //primary.addView(pointsLeaderBoard,primaryChartParams);
        pointsLeaderBoard.invalidate(); // refresh
    }

    private class decimalValueFormatter implements ValueFormatter {
        @Override
        public String getFormattedValue(float value, Entry entry, int dataSetIndex, ViewPortHandler viewPortHandler){
            DecimalFormat decimalFormatter = new DecimalFormat("#0.00");
            return decimalFormatter.format(value);
        }
    }
}

