package soa.sistemaderiegov3.Activities;

import soa.sistemaderiegov3.R;
import soa.sistemaderiegov3.Services.BluetoothService;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

public class StartDeviceActivity extends AppCompatActivity {
    

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //TODO: get from embebbed device the current circuits statuses
        setContentView(R.layout.activity_start_device);
    }

    /**
     * sends the start/stop configuration to the embebbed device
     * **/
    public void sendStartStopConfiguration(View view){
        //TODO: call bluetooth service and send the setted information to the embebbed device
    }
}
