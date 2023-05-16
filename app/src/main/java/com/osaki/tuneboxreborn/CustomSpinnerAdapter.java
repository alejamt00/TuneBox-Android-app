package com.osaki.tuneboxreborn;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

/**
 * La clase CustomSpinnerAdapter es un adaptador personalizado para un Spinner que muestra una lista de géneros musicales.
 */
public class CustomSpinnerAdapter extends ArrayAdapter<String> {

    private int hidingItemIndex;

    /**
     * Constructor para la clase CustomSpinnerAdapter.
     * @param context el contexto de la aplicación.
     * @param textViewResourceId el ID del recurso de la vista de texto.
     * @param objects la lista de géneros musicales.
     * @param hidingItemIndex el índice del elemento que se ocultará en el Spinner.
     */
    public CustomSpinnerAdapter(Context context, int textViewResourceId, String[] objects, int hidingItemIndex) {
        super(context, textViewResourceId, objects);
        this.hidingItemIndex = hidingItemIndex;
    }

    /**
     * Devuelve la vista para un elemento del Spinner.
     * @param position la posición del elemento en la lista.
     * @param convertView la vista a reutilizar.
     * @param parent el ViewGroup en el que se agregará la vista.
     * @return la vista para el elemento del Spinner.
     */
    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent) {
        View v = null;
        if (position == hidingItemIndex) {
            TextView tv = new TextView(getContext());
            tv.setVisibility(View.GONE);
            v = tv;
        } else {
            v = super.getDropDownView(position, null, parent);
        }
        return v;
    }
}