package soa.sistemaderiegov3.Activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import soa.sistemaderiegov3.Activities.ConfigurationMenuActivity;
import soa.sistemaderiegov3.Activities.MainMenuActivity;
import soa.sistemaderiegov3.R;

public class MainInformationActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_information);
    }

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
