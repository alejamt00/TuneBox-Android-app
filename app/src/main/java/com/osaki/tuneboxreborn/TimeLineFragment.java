package com.osaki.tuneboxreborn;

import static android.app.Activity.RESULT_OK;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.Animatable;
import android.graphics.drawable.AnimatedVectorDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
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
import android.widget.ImageSwitcher;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.bumptech.glide.signature.ObjectKey;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.Query;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

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
    private ActivityResultLauncher<Intent> pickImageLauncher;
    private Uri avatarUri;
    private boolean avatarSelected;
    private ImageView ivAvatarMod;


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
     * Se llama cuando el fragmento se reanuda. Si el usuario no es nulo, carga los mensajes.
     */
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

    /**
     * Inicia la animación de carga.
     */
    public void startMoveAnimation(){
        Drawable drawable = getContext().getDrawable(R.drawable.logo_animado);
        ivLogo.setImageDrawable(drawable);

        if (drawable instanceof Animatable) {
            final Animatable animatable = (Animatable) drawable;
            animatable.start();
        }
    }

    /**
     * Inicia la animación de cambio de color del logo.
     */
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

        pickImageLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
            @Override
            public void onActivityResult(ActivityResult result) {
                if (result.getResultCode() == RESULT_OK && result.getData() != null && result.getData().getData() != null) {
                    // Obtener la URI de la imagen seleccionada
                    avatarUri = result.getData().getData();
                    ivAvatarMod.setImageURI(avatarUri);
                    avatarSelected = true;
                }
            }
        });



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

                Glide.with(getContext())
                        .load(user.getAvatarUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivAvatar);

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

                Glide.with(getContext())
                        .load(user.getAvatarUrl())
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .into(ivAvatarTune);

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
                Button bDeleteUser = vAlert.findViewById(R.id.bDeleteUser);
                Button bChangeFavGenre = vAlert.findViewById(R.id.bChangeFavGenre);
                Button bChangePname = vAlert.findViewById(R.id.bChangeName);
                Button bChangeAvatar = vAlert.findViewById(R.id.bChangeAvatar);
                Button bChangePass = vAlert.findViewById(R.id.bChangePassword);
                AlertDialog alert = builder.create();

                bChangePass.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        AlertDialog.Builder bPassC = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                        View vAlertPassC = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_change_pass_relog_layout, null);
                        bPassC.setView(vAlertPassC);
                        AlertDialog alertPassC = bPassC.create();
                        alertPassC.show();
                        Button confirmChange = vAlertPassC.findViewById(R.id.bConfirmar);

                        confirmChange.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText usernameET = vAlertPassC.findViewById(R.id.emailBox);
                                EditText passET = vAlertPassC.findViewById(R.id.passBox);
                                EditText newPassET = vAlertPassC.findViewById(R.id.newPassBox);

                                final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                String email = usernameET.getText().toString();
                                String password = passET.getText().toString();
                                String newPass = newPassET.getText().toString();

                                if (!newPass.isEmpty() && !email.isEmpty() && !password.isEmpty() && password.length()>=6) {
                                    alertPassC.dismiss();
                                    startMoveAnimation();
                                    rootView.addView(loadingView);

                                    AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                                    user.reauthenticate(credential)
                                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                @Override
                                                public void onComplete(@NonNull Task<Void> task) {
                                                    if (task.isSuccessful()) {
                                                        startColorAnimation();
                                                        user.updatePassword(newPass).addOnCompleteListener(new OnCompleteListener<Void>() {
                                                            @Override
                                                            public void onComplete(@NonNull Task<Void> task) {
                                                                if (task.isSuccessful()) {
                                                                    FirebaseAuth.getInstance().signOut();
                                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                                                                            .setCustomAnimations(
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out,
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out
                                                                            );
                                                                    rootView.removeView(loadingView);
                                                                    Toast.makeText(getContext(), getString(R.string.changedPassCorrectly), Toast.LENGTH_SHORT).show();
                                                                    ft.replace(R.id.fragmentContainerView, new LoginFragment()).commit();
                                                                } else {
                                                                }
                                                            }
                                                        });
                                                    } else {
                                                    }
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    String errorMessage;
                                                    TransitionManager.beginDelayedTransition(rootView,new Fade());
                                                    rootView.removeView(loadingView);

                                                    Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT).show();
                                                }
                                            });


                                } else {
                                    Toast.makeText(getContext(), getString(R.string.fillBothFields), Toast.LENGTH_SHORT).show();
                                }
                            }
                        });
                    }
                });

                bChangeAvatar.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        avatarSelected = false;

                        AlertDialog.Builder bChangeAv = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                        View vAlertChangeAv = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_change_avatar_layout, null);
                        bChangeAv.setView(vAlertChangeAv);
                        AlertDialog alertChangeAv = bChangeAv.create();
                        alertChangeAv.show();
                        Button confirmAvatar = vAlertChangeAv.findViewById(R.id.bConfirmar);
                        ivAvatarMod = vAlertChangeAv.findViewById(R.id.ivAvatar);

                        ivAvatarMod.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                Intent intent = new Intent();
                                intent.setType("image/*");
                                intent.setAction(Intent.ACTION_GET_CONTENT);
                                pickImageLauncher.launch(Intent.createChooser(intent, "Select Picture"));

                            }
                        });

                        confirmAvatar.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                if(avatarSelected){
                                    alertChangeAv.dismiss();

                                    FirebaseStorage storage = FirebaseStorage.getInstance();
                                    StorageReference storageRef = storage.getReference();
                                    StorageReference oldAvatarRef = storageRef.child("avatars/" + uFire.getUid());
                                    oldAvatarRef.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            // El archivo se eliminó correctamente
                                            // Subir el nuevo avatar
                                            StorageReference newAvatarRef = storageRef.child("avatars/" + uFire.getUid());
                                            UploadTask uploadTask = newAvatarRef.putFile(avatarUri);
                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    // La imagen se subió correctamente
                                                    // Obtener la URL de descarga de la imagen
                                                    newAvatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String downloadUrl = uri.toString();
                                                            // Actualizar el valor del campo "avatar" en el documento del usuario
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            DocumentReference docRef = db.collection("users").document(uFire.getUid());
                                                            docRef.update("avatarUrl", downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getContext(), getString(R.string.avatarChangedConfirmed), Toast.LENGTH_SHORT).show();
                                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                                                                            .setCustomAnimations(
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out,
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out
                                                                            );
                                                                    CollectionReference tunesColRef = db.collection("tunes");
                                                                    tunesColRef.whereEqualTo("authorId", uFire.getUid()).get()
                                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                        document.getReference().update("avatar", downloadUrl);
                                                                                    }
                                                                                    loadTunes();
                                                                                }
                                                                            });


                                                                    ft.replace(R.id.fragmentContainerView, new TimeLineFragment()).commit();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Ocurrió un error al intentar actualizar el campo
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Ocurrió un error al intentar subir la imagen
                                                }
                                            });

                                            // ...
                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // El archivo se eliminó correctamente
                                            // Subir el nuevo avatar
                                            StorageReference newAvatarRef = storageRef.child("avatars/" + uFire.getUid());
                                            UploadTask uploadTask = newAvatarRef.putFile(avatarUri);
                                            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                                                @Override
                                                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                                    // La imagen se subió correctamente
                                                    // Obtener la URL de descarga de la imagen
                                                    newAvatarRef.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                                        @Override
                                                        public void onSuccess(Uri uri) {
                                                            String downloadUrl = uri.toString();
                                                            // Actualizar el valor del campo "avatar" en el documento del usuario
                                                            FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                            DocumentReference docRef = db.collection("users").document(uFire.getUid());
                                                            docRef.update("avatarUrl", downloadUrl).addOnSuccessListener(new OnSuccessListener<Void>() {
                                                                @Override
                                                                public void onSuccess(Void aVoid) {
                                                                    Toast.makeText(getContext(), getString(R.string.avatarChangedConfirmed), Toast.LENGTH_SHORT).show();
                                                                    FragmentTransaction ft = getParentFragmentManager().beginTransaction()
                                                                            .setCustomAnimations(
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out,
                                                                                    R.anim.fade_in,
                                                                                    R.anim.fade_out
                                                                            );
                                                                    CollectionReference tunesColRef = db.collection("tunes");
                                                                    tunesColRef.whereEqualTo("authorId", uFire.getUid()).get()
                                                                            .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                                @Override
                                                                                public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                                    for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                                        document.getReference().update("avatar", downloadUrl);
                                                                                    }
                                                                                    loadTunes();
                                                                                }
                                                                            });

                                                                    ft.replace(R.id.fragmentContainerView, new TimeLineFragment()).commit();
                                                                }
                                                            }).addOnFailureListener(new OnFailureListener() {
                                                                @Override
                                                                public void onFailure(@NonNull Exception e) {
                                                                    // Ocurrió un error al intentar actualizar el campo
                                                                }
                                                            });
                                                        }
                                                    });
                                                }
                                            }).addOnFailureListener(new OnFailureListener() {
                                                @Override
                                                public void onFailure(@NonNull Exception e) {
                                                    // Ocurrió un error al intentar subir la imagen
                                                }
                                            });

                                        }
                                    });
                                } else {
                                    alertChangeAv.dismiss();
                                }

                            }
                        });
                    }
                });

                bChangePname.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();

                        AlertDialog.Builder bChange = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                        View vAlertChange = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_change_name_layout, null);
                        bChange.setView(vAlertChange);
                        AlertDialog alertChange = bChange.create();
                        alertChange.show();
                        Button confirmChangeB = vAlertChange.findViewById(R.id.bConfirmar);

                        confirmChangeB.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                EditText name = vAlertChange.findViewById(R.id.nameBox);
                                String newName = name.getText().toString();
                                if(!newName.trim().isEmpty()){
                                    alertChange.dismiss();
                                    FirebaseFirestore db = FirebaseFirestore.getInstance();
                                    DocumentReference docRef = db.collection("users").document(uFire.getUid());
                                    docRef.update("pName", name.getText().toString()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Toast.makeText(getContext(), getString(R.string.pNameChangedConfirmed), Toast.LENGTH_SHORT).show();
                                            CollectionReference tunesColRef = db.collection("tunes");
                                            tunesColRef.whereEqualTo("authorId", uFire.getUid()).get()
                                                    .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                        @Override
                                                        public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                            for (DocumentSnapshot document : queryDocumentSnapshots.getDocuments()) {
                                                                document.getReference().update("publicName", name.getText().toString());
                                                            }
                                                            loadTunes();
                                                        }
                                                    });

                                        }
                                    }).addOnFailureListener(new OnFailureListener() {
                                        @Override
                                        public void onFailure(@NonNull Exception e) {
                                            // Ocurrió un error al intentar actualizar el campo
                                        }
                                    });
                                }

                            }
                        });
                    }
                });

                bDeleteUser.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        alert.dismiss();
                        AlertDialog.Builder bDelete = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);

                        View vAlertDelete = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_delete_user_layout, null);
                        bDelete.setView(vAlertDelete);
                        AlertDialog alertDelete = bDelete.create();
                        alertDelete.show();

                        Button bCancelD = vAlertDelete.findViewById(R.id.bCancel);
                        Button bAcceptD = vAlertDelete.findViewById(R.id.bAccept);

                        bCancelD.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDelete.dismiss();
                            }
                        });

                        bAcceptD.setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                alertDelete.dismiss();

                                AlertDialog.Builder bDeleteC = new AlertDialog.Builder(getContext(), R.style.MyAlertDialogStyle);
                                View vAlertDeleteC = LayoutInflater.from(getContext()).inflate(R.layout.alert_dialog_delete_user_relog_layout, null);
                                bDeleteC.setView(vAlertDeleteC);
                                AlertDialog alertDeleteC = bDeleteC.create();
                                alertDeleteC.show();
                                Button confirmDelete = vAlertDeleteC.findViewById(R.id.bConfirmar);

                                confirmDelete.setOnClickListener(new View.OnClickListener() {
                                    @Override
                                    public void onClick(View view) {
                                        EditText usernameET = vAlertDeleteC.findViewById(R.id.emailBox);
                                        EditText passET = vAlertDeleteC.findViewById(R.id.passBox);

                                        final FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

                                        String email = usernameET.getText().toString();
                                        String password = passET.getText().toString();
                                        AuthCredential credential = EmailAuthProvider.getCredential(email, password);

                                        user.reauthenticate(credential)
                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                    @Override
                                                    public void onComplete(@NonNull Task<Void> task) {
                                                        alertDeleteC.dismiss();
                                                        FirebaseStorage storage = FirebaseStorage.getInstance();
                                                        StorageReference storageRef = storage.getReference();
                                                        StorageReference avatarRef = storageRef.child("avatars/" + user.getUid());
                                                        avatarRef.delete();

                                                        FirebaseFirestore db = FirebaseFirestore.getInstance();
                                                        CollectionReference tunesRef = db.collection("tunes");

                                                        tunesRef.whereEqualTo("authorId", user.getUid())
                                                                .get()
                                                                .addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
                                                                    @Override
                                                                    public void onSuccess(QuerySnapshot queryDocumentSnapshots) {
                                                                        for (QueryDocumentSnapshot document : queryDocumentSnapshots) {
                                                                            document.getReference().delete();
                                                                        }
                                                                    }
                                                                });


                                                        DocumentReference userRef = db.collection("users").document(user.getUid());
                                                        userRef.delete();

                                                        user.delete()
                                                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                                                    @Override
                                                                    public void onComplete(@NonNull Task<Void> task) {
                                                                        if (task.isSuccessful()) {
                                                                            Toast.makeText(getContext(), getString(R.string.deletedUserConfirmed), Toast.LENGTH_SHORT).show();
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
                                                                    }
                                                                });


                                                    }
                                                })
                                                .addOnFailureListener(new OnFailureListener() {
                                                    @Override
                                                    public void onFailure(@NonNull Exception e) {
                                                        Toast.makeText(getContext(), getString(R.string.wrongData), Toast.LENGTH_SHORT).show();
                                                    }
                                                });


                                    }
                                });



                            }
                        });



                    }
                });

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
                                        Toast.makeText(getContext(), getString(R.string.genreChangedConfirmed), Toast.LENGTH_SHORT).show();
                                        user.setGenre(genreSpinner.getSelectedItem().toString());
                                        loadTunes();
                                        alertGenre.dismiss();
                                    }
                                }).addOnFailureListener(new OnFailureListener() {
                                    @Override
                                    public void onFailure(@NonNull Exception e) {
                                        Toast.makeText(getContext(), getString(R.string.errorChangeGenre), Toast.LENGTH_SHORT).show();
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

        ivLogo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadTunes();
            }
        });

        return view;
    }
}