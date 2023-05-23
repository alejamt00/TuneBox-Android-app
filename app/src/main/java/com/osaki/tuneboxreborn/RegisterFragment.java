package com.osaki.tuneboxreborn;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

/**
 * Clase LoginFragment que extiende la clase Fragment.
 * Se utiliza para crear un fragmento de inicio de sesión.
 */
public class RegisterFragment extends Fragment {

    private String[] genreArray;
    private Spinner genreSpinner;
    private CustomSpinnerAdapter genreSpinnerAdapter;
    private EditText mailBox, passBox;
    private Button registerB;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private FirebaseAuth fAuth;
    private FirebaseUser fUser;


    /**
     * Constructor vacío requerido.
     */
    public RegisterFragment() {
    }

    /**
     * Método para crear una nueva instancia de la clase LoginFragment.
     *
     * @return Una nueva instancia de la clase LoginFragment.
     */
    public static RegisterFragment newInstance() {
        RegisterFragment fragment = new RegisterFragment();
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
//    public  boolean dataFilled(){
//        return (!usernameET.getText().toString().equals("")&&!passET.getText().toString().equals(""));
//    }

    private void initData(View view) {
        fAuth = FirebaseAuth.getInstance();
        fUser = fAuth.getCurrentUser();
        mailBox = view.findViewById(R.id.emailBox);
        passBox = view.findViewById(R.id.passBox);
        registerB = view.findViewById(R.id.bRegister);
        genreArray = new String[11];
        genreArray[0] = getString(R.string.genreString);
        genreArray[1] = getString(R.string.bsString);
        genreArray[2] = getString(R.string.rockString);
        genreArray[3] = getString(R.string.indieString);
        genreArray[4] = getString(R.string.metalString);
        genreArray[5] = getString(R.string.popString);
        genreArray[6] = getString(R.string.reggaetonString);
        genreArray[7] = getString(R.string.classicalString);
        genreArray[8] = getString(R.string.technoString);
        genreArray[9] = getString(R.string.hiphopString);
        genreArray[10] = getString(R.string.flamencoString);
        genreSpinner = view.findViewById(R.id.spinnerGenre);
        genreSpinnerAdapter = new CustomSpinnerAdapter(view.getContext(),R.layout.spinner_item_layout,genreArray,0);
        genreSpinner.setAdapter(genreSpinnerAdapter);

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
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initData(view);

        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PerformAuth();
            }
        });

        return view;
    }

    private void PerformAuth() {
        String email = mailBox.getText().toString();
        String pass = passBox.getText().toString();

        if(!email.matches(emailPattern)){
            Toast toast = Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
        } else if (pass.isEmpty()){
            Toast toast = Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
        } else {
            fAuth.createUserWithEmailAndPassword(email,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        Toast toast = Toast.makeText(getContext(), getString(R.string.registerSuccess), Toast.LENGTH_SHORT);
                        toast.setMargin(50, 50);
                        toast.show();
                        Handler handler = new Handler();
                        handler.postDelayed(new Runnable() {
                            public void run() {
                                getParentFragmentManager().popBackStackImmediate();
                            }
                        }, 1000);

                    } else {
                        Toast toast = Toast.makeText(getContext(), task.getException()+"", Toast.LENGTH_SHORT);
                        toast.setMargin(50, 50);
                        toast.show();
                    }
                }
            });
        }
    }


}