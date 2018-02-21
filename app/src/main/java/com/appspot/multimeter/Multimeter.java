package com.appspot.multimeter;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

import it.beppi.knoblibrary.Knob;

public class Multimeter extends AppCompatActivity {

    public static final String EXTRAS_DEVICE_NAME = "DEVICE_NAME";
    public static final String EXTRAS_DEVICE_ADDRESS = "DEVICE_ADDRESS";

    private Knob knob;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_multimeter);

        knob = (Knob) findViewById(R.id.knob);
    }
}
