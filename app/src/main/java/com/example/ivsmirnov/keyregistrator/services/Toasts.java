package com.example.ivsmirnov.keyregistrator.services;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.view.Gravity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.App;

/**
 * Тосты
 */
public class Toasts {

    public static final int TOAST_TAKE_KEY = 1;
    public static final int TOAST_THANKS = 2;
    public static final int TOAST_PUT_CARD_FIRST = 3;
    public static final int TOAST_WRONG_CARD = 4;
    public static final int TOAST_SELECT_ROOM_FIRST = 5;


    public static final Handler handler = new Handler(Looper.getMainLooper()) {
        public void handleMessage(Message msg) {
            View toastView = View.inflate(App.getAppContext(), R.layout.view_toast_fullsrceen, null);
            TextView toastText = (TextView) toastView.findViewById(R.id.toast_fullscreen_text);

            switch (msg.what) {
                case TOAST_TAKE_KEY:
                    toastView.setBackgroundResource(R.color.colorPrimary);
                    toastText.setText(App.getAppContext().getString(R.string.toast_take_key));
                    break;
                case TOAST_THANKS:
                    toastView.setBackgroundResource(R.color.colorPrimary);
                    toastText.setText(App.getAppContext().getString(R.string.toast_thanks));
                    break;
                case TOAST_PUT_CARD_FIRST:
                    toastView.setBackgroundResource(R.color.colorAccent);
                    toastText.setText(App.getAppContext().getString(R.string.toast_put_card));
                    break;
                case TOAST_WRONG_CARD:
                    toastView.setBackgroundResource(R.color.colorAccent);
                    toastText.setText(App.getAppContext().getString(R.string.toast_incorrect_card));
                    break;
                case TOAST_SELECT_ROOM_FIRST:
                    toastView.setBackgroundResource(R.color.colorAccent);
                    toastText.setText(App.getAppContext().getString(R.string.toast_choise_room_in_first));
                    break;
            }

            Toast toast = new Toast(App.getAppContext());
            toast.setDuration(android.widget.Toast.LENGTH_SHORT);
            toast.setGravity(Gravity.FILL, 0, 0);
            toast.setView(toastView);
            toast.show();

        }
    };
}
