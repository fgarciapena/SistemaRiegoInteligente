package soa.sistemaderiego;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

public class ConfigurationMenuActivity extends Activity {

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
    public ProgressReceiver rcv;

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
        passwordValue.setEnabled(false);
        mensaje = (TextView) findViewById(R.id.mensaje);

        buttonSaveConfiguration.setOnClickListener(buttonSaveConfigurationListener);

        //se especifica que mensajes debe aceptar el broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_CONFIGMENU);
        rcv = new ProgressReceiver();
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

        Intent msgIntent = new Intent(ConfigurationMenuActivity.this, BlueToothService.class);
        msgIntent.putExtra("Comando", "CMD|CONFIG#");
        startService(msgIntent);

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        // Comando "CMD|CONFIG#"
        showToast("Cargando datos");
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(rcv);
    }


    private void formatData(String data){
        Log.i("Informacion",data);
        String[] information = data.split("#");
        String[] dataSensores;
            //sensores 2 y 3
            dataSensores= information[0].split("\\|");
            if(dataSensores[1].equals("CONFIG")){
                lightSensorValue.setText(dataSensores[2]);
                rainSensorValue.setText(dataSensores[3]);
                passwordValue.setText(dataSensores[4].substring(0,4));
            }else
            if(dataSensores[1].equals("1")){
                wetSensor1Value.setText(dataSensores[2]);
            }
            if(dataSensores[1].equals("2")){
                wetSensor2Value.setText(dataSensores[2]);
            }else
            if(dataSensores[1].equals("3")){
                wetSensor3Value.setText(dataSensores[2]);
            }

    }

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener buttonSaveConfigurationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPackage = "CMD|SET|LIMITE|" + lightSensorValue.getText() + "|"+ rainSensorValue.getText() +
                    "|" + wetSensor1Value.getText() + "|" + wetSensor2Value.getText() + "|" + wetSensor3Value.getText() + '#';
            mensaje.setText(dataPackage);
            Intent msgIntent = new Intent(ConfigurationMenuActivity.this, BlueToothService.class);
            msgIntent.putExtra("Comando", dataPackage);
            startService(msgIntent);
            showToast("Guardado");
        }
    };


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }
}
