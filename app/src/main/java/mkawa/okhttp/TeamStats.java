package mkawa.okhttp;


public class TeamStats {

    private String teamName;
    private float teamDrinks;
    private float teamOz;
    private float teamAbv;
    private float teamIbu;
    private float teamDrinkTokens;
    private float teamOzTokens;
    private float teamAbvTokens;
    private float teamIbuTokens;
    private float teamDrinkPoints;
    private float teamOzPoints;
    private float teamAbvPoints;
    private float teamIbuPoints;
    private float teamOzPerDrink;
    private float teamAbvPerDrink;
    private float teamIbuPerDrink;
    private float teamAvgABV;
    private float teamTokensSubmitted;

    public TeamStats(){}


    public String getTeamName() {
        return teamName;
    }

    public void setTeamName(String teamName) {
        this.teamName = teamName;
    }

    public float getTeamDrinks() {
        return teamDrinks;
    }

    public void setTeamDrinks(float teamDrinks) {
        this.teamDrinks = teamDrinks;
    }

    public float getTeamOz() {
        return teamOz;
    }

    public void setTeamOz(float teamOz) {
        this.teamOz = teamOz;
    }

    public float getTeamAbv() {
        return teamAbv;
    }

    public void setTeamAbv(float teamAbv) {
        this.teamAbv = teamAbv;
    }

    public float getTeamIbu() {
        return teamIbu;
    }

    public void setTeamIbu(float teamIbu) {
        this.teamIbu = teamIbu;
    }

    public float getTeamDrinkPoints() {
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return Math.min(curLevel(teamDrinks,settings.drinkLevel)+getTeamDrinkTokens(),settings.catgPoints);
    }

    public void setTeamDrinkPoints(float teamDrinkPoints) {
        this.teamDrinkPoints = teamDrinkPoints;
    }

    public float getTeamOzPoints() {
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return Math.min(curLevel(teamOz,settings.ozLevel)+getTeamOzTokens(),settings.catgPoints);
    }

    public void setTeamOzPoints(float teamOzPoints) {
        this.teamOzPoints = teamOzPoints;
    }

    public float getTeamAbvPoints() {
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return Math.min(curLevel(teamAbv,settings.abvLevel)+getTeamAbvTokens(),settings.catgPoints);
    }

    public void setTeamAbvPoints(float teamAbvPoints) {
        this.teamAbvPoints = teamAbvPoints;
    }

    public float getTeamIbuPoints() {
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return Math.min(curLevel(teamIbu,settings.ibuLevel)+getTeamIbuTokens(),settings.catgPoints);
    }

    public void setTeamIbuPoints(float teamIbuPoints) {
        this.teamIbuPoints = teamIbuPoints;
    }

    public float getTeamOzPerDrink() {
        return getTeamOz()/getTeamDrinks();
    }

    public void setTeamOzPerDrink(float teamOzPerDrink) {
        this.teamOzPerDrink = teamOzPerDrink;
    }

    public float getTeamAbvPerDrink() {
        return getTeamAbv()/getTeamDrinks();
    }

    public void setTeamAbvPerDrink(float teamAbvPerDrink) {
        this.teamAbvPerDrink = teamAbvPerDrink;
    }

    public float getTeamIbuPerDrink() {
        return getTeamIbu()/getTeamDrinks();
    }

    public void setTeamIbuPerDrink(float teamIbuPerDrink) {
        this.teamIbuPerDrink = teamIbuPerDrink;
    }

    public float getTeamAvgABV() {
        return 100*getTeamAbv()/getTeamOz();
    }

    public void setTeamAvgABV(float teamAvgABV) {
        this.teamAvgABV = teamAvgABV;
    }

    public float getTeamPointPerDrink() {
        return (getTeamDrinkPoints()+getTeamOzPoints()+getTeamAbvPoints()+getTeamIbuPoints())/getTeamDrinks();
    }

    public float getTeamDrinkPct(){
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return getTeamDrinkPoints()/settings.catgPoints;
    }
    public float getTeamOzPct(){
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return getTeamOzPoints()/settings.catgPoints;
    }
    public float getTeamAbvPct(){
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return getTeamAbvPoints()/settings.catgPoints;
    }
    public float getTeamIbuPct(){
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return getTeamIbuPoints()/settings.catgPoints;
    }

    //method for determining current level
    public float curLevel(float current, float inc){
        float level;
        level = (current-(current % inc))/inc;
        return level;
    }

    //method for determining amount to next level
    public float nextLevel(float current, float inc){
        float level;
        level = (float)Math.round((inc-(current % inc))*10)/10;
        return level;
    }

    //method for determining percentage of current level complete
    public float pctComp(float current, float inc){
        float pct;
        pct = (inc-nextLevel(current, inc))/inc;
        return pct;
    }


    public float getTeamDrinkTokens() {
        return teamDrinkTokens;
    }

    public void setTeamDrinkTokens(float teamDrinkTokens) {
        this.teamDrinkTokens = teamDrinkTokens;
    }

    public float getTeamOzTokens() {
        return teamOzTokens;
    }

    public void setTeamOzTokens(float teamOzTokens) {
        this.teamOzTokens = teamOzTokens;
    }

    public float getTeamAbvTokens() {
        return teamAbvTokens;
    }

    public void setTeamAbvTokens(float teamAbvTokens) {
        this.teamAbvTokens = teamAbvTokens;
    }

    public float getTeamIbuTokens() {
        return teamIbuTokens;
    }

    public void setTeamIbuTokens(float teamIbuTokens) {
        this.teamIbuTokens = teamIbuTokens;
    }

    public float getTeamTokensSubmitted() {
        return teamTokensSubmitted;
    }

    public void setTeamTokensSubmitted(float teamTokensSubmitted) {
        this.teamTokensSubmitted = teamTokensSubmitted;
    }

    public float getTeamTotalPoints() {return getTeamDrinkPoints()+getTeamOzPoints()+getTeamAbvPoints()+getTeamIbuPoints();}
    public float getTeamRawPoints() {return getTeamTotalPoints() - getTeamTokenPoints();}
    public float getTeamTokenPoints() {return getTeamDrinkTokens()+getTeamOzTokens()+getTeamAbvTokens()+getTeamIbuTokens();}
    public float getTeamRPPD() {return getTeamRawPoints()/getTeamDrinks();}
    public float getTeamPPT() {return getTeamTokenPoints()/getTeamTokensSubmitted();}
    public float getTeamOPPD() {return getTeamTotalPoints()/getTeamDrinks();}
    public float getTeamVictoryPorgress() {
        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);
        return getTeamTotalPoints()/(settings.catgPoints*4);}


}
