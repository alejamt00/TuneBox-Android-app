package com.osaki.tuneboxreborn;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;


/**
 * Clase ProfileFragment que extiende la clase Fragment.
 * Se utiliza para crear un fragmento de perfil de un usuario concreto
 */
public class ProfileFragment extends Fragment {

    private ArrayList<TuneMsg> listaTunesPf;
    private RecyclerView recyclerTunes;
    private RecyclerAdapter recyclerAdapter;
    private String userId;
    private User userPf;


    /**
     * Constructor vacío requerido.
     */
    public ProfileFragment() {
    }

    /**
     * Método para crear una nueva instancia de la clase ProfileFragment.
     *
     * @return Una nueva instancia de la clase ProfileFragment.
     */
    public static ProfileFragment newInstance() {
        ProfileFragment fragment = new ProfileFragment();
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

    private void getUserData(String uuid, View view){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        DocumentReference docRef = db.collection("users").document(uuid);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        userPf = document.toObject(User.class);
                        ImageView ivAvatar = view.findViewById(R.id.ivPFP);
                        Glide.with(getContext()).load(userPf.getAvatarUrl()).into(ivAvatar);

                        TextView pName = view.findViewById(R.id.publicNameTV);
                        TextView userName = view.findViewById(R.id.userNameTV);
                        TextView favGenre = view.findViewById(R.id.favGenre);
                        pName.setText(userPf.getpName());
                        userName.setText("@"+userPf.getUser());
                        favGenre.setText(userPf.getGenre());


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




                        recyclerAdapter = new RecyclerAdapter(listaTunesPf,getContext(), ft);
                        recyclerTunes.setAdapter(recyclerAdapter);

                        loadTunes();


                    } else {
                        Log.d("DEBUGPROFILE", "No such document");
                    }
                } else {
                    Log.d("DEBUGPROFILE", "get failed with ", task.getException());
                }
            }
        });

    }

    /**
     * Método para cargar los datos de los Tunes en la lista.
     */
    public void loadTunes(){

        listaTunesPf.clear();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("tunes")
                .whereEqualTo("authorId", userId)
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
                                listaTunesPf.add(msg);

                            }
                            recyclerAdapter.notifyDataSetChanged();


                        } else {
                            Log.w("DEBUGMSG", "Error getting documents.", task.getException());
                        }
                    }
                });
    }

    /**
     * Método llamado cuando se crea la vista del fragmento.
     * Se utiliza para inflar la vista y configurar los elementos de la interfaz de usuario e implementar
     * sus funciones
     *
     * @param inflater El LayoutInflater para inflar la vista.
     * @param container El contenedor de la vista.
     * @param savedInstanceState El estado guardado de la instancia anterior.
     * @return La vista inflada.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        listaTunesPf = new ArrayList<>();
        if (getArguments() != null) {
            userId = getArguments().getString("uuid");
        }

        getUserData(userId, view);


        FloatingActionButton fabBack = view.findViewById(R.id.backButton);

        /** Vuelve al fragmento anterior */
        fabBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getParentFragmentManager().popBackStackImmediate();
            }
        });





        return view;
    }
}