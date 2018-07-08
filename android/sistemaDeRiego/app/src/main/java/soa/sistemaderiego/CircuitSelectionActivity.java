package soa.sistemaderiego;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

public class CircuitSelectionActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_circuit_selection);
    }

    public void exceptionConfigurationActivity (View view){
        Intent intent = new Intent(this, ExceptionConfigurationActivity.class);
           try{
            Bundle bundle = new Bundle();
            bundle.putString("circuitNumber",view.getTag().toString());
            intent.putExtras(bundle);
            startActivity(intent);
        }catch(Exception e){
            Log.d("Error", e.toString());
           }

    }
}
