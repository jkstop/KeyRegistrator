package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;
import android.util.Log;
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
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class adapter_persons_grid extends BaseAdapter {

    private SparseArray <String> card;
    private ArrayList <SparseArray> allItems;
    private int mType;
    private Context context;
    private LayoutInflater inflater;

    public adapter_persons_grid(Context c, ArrayList<SparseArray> all,int type) {
        allItems = all;
        context = c;
        mType = type;
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

        view.setTag(R.string.grid_item_tag_lastname,card.get(0));
        view.setTag(R.string.grid_item_tag_firstname,card.get(1));
        view.setTag(R.string.grid_item_tag_midname,card.get(2));
        view.setTag(R.string.grid_item_tag_division,card.get(3));
        view.setTag(R.string.grid_item_tag_sex,card.get(4));
        view.setTag(R.string.grid_item_tag_radio_label,card.get(6));

        if (mType==1){
            ImageLoader imageLoader = ImageLoader.getInstance();
            if (!imageLoader.isInited()){
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            }
            imageLoader.displayImage("file://"+card.get(5),image);
        }else{
            Bitmap bitmap;
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;

            if (card.get(5)!=null){
                Log.d("card5",card.get(5));
                byte[] decodedString = Base64.decode(card.get(5), Base64.DEFAULT);
                BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
                options.inSampleSize = DataBaseFavorite.calculateInSampleSize(options, 120, 160);
                options.inJustDecodeBounds = false;
                bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

            }else{
                //decodedString = Base64.decode("",Base64.DEFAULT);
                bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.person_male_colored);
                options.inSampleSize = DataBaseFavorite.calculateInSampleSize(options, 120, 160);
                options.inJustDecodeBounds = false;
                //image.setImageBitmap(bitmap);
            }
            image.setImageBitmap(bitmap);

        }

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int gridHeight = display.getHeight();
        int buttonHeight = gridHeight / 6;
        view.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, buttonHeight));
        return view;
    }

}
