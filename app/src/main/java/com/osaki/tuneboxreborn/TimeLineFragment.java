package com.osaki.tuneboxreborn;

import android.app.AlertDialog;
import android.content.ContentValues;
import android.content.DialogInterface;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.Serializable;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Locale;

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
    private SQLiteDatabase dbR, dbW;



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

    /**
     * Carga los mensajes de la línea de tiempo.
     */
    public void loadTunes(){

        String timeline = getArguments().getString("music_genre");

    }

    /**
     * Inserta un mensaje t en la base de datos.
     * @param t Objeto TuneMsg que contiene los datos del mensaje a insertar.
     * @param values Objeto ContentValues que se utilizará para insertar los valores en la base de datos.
     * @return El ID de la fila insertada.
     */
    public long uploadTune(TuneMsg t, ContentValues values){

        return 0;
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


        listaTunes = new ArrayList<TuneMsg>();
        ivAvatar = view.findViewById(R.id.ivAvatar);
        ivConf = view.findViewById(R.id.confButton);
        Bitmap bmp = BitmapFactory.decodeFile(getContext().getExternalFilesDir(null)+"/avatars/"+getArguments().getString("avatar"));
        ivAvatar.setImageBitmap(bmp);
        fabMsg = view.findViewById(R.id.addTuneMsg);
        tune = "";

        recyclerTunes = (RecyclerView) view.findViewById(R.id.recyclerListTunes);
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(view.getContext());
        recyclerTunes.setLayoutManager(linearLayoutManager);

        loadTunes();

        FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                .setCustomAnimations(
                        R.anim.fade_in,
                        R.anim.fade_out,
                        R.anim.fade_in,
                        R.anim.fade_out
                );
        ;

        recyclerAdapter = new RecyclerAdapter(listaTunes,getContext(), ft, getArguments().getString("id"));
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
                ImageView ivTune = vAlert.findViewById(R.id.ivAvatarTune);
                ivTune.setImageBitmap(bmp);

                AlertDialog alert = builder.create();

                bAccept.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        ContentValues values = new ContentValues();
                        tune = editText.getText().toString();

                        if(!tune.equals("")){
                            String date = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                            TuneMsg tuneMsg = new TuneMsg("",getArguments().getString("id"),getArguments().getString("public_name"), getArguments().getString("username"), getArguments().getString("avatar"), tune, date, getArguments().getString("music_genre"));
                            tuneMsg.setId(Long.toString(uploadTune(tuneMsg, values)));
                            listaTunes.add(0, tuneMsg);
                            recyclerAdapter.notifyDataSetChanged();
                        }
                        alert.dismiss();
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

        /** Lanza el fragmento de perfil de usuario*/
        ivAvatar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Toast toast = Toast.makeText(getContext(), "Fragmento de perfil", Toast.LENGTH_SHORT);
                toast.setMargin(50, 50);
                toast.show();
            }
        });


        ivConf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
//                NewsFragment nf = new NewsFragment();
//                nf.setArguments(getArguments());
//                ft.replace(R.id.fragmentContainerView,nf).commit();
//                ft.addToBackStack(null);
                Toast toast = Toast.makeText(getContext(), "Fragmento de configuración", Toast.LENGTH_SHORT);
                toast.setMargin(50, 50);
                toast.show();
            }
        });


        return view;
    }
}