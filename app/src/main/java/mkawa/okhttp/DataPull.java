package mkawa.okhttp;

import com.google.api.client.util.Data;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.orm.SugarRecord;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;



public class DataPull extends SugarRecord {
    private ArrayList<PlayerStats> players = new ArrayList<>();
    private String userName;
    private String iPlayerName;
    private int iPlayerDrinks;
    private float iPlayerOz;
    private float iPlayerAbv;
    private float iPlayerIbu;
    private int iPlayerDrinkTokens;
    private int iPlayerOzTokens;
    private int iPlayerAbvTokens;
    private int iPlayerIbuTokens;

    final String url = "https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/4/public/full?alt=json";
    private String playerUserName;


    public DataPull(
            ArrayList<PlayerStats> players,
            String iPlayerName,
            int iPlayerDrinks,
            float iPlayerOz,
            float iPlayerAbv,
            float iPlayerIbu,
            int iPlayerDrinkTokens,
            int iPlayerOzTokens,
            int iPlayerAbvTokens,
            int iPlayerIbuTokens){
        this.players = players;
        this.iPlayerName = iPlayerName;
        this.iPlayerDrinks = iPlayerDrinks;
        this.iPlayerOz = iPlayerOz;
        this.iPlayerAbv = iPlayerAbv;
        this.iPlayerIbu = iPlayerIbu;
        this.iPlayerDrinkTokens = iPlayerDrinkTokens;
        this.iPlayerOzTokens = iPlayerOzTokens;
        this.iPlayerAbvTokens = iPlayerAbvTokens;
        this.iPlayerIbuTokens = iPlayerIbuTokens;
    }

    void requestDataPull(String url, final String userName) throws Exception{
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


            }
        });
    }


}
