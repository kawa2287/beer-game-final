package mkawa.okhttp;

/**
 * Created by mattkawahara on 8/23/16.
 */
public class Token {

    private int name;
    private int tokenId;
    private String tokenTitle;

    public Token(){
    }

    public Token(int name, String tokenTitle){
        this.name = name;
        this.tokenTitle = tokenTitle;
    }

    public int getName(){
        return name;
    }

    public void setName(int name){
        this.name = name;
    }

    public String getTokenTitle() {
        return tokenTitle;
    }
    public void setTokenTitle(String tokenTitle) {
        this.tokenTitle = tokenTitle;
    }



}
