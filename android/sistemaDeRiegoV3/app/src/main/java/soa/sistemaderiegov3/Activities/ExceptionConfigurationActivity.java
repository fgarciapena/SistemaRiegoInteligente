package soa.sistemaderiegov3.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Switch;
import android.widget.TextView;

import java.sql.Time;

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
    public TextView titleText;
    public String circuitNumber;
    public TextView horaDesde;
    public TextView horaHasta;
    public TextView mensaje;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses

        setContentView(R.layout.activity_exception_configuration);
       try{
            Intent intentExtras = getIntent();
            Bundle bundle = intentExtras.getExtras();
            if(!bundle.isEmpty()){
                titleText = findViewById(R.id.textView9);
                circuitNumber = bundle.getString("circuitNumber").toString();
                titleText.setText("Circuito " + circuitNumber);
            }
        }catch(Exception e){
            Log.d("Error", e.toString());
        }



        saveExceptionButton = findViewById(R.id.saveExceptionButton);

        mondaySwitch   = findViewById(R.id.mondaySwitch);
        tuesdaySwitch   = findViewById(R.id.tuesdaySwitch);
        wednesdaySwitch   = findViewById(R.id.wednesdaySwitch);
        thursdaySwitch   = findViewById(R.id.thursdaySwitch);
        fridaySwitch   = findViewById(R.id.fridaySwitch);
        saturdaySwitch   = findViewById(R.id.saturdaySwitch);
        sundaySwitch   = findViewById(R.id.sundaySwitch);
        horaDesde = findViewById(R.id.startHourValue);
        horaHasta = findViewById(R.id.endHourValue);

        mensaje = findViewById(R.id.mensaje);

        saveExceptionButton.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                            String dataPackage = "CMD|SET|EXC|" + circuitNumber + "|" +(sundaySwitch.isChecked() ? "0": "") + (sundaySwitch.isChecked() ? "," : "") + (mondaySwitch.isChecked() ? "1": "") +
                                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() ? "," : "") + (tuesdaySwitch.isChecked() ? "2": "") +
                                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() ? "," : "") + (wednesdaySwitch.isChecked() ? "3": "") +
                                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() ? "," : "") + (thursdaySwitch.isChecked() ? "4": "") +
                                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() || thursdaySwitch.isChecked() ? "," : "") + (fridaySwitch.isChecked() ? "5": "") +
                                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() || thursdaySwitch.isChecked() || fridaySwitch.isChecked() ? "," : "") + (saturdaySwitch.isChecked() ? "6": "") +
                                    (horaDesde.getText().length() > 0 && !horaDesde.getText().equals("") ? "|" + horaDesde.getText().toString().split(":")[0] + "|" + horaDesde.getText().toString().split(":")[1] : "") +
                                    (horaDesde.getText().length() > 0 && !horaHasta.getText().equals("") ? "|" + horaHasta.getText().toString().split(":")[0] + "|" + horaHasta.getText().toString().split(":")[1] : "")
                                    + '#';
                            mensaje.setText(dataPackage);
                            //sendExceptionConfiguration(dataPackage);
                    }
                });
    }

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    public void sendExceptionConfiguration(String configuration){
        //TODO: call bluetooth service and send the setted information to the embebbed device
    }
}
