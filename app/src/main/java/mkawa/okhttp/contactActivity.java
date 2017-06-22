package mkawa.okhttp;


    import android.app.Activity;
    import android.content.Context;
    import android.content.DialogInterface;
    import android.content.Intent;
    import android.content.SharedPreferences;
    import android.graphics.Bitmap;
    import android.graphics.Matrix;
    import android.graphics.drawable.BitmapDrawable;
    import android.graphics.drawable.Drawable;
    import android.os.AsyncTask;
    import android.os.Bundle;
    import android.support.v4.content.ContextCompat;
    import android.support.v7.app.AlertDialog;
    import android.text.TextUtils;
    import android.util.Log;
    import android.util.TypedValue;
    import android.view.View;
    import android.view.ViewGroup;
    import android.view.inputmethod.InputMethodManager;
    import android.widget.Button;
    import android.widget.EditText;
    import android.widget.GridLayout;
    import android.widget.ImageView;
    import android.widget.LinearLayout;
    import android.widget.ProgressBar;
    import android.widget.TextView;
    import android.widget.Toast;

    import com.github.mikephil.charting.charts.HorizontalBarChart;
    import com.github.mikephil.charting.components.AxisBase;
    import com.github.mikephil.charting.components.XAxis;
    import com.github.mikephil.charting.components.YAxis;
    import com.github.mikephil.charting.data.BarData;
    import com.github.mikephil.charting.data.BarDataSet;
    import com.github.mikephil.charting.data.BarEntry;
    import com.github.mikephil.charting.formatter.AxisValueFormatter;
    import com.github.mikephil.charting.utils.ColorTemplate;
    import com.google.api.client.repackaged.org.apache.commons.codec.binary.StringUtils;
    import com.google.gson.JsonArray;
    import com.google.gson.JsonElement;
    import com.google.gson.JsonParser;
    import com.vstechlab.easyfonts.EasyFonts;

    import okhttp3.Call;
    import okhttp3.Callback;
    import okhttp3.MediaType;
    import okhttp3.OkHttpClient;
    import okhttp3.Request;
    import okhttp3.RequestBody;
    import okhttp3.Response;

    import java.io.IOException;
    import java.io.UnsupportedEncodingException;
    import java.net.URLEncoder;
    import java.util.ArrayList;
    import java.util.List;
    import java.util.NoSuchElementException;


public class contactActivity extends Activity {

    public static final MediaType FORM_DATA_TYPE
            = MediaType.parse("application/x-www-form-urlencoded; charset=utf-8");
    //URL derived from form URL
    public static final String URL = "https://docs.google.com/forms/d/1xVzwPQyQdasuy2wSjJnQ1v6Vn9o_I9czcCHN42-9qYc/formResponse";
    //input element ids found from the live form page
    public static final String NAME_KEY = "entry.468624322";
    public static final String DRINK_KEY = "entry.1047701293";
    public static final String ABV_KEY = "entry.203930076";
    public static final String IBU_KEY = "entry.1323911453";
    public static final String OZ_KEY = "entry.1808952527";
    public static final String TEAM_KEY = "entry.2118900696";


    private Context context;
    private EditText nameEditText;
    private EditText drinkEditText;
    private EditText breweryEditText;
    private EditText styleEditText;
    private EditText abvEditText;
    private EditText ibuEditText;
    private EditText ozEditText;
    private TextView abvTextView;
    private TextView ozTextView;
    private TextView ibuTextView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_contact);


        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE", MODE_PRIVATE);
        final String playerUserName = sharedPreferences.getString("USERNAME", "");
        final String playerTeam = sharedPreferences.getString("TEAM","");

        //save the activity in a context variable to be used afterwards
        context = this;
        final String query = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/2/public/full?alt=json";

        try {
            requestSpreadsheetData(query, playerUserName);
        } catch (Exception e) {
            e.printStackTrace();
        }

        final ArrayList<String> tokenList = getIntent().getStringArrayListExtra("tokens");


        //Get references to UI elements in the layout
        Button sendButton = (Button) findViewById(R.id.sendButton);
        nameEditText = (EditText) findViewById(R.id.nameEditText);
        nameEditText.setTypeface(EasyFonts.robotoThinItalic(this));
        nameEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.LightCyan));

        drinkEditText = (EditText) findViewById(R.id.drinkEditText);
        drinkEditText.setTypeface(EasyFonts.ostrichRegular(this));
        drinkEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 40);
        drinkEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        breweryEditText = (EditText) findViewById(R.id.breweryEditText);
        breweryEditText.setTypeface(EasyFonts.ostrichBlack(this));
        breweryEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        breweryEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        styleEditText = (EditText) findViewById(R.id.styleEditText);
        styleEditText.setTypeface(EasyFonts.ostrichRegular(this));
        styleEditText.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14);
        styleEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        abvEditText = (EditText) findViewById(R.id.abvEditText);
        abvEditText.setTypeface(EasyFonts.robotoLight(this));
        abvEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        ibuEditText = (EditText) findViewById(R.id.ibuEditText);
        ibuEditText.setTypeface(EasyFonts.robotoLight(this));
        ibuEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        ozEditText = (EditText) findViewById(R.id.ozEditText);
        ozEditText.setTypeface(EasyFonts.robotoLight(this));
        ozEditText.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Purple));

        abvTextView = (TextView) findViewById(R.id.abvHeader);
        abvTextView.setTypeface(EasyFonts.ostrichBlack(this));

        ozTextView = (TextView) findViewById(R.id.ozHeader);
        ozTextView.setTypeface(EasyFonts.ostrichBlack(this));

        ibuTextView = (TextView) findViewById(R.id.ibuHeader);
        ibuTextView.setTypeface(EasyFonts.ostrichBlack(this));

        nameEditText.setText(playerUserName);

        if (sendButton != null) {
            sendButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    //MAKE SURE ALL FIELDS ARE FILLED WITH VALUES
                    if (TextUtils.isEmpty(nameEditText.getText().toString()) ||
                            TextUtils.isEmpty(drinkEditText.getText().toString()) ||
                            TextUtils.isEmpty(abvEditText.getText().toString()) ||
                            TextUtils.isEmpty(ibuEditText.getText().toString()) ||
                            TextUtils.isEmpty(ozEditText.getText().toString()))
                    {
                        Toast.makeText(context, "All fields are mandatory.", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //CHECK IF NUMBER IS PROVIDED
                    if (!TextUtils.isDigitsOnly(ozEditText.getText().toString()))
                    {
                        Toast.makeText(context, "OZ Must Be a Whole Number", Toast.LENGTH_LONG).show();
                        return;
                    }

                    //Create an object for PostDataTask AsyncTask
                    final PostDataTask postDataTask = new PostDataTask();

                    final sendShitParams params = new sendShitParams(
                            true,
                            query,
                            nameEditText.getText().toString(),
                            tokenList,
                            URL,
                            drinkEditText.getText().toString(),
                            abvEditText.getText().toString(),
                            ibuEditText.getText().toString(),
                            ozEditText.getText().toString(),
                            null, null, null, null, null, 0, playerTeam);

                    //execute asynctask
                    if (Float.valueOf(params.oz) < 8) {
                        new AlertDialog.Builder(context)
                                .setTitle("Confirm Submission")
                                .setMessage("You will not receive tokens because ounces < 8.  OK to proceed?")
                                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        postDataTask.execute(params);
                                    }
                                })
                                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog, int which) {
                                        // do nothing
                                    }
                                })
                                .setIcon(android.R.drawable.ic_dialog_alert)
                                .show();
                    } else {
                        postDataTask.execute(params);
                    }

                    //hide keyboard
                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);

                }
            });
        }

        //Locate Linear Layout where TextView results are stored
        LinearLayout myLinearLayout = (LinearLayout) findViewById(R.id.tokenList);

        //Perform housekeeping and clear all current textViews in Layout
        if (myLinearLayout != null) {
            myLinearLayout.removeAllViewsInLayout();
        }

        //retrieve information from beerSearch.java
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            drinkEditText.setText(extras.getString("beerName").replaceAll("\"", ""));
            breweryEditText.setText(extras.getString("breweryName").replaceAll("\"", ""));
            styleEditText.setText(extras.getString("styleName").replaceAll("\"", ""));
            abvEditText.setText(extras.getString("abv"));
            ibuEditText.setText(extras.getString("ibu"));

            final ArrayList<String> stock_list = getIntent().getStringArrayListExtra("tokens");
            int listLen = stock_list.size();

            for (int i = 0; i < listLen; i++) {

                ImageView tokenImageView = new ImageView(this);

                if (!stock_list.get(i).equals("Nothing to See here")) {
                    int token = getResources().getIdentifier(stock_list.get(i).toLowerCase().replaceAll("\"", ""), "mipmap", getPackageName());
                    tokenImageView.setImageResource(token);
                    tokenImageView.setAdjustViewBounds(true);

                    if (myLinearLayout != null) {
                        myLinearLayout.addView(tokenImageView);
                    }
                }
            }
        }


    }//END ON CREATE

    //AsyncTask to send data as a http POST request
    private class PostDataTask extends AsyncTask<sendShitParams, Void, sendShitParams> {

        @Override
        protected sendShitParams doInBackground(sendShitParams... params) {
            Boolean result = params[0].test;
            String url = params[0].postUrl;
            String name = params[0].playerName;
            String drink = params[0].drink;
            String abv = params[0].abv;
            String ibu = params[0].ibu;
            String oz = params[0].oz;
            String postBody = "";
            String query = params[0].ssUrl;
            ArrayList<String> tokens = params[0].tokens;
            String team = params[0].teamName;

            sendShitParams w = new sendShitParams(result, query, name, tokens, url, drink, abv, ibu, oz, null, null, null, null, null, 0, team);

            try {
                //all values must be URL encoded to make sure that special characters like & | ",etc.
                //do not cause problems
                postBody = NAME_KEY + "=" + URLEncoder.encode(name, "UTF-8") +
                        "&" + DRINK_KEY + "=" + URLEncoder.encode(drink, "UTF-8") +
                        "&" + ABV_KEY + "=" + URLEncoder.encode(abv, "UTF-8") +
                        "&" + IBU_KEY + "=" + URLEncoder.encode(ibu, "UTF-8") +
                        "&" + OZ_KEY + "=" + URLEncoder.encode(oz, "UTF-8") +
                        "&" + TEAM_KEY + "=" + URLEncoder.encode(team, "UTF-8") ;
            } catch (UnsupportedEncodingException ex) {

                w.test = false;
            }

            try {
                //Create OkHttpClient for sending request
                OkHttpClient client = new OkHttpClient();
                //Create the request body with the help of Media Type
                RequestBody body = RequestBody.create(FORM_DATA_TYPE, postBody);
                Request request = new Request.Builder()
                        .url(url)
                        .post(body)
                        .build();
                //Send the request
                Response response = client.newCall(request).execute();
            } catch (IOException exception) {
                w.test = false;
            }
            return w;
        }

        @Override
        protected void onPostExecute(sendShitParams w) {
            //Print Success or failure message accordingly
            Toast.makeText(context, w.test ? "Submission Successful" : "There was some error in sending message. Please try again after some time.", Toast.LENGTH_LONG).show();


            final String query = w.ssUrl;
            final String user = w.playerName;
            final ArrayList<String> tokenList = w.tokens;
            final float ozSub = Float.valueOf(w.oz);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    ProgressBar loadingCircle = new ProgressBar(getApplicationContext());
                    LinearLayout mainWindow = (LinearLayout) findViewById(R.id.userStatChart);
                    mainWindow.removeAllViewsInLayout();
                    mainWindow.addView(loadingCircle);

                    //re run spreadsheet query
                    try {
                        requestSpreadsheetData(query, user);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    //Locate Linear Layout where TextView results are stored
                    LinearLayout myLinearLayout = (LinearLayout) findViewById(R.id.tokenList);

                    //Perform housekeeping and clear all current textViews in Layout
                    if (myLinearLayout != null) {
                        myLinearLayout.removeAllViewsInLayout();
                    }

                    //Handle Tokens
                    if (ozSub >= 8) {
                        tokenConfig(tokenList);
                    } else {
                        //do not apply tokens
                    }
                }
            });
        }
    }

    //Pull Data From Spreadsheet
    OkHttpClient client = new OkHttpClient();

    void requestSpreadsheetData(String url, final String userName) throws Exception {
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
                final ArrayList<PlayerGroup> sortCont = new ArrayList<>();

                //Iterate to get all results and fill ArrayList Containers at "data" level
                for (JsonElement sheetsResponse : elemArr) {
                    PlayerGroup group = new PlayerGroup();

                    //Player Name Fill Container
                    JsonElement nameField = sheetsResponse.getAsJsonObject().get("gsx$name").getAsJsonObject().get("$t");
                    group.player = nameField.toString().replaceAll("\"", "");

                    //Ounces Fill Container
                    JsonElement ozField = sheetsResponse.getAsJsonObject().get("gsx$totaloz").getAsJsonObject().get("$t");
                    group.oz = ozField.getAsInt();

                    //Drink # Fill Container
                    JsonElement drinkField = sheetsResponse.getAsJsonObject().get("gsx$totaldrinks").getAsJsonObject().get("$t");
                    group.drinks = drinkField.getAsInt();

                    //IBU Fill Container
                    JsonElement ibuField = sheetsResponse.getAsJsonObject().get("gsx$totalibu").getAsJsonObject().get("$t");
                    group.ibu = ibuField.getAsInt();

                    //ABVoz Fill Container
                    JsonElement abvField = sheetsResponse.getAsJsonObject().get("gsx$totalabvoz").getAsJsonObject().get("$t");
                    group.abv = abvField.getAsFloat();

                    sortCont.add(group);

                }

                //Set up Personal Stats
                final PlayerStats userStats = new PlayerStats();

                for (int i = 0; i < sortCont.size(); i++) {
                    if (userName.toUpperCase().equals(sortCont.get(i).player)) {
                        userStats.setName(sortCont.get(i).player);
                        userStats.setDrinks(sortCont.get(i).drinks);
                        userStats.setOz(sortCont.get(i).oz);
                        userStats.setAbv(sortCont.get(i).abv);
                        userStats.setIbu(sortCont.get(i).ibu);
                    }
                }



                //Set up UI and pass variables from Async
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        /*
                        //Add Values to chart
                        ArrayList<BarEntry> entries = new ArrayList<>();
                        entries.add(new BarEntry(1f, userStats.getIbuPct()));
                        entries.add(new BarEntry(3f, userStats.getAbvPct()));
                        entries.add(new BarEntry(5f, userStats.getOzPct()));
                        entries.add(new BarEntry(7f, userStats.getDrinkPct()));

                        //create dataSet
                        BarDataSet dataSet = new BarDataSet(entries, "points");
                        dataSet.setColors(ColorTemplate.PASTEL_COLORS);

                        //setup data
                        HorizontalBarChart userStats = new HorizontalBarChart(getApplicationContext());

                        //define labels
                        final ArrayList<String> labels = new ArrayList<>();
                        labels.add("DRINKS");
                        labels.add("OUNCES");
                        labels.add("ABV");
                        labels.add("IBU");

                        //define chart data
                        BarData data = new BarData(dataSet);
                        data.setValueFormatter(new MyValueFormatter());
                        data.setValueTextColor(ContextCompat.getColor(getApplicationContext(), R.color.SlateGray));
                        data.setBarWidth(0.50f); // set the width of each bar
                        data.setValueTextSize(25f);

                        userStats.setData(data);
                        userStats.setFitBars(false);
                        userStats.animateY(3000);
                        userStats.getXAxis().setDrawGridLines(false);
                        userStats.getAxisRight().setEnabled(false);
                        userStats.invalidate(); // refresh

                        //set up formatting for labels

                        AxisValueFormatter formatter = new AxisValueFormatter() {
                            @Override
                            public String getFormattedValue(float value, AxisBase axis) {
                                switch ((int)value){
                                    case 1:
                                        return "IBU";
                                    case 3:
                                        return "ABV";
                                    case 5:
                                        return "OUNCES";
                                    case 7:
                                        return "DRINKS";
                                    default:
                                        return "";
                                }
                            }

                            @Override
                            public int getDecimalDigits() {
                                return 0;
                            }
                        };


                        XAxis xAxis = userStats.getXAxis();
                        xAxis.setDrawAxisLine(false);
                        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
                        xAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
                        xAxis.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
                        xAxis.setTextSize(15f);
                        xAxis.setAxisMinValue(0f);
                        xAxis.setAxisMaxValue(8f);
                        xAxis.setGranularity(1f);
                        xAxis.setLabelCount(9, true);
                        xAxis.setValueFormatter(formatter);

                        YAxis yAxis = userStats.getAxisLeft();
                        yAxis.setDrawAxisLine(false);
                        yAxis.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
                        yAxis.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
                        yAxis.setTextSize(15f);
                        yAxis.setAxisMinValue(0f);
                        yAxis.setAxisMaxValue(1f);
                        yAxis.setGridColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGoldenrod));

                        //define layout parameters for chart
                        LinearLayout.LayoutParams pntChrtParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);

                        //define layout for chart to be applied and apply
                        LinearLayout userStatChart = (LinearLayout) findViewById(R.id.userStatChart);
                        if(userStatChart != null){
                            userStatChart.addView(userStats, pntChrtParams);
                        }
                        */



                    }
                });

            }
        });

    }

    public class sendShitParams {
        public Boolean test = true;
        public String ssUrl;
        public String playerName;
        public ArrayList<String> tokens;
        public String postUrl;
        public String drink;
        public String abv;
        public String ibu;
        public String oz;
        public String tradeToken;
        public String drinkPoints;
        public String ozPoints;
        public String abvPoints;
        public String ibuPoints;
        public float keepStock;
        public String teamName;

        sendShitParams(Boolean test,
                       String ssUrl,
                       String playerName,
                       ArrayList<String> tokens,
                       String postUrl,
                       String drink,
                       String abv,
                       String ibu,
                       String oz,
                       String tradeToken,
                       String drinkPoints,
                       String ozPoints,
                       String abvPoints,
                       String ibuPoints,
                       float keepStock,
                       String teamName) {
            this.test = test;
            this.ssUrl = ssUrl;
            this.playerName = playerName;
            this.tokens = tokens;
            this.postUrl = postUrl;
            this.drink = drink;
            this.abv = abv;
            this.ibu = ibu;
            this.oz = oz;
            this.tradeToken = tradeToken;
            this.drinkPoints = drinkPoints;
            this.ozPoints = ozPoints;
            this.abvPoints = abvPoints;
            this.ibuPoints = ibuPoints;
            this.keepStock = keepStock;
            this.teamName = teamName;
        }


    }

    // method for handling tokens
    public void tokenConfig(ArrayList<String> tokenList) {
        List<TokenPurse> token_purse;
        long numberOfTokens = TokenPurse.count(TokenPurse.class, null, null);
        token_purse = TokenPurse.listAll(TokenPurse.class);
        int duplicate = 0;
        if (numberOfTokens < 0) {
            for (int k = 0; k < tokenList.size(); k++) {
                TokenPurse token = new TokenPurse();
                token.tokenName = tokenList.get(k);
                token.tokenShare = 1;
                token.save();
                System.out.println(token.tokenName + " has been added to purse for 1st time");
            }
        } else {
            for (int i = 0; i < tokenList.size(); i++) {
                for (int j = 0; j < numberOfTokens; j++) {
                    if (tokenList.get(i).equals(token_purse.get(j).tokenName)) {
                        token_purse.get(j).tokenShare = token_purse.get(j).tokenShare + 1;
                        token_purse.get(j).save();
                        System.out.println(token_purse.get(i).tokenName + " was already in purse, Shares are now at: " + token_purse.get(i).tokenShare);
                        duplicate = 1;
                        break;
                    }
                }
                if (duplicate == 0) {
                    TokenPurse token = new TokenPurse();
                    token.tokenName = tokenList.get(i);
                    token.tokenShare = 1;
                    token.save();
                    System.out.println(token.tokenName + " was put in purse for 1st time");
                }
                duplicate = 0;
            }
        }
        //If Data successfully sent- then clear all fields
        drinkEditText.setText("");
        abvEditText.setText("");
        ibuEditText.setText("");
        ozEditText.setText("");
        breweryEditText.setText("");
        styleEditText.setText("");

        Intent allDone = new Intent(getApplicationContext(), Dashboard.class);
        startActivity(allDone);
        finish();
    }
}

