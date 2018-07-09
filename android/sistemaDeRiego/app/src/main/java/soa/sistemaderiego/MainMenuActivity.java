package soa.sistemaderiego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

public class MainMenuActivity extends Activity {

    public static final String EXTRA_MESSAGE = " soa.sistemaderiegov3.Activities.MESSAGE";

    public String automaticoActivado;
    public String address;

    private SensorManager manager;
    private SensorEventListener listener;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        try {
            if (intent.hasExtra("AutomaticoActivado") && intent.hasExtra("Direccion_Bluethoot")) {
                Bundle extras = intent.getExtras();
                address = extras.getString("Direccion_Bluethoot");
                automaticoActivado = extras.getString("AutomaticoActivado");
            }else{
                automaticoActivado = "";
                address = "";
            }
        }
        catch (Exception ex)
        {
            showToast( "Fallo el intent: " + ex.toString());
        }




        manager = (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
        listener = new SensorEventListener() {
            @Override
            public void onAccuracyChanged(Sensor arg0, int arg1) {
            }

            @Override
            public void onSensorChanged(SensorEvent event) {
                Sensor sensor = event.sensor;
                if (sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

                }
                else if (sensor.getType() == Sensor.TYPE_GYROSCOPE) {

                }
            }
        };
    }




    /*Called when the user taps the Vincular Button*/
    public void startSystem (View view){
        Intent intent = new Intent(this, StartDeviceActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        extras.putString("Direccion_Bluethoot", address);
        extras.putString("AutomaticoActivado", automaticoActivado);
        intent.putExtras(extras);
        startActivity(intent);
        startActivity(intent);
    }

    /*Called when the user taps the Configuracion Button*/
    public void configurationMenu (View view){
        Intent intent = new Intent(this, ConfigurationMenuActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        extras.putString("Direccion_Bluethoot", address);
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void circuitSelection (View view){
        Intent intent = new Intent(this, CircuitSelectionActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        extras.putString("Direccion_Bluethoot", address);
        intent.putExtras(extras);

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


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
