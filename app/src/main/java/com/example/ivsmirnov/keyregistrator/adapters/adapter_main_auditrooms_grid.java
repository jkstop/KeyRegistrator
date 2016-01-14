package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.RoomItem;
import com.example.ivsmirnov.keyregistrator.fragments.Main_Fragment;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

import java.util.ArrayList;

public class adapter_main_auditrooms_grid extends BaseAdapter {

    private Context context;
    private ArrayList<RoomItem> mRoomItems;
    private LayoutInflater inflater;
    private SharedPreferences preferences;


    public adapter_main_auditrooms_grid(Context c, ArrayList<RoomItem> roomItems) {
        context = c;
        this.mRoomItems = roomItems;
        inflater = (LayoutInflater)this.context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        preferences = PreferenceManager.getDefaultSharedPreferences(context);

    }
    @Override
    public int getCount() {
        return mRoomItems.size();
    }

    @Override
    public Object getItem(int position) {
        return mRoomItems.get(position);
    }

    @Override
    public long getItemId(int position) {
        return 0;
    }

    @Override
    public View getView(final int position, View convertView, ViewGroup parent) {
        View view;

        Button button;
        ImageView image;
        TextView textAud,textPerson;

        if (convertView == null) {
                if (mRoomItems.get(position).Status==1){
                    view = inflater.inflate(R.layout.cell_for_grid_is_free, null);
                }else{
                    view = inflater.inflate(R.layout.cell_for_grid_is_busy,null);
                }
            }else{
                view = convertView;
            }

        int space = (int) context.getResources().getDimension(R.dimen.grid_vertical_spacing);
        int heightGrid = Main_Fragment.gridView.getHeight();
        int childCount = Main_Fragment.gridView.getCount();
        int rows;
        rows = (int) Math.ceil((double) childCount / preferences.getInt(Values.COLUMNS_AUD_COUNT, 1));
        int btnHeight = heightGrid/rows - space;

        AbsListView.LayoutParams layoutParams = new AbsListView.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                btnHeight);
        view.setLayoutParams(layoutParams);

        if (mRoomItems.get(position).Status==1){
            image = (ImageView)view.findViewById(R.id.imageButton);
            textAud = (TextView)view.findViewById(R.id.textButton);
            textAud.setText(mRoomItems.get(position).Auditroom);
            view.setBackgroundResource(R.drawable.button_background);
            image.setImageResource(R.drawable.key_colored);
        }else{
            image = (ImageView)view.findViewById(R.id.image_key_person);
            textAud = (TextView)view.findViewById(R.id.text_aud);
            textPerson = (TextView)view.findViewById(R.id.textButton);

            if (!mRoomItems.get(position).Photo.equalsIgnoreCase("")){
                ImageLoader imageLoader = ImageLoader.getInstance();
                if (!imageLoader.isInited()){
                    imageLoader.init(ImageLoaderConfiguration.createDefault(context));
                }
                imageLoader.displayImage("file://"+mRoomItems.get(position).Photo,image);
            }else{
                image.setImageResource(R.drawable.person_male_colored);
            }
            textAud.setText(String.valueOf(mRoomItems.get(position).Auditroom));
            textPerson.setText(mRoomItems.get(position).LastVisiter);

            if (mRoomItems.get(position).Tag.equalsIgnoreCase("99 80 DC 1A 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("EF 36 83 D9 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("0F 4B 7C D9 00 00")
                    ||mRoomItems.get(position).Tag.equalsIgnoreCase("69 B1 D2 29 00 00")){
                view.setBackgroundResource(R.drawable.button_background_support);
            }else{
                view.setBackgroundResource(R.drawable.button_background_selected);
            }
        }

        view.setTag(mRoomItems.get(position).Auditroom);

        return view;
    }
}
