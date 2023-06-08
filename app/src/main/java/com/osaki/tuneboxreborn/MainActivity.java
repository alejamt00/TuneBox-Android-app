package com.osaki.tuneboxreborn;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

/**
 * Clase principal de la aplicación que muestra el contenedor de fragmentos en el que se van mostrando las diferentes ventanas.
 */
public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
    }
}