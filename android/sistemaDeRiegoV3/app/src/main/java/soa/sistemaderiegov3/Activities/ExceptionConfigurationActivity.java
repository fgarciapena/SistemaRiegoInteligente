package soa.sistemaderiegov3.Activities;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;

import soa.sistemaderiegov3.R;

public class ExceptionConfigurationActivity extends AppCompatActivity {

    public Button saveExceptionButton;
    public Switch mondaySwitch;
    public Switch tuesdaySwitch;
    public Switch wednesdaySwitch;
    public Switch thursdaySwitch;
    public Switch fridaySwitch;
    public Switch saturdaySwitch;
    public Switch sundaySwitch;
    public Switch circuit1Switch;
    public Switch circuit2Switch;
    public Switch circuit3Switch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses
        setContentView(R.layout.activity_exception_configuration);

        saveExceptionButton = findViewById(R.id.saveExceptionButton);

        mondaySwitch   = findViewById(R.id.mondaySwitch);
        tuesdaySwitch   = findViewById(R.id.tuesdaySwitch);
        wednesdaySwitch   = findViewById(R.id.wednesdaySwitch);
        thursdaySwitch   = findViewById(R.id.thursdaySwitch);
        fridaySwitch   = findViewById(R.id.fridaySwitch);
        saturdaySwitch   = findViewById(R.id.saturdaySwitch);
        sundaySwitch   = findViewById(R.id.sundaySwitch);
        circuit1Switch   = findViewById(R.id.circuitSwitch1);
        circuit2Switch   = findViewById(R.id.circuitSwitch2);
        circuit3Switch   = findViewById(R.id.circuitSwitch3);

/*        saveExceptionButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                    }
                });*/
    }

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    public void sendStartStopConfiguration(View view){
        //TODO: call bluetooth service and send the setted information to the embebbed device
    }
}
