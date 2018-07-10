package soa.sistemaderiego;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Switch;
import android.widget.Toast;

public class StartDeviceActivity extends Activity {

    public String circuitosEncendidos;

    public ProgressReceiver rcv;

    public Switch circuit1;
    public Switch circuit2;
    public Switch circuit3;

    public String dataPackage = "";

    public Button button_send_start_stop;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start_device);
        Intent intent=getIntent();
        try {
            if (intent.hasExtra("CircuitosEncendidos")) {
                Bundle extras = intent.getExtras();
                circuitosEncendidos = extras.getString("CircuitosEncendidos");
            }else{
                circuitosEncendidos = "";
            }
        }
        catch (Exception ex)
        {
            showToast( "Fallo el intent: " + ex.toString());
        }

        circuit1   = (Switch) findViewById(R.id.switch_circuit_1);
        circuit2   = (Switch) findViewById(R.id.switch_circuit_2);
        circuit3   = (Switch) findViewById(R.id.switch_circuit_3);
        button_send_start_stop   = (Button) findViewById(R.id.button_send_start_stop);

        button_send_start_stop.setOnClickListener(saveCircuitButtonListener);


        String[] circuitos = circuitosEncendidos.split(",");
        if(circuitos[0].equals("SI"))
            circuit1.setChecked(true);
        else
            circuit1.setChecked(false);

        if(circuitos[1].equals("SI"))
            circuit2.setChecked(true);
        else
            circuit2.setChecked(false);
        if(circuitos[2].equals("SI"))
            circuit3.setChecked(true);
        else
            circuit3.setChecked(false);

        //se especifica que mensajes debe aceptar el broadcastreceiver
        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_MODOMANUAL);
        rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);
    }


    public class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BlueToothService.ACTION_MODOMANUAL)) {
                Bundle extras = intent.getExtras();
                String prog = extras.getString("Informacion");

            }
            else {
                Toast.makeText(StartDeviceActivity.this, "Error al procesar información", Toast.LENGTH_SHORT).show();
            }
        }
    }

    public void onResume() {
        super.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        unregisterReceiver(rcv);
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private View.OnClickListener saveCircuitButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPackage = "Circuits" + "|" + (circuit1.isChecked() ? "1": "0" )+ "|"+ (circuit2.isChecked() ? "1": "0" ) + "|" + (circuit3.isChecked() ? "1": "0" );
            Intent msgIntent = new Intent(StartDeviceActivity.this, BlueToothService.class);
            msgIntent.putExtra("Comando", dataPackage);
            startService(msgIntent);
            showToast("Enviado");

        }
    };
}
