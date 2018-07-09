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
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.UUID;

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

     String automaticoActivado;

    @SuppressLint({"WrongViewCast", "HandlerLeak"})
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_information);


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
        ProgressReceiver rcv = new ProgressReceiver();
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

        Intent msgIntent = new Intent(MainInformationActivity.this, BlueToothService.class);
        msgIntent.putExtra("Direccion_Bluethoot", address);
        msgIntent.putExtra("Comando", "CMD|STATUS#");
        startService(msgIntent);
    }

    private void formatData(String data){
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
                    wetSensor1Value.setText(parts[2] + "%");
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
                                    diasExcep1 += "Domingo";
                                    break;
                                case "1":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Lunes";
                                    else
                                        diasExcep1 += "Lunes";
                                    break;
                                case "2":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Martes";
                                    else
                                        diasExcep1 += "Martes";
                                    break;
                                case "3":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Miercoles";
                                    else
                                        diasExcep1 += "Miercoles";
                                    break;
                                case "4":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Jueves";
                                    else
                                        diasExcep1 += "Jueves";
                                    break;
                                case "5":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Viernes";
                                    else
                                        diasExcep1 += "Viernes";
                                    break;
                                case "6":
                                    if (diasExcep1.equals(""))
                                        diasExcep1 += " - Sabado";
                                    else
                                        diasExcep1 += "Sabado";
                                    break;
                            }
                        }
                    } else
                        diasExcep1 = parts[4];

                    String horario1= "";
                    if(parts.length == 8){
                        horario1 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
                    }
                    exceptionCircuit1.setText(diasExcep1 + " " + horario1);
                    break;
                case "2":
                    wetSensor2Value.setText(parts[2] + "%");
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
                                    diasExcep2 += "Domingo";
                                    break;
                                case "1":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Lunes";
                                    else
                                        diasExcep2 += "Lunes";
                                    break;
                                case "2":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Martes";
                                    else
                                        diasExcep2 += "Martes";
                                    break;
                                case "3":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Miercoles";
                                    else
                                        diasExcep2 += "Miercoles";
                                    break;
                                case "4":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Jueves";
                                    else
                                        diasExcep2 += "Jueves";
                                    break;
                                case "5":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Viernes";
                                    else
                                        diasExcep2 += "Viernes";
                                    break;
                                case "6":
                                    if (diasExcep2.equals(""))
                                        diasExcep2 += " - Sabado";
                                    else
                                        diasExcep2 += "Sabado";
                                    break;
                            }
                        }
                    } else
                        diasExcep2 = parts[4];
                    String horario2 = "";
                    if(parts.length == 8){
                        horario2 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
                    }
                    exceptionCircuit2.setText(diasExcep2 + " " + horario2);
                    break;
                case "3":
                    wetSensor3Value.setText(parts[2] + "%");
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
                                    diasExcep3 += "Domingo";
                                    break;
                                case "1":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Lunes";
                                    else
                                        diasExcep3 += "Lunes";
                                    break;
                                case "2":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Martes";
                                    else
                                        diasExcep3 += "Martes";
                                    break;
                                case "3":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Miercoles";
                                    else
                                        diasExcep3 += "Miercoles";
                                    break;
                                case "4":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Jueves";
                                    else
                                        diasExcep3 += "Jueves";
                                    break;
                                case "5":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Viernes";
                                    else
                                        diasExcep3 += "Viernes";
                                    break;
                                case "6":
                                    if (diasExcep3.equals(""))
                                        diasExcep3 += " - Sabado";
                                    else
                                        diasExcep3 += "Sabado";
                                    break;
                            }
                        }
                    } else
                        diasExcep3 = parts[4];

                    String horario3 = "";
                    if(parts.length == 8){
                        horario3 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
                    }
                    exceptionCircuit3.setText(diasExcep3 + " " + horario3);
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
        intent.putExtra("Direccion_Bluethoot", address);
        startActivity(intent);
    }
}
