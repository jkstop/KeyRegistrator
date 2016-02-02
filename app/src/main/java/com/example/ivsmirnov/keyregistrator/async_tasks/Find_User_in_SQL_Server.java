package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Base64;
import android.view.View;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.items.PersonItem;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseFavorite;
import com.example.ivsmirnov.keyregistrator.interfaces.Find_User_in_SQL_Server_Interface;

import java.io.ByteArrayOutputStream;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

/**
 * Created by ivsmirnov on 07.12.2015.
 */
public class Find_User_in_SQL_Server extends AsyncTask<ResultSet,Void,ArrayList<PersonItem>> {

    private Find_User_in_SQL_Server_Interface mListener;
    private Context mContext;

    public Find_User_in_SQL_Server (Context context, Find_User_in_SQL_Server_Interface listener){
        this.mContext = context;
        mListener = listener;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
        mListener.changeProgressBar(View.VISIBLE);
    }

    @Override
    protected ArrayList<PersonItem> doInBackground(ResultSet... params) {
        ArrayList<PersonItem> mItems = new ArrayList<>();
        try {
            while (params[0].next()){
                String photo = params[0].getString("PHOTO");

                if (photo==null){
                    photo = getBase64DefaultPhotoFromResources(mContext);
                }

                mItems.add(new PersonItem()
                        .setLastname(params[0].getString("LASTNAME"))
                        .setFirstname(params[0].getString("FIRSTNAME"))
                        .setMidname(params[0].getString("MIDNAME"))
                        .setDivision(params[0].getString("NAME_DIVISION"))
                        .setSex(params[0].getString("SEX"))
                        .setPhotoPreview(DataBaseFavorite.getPhotoPreview(photo))
                        .setPhotoOriginal(photo)
                        .setRadioLabel(params[0].getString("RADIO_LABEL")));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return mItems;
    }

    @Override
    protected void onPostExecute(ArrayList<PersonItem> personItems) {
        super.onPostExecute(personItems);
        mListener.changeProgressBar(View.INVISIBLE);
        mListener.updateGrid(personItems);
    }

    public static String getBase64DefaultPhotoFromResources(Context context){
        BitmapFactory.Options options = new BitmapFactory.Options();
        Bitmap bitmap = BitmapFactory.decodeResource(context.getResources(), R.drawable.ic_person_black_48dp, options);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.PNG,100,byteArrayOutputStream);
        byte[] byteArray = byteArrayOutputStream.toByteArray();
        return Base64.encodeToString(byteArray,Base64.NO_WRAP);
    }
}
