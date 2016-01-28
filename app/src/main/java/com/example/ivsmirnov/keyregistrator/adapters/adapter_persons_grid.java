package com.example.ivsmirnov.keyregistrator.adapters;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.widget.RecyclerView;
import android.util.Base64;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AbsListView;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.custom_views.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.RecycleItemClickListener;
import com.example.ivsmirnov.keyregistrator.others.Values;

import java.util.ArrayList;

public class adapter_persons_grid extends RecyclerView.Adapter<adapter_persons_grid.ViewHolder>{

    private SparseArray <String> card;
    private ArrayList <PersonItem> allItems;
    private int mType;
    private Context context;
    private LayoutInflater inflater;
    private RecycleItemClickListener mListener;

    public adapter_persons_grid(Context c, ArrayList<PersonItem> all,int type,RecycleItemClickListener listener) {
        allItems = all;
        context = c;
        mType = type;
        this.mListener = listener;
        inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    }


    static class ViewHolder extends RecyclerView.ViewHolder{
        public ImageView imageView;
        public TextView textSurname;
        public TextView textMidname;
        public TextView textLastname;
        public TextView textDivision;

        public ViewHolder(View itemView) {
            super(itemView);
            imageView = (ImageView)itemView.findViewById(R.id.image_sql);
            textSurname = (TextView) itemView.findViewById(R.id.person_card_text_lastname);
            textMidname = (TextView)itemView.findViewById(R.id.person_card_text_firstname);
            textLastname = (TextView)itemView.findViewById(R.id.person_card_text_midname);
            textDivision = (TextView)itemView.findViewById(R.id.person_card_text_division);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View rowView = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_person,parent,false);
        final ViewHolder viewHolder = new ViewHolder(rowView);

        //WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        //Display display = windowManager.getDefaultDisplay();
        //int gridHeight = display.getHeight();
        //int buttonHeight = gridHeight / 6;
        rowView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, AbsListView.LayoutParams.WRAP_CONTENT));
        rowView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onItemClick(v,viewHolder.getLayoutPosition());
            }
        });
        rowView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                mListener.onItemLongClick(v,viewHolder.getLayoutPosition(),0);
                return true;
            }
        });
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.textSurname.setText(allItems.get(position).Lastname);
        holder.textMidname.setText(allItems.get(position).Firstname);
        holder.textLastname.setText(allItems.get(position).Midname);
        holder.textDivision.setText(allItems.get(position).Division);


        Bitmap bitmap = null;
        String photo = null;

        if (mType== Values.SHOW_FAVORITE_PERSONS){
            photo = allItems.get(position).PhotoPreview;
        }else if (mType==Values.SHOW_ALL_PERSONS){
            photo = allItems.get(position).PhotoOriginal;
        }

        if (photo!=null){
            byte[] decodedString = Base64.decode(photo, Base64.DEFAULT);
            bitmap = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length);

            holder.imageView.setImageBitmap(bitmap);

        }/*else{
            //if(allItems.get(position).Sex.equals("Ж")){
            //    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.person_female_colored);
            //}else{
            //    bitmap = BitmapFactory.decodeResource(context.getResources(),R.drawable.person_male_colored);
            //}
            //options.inSampleSize = DataBaseFavorite.calculateInSampleSize(options, 120, 160);
            //options.inJustDecodeBounds = false;
        }*/


    }



    @Override
    public int getItemCount() {
        return allItems.size();
    }



/*
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        ViewHolder holder;
        View rowView = convertView;


        if (rowView==null){
            rowView = inflater.inflate(R.layout.cell_for_base_sql, null);

            holder.textSurname = (TextView) rowView.findViewById(R.id.text_familia);
            holder.textMidname = (TextView)rowView.findViewById(R.id.text_imya);
            holder.textLastname = (TextView)rowView.findViewById(R.id.otchestvo);
            holder.textDivision = (TextView)rowView.findViewById(R.id.kafedra);
            holder.imageView = (ImageView)rowView.findViewById(R.id.image_sql);
            rowView.setTag(holder);
        }else{
            holder = (ViewHolder)rowView.getTag();
        }




        rowView.setTag(R.string.grid_item_tag_lastname,card.get(0));
        rowView.setTag(R.string.grid_item_tag_firstname,card.get(1));
        rowView.setTag(R.string.grid_item_tag_midname,card.get(2));
        rowView.setTag(R.string.grid_item_tag_division,card.get(3));
        rowView.setTag(R.string.grid_item_tag_sex,card.get(4));
        rowView.setTag(R.string.grid_item_tag_radio_label,card.get(6));

        if (mType==1){
            ImageLoader imageLoader = ImageLoader.getInstance();
            if (!imageLoader.isInited()){
                imageLoader.init(ImageLoaderConfiguration.createDefault(context));
            }
            imageLoader.displayImage("file://"+card.get(5), holder.imageView);
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
            holder.imageView.setImageBitmap(bitmap);

        }

        WindowManager windowManager = (WindowManager)context.getSystemService(Context.WINDOW_SERVICE);
        Display display = windowManager.getDefaultDisplay();
        int gridHeight = display.getHeight();
        int buttonHeight = gridHeight / 6;
        rowView.setLayoutParams(new AbsListView.LayoutParams(AbsListView.LayoutParams.MATCH_PARENT, buttonHeight));
        return rowView;
    }
*/

}
