package soa.sistemaderiego;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.UUID;

public class ExceptionConfigurationActivity extends Activity {


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
                titleText = (TextView) findViewById(R.id.textView9);
                circuitNumber = bundle.getString("circuitNumber").toString();
                titleText.setText("Circuito " + circuitNumber);
            }
        }catch(Exception e){
            Log.d("Error", e.toString());
        }

        //obtengo el adaptador del bluethoot
        btAdapter = BluetoothAdapter.getDefaultAdapter();

        //defino el Handler de comunicacion entre el hilo Principal  el secundario.
        //El hilo secundario va a mostrar informacion al layout atraves utilizando indeirectamente a este handler
        bluetoothIn = Handler_Msg_Hilo_Principal();


        saveExceptionButton = (Button) findViewById(R.id.saveExceptionButton);

        mondaySwitch   = (Switch) findViewById(R.id.mondaySwitch);
        tuesdaySwitch   = (Switch) findViewById(R.id.tuesdaySwitch);
        wednesdaySwitch   = (Switch) findViewById(R.id.wednesdaySwitch);
        thursdaySwitch   = (Switch) findViewById(R.id.thursdaySwitch);
        fridaySwitch   = (Switch) findViewById(R.id.fridaySwitch);
        saturdaySwitch   = (Switch) findViewById(R.id.saturdaySwitch);
        sundaySwitch   = (Switch) findViewById(R.id.sundaySwitch);
        horaDesde = (TextView) findViewById(R.id.startHourValue);
        horaHasta = (TextView) findViewById(R.id.endHourValue);

        mensaje = (TextView) findViewById(R.id.mensaje);

        mConnectedThread.write("CMD|EXC#");

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
                        mConnectedThread.write(dataPackage);
                        try {
                            mConnectedThread.sleep(2000);
                            mConnectedThread.write("CMD|EXC#");
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                    }
                });
    }

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        Bundle extras=intent.getExtras();

        address= extras.getString("Direccion_Bluethoot");

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
            public void handleMessage(Message msg)
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
                        String data = recDataString.substring(0, endOfLineIndex);
                        String[] parts = data.split("|");
                        String diasExcep1= "";

                        if(parts[3].contains(",")){
                            String[] dias = parts[3].split(",");
                            for(int i = 0;i< dias.length ; i++){
                                switch (dias[i]){
                                    case "0":
                                        sundaySwitch.setChecked(true);
                                        break;
                                    case "1":
                                        mondaySwitch.setChecked(true);
                                        break;
                                    case "2":
                                        tuesdaySwitch.setChecked(true);
                                        break;
                                    case "3":
                                        wednesdaySwitch.setChecked(true);
                                        break;
                                    case "4":
                                        thursdaySwitch.setChecked(true);
                                        break;
                                    case "5":
                                        fridaySwitch.setChecked(true);
                                        break;
                                    case "6":
                                        saturdaySwitch.setChecked(true);
                                        break;
                                }
                            }
                        }
                        String horadesde = parts[4] + ":" + parts[5];
                        String horahasta = parts[6] + ":" + parts[7];
                        horaDesde.setText(horadesde);
                        horaHasta.setText(horahasta);
                        recDataString.delete(0, recDataString.length());
                    }
                }
            }
        };

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
}
