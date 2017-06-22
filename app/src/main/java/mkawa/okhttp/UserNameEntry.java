package mkawa.okhttp;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

public class UserNameEntry extends Activity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_user_name_entry);

        EditText editText = (EditText) findViewById(R.id.inputBox);
        TextView usernameTV = (TextView) findViewById(R.id.userNameText);
        Button button = (Button) findViewById(R.id.buttonSubmit);

        editText.setTypeface(EasyFonts.ostrichRegular(getApplicationContext()));
        usernameTV.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));
        button.setTypeface(EasyFonts.ostrichBlack(getApplicationContext()));

    }
    public void SaveName(String key, String value) {
        SharedPreferences sharedPreferences = getSharedPreferences("MAIN_STORAGE", MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.putString(key, value);
        editor.apply();
    }

    public void writeUserNameToVar(View v){
        EditText editText = (EditText) findViewById(R.id.inputBox);
        String uNs = editText.getText().toString();
        SaveName("USERNAME",uNs);
        editText.setText("");
        Intent teamSelect = new Intent(getApplicationContext(), SelectTeam.class);
        startActivity(teamSelect);
    }
}





