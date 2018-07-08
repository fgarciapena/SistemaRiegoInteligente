package soa.sistemaderiego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;

public class MainMenuActivity extends Activity {

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
        String address = "";
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent extraIntent=getIntent();
        if(intent.hasExtra("Direccion_Bluethoot")){
            extras=intent.getExtras();
            address= extras.getString("Direccion_Bluethoot");
        }
        intent.putExtra("Direccion_Bluethoot", address);
        startActivity(intent);
    }

    public void circuitSelection (View view){
        Intent intent = new Intent(this, CircuitSelectionActivity.class);
        startActivity(intent);
    }

    public void showModalForManualMode(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        /*TODO: the button should have 2 possible values, "modo manual" or "modo automatico", it depends on the embebbed device actual status.this method must consider the both options
        */
        alert.setTitle("¿Quieres utilizar el modo manual?");

        alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int whichButton) {
                String dataPackage = "CMD|AUTO|ON#";
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
