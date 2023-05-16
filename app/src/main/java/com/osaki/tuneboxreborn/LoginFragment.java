package com.osaki.tuneboxreborn;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Clase LoginFragment que extiende la clase Fragment.
 * Se utiliza para crear un fragmento de inicio de sesión.
 */
public class LoginFragment extends Fragment {

    private Button loginButton;
    private EditText usernameET, passET;
    private TextView regText;
    private FirebaseFirestore ff;
    private FirebaseAuth fAuth;


    /**
     * Constructor vacío requerido.
     */
    public LoginFragment() {
    }

    /**
     * Método para crear una nueva instancia de la clase LoginFragment.
     *
     * @return Una nueva instancia de la clase LoginFragment.
     */
    public static LoginFragment newInstance() {
        LoginFragment fragment = new LoginFragment();
        return fragment;
    }

    /**
     * Método llamado cuando se crea el fragmento.
     *
     * @param savedInstanceState El estado guardado de la instancia anterior.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Método para verificar si los campos de nombre de usuario y contraseña están llenos.
     *
     * @return Verdadero si ambos campos están llenos, falso en caso contrario.
     */
    public  boolean dataFilled(){
        return (!usernameET.getText().toString().equals("")&&!passET.getText().toString().equals(""));
    }

    public void login(String correo, String pass){
        fAuth.signInWithEmailAndPassword(correo,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast toast = Toast.makeText(getContext(), getString(R.string.existError), Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                } else {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                }
            }
        });
    }

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Se utiliza para inflar la vista y configurar los elementos de la interfaz y la implementación
     * de sus funciones
     *
     * @param inflater El LayoutInflater para inflar la vista.
     * @param container El contenedor de la vista.
     * @param savedInstanceState El estado guardado de la instancia anterior.
     * @return La vista inflada.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_login, container, false);

        fAuth = FirebaseAuth.getInstance();

        loginButton = view.findViewById(R.id.bLogin);
        usernameET = view.findViewById(R.id.emailBox);
        passET = view.findViewById(R.id.passBox);
        regText = view.findViewById(R.id.registerText);

        loginButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        loginButton.setTextColor(Color.WHITE);
                        if(dataFilled()) {
                            login(usernameET.getText().toString(),passET.getText().toString());
                        } else {
                            Toast toast = Toast.makeText(getContext(), getString(R.string.fillBothFields), Toast.LENGTH_SHORT);
                            toast.setMargin(50, 50);
                            toast.show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        loginButton.setTextColor(Color.parseColor("#BF40BF"));
                        break;
                }
                return false;
            }
        });


        regText.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        regText.setTextColor(Color.BLACK);
                        FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.fade_in,
                                        R.anim.fade_out,
                                        R.anim.fade_in,
                                        R.anim.fade_out
                                );

                        RegisterFragment tlf = new RegisterFragment();
                        ft.replace(R.id.fragmentContainerView,tlf).commit();
                        ft.addToBackStack(null);

                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        regText.setTextColor(Color.parseColor("#BF40BF"));
                        break;
                }
                return false;
            }
        });

        return view;
    }
}