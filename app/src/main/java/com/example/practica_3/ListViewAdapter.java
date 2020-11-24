package com.example.practica_3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class ListViewAdapter extends BaseAdapter {

    private ArrayList<Entrada> entradas;
    private Context context;

    public ListViewAdapter(Context context, List<Entrada> entradas) { //???
        this.context = context;
        this.entradas = (ArrayList<Entrada>) entradas;
    }

    @Override
    public int getCount() {
        return entradas.size();
    }

    @Override
    public Object getItem(int position) {
        return entradas.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {

        if(convertView == null){
            convertView = LayoutInflater.from(context).inflate(R.layout.list_item, parent, false);
        }

        Entrada entrada = (Entrada) getItem(position);

        TextView tv_nombre_usuario = convertView.findViewById(R.id.tv_nombre_usuario);
        TextView tv_titular = convertView.findViewById(R.id.tv_titular);

        tv_nombre_usuario.setText(entrada.getAutor());
        tv_titular.setText(entrada.getTitular());

        return convertView;
    }
}
