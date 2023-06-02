package com.osaki.tuneboxreborn;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.transition.Fade;
import android.transition.TransitionManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
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

    private ImageView ivAvatar, ivConf;
    private FloatingActionButton fabMsg;
    private String tune;
    private ArrayList<TuneMsg> listaTunes;
    private RecyclerView recyclerTunes;
    private RecyclerAdapter recyclerAdapter;
    private User user;
    private View loadingView;
    private ViewGroup rootView;


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
                            recyclerAdapter.notifyDataSetChanged();


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
        ivConf = view.findViewById(R.id.confButton);
        fabMsg = view.findViewById(R.id.addTuneMsg);
        tune = "";

        // Infla el layout
        loadingView = inflater.inflate(R.layout.dialog_progress, null);

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
                //Carga desde la url de firabase en formato String el avatar correcto
                Glide.with(getContext()).load(user.getAvatarUrl()).into(ivAvatar);
                Fade fade = new Fade();
                TransitionManager.beginDelayedTransition(rootView,fade);
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

        recyclerAdapter = new RecyclerAdapter(listaTunes,getContext(), ft, uFire.getUid());
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
                            TuneMsg t = new TuneMsg(uFire.getUid(),user.getpName(), user.getUser(), user.getAvatarUrl(), tune, date, user.getGenre());
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
                AlertDialog alert = builder.create();


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

        return view;
    }
}