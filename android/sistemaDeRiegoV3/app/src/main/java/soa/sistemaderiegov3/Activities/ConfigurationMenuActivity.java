package soa.sistemaderiegov3.Activities;

import soa.sistemaderiegov3.R;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ConfigurationMenuActivity extends AppCompatActivity {

    public Button buttonSaveConfiguration;
    public EditText wetSensorValue;
    public EditText lightSensorValue;
    public EditText rainSensorValue;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses
        setContentView(R.layout.activity_configuration);



        buttonSaveConfiguration = findViewById(R.id.buttonSave);

        wetSensorValue   = findViewById(R.id.wet_sensor_value);
        lightSensorValue   = findViewById(R.id.light_sensor_value);
        rainSensorValue   = findViewById(R.id.rain_sensor_value);


        buttonSaveConfiguration.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        Log.v("EditText", wetSensorValue.getText().toString() + lightSensorValue.getText().toString());
                    }
                });
    }

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    public void sendConfiguration(View view){
        //TODO: call bluetooth service and send the setted information to the embebbed device
    }
}
