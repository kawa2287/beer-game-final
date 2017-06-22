package mkawa.okhttp;

import android.os.AsyncTask;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.IOException;
import java.util.ArrayList;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class RequestUserStats {

    public PlayerStats userStats;

    public void callUserStats(final String userName) throws Exception {
        Request request = new Request.Builder()
                .url("https://spreadsheets.google.com/feeds/list/1K9xB3ZivGYa5S-1SdyEWNUUNN8tu1-9_NH-xyA9if-8/2/public/full?alt=json")
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
                //extract user info out of container
                final PlayerStats user = new PlayerStats();
                for (int i = 0; i < sortCont.size(); i++) {
                    if (sortCont.get(i).player.equals(userName.toUpperCase())) {
                        user.setDrinks(sortCont.get(i).drinks);
                        user.setOz(sortCont.get(i).oz);
                        user.setAbv(sortCont.get(i).abv);
                        user.setIbu(sortCont.get(i).ibu);
                        user.setDrinkTokens(sortCont.get(i).drinkPts);
                        user.setOzTokens(sortCont.get(i).ozPts);
                        user.setAbvTokens(sortCont.get(i).abvPts);
                        user.setIbuTokens(sortCont.get(i).ibuPts);
                    }
                    userStats = user;
                }


            }
        });
    }

    public PlayerStats getUserStats(String playerName) {
        try {
            callUserStats(playerName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return userStats;
    }
}
