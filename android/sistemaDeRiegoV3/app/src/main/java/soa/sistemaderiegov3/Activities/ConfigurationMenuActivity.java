package soa.sistemaderiegov3.Activities;

import soa.sistemaderiegov3.R;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
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

public class ConfigurationMenuActivity extends AppCompatActivity {

    /*BlueTooth Zone */

    Button btnApagar;
    Button btnEncender;
    TextView txtPotenciometro;

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

    /* Fin BluetoohZone */

    public Button buttonSaveConfiguration;
    public EditText wetSensor1Value;
    public EditText wetSensor2Value;
    public EditText wetSensor3Value;
    public EditText lightSensorValue;
    public EditText rainSensorValue;
    public TextView mensaje;
    public String dataPackage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses
        setContentView(R.layout.activity_configuration);



        buttonSaveConfiguration = findViewById(R.id.buttonSave);

        wetSensor1Value   = findViewById(R.id.wet_sensor1_value);
        wetSensor2Value   = findViewById(R.id.wet_sensor2_value);
        wetSensor3Value   = findViewById(R.id.wet_sensor3_value);
        lightSensorValue   = findViewById(R.id.light_sensor_value);
        rainSensorValue   = findViewById(R.id.rain_sensor_value);


        mensaje = findViewById(R.id.mensaje);


/*        buttonSaveConfiguration.setOnClickListener(
                new View.OnClickListener()
                {
                    public void onClick(View view)
                    {
                        dataPackage = "CMD|SET|LUZ|" + lightSensorValue.getText() + "|LLUVIA|"+ rainSensorValue.getText() +
                                "|HUMEDAD1|" + wetSensor1Value.getText() + "|HUMEDAD2|" + wetSensor2Value.getText() + "|HUMEDAD3|" + wetSensor3Value.getText() + '#';

                        mensaje.setText(dataPackage);

                    }


                });*/

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();

        //defino los handlers para los botones Apagar y encender
        buttonSaveConfiguration.setOnClickListener(buttonSaveConfigurationListener);
    }


    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        if(intent.hasExtra("Direccion_Bluethoot")){
            extras=intent.getExtras();
            address= extras.getString("Direccion_Bluethoot");
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
        mConnectedThread.write("x");
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
    private Handler Handler_Msg_Hilo_Principal ()
    {
        return new Handler() {
            public void handleMessage(android.os.Message msg)
            {
                //si se recibio un msj del hilo secundario
                if (msg.what == handlerState)
                {
                    //voy concatenando el msj
                    String readMessage = (String) msg.obj;
                    recDataString.append(readMessage);
                    int endOfLineIndex = recDataString.indexOf("#");

                    //cuando recibo toda una linea la muestro en el layout
                    if (endOfLineIndex > 0)
                    {
                        String dataInPrint = recDataString.substring(0, endOfLineIndex);
                        txtPotenciometro.setText(dataInPrint);

                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

    }

    //Listener del boton encender que envia  msj para Apagar Led a Arduino atraves del Bluethoot
    private View.OnClickListener buttonSaveConfigurationListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPackage = "CMD|SET|LUZ|" + lightSensorValue.getText() + "|LLUVIA|"+ rainSensorValue.getText() +
                    "|HUMEDAD1|" + wetSensor1Value.getText() + "|HUMEDAD2|" + wetSensor2Value.getText() + "|HUMEDAD3|" + wetSensor3Value.getText() + '#';
            mConnectedThread.write(dataPackage);    // Send all data via Bluetooth
            showToast("Apagar el LED");
        }
    };


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
                    bytes = mmInStream.read(buffer);
                    String readMessage = new String(buffer, 0, bytes);

                    //se muestran en el layout de la activity, utilizando el handler del hilo
                    // principal antes mencionado
                    bluetoothIn.obtainMessage(handlerState, bytes, -1, readMessage).sendToTarget();
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

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    public void sendConfiguration(String configuracion){
        //TODO: call bluetooth service and send the setted information to the embebbed device
    }
}
