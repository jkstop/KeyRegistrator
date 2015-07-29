package com.example.ivsmirnov.keyregistrator;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Base64;
import android.util.Base64InputStream;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * Created by IVSmirnov on 02.07.2015.
 */
public class base_sql_activity_adapter extends BaseAdapter {

    private SparseArray <String> card;

    private ArrayList <SparseArray> allItems;

    private Context context;
    private LayoutInflater inflater;

    private Loader_Image.OnImageLoaded listener;

    public base_sql_activity_adapter(Context c,ArrayList<SparseArray> all,Loader_Image.OnImageLoaded l){
        allItems = all;
        context = c;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        this.listener = l;

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
        Log.d("setAdapter",card.get(0));
        Log.d("allItemsSize", String.valueOf(allItems.size()));

        if (convertView==null){
            view = inflater.inflate(R.layout.cell_for_base_sql, null);
        }else{
            view = (View)convertView;
        }

        TextView familia = (TextView) view.findViewById(R.id.text_familia);
        TextView imya = (TextView)view.findViewById(R.id.text_imya);
        TextView otchestvo = (TextView)view.findViewById(R.id.otchestvo);
        TextView kafedra = (TextView)view.findViewById(R.id.kafedra);
        final ImageView image = (ImageView)view.findViewById(R.id.image_sql);
        ProgressBar progressBar = (ProgressBar)view.findViewById(R.id.progressBar);

        familia.setText(card.get(0));
        imya.setText(card.get(1));
        otchestvo.setText(card.get(2));
        kafedra.setText(card.get(3));


            if (card.get(5).equalsIgnoreCase("preload")){
                Log.d("preloadFor",card.get(0)+card.get(1)+card.get(2));
                progressBar.setVisibility(View.VISIBLE);
                image.setVisibility(View.INVISIBLE);
                Log.d("nowStartLoader","from adapter");
                Loader_Image loader_image = new Loader_Image(context,new String[]{card.get(0),card.get(1),card.get(2),card.get(3)},listener);
                loader_image.execute();
            }else{
                progressBar.setVisibility(View.INVISIBLE);
                image.setVisibility(View.VISIBLE);
                ImageLoader imageLoader = ImageLoader.getInstance();
                if (!imageLoader.isInited()){
                    imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                }
                    DataBases db = new DataBases(context);
                Log.d("getPhotoID","ok");
                    imageLoader.displayImage("file://" + db.getPhotoID(card),image);
                    db.closeDBconnection();

            }


        int gridHeight = base_sql_activity.gridView.getHeight();
        int buttonHeight = gridHeight / 6;
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, buttonHeight));
        return view;
    }

}
