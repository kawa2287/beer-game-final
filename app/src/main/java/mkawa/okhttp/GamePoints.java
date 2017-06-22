package mkawa.okhttp;

/**
 * Created by mattkawahara on 10/24/16.
 */

public class GamePoints {

    private String playerName;
    private String TeamName;
    private String drinkPoints;
    private String ozPoints;
    private String abvPoints;
    private String ibuPoints;
    private Boolean test;
    private String URL;

    public String getPlayerName() {
        return playerName;
    }

    public String getTeamName() {
        return TeamName;
    }

    public void setTeamName(String teamName) {
        TeamName = teamName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    public String getDrinkPoints() {
        return drinkPoints;
    }

    public void setDrinkPoints(String drinkPoints) {
        this.drinkPoints = drinkPoints;
    }

    public String getOzPoints() {
        return ozPoints;
    }

    public void setOzPoints(String ozPoints) {
        this.ozPoints = ozPoints;
    }

    public String getAbvPoints() {
        return abvPoints;
    }

    public void setAbvPoints(String abvPoints) {
        this.abvPoints = abvPoints;
    }

    public String getIbuPoints() {
        return ibuPoints;
    }

    public void setIbuPoints(String ibuPoints) {
        this.ibuPoints = ibuPoints;
    }

    public Boolean getTest() {
        return test;
    }

    public void setTest(Boolean test) {
        this.test = test;
    }

    public String getURL() {
        return URL;
    }

    public void setURL(String URL) {
        this.URL = URL;
    }
}
