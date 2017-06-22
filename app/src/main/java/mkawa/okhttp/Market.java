package mkawa.okhttp;



import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Comparator;


import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class Market extends Activity implements TokenGraphAdapter.ClickListener {

    private List<TokenGraph> tokenGraphList = new ArrayList<>();
    private TokenGraphAdapter TGadapter;
    final long numberOfTokens = TokenPurse.count(TokenPurse.class,null,null);
    final String query = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/3/public/full?alt=json";
    float maxStockVal;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_market);

        //hit marketDatabase for token Info
        try {
            requestSpreadsheetData(query);
        } catch (Exception e) {
            e.printStackTrace();
        }

        //prepare recyclerView
        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.marketScrollWindow);
        TGadapter = new TokenGraphAdapter(tokenGraphList);
        TGadapter.setClickListener(this);
        RecyclerView.LayoutManager mLayoutManager = new LinearLayoutManager(getApplicationContext(),LinearLayoutManager.VERTICAL,false);
        recyclerView.setLayoutManager(mLayoutManager);
        recyclerView.setItemAnimator(new DefaultItemAnimator());
        recyclerView.setAdapter(TGadapter);
        recyclerView.setHasFixedSize(true);


        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        maxStockVal = settings.maxStockVal;

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
                final ArrayList<StockGroup>  sortCont = new ArrayList<>();

                //list tokens from purse and user's shares
                final List<TokenPurse> token_list;
                token_list = TokenPurse.listAll(TokenPurse.class);

                //Iterate to get all results and fill ArrayList Containers at "data" level
                for (JsonElement sheetsResponse : elemArr) {

                    StockGroup group = new StockGroup();

                    //Type Container
                    JsonElement typeField = sheetsResponse.getAsJsonObject().get("gsx$key").getAsJsonObject().get("$t");
                    group.type = typeField.toString().replaceAll("\"", "");
                    for (int i = 0; i < numberOfTokens;i++){
                        if (token_list.get(i).tokenName.equals(typeField.toString().replaceAll("\"", ""))){
                            group.purseVal = token_list.get(i).tokenShare;
                        }
                    }

                    //Current Value Container
                    JsonElement curValField = sheetsResponse.getAsJsonObject().get("gsx$valuecurrent").getAsJsonObject().get("$t");
                    group.curVal = curValField.getAsFloat();

                    //Value +1 Container
                    JsonElement val1Field = sheetsResponse.getAsJsonObject().get("gsx$valueprev1").getAsJsonObject().get("$t");
                    group.val1 = val1Field.getAsFloat();

                    //Value +2 Container
                    JsonElement val2Field = sheetsResponse.getAsJsonObject().get("gsx$valueprev2").getAsJsonObject().get("$t");
                    group.val2 = val2Field.getAsFloat();

                    //Value +3 Container
                    JsonElement val3Field = sheetsResponse.getAsJsonObject().get("gsx$valueprev3").getAsJsonObject().get("$t");
                    group.val3 = val3Field.getAsFloat();

                    //Value +4 Container
                    JsonElement val4Field = sheetsResponse.getAsJsonObject().get("gsx$valueprev4").getAsJsonObject().get("$t");
                    group.val4 = val4Field.getAsFloat();

                    //Value +5 Container
                    JsonElement val5Field = sheetsResponse.getAsJsonObject().get("gsx$valueprev5").getAsJsonObject().get("$t");
                    group.val5 = val5Field.getAsFloat();

                    //baseVal Container
                    JsonElement baseValField = sheetsResponse.getAsJsonObject().get("gsx$baseval").getAsJsonObject().get("$t");
                    group.baseVal = baseValField.getAsFloat();

                    //bv1 Container
                    JsonElement bv1Field = sheetsResponse.getAsJsonObject().get("gsx$basevalprev1").getAsJsonObject().get("$t");
                    group.bv1 = bv1Field.getAsFloat();

                    //bv2 Container
                    JsonElement bv2Field = sheetsResponse.getAsJsonObject().get("gsx$basevalprev2").getAsJsonObject().get("$t");
                    group.bv2 = bv2Field.getAsFloat();

                    //bv3 Container
                    JsonElement bv3Field = sheetsResponse.getAsJsonObject().get("gsx$basevalprev3").getAsJsonObject().get("$t");
                    group.bv3 = bv3Field.getAsFloat();

                    //bv4 Container
                    JsonElement bv4Field = sheetsResponse.getAsJsonObject().get("gsx$basevalprev4").getAsJsonObject().get("$t");
                    group.bv4 = bv4Field.getAsFloat();

                    //bv5 Container
                    JsonElement bv5Field = sheetsResponse.getAsJsonObject().get("gsx$basevalprev5").getAsJsonObject().get("$t");
                    group.bv5 = bv5Field.getAsFloat();


                    //Add to master container
                    sortCont.add(group);
                }

                //Sort based on curVal
                Comparator<StockGroup> stockComp = new Comparator<StockGroup>() {
                    @Override
                    public int compare(StockGroup cVal1, StockGroup cVal2) {
                        return Double.compare(cVal1.curVal, cVal2.curVal);
                    }
                };
                Collections.sort(sortCont, stockComp);
                Collections.reverse(sortCont);

                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {
                        findViewById(R.id.loadingPanel).setVisibility(View.GONE);
                        prepareTokenGraphData(sortCont);
                    }
                });
            }
        });
    }

    //method to set up values for each line item in recyclerview
    private void prepareTokenGraphData(ArrayList<StockGroup> infoContainer){
        final List<TokenPurse> token_list;
        token_list = TokenPurse.listAll(TokenPurse.class);
        for (int i = 0; i < infoContainer.size(); i++){
            TokenGraph tokenGraph = new TokenGraph();
            int tokenID = getResources().getIdentifier(infoContainer.get(i).type, "mipmap", getPackageName());
            tokenGraph.setTokenID(tokenID);
            tokenGraph.setTokenName(infoContainer.get(i).type);
            tokenGraph.setCurVal(infoContainer.get(i).curVal);
            tokenGraph.setVal1(infoContainer.get(i).val1);
            tokenGraph.setVal2(infoContainer.get(i).val2);
            tokenGraph.setVal3(infoContainer.get(i).val3);
            tokenGraph.setVal4(infoContainer.get(i).val4);
            tokenGraph.setVal5(infoContainer.get(i).val5);
            tokenGraph.setBaseVal(infoContainer.get(i).baseVal);
            tokenGraph.setBv1(infoContainer.get(i).bv1);
            tokenGraph.setBv2(infoContainer.get(i).bv2);
            tokenGraph.setBv3(infoContainer.get(i).bv3);
            tokenGraph.setBv4(infoContainer.get(i).bv4);
            tokenGraph.setBv5(infoContainer.get(i).bv5);
            tokenGraph.setMaxStockVal(maxStockVal);
            tokenGraph.setTokenPurseVal(infoContainer.get(i).purseVal);
            for (int k = 0; k < numberOfTokens; k++){
                if(infoContainer.get(i).type.equals(token_list.get(k).tokenName)){
                    tokenGraph.setHaveToken(true);
                    tokenGraphList.add(tokenGraph);
                    break;
                }else{
                    tokenGraph.setHaveToken(false);
                }
            }
            //tokenGraphList.add(tokenGraph);
        }
        TGadapter.notifyDataSetChanged();

    }

    //method for setting onclick listener to each line item and sending approriate variables to next activity
    @Override
    public void itemClicked(View view, int position) {
        //send variables over to TradeShare.java
        String typeSend = tokenGraphList.get(position).getTokenName();
        Float curValSend = tokenGraphList.get(position).getCurVal();

        Intent selectionSend = new Intent(getApplicationContext(), TradeShare.class);
        selectionSend.putExtra("tokenType",typeSend);
        selectionSend.putExtra("curVal",curValSend);
        startActivity(selectionSend);
        finish();

    }




    public class StockGroup {
        public String type;
        public float curVal;
        public float val1;
        public float val2;
        public float val3;
        public float val4;
        public float val5;
        public float baseVal;
        public float bv1;
        public float bv2;
        public float bv3;
        public float bv4;
        public float bv5;
        public float purseVal;
        public float maxStockVal;
        public float lowRecVal;
        public float highRecVal;
        public float curLowVal;
        public float curHighVal;
    }




}



