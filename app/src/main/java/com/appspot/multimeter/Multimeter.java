package com.appspot.multimeter;


import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.TextView;

import it.beppi.knoblibrary.Knob;

public class Multimeter extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    // knob states
    public static final int EXIT_STATE = 0;
    public static final int MA500_STATE = 1;
    public static final int OFF_STATE = 2;
    public static final int V3_STATE = 3;
    public static final int V5_STATE = 4;


    private Knob knob;
    private TextView value;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);

        // init knob
        knob = (Knob) findViewById(R.id.knob);
        knob.setOnStateChanged(knobChanged);
        value = (TextView) findViewById(R.id.txtValue);

        // set value font
        Typeface myTypeface = Typeface.createFromAsset(getAssets(), "fonts/digital.ttf");
        value.setTypeface(myTypeface);
        value.setText("123456789");
    }

    Knob.OnStateChanged knobChanged = new Knob.OnStateChanged() {
        @Override
        public void onState(int state) {
            switch (state) {
                case EXIT_STATE:
                    // Disconnect and return to intro page
                    AlertDialog.Builder builder = new AlertDialog.Builder(Multimeter.this);
                    builder.setMessage("Exit Multimeter?").setTitle("Please confirm exit");
                    builder.setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // open intro activity and close current activity
                            final Intent intent = new Intent(Multimeter.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        }
                    });
                    builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog, int id) {
                            // User cancelled the dialog
                        }
                    });
                    AlertDialog dialog = builder.create();
                    dialog.show();
                    break;
                case MA500_STATE:

                    break;
                case OFF_STATE:

                    break;
                case V3_STATE:

                    break;
                case V5_STATE:
                    break;
                default:
                    break;
            }
        }
    };
}
