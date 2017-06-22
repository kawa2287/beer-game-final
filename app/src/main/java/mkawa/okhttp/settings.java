package mkawa.okhttp;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.vstechlab.easyfonts.EasyFonts;

import org.andengine.entity.text.Text;


public class settings extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        TextView title = (TextView) findViewById(R.id.userNameEntry);
        EditText userNameEntry = (EditText)findViewById(R.id.userName);
        title.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        userNameEntry.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.GhostWhite));
        title.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        userNameEntry.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));

        TextView drinkPerPointTitle = (TextView) findViewById(R.id.drinkPerPointTitle);
        TextView ozPerPointTitle = (TextView) findViewById(R.id.ozPerPointTitle);
        TextView abvPerPointTitle = (TextView) findViewById(R.id.abvOzPerPointTitle);
        TextView ibuPerPointTitle = (TextView) findViewById(R.id.ibuPerPointTitle);
        TextView drinkPerPointValue = (TextView) findViewById(R.id.drinkPerPointValue);
        TextView ozPerPointValue = (TextView) findViewById(R.id.ozPerPointValue);
        TextView abvPerPointValue = (TextView) findViewById(R.id.abvOzPerPointValue);
        TextView ibuPerPointValue = (TextView) findViewById(R.id.ibuPerPointValue);

        drinkPerPointTitle.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ozPerPointTitle.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        abvPerPointTitle.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ibuPerPointTitle.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        drinkPerPointValue.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ozPerPointValue.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        abvPerPointValue.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        ibuPerPointValue.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));

        FetchSettings settings = FetchSettings.findById(FetchSettings.class,1);

        drinkPerPointValue.setText(String.valueOf(settings.drinkLevel));
        ozPerPointValue.setText(String.valueOf(settings.ozLevel));
        abvPerPointValue.setText(String.valueOf(settings.abvLevel));
        ibuPerPointValue.setText(String.valueOf(settings.ibuLevel));

        }


    public void SaveName(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }



    public void writeUserNameToVar(View v){
        EditText uN = (EditText)findViewById(R.id.userName);
        String uNs = uN.getText().toString();
        SaveName("USERNAME",uNs);
        Toast.makeText(getApplicationContext(), "Player Name Set to: " + uNs,
                Toast.LENGTH_LONG).show();
        //If Data successfully sent- then clear all fields
        uN.setText("");
    }

    public void emptyPurse(View v){
        TokenPurse.deleteAll(TokenPurse.class);
        Toast.makeText(getApplicationContext(), "Purse has been Emptied",
                Toast.LENGTH_LONG).show();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu_items,menu);
        return true;
    }



}

