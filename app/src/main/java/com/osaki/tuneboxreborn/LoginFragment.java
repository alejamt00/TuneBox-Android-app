package com.osaki.tuneboxreborn;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.transition.Fade;
import android.transition.TransitionManager;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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
    private Drawable dLoading;


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

    /**
     * Inicia la animación de carga.
     * @param loadingView la vista que contiene la imagen de carga.
     */
    public void startAnimation(View loadingView){

        dLoading = getContext().getDrawable(R.drawable.logo_animado);
        ImageView ivLogoLoading = loadingView.findViewById(R.id.logoIcono);
        ivLogoLoading.setImageDrawable(dLoading);

        if (dLoading instanceof Animatable) {
            final Animatable animatable = (Animatable) dLoading;
            animatable.start();
        }
    }

    /**
     * Inicia la animación de cambio de color del logo.
     * @param loadingView la vista que contiene la imagen del logo.
     */
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

    /**
     * Inicia sesión en la aplicación con correo y contraseña.
     * @param correo el correo electrónico del usuario.
     * @param pass la contraseña del usuario.
     * @param inflater el LayoutInflater para inflar la vista de carga.
     */
    public void login(String correo, String pass, LayoutInflater inflater){

        View loadingView = inflater.inflate(R.layout.dialog_progress, null);

        startAnimation(loadingView);
        // Agrega la vista de loading a la vista raíz de la actividad
        ViewGroup rootView = getActivity().findViewById(android.R.id.content);
        rootView.addView(loadingView);

        fAuth.signInWithEmailAndPassword(correo,pass).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if(task.isSuccessful()){
                    Toast.makeText(getContext(), getString(R.string.correctUser), Toast.LENGTH_SHORT).show();
                    FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                            .setCustomAnimations(
                                    R.anim.fade_in,
                                    R.anim.fade_out,
                                    R.anim.fade_in,
                                    R.anim.fade_out
                            );

                    TimeLineFragment tlf = new TimeLineFragment();
                    ft.replace(R.id.fragmentContainerView,tlf).commit();
                    ft.addToBackStack(null);

                } else {
                    Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT).show();
                }
                startAnimationColor(loadingView);
                TransitionManager.beginDelayedTransition(rootView,new Fade());
                rootView.removeView(loadingView);
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
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);

        if (fAuth.getCurrentUser() != null){
            FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                    .setCustomAnimations(
                            R.anim.fade_in,
                            R.anim.fade_out,
                            R.anim.fade_in,
                            R.anim.fade_out
                    );

            TimeLineFragment tlf = new TimeLineFragment();
            ft.replace(R.id.fragmentContainerView,tlf).commit();
            ft.addToBackStack(null);
        }

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
                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                            imm.hideSoftInputFromWindow(usernameET.getWindowToken(), 0);
                            imm.hideSoftInputFromWindow(passET.getWindowToken(), 0);
                            login(usernameET.getText().toString(),passET.getText().toString(),inflater);
                        } else {
                            Toast.makeText(getContext(), getString(R.string.fillBothFields), Toast.LENGTH_SHORT).show();
                        }
                        break;
                    case MotionEvent.ACTION_UP:
                    case MotionEvent.ACTION_CANCEL:
                        loginButton.setTextColor(Color.parseColor("#FFbf407f"));
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