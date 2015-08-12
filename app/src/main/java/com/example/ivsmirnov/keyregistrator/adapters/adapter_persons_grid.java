package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.util.SparseArray;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.databases.DataBases;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class adapter_persons_grid extends BaseAdapter {

    private SparseArray <String> card;

    private ArrayList <SparseArray> allItems;

    private Context context;
    private LayoutInflater inflater;


    public adapter_persons_grid(Context c, ArrayList<SparseArray> all) {
        allItems = all;
        context = c;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    }
    @Override
    public int getCount() {
        return allItems.size();
    }

    @Override
    public Object getItem(int position) {
        return allItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return allItems.indexOf(allItems.get(position));
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view;
        card = allItems.get(position);

        if (convertView==null){
            view = inflater.inflate(R.layout.cell_for_base_sql, null);
        }else{
            view = convertView;
        }

        TextView familia = (TextView) view.findViewById(R.id.text_familia);
        TextView imya = (TextView)view.findViewById(R.id.text_imya);
        TextView otchestvo = (TextView)view.findViewById(R.id.otchestvo);
        TextView kafedra = (TextView)view.findViewById(R.id.kafedra);
        final ImageView image = (ImageView)view.findViewById(R.id.image_sql);

        familia.setText(card.get(0));
        imya.setText(card.get(1));
        otchestvo.setText(card.get(2));
        kafedra.setText(card.get(3));

                ImageLoader imageLoader = ImageLoader.getInstance();
                if (!imageLoader.isInited()){
                    imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                }
                DataBases db = new DataBases(context);
                imageLoader.displayImage("file://" + db.getPhotoID(card), image);
                db.closeDBconnection();


        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int gridHeight = display.getHeight();
        int buttonHeight = gridHeight / 6;
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, buttonHeight));
        return view;
    }

}
