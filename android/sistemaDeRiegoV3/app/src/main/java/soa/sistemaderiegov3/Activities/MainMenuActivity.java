package soa.sistemaderiegov3.Activities;

import soa.sistemaderiegov3.R;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends AppCompatActivity {

    public static final String EXTRA_MESSAGE = " soa.sistemaderiegov3.Activities.MESSAGE";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);
    }

    /*Called when the user taps the Vincular Button*/
    public void startSystem (View view){
        Intent intent = new Intent(this, StartDeviceActivity.class);
        startActivity(intent);
    }

    /*Called when the user taps the Configuracion Button*/
    public void configurationMenu (View view){
        Intent intent = new Intent(this, ConfigurationMenuActivity.class);
        startActivity(intent);
    }

    public void exceptionConfigurationActivity (View view){
        Intent intent = new Intent(this, ExceptionConfigurationActivity.class);
        startActivity(intent);
    }

    public void showModalForManualMode(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        /*TODO: the button should have 2 possible values, "modo manual" or "modo automatico", it depends on the embebbed device actual status.this method must consider the both options
        */
        alert.setTitle("¿Quieres utilizar el modo manual?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                //TODO: send to the embebbed device the message
            }
        });

        alert.setNegativeButton("Cancelar",
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        //Do nothing
                    }
                });
        alert.show();
    }


}
