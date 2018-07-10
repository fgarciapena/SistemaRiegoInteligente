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
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

public class ExceptionConfigurationActivity extends Activity {


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

    public String dataPackage = "";

    public ProgressReceiver rcv;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_exception_configuration);

        try{
            Intent intentExtras = getIntent();
            Bundle bundle = intentExtras.getExtras();
            if(intentExtras.hasExtra("circuitNumber")){
                titleText = (TextView) findViewById(R.id.textView9);
                circuitNumber = bundle.getString("circuitNumber");
                titleText.setText("Circuito " + circuitNumber);
            }
        }catch(Exception e){
            Log.d("Error", e.toString());
        }

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



        saveExceptionButton.setOnClickListener(saveExceptionButtonListener);

        IntentFilter filter = new IntentFilter();
        filter.addAction(BlueToothService.ACTION_EXCEPCIONMENU);
        rcv = new ProgressReceiver();
        registerReceiver(rcv, filter);
    }

    public class ProgressReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            if(intent.getAction().equals(BlueToothService.ACTION_EXCEPCIONMENU)) {
                Bundle extras = intent.getExtras();
                String prog = extras.getString("Informacion");
                formatData(prog);
            }
            else {
                Toast.makeText(ExceptionConfigurationActivity.this  , "Error al procesar información", Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    //Cada vez que se detecta el evento OnResume se establece la comunicacion con el HC05, creando un
    //socketBluethoot
    public void onResume() {
        super.onResume();

        Intent msgIntent = new Intent(ExceptionConfigurationActivity.this, BlueToothService.class);
        msgIntent.putExtra("Comando", "CMD|EXC#");
        startService(msgIntent);

        //I send a character when resuming.beginning transmission to check device is connected
        //If it is not an exception will be thrown in the write method and finish() will be called
        // Comando "CMD|CONFIG#"
        showToast("Cargando datos");
        //mConnectedThread.write("CMD|EXC#");
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(rcv);
    }

    public void formatData(String data){
        Log.i("Info excepciones: ", data);
        String[] parts = data.split("\\|");
        String diasExcep1= "";
        if(parts[2].equals(circuitNumber)){
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
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }


    private View.OnClickListener saveExceptionButtonListener = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            dataPackage =  "CMD|SET|EXC|" + circuitNumber + "|" +(sundaySwitch.isChecked() ? "0": "") + (sundaySwitch.isChecked() ? "," : "") + (mondaySwitch.isChecked() ? "1": "") +
                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() ? "," : "") + (tuesdaySwitch.isChecked() ? "2": "") +
                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() ? "," : "") + (wednesdaySwitch.isChecked() ? "3": "") +
                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() ? "," : "") + (thursdaySwitch.isChecked() ? "4": "") +
                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() || thursdaySwitch.isChecked() ? "," : "") + (fridaySwitch.isChecked() ? "5": "") +
                    (sundaySwitch.isChecked() || mondaySwitch.isChecked() || tuesdaySwitch.isChecked() || wednesdaySwitch.isChecked() || thursdaySwitch.isChecked() || fridaySwitch.isChecked() ? "," : "") + (saturdaySwitch.isChecked() ? "6": "") +
                    (horaDesde.getText().length() > 0 && !horaDesde.getText().equals("") ? "|" + horaDesde.getText().toString().split(":")[0] + "|" + horaDesde.getText().toString().split(":")[1] : "") +
                    (horaDesde.getText().length() > 0 && !horaHasta.getText().equals("") ? "|" + horaHasta.getText().toString().split(":")[0] + "|" + horaHasta.getText().toString().split(":")[1] : "")
                    + '#';
            mensaje.setText(dataPackage);
            Intent msgIntent = new Intent(ExceptionConfigurationActivity.this, BlueToothService.class);
            msgIntent.putExtra("Comando", dataPackage);
            startService(msgIntent);
            showToast("Guardado");
        }
    };

}
