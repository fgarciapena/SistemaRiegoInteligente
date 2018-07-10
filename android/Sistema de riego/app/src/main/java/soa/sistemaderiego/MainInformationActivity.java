package soa.sistemaderiego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainInformationActivity extends Activity {

    //MAC ADDRESS BT
    private static String address = null;

    // Variables de la vista

     TextView wetSensor1Value;
     TextView wetSensor2Value;
     TextView wetSensor3Value;
     TextView lightSensorValue;
     TextView rainSensorValue;
     TextView automaticActivated;
     TextView exceptionCircuit1;
     TextView circuit1Value;
     TextView exceptionCircuit2;
     TextView circuit2Value;
     TextView exceptionCircuit3;
     TextView circuit3Value;

     String circuitosEncendidos = "";
     String automaticoActivado = "";
     public ProgressReceiver rcv;


    SensorManager mySensorManager;
    Sensor myProximitySensor;

    SensorEventListener proximitySensorEventListener = new SensorEventListener() {
        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub
        }
        @Override
        public void onSensorChanged(SensorEvent event) {
            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] < 10) {
                    Intent msgIntent = new Intent(MainInformationActivity.this, BlueToothService.class);
                    msgIntent.putExtra("Direccion_Bluethoot", address);
                    msgIntent.putExtra("Comando", "CMD|STATUS#");
                    startService(msgIntent);
                } else {
                }
            }
        }
    };

    @SuppressLint({"WrongViewCast", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_information);

        mySensorManager = (SensorManager) getSystemService(
                Context.SENSOR_SERVICE);
        myProximitySensor = mySensorManager.getDefaultSensor(
                Sensor.TYPE_PROXIMITY);


        wetSensor1Value   = (TextView) findViewById(R.id.wetSensor1Value);
        wetSensor2Value   = (TextView)  findViewById(R.id.wetSensor2Value);
        wetSensor3Value   = (TextView) findViewById(R.id.wetSensor3Value);
        lightSensorValue   = (TextView)  findViewById(R.id.lightSensorValue);
        rainSensorValue   = (TextView)  findViewById(R.id.rainSensorValue);
        automaticActivated   = (TextView)  findViewById(R.id.automatic_value);
        exceptionCircuit1   = (TextView)  findViewById(R.id.exceptionCircuit1);
        circuit1Value   = (TextView)  findViewById(R.id.circuit1Value);
        exceptionCircuit2   = (TextView)  findViewById(R.id.exceptionCircuit2);
        circuit2Value   = (TextView)  findViewById(R.id.circuit2Value);
        exceptionCircuit3   = (TextView)  findViewById(R.id.exceptionCircuit3);
        circuit3Value   = (TextView)  findViewById(R.id.circuit3Value);


        //se especifica que mensajes debe aceptar el broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_MAINMENU);
        rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);
    }

    public class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BlueToothService.ACTION_MAINMENU)) {
                Bundle extras = intent.getExtras();
                String prog = extras.getString("Informacion");
                formatData(prog);
            }
            else {
                Toast.makeText(MainInformationActivity.this, "Error al procesar información", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        if(address != ""){
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
        }
        if (myProximitySensor != null) {
            mySensorManager.registerListener(proximitySensorEventListener,
                    myProximitySensor,
                    SensorManager.SENSOR_DELAY_NORMAL);
        }
        Intent msgIntent = new Intent(MainInformationActivity.this, BlueToothService.class);
        msgIntent.putExtra("Direccion_Bluethoot", address);
        msgIntent.putExtra("Comando", "CMD|STATUS#");
        startService(msgIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();
        mySensorManager.unregisterListener(proximitySensorEventListener);
        unregisterReceiver(rcv);
    }

    private void formatData(String data){
        Log.i("MainInformation: ",data);

        String[] sensores = data.split("#");
        for (int i = 0; i < sensores.length; i++){
            String[] parts = sensores[i].split("\\|");

            switch (parts[1]) {
                case "STATUS":
                    lightSensorValue.setText( parts[2] + "%" );
                    rainSensorValue.setText(parts[3] + "%");
                    automaticActivated.setText(parts[4]);
                    automaticoActivado = parts[4];
                    break;
                case "1":
                    if(parts.length == 9){
                        wetSensor1Value.setText(parts[2] + "%");
                        circuitosEncendidos += parts[3];
                        if (parts[3].toLowerCase().equals("si"))
                            circuit1Value.setText("Encendido");
                        else
                            circuit1Value.setText("Apagado");

                        String diasExcep1 = "";
                        if (parts[4].contains(",")) {
                            String[] dias = parts[4].split(",");
                            for (int l = 0; l < dias.length; l++) {
                                switch (dias[l]) {
                                    case "0":
                                        diasExcep1 += "Dom";
                                        break;
                                    case "1":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Lun";
                                        else
                                            diasExcep1 += " - Lun";
                                        break;
                                    case "2":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Mar";
                                        else
                                            diasExcep1 += " - Mar";
                                        break;
                                    case "3":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Mier";
                                        else
                                            diasExcep1 += " - Mier";
                                        break;
                                    case "4":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Jue";
                                        else
                                            diasExcep1 += " - Jue";
                                        break;
                                    case "5":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Vie";
                                        else
                                            diasExcep1 += " - Vie";
                                        break;
                                    case "6":
                                        if (diasExcep1.equals(""))
                                            diasExcep1 += "Sab";
                                        else
                                            diasExcep1 += " - Sab";
                                        break;
                                }
                            }
                        } else
                            diasExcep1 = parts[4];

                        String horario1= "";
                        if(parts.length == 9){
                            horario1 = (parts[5].length() == 1 ? "0" + parts[5] : parts[5]) + ":" + (parts[6].length() == 1 ? "0" + parts[6] : parts[6]) + " " + (parts[7].length() == 1 ? "0" + parts[7] : parts[7]) + ":" + (parts[8].length() == 1 ? "0" + parts[8] : parts[8]);
                        }
                        exceptionCircuit1.setText(diasExcep1 + " " + horario1);
                    }

                    break;
                case "2":
                    if(parts.length == 9){
                        wetSensor2Value.setText(parts[2] + "%");
                        circuitosEncendidos += "," + parts[3];

                        if (parts[3].toLowerCase().equals("si"))
                            circuit2Value.setText("Encendido");
                        else
                            circuit2Value.setText("Apagado");

                        String diasExcep2 = "";
                        if (parts[4].contains(",")) {
                            String[] dias = parts[4].split(",");
                            for (int j = 0; j < dias.length; j++) {
                                switch (dias[j]) {
                                    case "0":
                                        diasExcep2 += "Dom";
                                        break;
                                    case "1":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Lun";
                                        else
                                            diasExcep2 += " - Lun";
                                        break;
                                    case "2":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Mar";
                                        else
                                            diasExcep2 += " - Mar";
                                        break;
                                    case "3":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Mier";
                                        else
                                            diasExcep2 += " - Mier";
                                        break;
                                    case "4":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Jue";
                                        else
                                            diasExcep2 += " - Jue";
                                        break;
                                    case "5":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Vie";
                                        else
                                            diasExcep2 += " - Vie";
                                        break;
                                    case "6":
                                        if (diasExcep2.equals(""))
                                            diasExcep2 += "Sab";
                                        else
                                            diasExcep2 += " - Sab";
                                        break;
                                }
                            }
                        } else
                            diasExcep2 = parts[4];
                        String horario2 = "";
                        if(parts.length == 9){
                            horario2 = (parts[5].length() == 1 ? "0" + parts[5] : parts[5]) + ":" + (parts[6].length() == 1 ? "0" + parts[6] : parts[6]) + " " + (parts[7].length() == 1 ? "0" + parts[7] : parts[7]) + ":" + (parts[8].length() == 1 ? "0" + parts[8] : parts[8]);
                        }
                        exceptionCircuit2.setText(diasExcep2 + " " + horario2);
                    }

                    break;
                case "3":
                    if(parts.length == 9){
                        wetSensor3Value.setText(parts[2] + "%");
                        circuitosEncendidos += "," + parts[3];
                        if (parts[3].toLowerCase().equals("si"))
                            circuit3Value.setText("Encendido");
                        else
                            circuit3Value.setText("Apagado");

                        String diasExcep3 = "";
                        if (parts[4].contains(",")) {
                            String[] dias = parts[4].split(",");
                            for (int k = 0; k < dias.length; k++) {
                                switch (dias[k]) {
                                    case "0":
                                        diasExcep3 += "Dom";
                                        break;
                                    case "1":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Lun";
                                        else
                                            diasExcep3 += " - Lun";
                                        break;
                                    case "2":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Mar";
                                        else
                                            diasExcep3 += " - Mar";
                                        break;
                                    case "3":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Mier";
                                        else
                                            diasExcep3 += " - Mier";
                                        break;
                                    case "4":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Jue";
                                        else
                                            diasExcep3 += " - Jue";
                                        break;
                                    case "5":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Vie";
                                        else
                                            diasExcep3 += " - Vie";
                                        break;
                                    case "6":
                                        if (diasExcep3.equals(""))
                                            diasExcep3 += "Sab";
                                        else
                                            diasExcep3 += " - Sab";
                                        break;
                                }
                            }
                        } else
                            diasExcep3 = parts[4];

                        String horario3 = "";
                        if(parts.length == 9){
                            horario3 = (parts[5].length() == 1 ? "0" + parts[5] : parts[5]) + ":" + (parts[6].length() == 1 ? "0" + parts[6] : parts[6]) + " " + (parts[7].length() == 1 ? "0" + parts[7] : parts[7]) + ":" + (parts[8].length() == 1 ? "0" + parts[8] : parts[8]);
                        }
                        exceptionCircuit3.setText(diasExcep3 + " " + horario3);
                    }

                    break;
            }
        }


    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    // Funcion para ir a la configuracion general
    public void mainMenu (View view){
        Intent intent = new Intent(this, MainMenuActivity.class);
        Bundle extras;

        intent.putExtra("AutomaticoActivado", automaticoActivado);
        intent.putExtra("CircuitosEncendidos", circuitosEncendidos);
        circuitosEncendidos = "";
        startActivity(intent);
    }
}
