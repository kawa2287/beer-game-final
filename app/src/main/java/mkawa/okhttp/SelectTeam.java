package mkawa.okhttp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

public class SelectTeam extends Activity {

    Animation animFadeIn;
    Animation blink;
    private TextView teamSelectTV;
    private TextView ratsTV;
    private TextView lemursTV;
    private TextView foxesTV;
    private TextView lizardsTV;
    private RelativeLayout ratsLay;
    private RelativeLayout lemursLay;
    private RelativeLayout foxesLay;
    private RelativeLayout lizardsLay ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_select_team);

        animFadeIn = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.fade_in);
        blink = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.blink);

        teamSelectTV = (TextView) findViewById(R.id.teamSelectText);
        ratsTV = (TextView) findViewById(R.id.ratsText);
        lemursTV = (TextView) findViewById(R.id.lemursText);
        foxesTV = (TextView) findViewById(R.id.foxesText);
        lizardsTV = (TextView) findViewById(R.id.lizardText);
        ratsLay = (RelativeLayout) findViewById(R.id.ratsLayout);
        lemursLay = (RelativeLayout) findViewById(R.id.lemursLayout);
        foxesLay = (RelativeLayout) findViewById(R.id.foxesLayout);
        lizardsLay = (RelativeLayout) findViewById(R.id.lizardsLayout);

        ratsLay.startAnimation(animFadeIn);
        lemursLay.startAnimation(animFadeIn);
        foxesLay.startAnimation(animFadeIn);
        lizardsLay.startAnimation(animFadeIn);


        teamSelectTV.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ratsTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        lemursTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        foxesTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        lizardsTV.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
    }

    public void SaveTeam(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void selectTeam(View v){
        //SaveName("USERNAME",uNs);
        //editText.setText("");
        switch(v.getId()) {
            case R.id.ratsLayout:
                ratsLay.startAnimation(blink);
                SaveTeam("TEAM","RATCHET RATS");
                break;
            case R.id.lemursLayout:
                lemursLay.startAnimation(blink);
                SaveTeam("TEAM","LIT LEMURS");
                break;
            case R.id.foxesLayout:
                foxesLay.startAnimation(blink);
                SaveTeam("TEAM","FADED FOXES");
                break;
            case R.id.lizardsLayout:
                lizardsLay.startAnimation(blink);
                SaveTeam("TEAM","SLIZARD LIZARDS");
                break;
            default:
                throw new RuntimeException("Unknown button ID");
        }

        Intent dashboard = new Intent(getApplicationContext(), Dashboard.class);
        startActivity(dashboard);
    }
}

