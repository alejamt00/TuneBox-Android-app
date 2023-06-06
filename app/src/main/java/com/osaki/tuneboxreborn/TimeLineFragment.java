package com.osaki.tuneboxreborn;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Handler;
import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Fragmento encargado de mostrar la lista de tunes en orden cronologico según el genero musical favorito
 * del usuario logueado
 */
public class TimeLineFragment extends Fragment {

    private ImageView ivAvatar, ivConf, ivLogo;
    private FloatingActionButton fabMsg;
    private String tune;
    private ArrayList<TuneMsg> listaTunes;
    private RecyclerView recyclerTunes;
    private RecyclerAdapter recyclerAdapter;
    private User user;
    private View loadingView;
    private ViewGroup rootView;
    private Boolean blockUpdate;


    /**
     * Método de fábrica para crear una nueva instancia de este fragmento.
     * @return Una nueva instancia del fragmento TimeLineFragment.
     */
    public static TimeLineFragment newInstance() {
        TimeLineFragment fragment = new TimeLineFragment();

        return fragment;
    }

    /**
     * Constructor vacío requerido.
     */
    public TimeLineFragment() {
    }


    /**
     * Método llamado en la creacion del fragmento
     *
     * @param savedInstanceState If the fragment is being re-created from
     * a previous saved state, this is the state.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        if (user != null) {
            loadTunes();
        }
    }

    /**
     * Carga los mensajes de la línea de tiempo.
     */
    public void loadTunes(){
        startMoveAnimation();
        recyclerTunes.stopScroll();
        listaTunes.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tunes")
                .whereEqualTo("musicTL", user.getGenre())
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("DEBUGMSG", document.getId() + " => " + document.getData());
                                TuneMsg msg = new TuneMsg(
                                        document.getId(),
                                        document.getString("authorId"),
                                        document.getString("publicName"),
                                        document.getString("userName"),
                                        document.getString("avatar"),
                                        document.getString("msg"),
                                        document.getString("date"),
                                        document.getString("musicTL")
                                );
                                listaTunes.add(msg);

                            }
                            getActivity().runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Handler handler = new Handler();
                                    handler.postDelayed(new Runnable() {
                                        @Override
                                        public void run() {
                                            recyclerAdapter.notifyDataSetChanged();
                                            Handler handler = new Handler();
                                            handler.postDelayed(new Runnable() {
                                                public void run() {
                                                    startColorAnimation();
                                                }
                                            }, 500);

                                        }
                                    }, 500);
                                }
                            });
                            recyclerTunes.smoothScrollToPosition(0);


                        } else {
                            Log.w("DEBUGMSG", "Error getting documents.", task.getException());
                        }
                    }
                });

    }

    /**
     * Inserta un mensaje t en la base de datos.
     * @param t Objeto TuneMsg que contiene los datos del mensaje a insertar.
     * @return True si el mensaje se ha subido correctamente.
     */
    public void uploadTune(TuneMsg t){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        startMoveAnimation();
        Map<String, Object> tune = new HashMap<>();
        tune.put("authorId", t.getAuthorId());
        tune.put("publicName", t.getPublicName());
        tune.put("userName", t.getUserName());
        tune.put("avatar", t.getAvatar());
        tune.put("msg", t.getMsg());
        tune.put("date", t.getDate());
        tune.put("musicTL", t.getMusicTL());
        tune.put("createdAt", FieldValue.serverTimestamp());

        db.collection("tunes")
                .add(tune)
                .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                    @Override
                    public void onSuccess(DocumentReference documentReference) {
                        Log.d("DEBUGMSG", "DocumentSnapshot added with ID: " + documentReference.getId());
                        loadTunes();
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("DEBUGMSG", "Error adding document", e);
                    }
                });

    }

    public void startMoveAnimation(){
        Drawable drawable = getContext().getDrawable(R.drawable.logo_animado);
        ivLogo.setImageDrawable(drawable);

        if (drawable instanceof Animatable) {
            final Animatable animatable = (Animatable) drawable;
            animatable.start();
        }
    }

    public void startColorAnimation(){
        // Obtiene el Drawable de la vista
        Drawable drawableLogo = ivLogo.getDrawable();

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
     *
     * Método llamado en la cración de la vista, encargado de inicializar todos los elementos y su
     * correcto funcionamiento
     *
     * @param inflater The LayoutInflater object that can be used to inflate
     * any views in the fragment,
     * @param container If non-null, this is the parent view that the fragment's
     * UI should be attached to.  The fragment should not add the view itself,
     * but this can be used to generate the LayoutParams of the view.
     * @param savedInstanceState If non-null, this fragment is being re-constructed
     * from a previous saved state as given here.
     *
     * @return
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_time_line, container, false);
        listaTunes = new ArrayList<>();

        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivLogo = view.findViewById(R.id.ivLogo);
        ivConf = view.findViewById(R.id.confButton);
        fabMsg = view.findViewById(R.id.addTuneMsg);

        tune = "";

        // Infla el layout
        loadingView = inflater.inflate(R.layout.dialog_progress, null);

        Drawable dLoading = getContext().getDrawable(R.drawable.logo_animado);
        ImageView ivLogoLoading = loadingView.findViewById(R.id.logoIcono);
        ivLogoLoading.setImageDrawable(dLoading);

        if (dLoading instanceof Animatable) {
            final Animatable animatable = (Animatable) dLoading;
            animatable.start();
        }


        startMoveAnimation();

        // Agrega la vista de loading a la vista raíz de la actividad
        rootView = getActivity().findViewById(android.R.id.content);
        rootView.addView(loadingView);

        //Get data from logged user
        FirebaseUser uFire = FirebaseAuth.getInstance().getCurrentUser();
        DocumentReference docRef = FirebaseFirestore.getInstance().collection("users").document(uFire.getUid());
        docRef.get().addOnSuccessListener(new OnSuccessListener<DocumentSnapshot>() {
            @Override
            public void onSuccess(DocumentSnapshot documentSnapshot) {
                user = documentSnapshot.toObject(User.class);
                loadTunes();
                // Obtiene el Drawable de la vista
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

                //Carga desde la url de firabase en formato String el avatar correcto
                Glide.with(getContext()).load(user.getAvatarUrl()).into(ivAvatar);
                TransitionManager.beginDelayedTransition(rootView,new Fade());
                rootView.removeView(loadingView);
            }
        });

        recyclerTunes = (RecyclerView) view.findViewById(R.id.recyclerListTunes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerTunes.setLayoutManager(linearLayoutManager);

        FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );
        ;

        recyclerAdapter = new RecyclerAdapter(listaTunes,getContext(), ft);
        recyclerTunes.setAdapter(recyclerAdapter);


        /** Lanza el alert dialog para escribir tunes */
        fabMsg.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

                View vAlert = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_layout, null);
                builder.setView(vAlert);
                EditText editText = vAlert.findViewById(R.id.editTextTuneMsg);
                Button bAccept = vAlert.findViewById(R.id.bPublish);
                Button bCancel = vAlert.findViewById(R.id.bCancel);
                ImageView ivAvatarTune = vAlert.findViewById(R.id.ivAvatarTune);
                Glide.with(getContext()).load(user.getAvatarUrl()).into(ivAvatarTune);

                AlertDialog alert = builder.create();

                bAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        tune = editText.getText().toString();

                        if(!tune.trim().equals("")){
                            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            TuneMsg t = new TuneMsg("tuneId", uFire.getUid(),user.getpName(), user.getUser(), user.getAvatarUrl(), tune, date, user.getGenre());
                            uploadTune(t);
                            alert.dismiss();
                        }
                    }
                });

                bCancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                    }
                });

                alert.show();
            }
        });

        ivConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

                View vAlert = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_layout_config, null);
                builder.setView(vAlert);
                Button bLogOff = vAlert.findViewById(R.id.bLogOff);
                Button bChangeFavGenre = vAlert.findViewById(R.id.bChangeFavGenre);
                AlertDialog alert = builder.create();


                bChangeFavGenre.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        AlertDialog.Builder bGenre = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

                        View vAlertGenre = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_layout_fav_genre, null);
                        bGenre.setView(vAlertGenre);

                        AlertDialog alertGenre = bGenre.create();
                        CustomSpinnerAdapter genreSpinnerAdapter;
                        Spinner genreSpinner = vAlertGenre.findViewById(R.id.spinnerGenre);
                        String[] genreArray = new String[11];
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
                        genreSpinnerAdapter = new CustomSpinnerAdapter(view.getContext(), R.layout.spinner_item_layout, genreArray, 0);
                        genreSpinner.setAdapter(genreSpinnerAdapter);

                        Button bCancel = vAlertGenre.findViewById(R.id.bCancelar);
                        Button bAccept = vAlertGenre.findViewById(R.id.bAceptar);

                        bCancel.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertGenre.dismiss();
                            }
                        });

                        bAccept.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                FirebaseFirestore db = FirebaseFirestore.getInstance();
                                DocumentReference docRef = db.collection("users").document(uFire.getUid());
                                docRef.update("genre", genreSpinner.getSelectedItem().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Toast.makeText(getContext(), "Género musical cambiado correctamente!", Toast.LENGTH_SHORT).show();
                                        user.setGenre(genreSpinner.getSelectedItem().toString());
                                        loadTunes();
                                        alertGenre.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), "Error al cambiar de género musical favorito!", Toast.LENGTH_SHORT).show();
                                        alertGenre.dismiss();
                                    }
                                });

                            }
                        });



                        alertGenre.show();


                    }
                });

                bLogOff.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        alert.dismiss();
                        FirebaseAuth.getInstance().signOut();
                        FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                                .setCustomAnimations(
                                        R.anim.fade_in,
                                        R.anim.fade_out,
                                        R.anim.fade_in,
                                        R.anim.fade_out
                                );

                        LoginFragment loginFragment = new LoginFragment();
                        ft.replace(R.id.fragmentContainerView, loginFragment).commit();
                    }
                });



                alert.show();
            }
        });

        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                        .setCustomAnimations(
                                R.anim.fade_in,
                                R.anim.fade_out,
                                R.anim.fade_in,
                                R.anim.fade_out
                        );
                ProfileFragment profileFragment = new ProfileFragment();
                Bundle args = new Bundle();
                args.putString("uuid", uFire.getUid());
                profileFragment.setArguments(args);


                ft.replace(R.id.fragmentContainerView, profileFragment).commit();
            }
        });

// Agrega un OnClickListener a tu vista para iniciar la animación al hacer clic
        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTunes();
            }
        });

        return view;
    }
}