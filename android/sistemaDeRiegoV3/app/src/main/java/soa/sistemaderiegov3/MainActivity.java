package soa.sistemaderiegov3;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;

/**
 * this is an example class. The methods show how to send data betweeen activities.
 * TODO: DELETE ALL
 * **/
public class MainActivity extends AppCompatActivity {
    public static final String EXTRA_MESSAGE = "soa.sistemaDeRiegoV3.MESSAGE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    /*Called when the user taps the Send Button*/
    public void sendMessage(View view){
        Intent intent = new Intent(this, DisplayMessageActivity.class);
        EditText editTest = (EditText) findViewById(R.id.editText);
        String message = editTest.getText().toString();
        intent.putExtra(EXTRA_MESSAGE, message);
        startActivity(intent);
    }
}
