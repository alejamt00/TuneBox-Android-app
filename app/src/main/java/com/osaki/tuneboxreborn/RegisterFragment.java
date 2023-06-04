package com.osaki.tuneboxreborn;

import static android.app.Activity.RESULT_OK;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.Inflater;

/**
 * Clase LoginFragment que extiende la clase Fragment.
 * Se utiliza para crear un fragmento de inicio de sesión.
 */
public class RegisterFragment extends Fragment {

    private ActivityResultLauncher<Intent> pickImageLauncher;

    private String[] genreArray;
    private Spinner genreSpinner;
    private ImageView avatarButton, ivLogoLoading;
    private CustomSpinnerAdapter genreSpinnerAdapter;
    private EditText nameBox, userBox, mailBox, passBox, dateBox;
    private TextView loginB;
    private Button registerB;
    private String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";
    private FirebaseAuth fAuth;
    private FirebaseFirestore fStore;
    private String userID;
    private Boolean minEdad;
    private Uri avatarUri;
    private Boolean avatarSelectad;


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

    private void initData(View view, LayoutInflater inflater) {
        fAuth = FirebaseAuth.getInstance();
        fStore = FirebaseFirestore.getInstance();
        minEdad = false;
        avatarSelectad = false;

        View loadingView = inflater.inflate(R.layout.dialog_progress, null);
        ivLogoLoading = loadingView.findViewById(R.id.logoIcono);
        avatarButton = view.findViewById(R.id.ivAvatar);
        nameBox = view.findViewById(R.id.nameBox);
        userBox = view.findViewById(R.id.userBox);
        mailBox = view.findViewById(R.id.emailBox);
        passBox = view.findViewById(R.id.passBox);
        dateBox = view.findViewById(R.id.dateBox);
        loginB = view.findViewById(R.id.loginText);

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

    public void startAnimation(View loadingView, LayoutInflater inflater){
        Drawable dLoading = getContext().getDrawable(R.drawable.logo_animado);
        ImageView ivLogoLoading = loadingView.findViewById(R.id.logoIcono);
        ivLogoLoading.setImageDrawable(dLoading);

        if (dLoading instanceof Animatable) {
            final Animatable animatable = (Animatable) dLoading;
            animatable.start();
        }
    }

    private void startAnimationColor(View loadingView) {
        ImageView ivLogoLoading = loadingView.findViewById(R.id.logoIcono);
        Drawable drawableLogo = ivLogoLoading.getDrawable();

        // Comprueba si el Drawable es una instancia de AnimatedVectorDrawable
        if (drawableLogo instanceof AnimatedVectorDrawable) {
            AnimatedVectorDrawable animatedVectorDrawable = (AnimatedVectorDrawable) drawableLogo;

            // Crea una copia modificable del Drawable
            AnimatedVectorDrawable mutableDrawable = (AnimatedVectorDrawable) animatedVectorDrawable.mutate();

            // Crea un ObjectAnimator para animar el cambio de color
            ObjectAnimator objectAnimator = ObjectAnimator.ofArgb(mutableDrawable, "tint", Color.parseColor("#FFbf407f"), Color.parseColor("#40bf80"));
            objectAnimator.setDuration(1000);
            objectAnimator.setRepeatCount(1);
            objectAnimator.setRepeatMode(ValueAnimator.REVERSE);

            // Inicia la animación
            objectAnimator.start();
        }

    }

    private void PerformAuth(LayoutInflater inflater) {
        String email = mailBox.getText().toString();
        String pass = passBox.getText().toString();
        String genre = genreSpinner.getSelectedItem().toString();
        String dateBirth = dateBox.getText().toString();
        String userName = userBox.getText().toString().toLowerCase();
        String pName = nameBox.getText().toString();

        if (!email.matches(emailPattern)) {
            Toast.makeText(getContext(), getString(R.string.notValidEmail), Toast.LENGTH_SHORT).show();
        } else if (passBox.getText().toString().trim().length() < 6) {
            Toast.makeText(getContext(), getString(R.string.passLengthError), Toast.LENGTH_SHORT).show();
        } else {
            Query query = fStore.collection("users").whereEqualTo("user", userName);

            // Infla el layout
            View loadingView = inflater.inflate(R.layout.dialog_progress, null);

            // Agrega la vista a la vista raíz de la actividad
            ViewGroup rootView = getActivity().findViewById(android.R.id.content);

            startAnimation(loadingView,inflater);

            rootView.addView(loadingView);


            Log.d("RegisterDeb", "Buscando usuarios con userName: " + userName);
            query.get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                @Override
                public void onComplete(@NonNull Task<QuerySnapshot> task) {
                    Log.d("RegisterDeb", "onComplete");
                    if (task.isSuccessful()) {
                        Log.d("RegisterDeb", "isSuccessful");
                        if (task.getResult().isEmpty()) {
                            Log.d("RegisterDeb", "isEmpty");
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

                                            // Obtener la referencia al almacenamiento de Firebase
                                            FirebaseStorage fStorage = FirebaseStorage.getInstance();

                                            // Crear una referencia al archivo de imagen en el almacenamiento
                                            StorageReference avatarRef = fStorage.getReference().child("avatars/" + userID);

                                            // Subir el archivo de imagen
                                            if(avatarSelectad){
                                                UploadTask uploadTask = avatarRef.putFile(avatarUri);
                                                uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                    @Override
                                                    public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                        // Obtener la URL de descarga de la imagen
                                                        avatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                            @Override
                                                            public void onSuccess(Uri uri) {
                                                                // Guardar la URL de descarga en el documento del usuario
                                                                documentReference.update("avatarUrl", uri.toString());
                                                                Fade fade = new Fade();
                                                                TransitionManager.beginDelayedTransition(rootView,new Fade());
                                                                startAnimationColor(loadingView);
                                                                Toast.makeText(getContext(), getString(R.string.registerSuccess), Toast.LENGTH_SHORT).show();
                                                                Handler handler = new Handler();
                                                                handler.postDelayed(new Runnable() {
                                                                    public void run() {
                                                                        rootView.removeView(loadingView);
                                                                        getParentFragmentManager().popBackStackImmediate();
                                                                    }
                                                                }, 1000);
                                                            }
                                                        });
                                                    }
                                                });
                                            } else {
                                                documentReference.update("avatarUrl", "https://firebasestorage.googleapis.com/v0/b/tunebox-reborn.appspot.com/o/defaultAvatar.png?alt=media&token=151ff5f8-d1fa-4f2a-be0a-353dcca8f6c3");
                                                Fade fade = new Fade();
                                                TransitionManager.beginDelayedTransition(rootView,fade);
                                                startAnimationColor(loadingView);
                                                Toast.makeText(getContext(), getString(R.string.registerSuccess), Toast.LENGTH_SHORT).show();
                                                Handler handler = new Handler();
                                                handler.postDelayed(new Runnable() {
                                                    public void run() {
                                                        rootView.removeView(loadingView);
                                                        getParentFragmentManager().popBackStackImmediate();
                                                    }
                                                }, 1000);

                                            }


                                        } else {
                                            Fade fade = new Fade();
                                            TransitionManager.beginDelayedTransition(rootView,fade);
                                            rootView.removeView(loadingView);
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
                                Fade fade = new Fade();
                                TransitionManager.beginDelayedTransition(rootView,fade);
                                rootView.removeView(loadingView);
                                Toast.makeText(getContext(), getString(R.string.minEdadError), Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            // Se encontró al menos un usuario con el userName dado
                            Log.d("RegisterDeb", "isNotEmpty");
                            Fade fade = new Fade();
                            TransitionManager.beginDelayedTransition(rootView,fade);
                            rootView.removeView(loadingView);
                            Toast.makeText(getContext(), getString(R.string.existUser), Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        // Ocurrió un error al realizar la consulta
                        // Manejo de errores
                        try{
                            Log.d("RegisterDeb", "Error al realizar la consulta: " + task.getException().getMessage());
                        } catch (Exception e){
                            Toast.makeText(getContext(), task.getException().toString(), Toast.LENGTH_SHORT).show();
                        }
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
        initData(view,inflater);

        dateBox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showDatePickerDialog();
            }
        });

        avatarButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent();
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));

            }
        });

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    // Obtener la URI de la imagen seleccionada
                    avatarUri = result.getData().getData();
                    avatarButton.setImageURI(avatarUri);
                    avatarSelectad = true;
                }
            }
        });

        registerB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        registerB.setTextColor(Color.WHITE);
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        registerB.setTextColor(Color.parseColor("#FFbf407f"));
                        break;
                }

                if(dataFilled()){
                    PerformAuth(inflater);
                } else {
                    Toast toast = Toast.makeText(getContext(), getString(R.string.errorFieldsNotFilled) + "", Toast.LENGTH_SHORT);
                    toast.setMargin(50, 50);
                    toast.show();
                }

                return false;
            }
        });

        loginB.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch(event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        loginB.setTextColor(Color.BLACK);
                        getParentFragmentManager().popBackStack();

                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        loginB.setTextColor(Color.parseColor("#FFbf407f"));
                        break;
                }
                return false;
            }
        });


        return view;
    }


}
