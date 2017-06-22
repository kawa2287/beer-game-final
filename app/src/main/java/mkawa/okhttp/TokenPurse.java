package mkawa.okhttp;


import com.orm.SugarRecord;

/**
 * Created by mattkawahara on 8/17/16.
 */
public class TokenPurse extends SugarRecord {
    String tokenName;
    float tokenShare;

    public TokenPurse(){
    }

    public TokenPurse(String tokenName, int tokenShare){
        this.tokenName = tokenName;
        this.tokenShare = tokenShare;
    }

}
