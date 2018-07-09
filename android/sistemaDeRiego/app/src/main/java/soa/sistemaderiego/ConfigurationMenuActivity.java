package soa.sistemaderiego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;


public class ConfigurationMenuActivity extends Activity {



    // String for MAC address del Hc05
    private static String address = null;

    /* Fin BluetoohZone */

    public Button buttonSaveConfiguration;
    public EditText wetSensor1Value;
    public EditText wetSensor2Value;
    public EditText wetSensor3Value;
    public EditText lightSensorValue;
    public EditText rainSensorValue;
    public EditText passwordValue;
    public TextView mensaje;
    public String dataPackage;
    public boolean activeActivity ;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses
        setContentView(R.layout.activity_configuration);

        buttonSaveConfiguration = (Button) findViewById(R.id.buttonSave);
        wetSensor1Value   = (EditText) findViewById(R.id.wet_sensor1_value);
        wetSensor2Value   = (EditText) findViewById(R.id.wet_sensor2_value);
        wetSensor3Value   = (EditText) findViewById(R.id.wet_sensor3_value);
        lightSensorValue   = (EditText) findViewById(R.id.light_sensor_value);
        rainSensorValue   = (EditText) findViewById(R.id.rain_sensor_value);
        passwordValue   = (EditText) findViewById(R.id.password_value);

        mensaje = (TextView) findViewById(R.id.mensaje);

        buttonSaveConfiguration.setOnClickListener(buttonSaveConfigurationListener);

        //se especifica que mensajes debe aceptar el broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_CONFIGMENU);
        ProgressReceiver rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);
    }

    public class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BlueToothService.ACTION_CONFIGMENU)) {
                Bundle extras = intent.getExtras();
                String prog = extras.getString("Informacion");
                formatData(prog);
            }
            else {
                Toast.makeText(ConfigurationMenuActivity.this, "Error al procesar información", Toast.LENGTH_SHORT).show();
            }
        }
    }


    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        try {
            if (intent.hasExtra("Direccion_Bluethoot")) {
                Bundle extras = intent.getExtras();
                address = extras.getString("Direccion_Bluethoot");
            }else{
                address = "";
            }
        }
        catch (Exception ex)
        {
            showToast( "Fallo el intent: " + ex.toString());
        }

        Intent msgIntent = new Intent(ConfigurationMenuActivity.this, BlueToothService.class);
        msgIntent.putExtra("Direccion_Bluethoot", address);
        msgIntent.putExtra("Comando", "CMD|CONFIG#");
        startService(msgIntent);

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        // Comando "CMD|CONFIG#"
        showToast("Cargando datos");
    }


    private void formatData(String data){
        Log.i("Informacion",data);
        String[] information = data.split("#");
        String[] dataSensores;
        if (information.length > 1){
            //Sensores luz y lluvia + password
            dataSensores= information[0].split("\\|");

            lightSensorValue.setText(dataSensores[2]);
            rainSensorValue.setText(dataSensores[3]);
            passwordValue.setText(dataSensores[4].substring(0,4));
            //Sensonres circuito 1 de humedad
            dataSensores= information[1].split("\\|");
            wetSensor1Value.setText(dataSensores[2]);
        }else{
            //sensores 2 y 3
            dataSensores= information[0].split("\\|");
            if(dataSensores[1].equals("2")){
                wetSensor2Value.setText(dataSensores[2]);
            }else{
                wetSensor3Value.setText(dataSensores[2]);
            }
        }
    }

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener buttonSaveConfigurationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPackage = "CMD|SET|LIMITE|" + lightSensorValue.getText() + "|"+ rainSensorValue.getText() +
                    "|" + wetSensor1Value.getText() + "|" + wetSensor2Value.getText() + "|" + wetSensor3Value.getText() + '#';
            mensaje.setText(dataPackage);
            showToast("Guardado");
        }
    };


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
