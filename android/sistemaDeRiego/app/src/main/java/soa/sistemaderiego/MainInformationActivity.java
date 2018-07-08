package soa.sistemaderiego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
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


    Handler bluetoothIn;
    final int handlerState = 0; //used to identify handler message

    private BluetoothAdapter btAdapter = null;
    private BluetoothSocket btSocket = null;
    private StringBuilder recDataString = new StringBuilder();

    private ConnectedThread mConnectedThread;

    // SPP UUID service  - Funciona en la mayoria de los dispositivos
    private static final UUID BTMODULEUUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    // String for MAC address del Hc05
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
     TextView mensaje;


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


        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

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

        BluetoothDevice device = btAdapter.getRemoteDevice(address);

        //se realiza la conexion del Bluethoot crea y se conectandose a atraves de un socket
        try
        {
            btSocket = createBluetoothSocket(device);
        }
        catch (IOException e)
        {
            showToast( "La creacción del Socket fallo");
        }
        // Establish the Bluetooth socket connection.
        try
        {
            btSocket.connect();
        }
        catch (IOException e)
        {
            try
            {
                btSocket.close();
            }
            catch (IOException e2)
            {
                //insert code to deal with this
            }
        }

        //Una establecida la conexion con el Hc05 se crea el hilo secundario, el cual va a recibir
        // los datos de Arduino atraves del bluethoot
        mConnectedThread = new ConnectedThread(btSocket);
        mConnectedThread.start();

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        mConnectedThread.write("CMD|STATUS#");


    }


    @Override
    //Cuando se ejecuta el evento onPause se cierra el socket Bluethoot, para no recibiendo datos
    public void onPause()
    {
        super.onPause();
        try
        {
            //Don't leave Bluetooth sockets open when leaving activity
            btSocket.close();
        } catch (IOException e2) {
            //insert code to deal with this
        }
    }

    //Metodo que crea el socket bluethoot
    private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException {

        return  device.createRfcommSocketToServiceRecord(BTMODULEUUID);
    }

    //Handler que sirve que permite mostrar datos en el Layout al hilo secundario
    @SuppressLint("HandlerLeak")
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg) {
                //si se recibio un msj del hilo secundario

                    switch (msg.what) {
                        case handlerState:
                            String readMessage = (String) msg.obj;

                            recDataString.append(readMessage);
                        //voy concatenando el msj
                        int endOfLineIndex = recDataString.indexOf("?");

                        //cuando recibo toda una linea la muestro en el layout
                        if (endOfLineIndex > 0) {

                            // comunicacion entre hilos ver como se hace.
                            String data = recDataString.substring(0, endOfLineIndex);

                            formatData(data);

                            recDataString = new StringBuilder();
                        }
                            break;
                    }

                }

        };
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

                    String horario1 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
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

                    String horario2 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
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

                    String horario3 = parts[5] + ":" + parts[6] + " " + parts[7] + ":" + parts[8];
                    exceptionCircuit3.setText(diasExcep3 + " " + horario3);
                    break;
            }
        }


    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    //******************************************** Hilo secundario del Activity**************************************
    //*************************************** recibe los datos enviados por el HC05**********************************

    private class ConnectedThread extends Thread
    {
        private final InputStream mmInStream;
        private final OutputStream mmOutStream;

        //Constructor de la clase del hilo secundario
        public ConnectedThread(BluetoothSocket socket)
        {
            InputStream tmpIn = null;
            OutputStream tmpOut = null;

            try
            {
                //Create I/O streams for connection
                tmpIn = socket.getInputStream();
                tmpOut = socket.getOutputStream();
            } catch (IOException e) { }

            mmInStream = tmpIn;
            mmOutStream = tmpOut;
        }

        //metodo run del hilo, que va a entrar en una espera activa para recibir los msjs del HC05
        public void run()
        {
            byte[] buffer = new byte[256];
            int bytes;

            //el hilo secundario se queda esperando mensajes del HC05
            while (true)
            {
                try
                {
                    //se leen los datos del Bluethoot
//                    buffer = new byte[512];
//                    bytes = mmInStream.read(buffer);
//                    if(buffer[0] != 0) {
//                        String readMessage = new String(buffer);
//                        if(readMessage.contains("#"))
//                            bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
//                    }
                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado

                } catch (IOException e) {
                    break;
                }
            }
        }


        //write method
        public void write(String input) {
            byte[] msgBuffer = input.getBytes();           //converts entered String into bytes
            try {
                mmOutStream.write(msgBuffer);                //write bytes over BT connection via outstream
            } catch (IOException e) {
                //if you cannot write, close the application
                showToast("La conexion fallo");
                finish();

            }
        }
    }

    // Funcion para ir a la configuracion general
    public void mainMenu (View view){
        Intent intent = new Intent(this, MainMenuActivity.class);
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
}
