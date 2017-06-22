package mkawa.okhttp;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.GradientDrawable;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.RelativeLayout.LayoutParams;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.vstechlab.easyfonts.EasyFonts;

import java.io.IOException;
import java.util.ArrayList;
import java.lang.Exception;


public class beerSearch extends Activity
{


    protected void onCreate(Bundle savedInstanceState)
    {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beer_search);

        //set table for tokens
        TokenPurse start = new TokenPurse("sample",0);
        start.save();
        start.delete();


        //Set up Submit button that will query and run HTTP get request
        final ImageView button = (ImageView) findViewById(R.id.drinkSearchButton);
        if (button != null)
        {
            button.setOnClickListener(new View.OnClickListener()
            {
                public void onClick(View v)
                {
                    EditText drinkSearch = (EditText)findViewById(R.id.beerSearchQuery);
                    String drinkSearchPasser = null;
                    if (drinkSearch != null)
                    {
                        drinkSearchPasser = drinkSearch.getText().toString();
                    }
                    try
                    {
                        String source1 ="http://api.brewerydb.com/v2/search?q=";
                        String source2 ="&type=beer&withBreweries=y&";
                        String apiKey = "key=a0f0b3ee91ad3a9b1236295169b259b8";
                        String query = source1 + drinkSearchPasser + source2 + apiKey;
                        doIt(query);
                    }
                    catch (Exception e)
                    {
                        e.printStackTrace();
                    }

                    InputMethodManager inputManager = (InputMethodManager)
                            getSystemService(Context.INPUT_METHOD_SERVICE);

                    inputManager.hideSoftInputFromWindow((null == getCurrentFocus()) ? null : getCurrentFocus().getWindowToken(),
                            InputMethodManager.HIDE_NOT_ALWAYS);
                }
            });
        }
    }

    //Set up Web Client for data source
    OkHttpClient client = new OkHttpClient();

    //Method for retrieving Data on worker thread
    void doIt(String url) throws Exception
    {
        Request request = new Request.Builder()
        .url(url)
        .build();

        runOnUiThread(new Runnable()
        {
            @Override
            public void run()
            {
                ProgressBar loadingCircle = new ProgressBar(getApplicationContext());
                LinearLayout mainWindow = (LinearLayout) findViewById(R.id.resultScrollWindow);
                mainWindow.removeAllViewsInLayout();
                mainWindow.addView(loadingCircle);
            }
        });

    client.newCall(request).enqueue(new Callback()
    {
        @Override
        public void onFailure(Call call, IOException e)
        {
            e.printStackTrace();
        }

        @Override
        public void onResponse(Call call, Response response) throws IOException
        {
            if(!response.isSuccessful()) throw new IOException("unexpected code " + response);

            //get Web response and store into variable
            final String responseData = response.body().string();

            //Parse JSON
            JsonParser jsonParser = new JsonParser();
            JsonElement elem = jsonParser.parse(responseData);

            //Get # of Response Results
            JsonObject nResObj = elem.getAsJsonObject();
            JsonElement nResElem;
            int nResTemp;

            //Error handle to see if query exists
            if (nResObj.has("totalResults"))
            {
                nResElem = nResObj.get("totalResults");
                nResTemp = nResElem.getAsInt();
            }
            else
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        LinearLayout myLinearLayout = (LinearLayout) findViewById(R.id.resultScrollWindow);
                        myLinearLayout.removeAllViewsInLayout();
                        TextView noResultsText = new TextView(getApplicationContext());
                        noResultsText.setText("Could not find any results");
                        myLinearLayout.addView(noResultsText);
                    }
                });
                return;
            }
            final int nRes = Math.min(nResTemp,25);

            //Set JsonElement to JsonArray for looping
            JsonArray elemArr = elem.getAsJsonObject().getAsJsonArray("data").getAsJsonArray();

            //Set up ArrayList Containers
            final ArrayList<String> beerNames = new ArrayList<>();
            final ArrayList<Double> beerAbv = new ArrayList<>();
            final ArrayList<String> breweryName = new ArrayList<>();
            final ArrayList<String> styleName = new ArrayList<>();
            final ArrayList<Integer> beerIbu = new ArrayList<>();
            final ArrayList<ArrayList<String>> tokensList = new ArrayList<ArrayList<String>>();
            final ArrayList<String> brewCountry = new ArrayList<>();

            //Iterate to get all results and fill ArrayList Containers at "data" level
            for (JsonElement beerFeedback : elemArr){

                //Base JSON object for testing response fields
                JsonObject mainObjectTester = beerFeedback.getAsJsonObject();

                //NAME container
                JsonElement bName = beerFeedback.getAsJsonObject().get("name");
                beerNames.add(bName.toString());

                //BREWERY container
                try
                {
                    JsonArray brewNameArr = beerFeedback.getAsJsonObject().getAsJsonArray("breweries").getAsJsonArray();
                    JsonObject brewNameObj = brewNameArr.get(0).getAsJsonObject();
                    JsonElement brewName = brewNameObj.get("nameShortDisplay");
                    breweryName.add(brewName.toString());
                }
                catch (Exception e)
                {
                    breweryName.add("unknown");
                }

                //ABV container
                JsonElement bAbv = beerFeedback.getAsJsonObject().get("abv");
                if(bAbv != null)
                {
                    beerAbv.add(bAbv.getAsDouble());
                }
                else
                {
                    if (mainObjectTester.has("style"))
                    {
                        JsonObject abvMaxTest = beerFeedback.getAsJsonObject().get("style").getAsJsonObject();
                        JsonObject abvMinTest = beerFeedback.getAsJsonObject().get("style").getAsJsonObject();

                        Double abvMax;
                        Double abvMin;

                        if (abvMaxTest.has("abvMax"))
                        {
                            JsonElement abvMaxTemp = beerFeedback.getAsJsonObject().get("style").getAsJsonObject().get("abvMax");
                            abvMax = abvMaxTemp.getAsDouble();
                        }
                        else
                        {
                            abvMax = null;
                        }

                        if (abvMinTest.has("abvMin"))
                        {
                            JsonElement abvMinTemp = beerFeedback.getAsJsonObject().get("style").getAsJsonObject().get("abvMin");
                            abvMin = abvMinTemp.getAsDouble();
                        }
                        else
                        {
                            abvMin = null;
                        }
                        if(abvMax == null && abvMin == null)
                        {
                            beerAbv.add(0.0);
                        }
                        else if(abvMax == null)
                        {
                            beerAbv.add(abvMin);
                        }
                        else if(abvMin == null)
                        {
                            beerAbv.add(abvMax);
                        }
                        else
                        {
                            Double calcAbv = round((abvMax + abvMin)/2, 1);
                            beerAbv.add(calcAbv);
                        }
                    }
                    else
                    {
                        beerAbv.add(0.0);
                    }
                }

                //IBU container
                JsonElement bIbu = beerFeedback.getAsJsonObject().get("ibu");
                if(bIbu != null)
                {
                    Double temp = bIbu.getAsDouble();
                    beerIbu.add(temp.intValue());
                }
                else
                {
                    if(mainObjectTester.has("style"))
                    {
                        JsonElement ibuMax = beerFeedback.getAsJsonObject().get("style").getAsJsonObject().get("ibuMax");
                        JsonElement ibuMin = beerFeedback.getAsJsonObject().get("style").getAsJsonObject().get("ibuMin");

                        if(ibuMax == null && ibuMin == null)
                        {
                            beerIbu.add(0);
                        }
                        else if(ibuMax == null)
                        {
                            Double ibuMin1 = ibuMin.getAsDouble();
                            beerIbu.add(ibuMin1.intValue());
                        }
                        else if(ibuMin == null)
                        {
                            Double ibuMax1 = ibuMax.getAsDouble();
                            beerIbu.add(ibuMax1.intValue());
                        }
                        else
                        {
                            Double calcIbu = round((ibuMax.getAsDouble() + ibuMin.getAsDouble())/2, 1);
                            beerIbu.add(calcIbu.intValue());
                        }
                    }
                    else
                    {
                        beerIbu.add(0);
                    }
                }

                //COUNTRY container
                try
                {
                    JsonArray brewCountryArr = beerFeedback.getAsJsonObject().getAsJsonArray("breweries").get(0).getAsJsonObject().getAsJsonArray("locations").getAsJsonArray();
                    JsonObject brewCountryObj = brewCountryArr.get(0).getAsJsonObject().get("country").getAsJsonObject();
                    JsonElement countryIso = brewCountryObj.get("isoCode");
                    if(countryIso != null)
                    {
                        brewCountry.add(countryIso.toString());
                    }
                    else
                    {
                        brewCountry.add("unknown");
                    }
                }
                catch (Exception e)
                {
                    brewCountry.add("unknown");
                }

                //parse beer name as well and add to chip container
                ArrayList<String> nameChipCollection;

                BeerParser nameParse = new BeerParser();
                nameChipCollection = nameParse.beerDetermine(bName.toString());

                //STYLE container
                ArrayList<String> chipCollection = new ArrayList<>();
                ArrayList<String> masterChipColletion = new ArrayList<>();
                try
                {
                    JsonElement styleObj = beerFeedback.getAsJsonObject().get("style").getAsJsonObject().get("shortName");
                    styleName.add(styleObj.toString());
                    BeerParser parseMe = new BeerParser();
                    chipCollection = parseMe.beerDetermine(styleObj.toString());
                    for (int i = 0; i < nameChipCollection.size(); i++)
                    {
                        int duplicate = 0;
                        for (int k = 0; k < chipCollection.size(); k++)
                        {
                            if (nameChipCollection.get(i).equals(chipCollection.get(k)))
                            {
                                duplicate = 1;
                                break;
                            }
                        }
                        if(duplicate == 0)
                        {
                            masterChipColletion.add(nameChipCollection.get(i));
                        }
                    }
                    for (int n = 0; n < chipCollection.size(); n++)
                    {
                        masterChipColletion.add(chipCollection.get(n));
                    }
                    tokensList.add(masterChipColletion);
                } catch (Exception e)
                {
                    styleName.add("unknown");
                    chipCollection.add("unknown");
                    tokensList.add(masterChipColletion);
                }
            }

            //Send shit back to main UI thread
            runOnUiThread(new Runnable()
            {
                    @Override
                    public void run()
                    {
                        //Based on search results... create textViews dynamically

                        //Locate Linear Layout where TextView results are stored
                        LinearLayout myLinearLayout = (LinearLayout)findViewById(R.id.resultScrollWindow);

                        //Perform housekeeping and clear all current textViews in Layout
                        myLinearLayout.removeAllViewsInLayout();

                        final int N = nRes;  //total number of textViews to add
                        final TextView[] myTextViews = new TextView[N];  // create an empty array
                        for (int i = 0; i < N; i++)
                        {
                            //Set up Individual result Structure
                            RelativeLayout mainLayout = new RelativeLayout(getApplicationContext());
                            mainLayout.setId(6);
                            mainLayout.setClickable(true);
                            RelativeLayout leftLayout = new RelativeLayout(getApplicationContext());
                            leftLayout.setId(7);
                            RelativeLayout rightLayout = new RelativeLayout(getApplicationContext());
                            rightLayout.setId(8);
                            RelativeLayout secondaryLayout = new RelativeLayout(getApplicationContext());
                            secondaryLayout.setId(9);

                            GradientDrawable rightBackground = new GradientDrawable();
                            rightBackground.setCornerRadii(new float[]{0,0,20,20,20,20,0,0});
                            rightBackground.setColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkSlateGray));
                            rightLayout.setBackground(rightBackground);

                            GradientDrawable leftBackground = new GradientDrawable();
                            leftBackground.setCornerRadii(new float[]{20,20,0,0,0,0,20,20});
                            leftBackground.setColor(ContextCompat.getColor(getApplicationContext(), R.color.SlateGray));
                            leftLayout.setBackground(leftBackground);

                            mainLayout.setBackgroundColor(ContextCompat.getColor(getApplicationContext(), R.color.DarkGray));

                            RelativeLayout.LayoutParams mainLayoutParams = new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, 300);
                            RelativeLayout.LayoutParams leftLayoutParams = new RelativeLayout.LayoutParams(250  , LayoutParams.MATCH_PARENT);
                            RelativeLayout.LayoutParams rightLayoutParams= new RelativeLayout.LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.MATCH_PARENT);

                            mainLayout.setPadding(20,10,20,10);
                            leftLayout.setPadding(10,10,10,10);
                            rightLayout.setPadding(40,40,40,40);

                            //create new textView
                            final TextView beerNameTextView = new TextView(getApplicationContext());
                            beerNameTextView.setId(1);
                            final TextView breweryNameTextView = new TextView(getApplicationContext());
                            breweryNameTextView.setId(2);
                            breweryNameTextView.setPadding(0,10,0,0);
                            final TextView abvTextView = new TextView(getApplicationContext());
                            abvTextView.setId(3);
                            final TextView styleNameTextView = new TextView(getApplicationContext());
                            styleNameTextView.setId(4);
                            final TextView ibuTextView = new TextView(getApplicationContext());
                            ibuTextView.setId(10);

                            //create new imageView
                            final ProportionalImageView pic = new ProportionalImageView(getApplicationContext());
                            pic.setId(5);
                            pic.setPadding(20,20,20,20);
                            pic.setScaleType(ImageView.ScaleType.FIT_CENTER);


                            //set some text values
                            beerNameTextView.setText(beerNames.get(i).toLowerCase().replaceAll("\"", ""));
                            breweryNameTextView.setText(breweryName.get(i).toLowerCase().replaceAll("\"", ""));
                            abvTextView.setText(beerAbv.get(i).toString() + "%");
                            styleNameTextView.setText(styleName.get(i).toLowerCase().replaceAll("\"", ""));
                            ibuTextView.setText(beerIbu.get(i).toString());

                            //set fonts
                            beerNameTextView.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
                            beerNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,28);
                            breweryNameTextView.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
                            breweryNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,10);
                            styleNameTextView.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
                            styleNameTextView.setTextSize(TypedValue.COMPLEX_UNIT_SP,20);
                            styleNameTextView.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.Goldenrod));
                            abvTextView.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
                            ibuTextView.setTypeface(EasyFonts.ostrichRounded(getApplicationContext()));

                            //set image
                            try
                            {
                                int countryPicId = getResources().getIdentifier(brewCountry.get(i).toLowerCase().replaceAll("\"", ""),"mipmap", getPackageName());
                                pic.setImageResource(countryPicId);
                            }
                            catch (Exception e)
                            {
                                int defaultId = getResources().getIdentifier("bluediamond","mipmap", getPackageName());
                                pic.setImageResource(defaultId);
                            }

                            //add TextView properties if required
                            leftLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);
                            rightLayoutParams.addRule(RelativeLayout.RIGHT_OF, leftLayout.getId());
                            rightLayoutParams.addRule(RelativeLayout.ALIGN_LEFT);

                            //apply layoutParams//
                            //breweryNameTextView Parameters
                            RelativeLayout.LayoutParams brewNameParam = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            brewNameParam.addRule(RelativeLayout.BELOW, beerNameTextView.getId());

                            //styleNameTextView Parameters
                            RelativeLayout.LayoutParams styleNameParam = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            styleNameParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            styleNameParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                            //picImageView Parameters
                            RelativeLayout.LayoutParams picParam = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            picParam.addRule(RelativeLayout.ALIGN_PARENT_TOP);
                            picParam.addRule(RelativeLayout.CENTER_HORIZONTAL);

                            //abvTextView Parameters
                            RelativeLayout.LayoutParams abvParam = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            abvParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            abvParam.addRule(RelativeLayout.ALIGN_PARENT_LEFT);

                            //ibuTextView Parameters
                            RelativeLayout.LayoutParams ibuParam = new RelativeLayout.LayoutParams(
                                    ViewGroup.LayoutParams.WRAP_CONTENT,
                                    ViewGroup.LayoutParams.WRAP_CONTENT);
                            ibuParam.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);
                            ibuParam.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);

                            //add the textViews and Layouts to the Main layout
                                myLinearLayout.addView(mainLayout, mainLayoutParams);
                                mainLayout.addView(leftLayout, leftLayoutParams);
                                mainLayout.addView(rightLayout, rightLayoutParams);
                                leftLayout.addView(pic, picParam);
                                leftLayout.addView(abvTextView, abvParam);
                                leftLayout.addView(ibuTextView, ibuParam);
                                rightLayout.addView(beerNameTextView);
                                rightLayout.addView(breweryNameTextView, brewNameParam);
                                rightLayout.addView(styleNameTextView, styleNameParam);

                            final int recallNum = i;

                            mainLayout.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    //send variables over to contactActivity.java
                                    String beerSend = beerNames.get(recallNum);
                                    String styleSend = styleName.get(recallNum);
                                    String brewerySend = breweryName.get(recallNum);
                                    String abvSend = beerAbv.get(recallNum).toString();
                                    String ibuSend = beerIbu.get(recallNum).toString();
                                    ArrayList<String> tokensSend = tokensList.get(recallNum);

                                    Intent selectionSend = new Intent(getApplicationContext(), contactActivity.class);
                                    selectionSend.putExtra("beerName",beerSend);
                                    selectionSend.putExtra("styleName",styleSend);
                                    selectionSend.putExtra("breweryName",brewerySend);
                                    selectionSend.putExtra("abv",abvSend);
                                    selectionSend.putExtra("ibu",ibuSend);
                                    selectionSend.putExtra("tokens", tokensSend);
                                    startActivity(selectionSend);
                                    finish();
                                }
                            });
                        }
                    }
                });
            }
        });
    }

    private static double round (double value, int precision)
    {
        int scale =  (int) Math.pow(10, precision);
        return  (double) Math.round(value * scale)/scale;
    }
}
