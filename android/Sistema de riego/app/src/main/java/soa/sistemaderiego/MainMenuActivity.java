package soa.sistemaderiego;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Vibrator;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

public class MainMenuActivity extends Activity {

    public static final String EXTRA_MESSAGE = " soa.sistemaderiegov3.Activities.MESSAGE";

    public String automaticoActivado;
    public String circuitosEncendidos;

    public Button buttonstart;
    public TextView automaticoLabel;


    private ShakeListener mShaker;


    public ProgressReceiver rcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_menu);

        final Vibrator vibe = (Vibrator)getSystemService(Context.VIBRATOR_SERVICE);

        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        Intent intent=getIntent();
        try {
            if (intent.hasExtra("AutomaticoActivado") && intent.hasExtra("CircuitosEncendidos")) {
                Bundle extras = intent.getExtras();
                automaticoActivado = extras.getString("AutomaticoActivado");
                circuitosEncendidos = extras.getString("CircuitosEncendidos");
            }else{
                automaticoActivado = "";
            }
        }
        catch (Exception ex)
        {
            showToast( "Fallo el intent: " + ex.toString());
        }

        buttonstart = (Button) findViewById(R.id.button_start);
        automaticoLabel   = (TextView) findViewById(R.id.automaticoLabel);


        if(automaticoActivado.equals("SI")){
            automaticoLabel.setText("Modo automatico activado");
            buttonstart.setEnabled(false);
        }
        else{
            automaticoLabel.setText("Modo manual activado");
            buttonstart.setEnabled(true);
        }

        mShaker = new ShakeListener(this);
        mShaker.setOnShakeListener(new ShakeListener.OnShakeListener () {
            public void onShake()
            {
                vibe.vibrate(100);
                showModalForManualMode(null);
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_MAINAUTOMATIC);
        rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);
    }


    public class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BlueToothService.ACTION_MAINAUTOMATIC)) {
                Bundle extras = intent.getExtras();
                String prog = extras.getString("Informacion");
            }
            else {
                Toast.makeText(MainMenuActivity.this, "Error al procesar información", Toast.LENGTH_SHORT).show();
            }
        }
    }


    /*Called when the user taps the Vincular Button*/
    public void startSystem (View view){
        Intent intent = new Intent(this, StartDeviceActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        extras.putString("CircuitosEncendidos", circuitosEncendidos);
        intent.putExtras(extras);
        startActivity(intent);

    }

    /*Called when the user taps the Configuracion Button*/
    public void configurationMenu (View view){
        Intent intent = new Intent(this, ConfigurationMenuActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        intent.putExtras(extras);
        startActivity(intent);
    }

    public void circuitSelection (View view){
        Intent intent = new Intent(this, CircuitSelectionActivity.class);
        Bundle extras;
        //Obtengo el parametro, aplicando un Bundle, que me indica la Mac Adress del HC05
        extras = new Bundle();
        intent.putExtras(extras);

        startActivity(intent);
    }

    public void showModalForManualMode(View view) {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
        if(automaticoActivado.equals("SI")){
            alert.setTitle("¿Quieres utilizar el modo manual?");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String dataPackage = "CMD|AUTO|OFF#";
                    Intent msgIntent = new Intent(MainMenuActivity.this, BlueToothService.class);
                    msgIntent.putExtra("Comando", dataPackage);
                    startService(msgIntent);
                    automaticoActivado = "NO";

                    if(automaticoActivado.equals("SI")){
                        automaticoLabel.setText("Modo automatico activado");
                        buttonstart.setEnabled(false);
                    }
                    else{
                        automaticoLabel.setText("Modo manual activado");
                        buttonstart.setEnabled(true);
                    }

                }
            });

            alert.setNegativeButton("Cancelar",
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int whichButton) {
                            //Do nothing
                        }
                    });
            alert.show();
        }else  if(automaticoActivado.equals("NO")){

            alert.setTitle("¿Quieres utilizar el modo automatico?");

            alert.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialog, int whichButton) {
                    String dataPackage = "CMD|AUTO|ON#";
                    Intent msgIntent = new Intent(MainMenuActivity.this, BlueToothService.class);
                    msgIntent.putExtra("Comando", dataPackage);
                    startService(msgIntent);
                    automaticoActivado = "SI";

                    if(automaticoActivado.equals("SI")){
                        automaticoLabel.setText("Modo automatico activado");
                        buttonstart.setEnabled(false);
                    }
                    else{
                        automaticoLabel.setText("Modo manual activado");
                        buttonstart.setEnabled(true);
                    }
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


    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }



    @Override
    public void onResume()
    {
        Intent msgIntent = new Intent(MainMenuActivity.this, BlueToothService.class);
        msgIntent.putExtra("Comando", "#");
        startService(msgIntent);
        mShaker.resume();
        super.onResume();
    }
    @Override
    public void onPause()
    {
        unregisterReceiver(rcv);
        mShaker.pause();
        super.onPause();
    }
}
