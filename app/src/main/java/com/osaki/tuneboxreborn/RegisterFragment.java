package com.osaki.tuneboxreborn;

import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Spinner;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

/**
 * Clase LoginFragment que extiende la clase Fragment.
 * Se utiliza para crear un fragmento de inicio de sesión.
 */
public class RegisterFragment extends Fragment {

    private String[] genreArray;
    private Spinner genreSpinner;
    private CustomSpinnerAdapter genreSpinnerAdapter;
    private ProgressBar progressBar;
    private EditText nameBox, userBox, mailBox, passBox, dateBox;
    private Button registerB;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private Boolean minEdad;


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
     * Método para verificar si los campos están llenos y son correctos.
     *
     * @return Verdadero si están llenos y son correctos, falso en caso contrario.
     */
    public boolean dataFilled() {
        return (!genreSpinner.getSelectedItem().toString().equals(getString(R.string.genreString))
                && !dateBox.getText().toString().trim().equals("") && !userBox.getText().toString().trim().equals("")
                && !nameBox.getText().toString().trim().equals("") && !passBox.getText().toString().trim().equals(""));
    }

    private void initData(View view) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        progressBar = view.findViewById(R.id.progressBar);
        minEdad = false;

        nameBox = view.findViewById(R.id.nameBox);
        userBox = view.findViewById(R.id.userBox);
        mailBox = view.findViewById(R.id.emailBox);
        passBox = view.findViewById(R.id.passBox);
        dateBox = view.findViewById(R.id.dateBox);
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
        genreSpinnerAdapter = new CustomSpinnerAdapter(view.getContext(), R.layout.spinner_item_layout, genreArray, 0);
        genreSpinner.setAdapter(genreSpinnerAdapter);

        registerB = view.findViewById(R.id.bRegister);

    }

    /**
     * Este método toma un entero n y devuelve una cadena con su valor pero con dos dígitos.
     * @param n
     */
    private String twoDigits(int n) {
        return (n<=9) ? ("0"+n) : String.valueOf(n);
    }

    /**
     * Muestra el dialog para elegir la fecha y la setea en el cuadro de fecha de nacimiento
     */
    private void showDatePickerDialog() {
        DatePickerFragment newFragment = DatePickerFragment.newInstance(new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker datePicker, int year, int month, int day) {
                // +1 because January is zero
                final String selectedDate = twoDigits(day) + " / " + twoDigits((month+1)) + " / " + year;
                dateBox.setText(selectedDate);
                minEdad = false;

                // Verificar si la fecha seleccionada es menor a una cierta edad
                int ageLimit = 13;
                Calendar selectedCalendar = Calendar.getInstance();
                selectedCalendar.set(year, month, day);
                Calendar currentCalendar = Calendar.getInstance();
                currentCalendar.add(Calendar.YEAR, -ageLimit);

                // Comparar los dos objetos Calendar para ver si la fecha seleccionada es anterior a la fecha límite
                if (selectedCalendar.compareTo(currentCalendar) <= 0) {
                    // (el usuario tiene al menos ageLimit años)
                    minEdad = true;
                }

            }
        });

        newFragment.show(getActivity().getSupportFragmentManager(), "datePicker");
    }


    private void PerformAuth() {
        String email = mailBox.getText().toString();
        String pass = passBox.getText().toString();
        String genre = genreSpinner.getSelectedItem().toString();
        String dateBirth = dateBox.getText().toString();
        String userName = userBox.getText().toString().toLowerCase();
        String pName = nameBox.getText().toString();

        if (!email.matches(emailPattern)) {
            Toast toast = Toast.makeText(getContext(), getString(R.string.notValidEmail), Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
        } else if (passBox.getText().toString().trim().length() < 6) {
            Toast toast = Toast.makeText(getContext(), getString(R.string.passLengthError), Toast.LENGTH_SHORT);
            toast.setMargin(50, 50);
            toast.show();
        } else {
            Query query = fStore.collection("users").whereEqualTo("user", userName);
            progressBar.setVisibility(View.VISIBLE);
            Log.d("RegisterDeb", "Buscando usuarios con userName: " + userName);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    if (task.isSuccessful()) {
                        if (task.getResult().isEmpty()) {
                            // No se encontró ningún usuario con el userName dado
                            if(minEdad){
                                fAuth.createUserWithEmailAndPassword(email, pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {
                                        if (task.isSuccessful()) {
                                            userID = fAuth.getCurrentUser().getUid();
                                            DocumentReference documentReference = fStore.collection("users").document(userID);
                                            Map<String,Object> user = new HashMap<>();
                                            user.put("pName",pName);
                                            user.put("user",userName);
                                            user.put("dateBirth",dateBirth);
                                            user.put("genre",genre);
                                            user.put("pName",pName);
                                            documentReference.set(user).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                @Override
                                                public void onSuccess(Void unused) {
                                                    Log.d("RegisterDeb", "Datos registrados correctamente para usuario "+ userID);
                                                }
                                            });
                                            progressBar.setVisibility(View.INVISIBLE);
                                            Toast toast = Toast.makeText(getContext(), getString(R.string.registerSuccess), Toast.LENGTH_SHORT);
                                            toast.setMargin(50, 50);
                                            toast.show();
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                public void run() {
                                                    getParentFragmentManager().popBackStackImmediate();
                                                }
                                            }, 500);

                                        } else {
                                            progressBar.setVisibility(View.INVISIBLE);
                                            Exception e = task.getException();
                                            String errorMessage;
                                            if (e instanceof FirebaseAuthUserCollisionException) {
                                                errorMessage = getString(R.string.existEmailError);
                                            } else {
                                                errorMessage = getString(R.string.errorCreate);
                                            }
                                            Toast.makeText(getContext(), errorMessage, Toast.LENGTH_SHORT).show();
                                        }
                                    }
                                });

                            } else {
                                Toast toast = Toast.makeText(getContext(), getString(R.string.existUser), Toast.LENGTH_SHORT);
                                toast.setMargin(50, 50);
                                toast.show();
                            }
                        } else {
                            // Se encontró al menos un usuario con el userName dado
                            progressBar.setVisibility(View.INVISIBLE);
                            Toast toast = Toast.makeText(getContext(), getString(R.string.existUser), Toast.LENGTH_SHORT);
                            toast.setMargin(50, 50);
                            toast.show();
                        }
                    } else {
                        // Ocurrió un error al realizar la consulta
                    }
                }
            });
        }
    }

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Se utiliza para inflar la vista y configurar los elementos de la interfaz y la implementación
     * de sus funciones
     *
     * @param inflater           El LayoutInflater para inflar la vista.
     * @param container          El contenedor de la vista.
     * @param savedInstanceState El estado guardado de la instancia anterior.
     * @return La vista inflada.
     */
    @SuppressLint("ClickableViewAccessibility")
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register, container, false);
        initData(view);

        dateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        registerB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(dataFilled()){
                    PerformAuth();
                } else {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.errorFieldsNotFilled) + "", Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                }
            }
        });

        return view;
    }


}
