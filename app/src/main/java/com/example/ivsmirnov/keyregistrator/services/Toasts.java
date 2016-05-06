package com.example.ivsmirnov.keyregistrator.services;

import android.content.Context;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;

/**
 * Тосты
 */
public class Toasts {

    public static final int TOAST_NEGATIVE = 0;
    public static final int TOAST_POSITIVE = 1;

    public static void showFullscreenToast(Context context, String message, int type){
        //LayoutInflater inflater = (LayoutInflater)context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View toastView = View.inflate(context, R.layout.layout_toast_fullscreen, null);
        TextView toastText = (TextView)toastView.findViewById(R.id.toast_fullscreen_text);
        toastText.setText(message);

        switch (type){
            case TOAST_NEGATIVE:
                toastView.setBackgroundResource(R.color.colorAccent);
                break;
            case TOAST_POSITIVE:
                toastView.setBackgroundResource(R.color.colorPrimary);
                break;
            default:
                break;
        }

        Toast toast = new Toast(context);
        toast.setDuration(android.widget.Toast.LENGTH_SHORT);
        toast.setGravity(Gravity.FILL,0,0);
        toast.setView(toastView);
        toast.show();
    }
}
