//package soa.sistemaderiegov3.Activities;
//
//import android.content.Intent;
//import android.support.v7.app.AppCompatActivity;
//import android.os.Bundle;
//import android.widget.TextView;
//
//import soa.sistemaderiegov3.R;
//
//public class DisplayMessageActivity extends AppCompatActivity {
//
//    @Override
//    protected void onCreate(Bundle savedInstanceState) {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.activity_display_message);
//
//        //Get the intent that started this activity and extract the string
//        Intent intent = getIntent();
//        String message = intent.getStringExtra(MainActivity.EXTRA_MESSAGE);
//
//        //capture the layout's textView and set the string as its text
//        TextView textView = findViewById(R.id.textView);
//        textView.setText(message);
//    }
//}
