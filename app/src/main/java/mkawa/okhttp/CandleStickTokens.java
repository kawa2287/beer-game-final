package mkawa.okhttp;

import android.app.Activity;
import android.graphics.Paint;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.View;
import android.widget.TextView;

import com.github.mikephil.charting.charts.CandleStickChart;
import com.github.mikephil.charting.components.AxisBase;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.CandleData;
import com.github.mikephil.charting.data.CandleDataSet;
import com.github.mikephil.charting.data.CandleEntry;
import com.github.mikephil.charting.formatter.AxisValueFormatter;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class CandleStickTokens extends Activity {

    final long numberOfTokens = TokenPurse.count(TokenPurse.class,null,null);
    final String query = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/3/public/full?alt=json";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_candle_stick_tokens);

        TextView chartHeader = (TextView) findViewById(R.id.chartHeader);
        chartHeader.setText("TOKEN HISTORY");
        chartHeader.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        chartHeader.setTextSize(30f);
        chartHeader.setTextColor(ContextCompat.getColor(getApplicationContext(),R.color.WhiteSmoke));

        //hit marketDatabase for token Info
        try {
            requestSpreadsheetData(query);
        } catch (Exception e) {
            e.printStackTrace();
        }


    } //END ON CREATE

    //Method to Pull Data From Spreadsheet
    OkHttpClient client = new OkHttpClient();
    void requestSpreadsheetData(String url) throws Exception {
        Request request = new Request.Builder()
                .url(url)
                .build();

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
                final ArrayList<CandleGroup> sortCont = new ArrayList<>();

                //list tokens from purse and user's shares
                final List<TokenPurse> token_list;
                token_list = TokenPurse.listAll(TokenPurse.class);

                //Iterate to get all results and fill ArrayList Containers at "data" level
                for (JsonElement sheetsResponse : elemArr) {

                    CandleGroup group = new CandleGroup();

                    //Type Container
                    JsonElement typeField = sheetsResponse.getAsJsonObject().get("gsx$key").getAsJsonObject().get("$t");
                    group.type = typeField.toString().replaceAll("\"", "");
                    for (int i = 0; i < numberOfTokens;i++){
                        if (token_list.get(i).tokenName.equals(typeField.toString().replaceAll("\"", "")))
                        {
                            group.userHas = 1;
                        }
                        else
                        {
                            group.userHas = 0;
                        }
                    }

                    //Current Value Container
                    JsonElement curValField = sheetsResponse.getAsJsonObject().get("gsx$valuecurrent").getAsJsonObject().get("$t");
                    group.curVal = curValField.getAsFloat();

                    //Current Value Container
                    JsonElement lowRecField = sheetsResponse.getAsJsonObject().get("gsx$lowestrecordedval").getAsJsonObject().get("$t");
                    group.lowRecVal = lowRecField.getAsFloat();

                    //Value +1 Container
                    JsonElement highRecField = sheetsResponse.getAsJsonObject().get("gsx$highestrecordedval").getAsJsonObject().get("$t");
                    group.highRecVal = highRecField.getAsFloat();

                    //Value +2 Container
                    JsonElement curLowField = sheetsResponse.getAsJsonObject().get("gsx$currentlow").getAsJsonObject().get("$t");
                    group.curLowVal = curLowField.getAsFloat();

                    //Value +3 Container
                    JsonElement curHighField = sheetsResponse.getAsJsonObject().get("gsx$currenthigh").getAsJsonObject().get("$t");
                    group.curHighVal = curHighField.getAsFloat();

                    //Add to master container
                    sortCont.add(group);
                }

                //Sort based on curVal
                Comparator<CandleGroup> stockComp = new Comparator<CandleGroup>() {
                    @Override
                    public int compare(CandleGroup a, CandleGroup b) {
                        return Double.compare(a.curVal, b.curVal);
                    }
                };
                Collections.sort(sortCont, stockComp);
                Collections.reverse(sortCont);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run()
                    {
                        printCandles(sortCont);
                    }
                });
            }
        });
    }

    private class CandleGroup {
        public String type;
        public float curVal;
        public float lowRecVal;
        public float highRecVal;
        public float curLowVal;
        public float curHighVal;
        public int userHas;
    }

    private void printCandles(ArrayList<CandleGroup> candleGroup)
    {
        CandleStickChart candleStickChart = (CandleStickChart) findViewById(R.id.candleStickChart);

        //POPULATE DATA AND LABELS
        ArrayList<CandleEntry> entries = new ArrayList<>();
        ArrayList<String> labels = new ArrayList<>();
        float maxFloat = 1;
        labels.add("");
        for (int i = 0; i < candleGroup.size(); i++)
        {
            entries.add(new CandleEntry(i+1,candleGroup.get(i).highRecVal,candleGroup.get(i).lowRecVal,candleGroup.get(i).curLowVal,candleGroup.get(i).curHighVal));
            labels.add(candleGroup.get(i).type);
            maxFloat = maxFloat + 1;
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


        CandleDataSet dataSet = new CandleDataSet(entries,"");
        dataSet.setShadowWidth(0.7f);
        dataSet.setShowCandleBar(true);
        dataSet.setShadowColor(ContextCompat.getColor(getApplicationContext(),R.color.NavajoWhite));
        dataSet.setDecreasingPaintStyle(Paint.Style.FILL);
        dataSet.setIncreasingColor(ContextCompat.getColor(getApplicationContext(),R.color.NavajoWhite));
        dataSet.setIncreasingPaintStyle(Paint.Style.FILL);
        dataSet.setNeutralColor(ContextCompat.getColor(getApplicationContext(),R.color.DodgerBlue));
        //dataSet.setColors(ColorTemplate.COLORFUL_COLORS);

        CandleData data = new CandleData(dataSet);
        data.setValueFormatter(new MyValueFormatter());
        data.setDrawValues(false);
        candleStickChart.setData(data);

        XAxis xAxis = candleStickChart.getXAxis();
        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        xAxis.setValueFormatter(formatter);
        xAxis.setDrawAxisLine(false);
        xAxis.setDrawGridLines(false);
        xAxis.setGranularity(1f);
        xAxis.setAxisMinValue(0f);
        xAxis.setAxisMaxValue(maxFloat);
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        //xAxis.setLabelCount(nameLabels.length, true);
        xAxis.setTextSize(12f);
        xAxis.setLabelRotationAngle(-90);


        YAxis yAxis = candleStickChart.getAxisLeft();
        yAxis.setDrawAxisLine(false);
        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
        yAxis.setAxisMinValue(0f);
        yAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        yAxis.setTextSize(15f);
        yAxis.setGridColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGoldenrod));

        candleStickChart.getAxisRight().setEnabled(false);

        candleStickChart.setDragEnabled(true);
        candleStickChart.setPinchZoom(true);
        candleStickChart.setDragDecelerationEnabled(true);
        candleStickChart.setTouchEnabled(true);
        candleStickChart.setDragDecelerationFrictionCoef(0.5f);
        candleStickChart.setScaleXEnabled(true);
        candleStickChart.setDescription("");
        candleStickChart.setNoDataText("LOADING... HOLD YOUR TITS");
        candleStickChart.setNoDataTextDescription("");
        candleStickChart.invalidate();


    }

}
