package soa.sistemaderiego;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class CircuitSelectionActivity extends Activity {

    public String address = "";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuit_selection);

        Intent intent = getIntent();
        Bundle extras;

        try {
            if(intent.hasExtra("Direccion_Bluethoot")){
                extras=intent.getExtras();
                address= extras.getString("Direccion_Bluethoot");
            }else{
                address = "";
            }
        }
        catch (Exception ex)
        {
            showToast( "Fallo el intent: " + ex.toString());
        }
    }

    private void showToast(String message) {
        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT).show();
    }

    public void exceptionConfigurationActivity (View view){
        Intent intent = new Intent(this, ExceptionConfigurationActivity.class);
           try{
            Bundle bundle = new Bundle();
            bundle.putString("circuitNumber",view.getTag().toString());
            bundle.putString("Direccion_Bluethoot", address);
            intent.putExtras(bundle);
            startActivity(intent);


           }catch(Exception e){
            Log.d("Error", e.toString());
           }

    }
}
