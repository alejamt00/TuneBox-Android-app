package com.osaki.tuneboxreborn;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.DialogFragment;

import java.util.Calendar;


/**
 * Clase DatePickerFragment que extiende la clase DialogFragment.
 * Se utiliza para crear un cuadro de diálogo de selección de fecha.
 */
public class DatePickerFragment extends DialogFragment {

    private DatePickerDialog.OnDateSetListener listener;

    /**
     * Método para crear una nueva instancia de la clase DatePickerFragment y establecer el listener OnDateSetListener.
     *
     * @param listener El listener para el evento de selección de fecha.
     * @return Una nueva instancia de la clase DatePickerFragment.
     */
    public static DatePickerFragment newInstance(DatePickerDialog.OnDateSetListener listener) {
        DatePickerFragment fragment = new DatePickerFragment();
        fragment.setListener(listener);
        return fragment;
    }

    /**
     * Método para establecer el listener para el evento de selección de fecha.
     *
     * @param listener El listener para el evento de selección de fecha.
     */
    public void setListener(DatePickerDialog.OnDateSetListener listener) {
        this.listener = listener;
    }

    /**
     * Método llamado cuando se crea el cuadro de diálogo.
     * Devuelve un nuevo DatePickerDialog con la fecha actual establecida como fecha predeterminada.
     *
     * @param savedInstanceState El estado guardado de la instancia anterior.
     * @return Un nuevo DatePickerDialog.
     */
    @Override
    @NonNull
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Calendar c = Calendar.getInstance();
        int year = c.get(Calendar.YEAR);
        int month = c.get(Calendar.MONTH);
        int day = c.get(Calendar.DAY_OF_MONTH);

        return new DatePickerDialog(getActivity(), listener, year, month, day);
    }

}