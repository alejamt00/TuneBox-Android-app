package com.osaki.tuneboxreborn;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;


/**
 * Clase RecyclerAdapter que extiende la clase RecyclerView.Adapter.
 * Se utiliza para crear un adaptador para el RecyclerView que muestra los Tunes.
 */
public class RecyclerAdapter extends RecyclerView.Adapter<RecyclerAdapter.ViewHolder>{

    // Variables necesarias de la clase
    private ArrayList<TuneMsg> listaTunes;
    private LayoutInflater lInflater;
    private FragmentTransaction ft;
    private Context c;


    /**
     * Constructor de la clase RecyclerAdapter.
     *
     * @param listaTunes La lista de Tunes a mostrar.
     * @param context El contexto de la aplicación.
     * @param ft La transacción de fragmento para cambiar de fragmento.
     */
    public RecyclerAdapter(ArrayList<TuneMsg> listaTunes, Context context, FragmentTransaction ft){
        this.lInflater = LayoutInflater.from(context);
        this.c = context;
        this.listaTunes = listaTunes;
        this.ft = ft;
    }

    /**
     * Método llamado cuando se crea una nueva vista para un elemento del RecyclerView.
     *
     * @param parent El ViewGroup padre.
     * @param viewType El tipo de vista.
     * @return Un nuevo ViewHolder para el elemento.
     */
    @NonNull
    @Override
    public RecyclerAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = lInflater.inflate(R.layout.recycler_tunemsg_row, parent, false);
        return new RecyclerAdapter.ViewHolder(view);
    }

    /**
     * Método llamado para enlazar los datos de un elemento del RecyclerView con su vista.
     * También implementa el onClickListener de los elementos, que permiten borrar Tunes si el usuario
     * logueado coincide con el del autor del tune
     *
     * @param holder El ViewHolder del elemento.
     * @param position La posición del elemento en la lista.
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerAdapter.ViewHolder holder, @SuppressLint("RecyclerView") int position) {
        holder.bindData(listaTunes.get(position));
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FirebaseUser currentUser = FirebaseAuth.getInstance().getCurrentUser();
                if(listaTunes.get(position).getAuthorId().equals(currentUser.getUid())){
                    AlertDialog.Builder builder = new AlertDialog.Builder(c, R.style.MyAlertDialogStyle);

                    View vAlert = LayoutInflater.from(c).inflate(R.layout.alert_dialog_delete_layout, null);
                    builder.setView(vAlert);
                    Button bAccept = vAlert.findViewById(R.id.bAccept);
                    Button bCancel = vAlert.findViewById(R.id.bCancel);

                    AlertDialog alert = builder.create();

                    bAccept.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                            DocumentReference docRef = db.collection("tunes").document(listaTunes.get(position).getTuneId());
                            docRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    listaTunes.remove(position);
                                    notifyDataSetChanged();
                                }
                            }).addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    // Ocurrió un error al intentar eliminar el documento
                                }
                            });
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

                } else {

                    ProfileFragment profileFragment = new ProfileFragment();
                    Bundle args = new Bundle();
                    args.putString("uuid", listaTunes.get(position).getAuthorId());
                    profileFragment.setArguments(args);

                    ft.replace(R.id.fragmentContainerView, profileFragment).commit();

                }
            }
        });
    }

    /**
     * Devuelve el tamaño de la lista de "tunes"
     */
    @Override
    public int getItemCount() {
        return listaTunes.size();
    }


    /**
     * Clase ViewHolder que extiende la clase RecyclerView.ViewHolder.
     * Se utiliza para almacenar y "reciclar" las vistas de los elementos del RecyclerView.
     */
    public class ViewHolder extends RecyclerView.ViewHolder{

        private ImageView avatarIV;
        private TextView publicNameTV;
        private TextView userNameTV;
        private TextView tuneMsgTV;
        private TextView dateTV;

        /**
         * Constructor de la clase ViewHolder.
         *
         * @param itemView La vista del elemento.
         */
        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            avatarIV = itemView.findViewById(R.id.ivAvatarTune);
            publicNameTV = itemView.findViewById(R.id.publicNameTV);
            userNameTV = itemView.findViewById(R.id.userName);
            tuneMsgTV = itemView.findViewById(R.id.msgTune);
            dateTV = itemView.findViewById(R.id.date);

        }

        /**
         * Setea el contenido de los contenedores con la informacion del mensaje "tMsg"
         *
         * @param tMsg Mensaje a mostrar
         */
        public void bindData(TuneMsg tMsg){
            Glide.with(itemView.getContext()).load(tMsg.getAvatar()).into(avatarIV);
            publicNameTV.setText(tMsg.getPublicName());
            userNameTV.setText("@" + tMsg.getUserName());
            tuneMsgTV.setText(tMsg.getMsg());
            dateTV.setText(tMsg.getDate());
        }
    }

}