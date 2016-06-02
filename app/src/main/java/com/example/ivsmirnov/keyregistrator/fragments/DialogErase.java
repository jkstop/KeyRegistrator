package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.View;
import android.widget.CheckBox;

import com.example.ivsmirnov.keyregistrator.R;

import java.util.ArrayList;

/**
 * Диалог очистики БД
 */
public class DialogErase extends DialogFragment {

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View dialogView = View.inflate(getContext(), R.layout.dialog_erase, null);

        final ArrayList<String> checkedItems = new ArrayList<>();
        final CheckBox journalCheck = (CheckBox)dialogView.findViewById(R.id.dialog_erase_journal_check);
        final CheckBox personCheck = (CheckBox)dialogView.findViewById(R.id.dialog_erase_persons_check);
        final CheckBox roomCheck = (CheckBox)dialogView.findViewById(R.id.dialog_erase_rooms_check);
        final CheckBox serverCheck = (CheckBox)dialogView.findViewById(R.id.dialog_erase_server_check);
        return new AlertDialog.Builder(getContext())
                .setTitle(getString(R.string.shared_preferences_backup_erase_base))
                .setView(dialogView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (journalCheck.isChecked()) checkedItems.add(journalCheck.getText().toString());

                        if (personCheck.isChecked()) checkedItems.add(personCheck.getText().toString());

                        if (roomCheck.isChecked()) checkedItems.add(roomCheck.getText().toString());

                        if (serverCheck.isChecked()) checkedItems.add(serverCheck.getText().toString());

                        if (!checkedItems.isEmpty()) DialogPassword.newInstance(null,checkedItems).show(getFragmentManager(), DialogPassword.ERASE_ACCESS);

                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                    }
                })
                .create();
    }
}
