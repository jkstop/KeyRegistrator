package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Environment;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v7.widget.DefaultItemAnimator;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.adapters.AdapterEmailExtras;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.databases.AccountDB;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.GetAccountInterface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.nononsenseapps.filepicker.FilePickerActivity;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;

/**
 * email fragment
 */
public class EmailFr extends Fragment implements GetAccountInterface, EmailInterface {

    public static final String ADD_NEW_RECIPIENT = "add_new_recipient";
    public static final int REQUEST_CODE_SELECT_EMAIL_ATTACHMENT = 206;

    private Context mContext;
    private ImageView mAccountImage;
    private ArrayList<String> mRecepientList, mAttachmentList;
    private AdapterEmailExtras mAdapterRecipients, mAdapterAttachments;
    private FloatingActionButton mSendButton;
    private AccountItem mAccount;


    public static EmailFr newInstance(){
        return new EmailFr();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == REQUEST_CODE_SELECT_EMAIL_ATTACHMENT){
            if (data!=null){
                mAttachmentList.add(data.getData().getPath());
                Settings.setAttachments(mAttachmentList);
                mAdapterAttachments.notifyItemInserted(mAttachmentList.size());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_email_fr,container,false);
        mContext = rootView.getContext();

        mAccountImage = (ImageView)rootView.findViewById(R.id.email_fragment_account_information_image);
        ImageView mAddRecipient = (ImageView) rootView.findViewById(R.id.email_fragment_add_recipient);
        ImageView mAddAttachment = (ImageView) rootView.findViewById(R.id.email_fragment_add_attachment);

        TextInputLayout mInputThemeMessage = (TextInputLayout) rootView.findViewById(R.id.email_fragment_input_message_theme);
        TextInputLayout mInputBodyMessage = (TextInputLayout) rootView.findViewById(R.id.email_fragment_input_message_body);

        mSendButton = (FloatingActionButton)rootView.findViewById(R.id.email_fragment_send_fab);

        TextView mAccountName = (TextView)rootView.findViewById(R.id.email_fragment_account_information_name);
        TextView mAccountEmail = (TextView)rootView.findViewById(R.id.email_fragment_account_information_email);

        mAccount = AccountDB.getAccount(Settings.getActiveAccountID());
        if (mAccount!=null){
            mAccountName.setText(mAccount.getLastname());
            mAccountEmail.setText(mAccount.getEmail());
            Picasso.with(mContext).load(mAccount.getPhoto()).into(mAccountImage);
        }else{
            mAccountName.setText(getResources().getString(R.string.email_fragment_logon));
            mAccountEmail.setText(Values.EMPTY);
            mAccountImage.setImageDrawable(null);
        }

        mInputThemeMessage.getEditText().setText(Settings.getMessageTheme());
        mInputBodyMessage.getEditText().setText(Settings.getMessageBody());

        mInputBodyMessage.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Settings.setMessageBody(s.toString());
            }
        });
        mInputThemeMessage.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                Settings.setMessageTheme(s.toString());
            }
        });

        mAddRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecepientList.add(ADD_NEW_RECIPIENT);
                Settings.setRecepients(mRecepientList);
                mAdapterRecipients.notifyItemInserted(mRecepientList.size());
                checkSendButtonVisibility();
            }
        });

        mAddAttachment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentAddAttachment = new Intent(Intent.ACTION_GET_CONTENT);
                intentAddAttachment.putExtra(FilePickerActivity.EXTRA_MODE, FilePickerActivity.MODE_FILE);
                intentAddAttachment.putExtra(FilePickerActivity.EXTRA_START_PATH, Environment.getExternalStorageDirectory().getAbsolutePath());
                startActivityForResult(intentAddAttachment, REQUEST_CODE_SELECT_EMAIL_ATTACHMENT);
            }
        });

        mRecepientList = Settings.getRecepients();
        checkSendButtonVisibility();
        mAttachmentList = Settings.getAttachments();

        mAdapterRecipients = new AdapterEmailExtras(mContext, this, AdapterEmailExtras.RECIPIENTS, mRecepientList);
        mAdapterAttachments = new AdapterEmailExtras(mContext, this, AdapterEmailExtras.ATTACHMENTS, mAttachmentList);

        RecyclerView mRecipientRecycler = (RecyclerView) rootView.findViewById(R.id.fragment_email_recipients_recycler);
        mRecipientRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecipientRecycler.setLayoutManager(/*new RecyclerWrapContentHeightManager(mContext, LinearLayoutManager.VERTICAL, false)*/new LinearLayoutManager(mContext));
        mRecipientRecycler.setAdapter(mAdapterRecipients);

        RecyclerView mAttachmentsRecycler = (RecyclerView) rootView.findViewById(R.id.email_fagment_attachments_recycler);
        mAttachmentsRecycler.setLayoutManager(/*new RecyclerWrapContentHeightManager(mContext, LinearLayoutManager.VERTICAL, false)*/new LinearLayoutManager(mContext));
        mAttachmentsRecycler.setAdapter(mAdapterAttachments);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Send_Email(mContext, Send_Email.DIALOG_ENABLED)
                        .execute();
            }
        });

        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        ((Launcher)getActivity()).setToolbarTitle(R.string.toolbar_title_email);
    }

    private void checkSendButtonVisibility(){
        if (mAccount!=null){
            if (mSendButton!=null) {
                if (mRecepientList != null &&
                        mRecepientList.size() != 0) {
                    if (mRecepientList.size() == 1 && mRecepientList.contains(ADD_NEW_RECIPIENT)) {
                        mSendButton.setVisibility(View.INVISIBLE);
                    } else {
                        mSendButton.setVisibility(View.VISIBLE);
                    }
                } else {
                    mSendButton.setVisibility(View.INVISIBLE);
                }
            }
        }else{
            if (mSendButton!=null){
                mSendButton.setVisibility(View.INVISIBLE);
            }
        }
    }

    @Override
    public void onUserRecoverableAuthException(UserRecoverableAuthException e) {

    }

    @Override
    public void onChangeAccount() {

    }

    @Override
    public void onAccountImageLoaded(Bitmap bitmap) {
        if (mAccountImage!=null){
            mAccountImage.setImageBitmap(bitmap);
        }
    }

    private void removeRecipient(int position){
        mRecepientList.remove(position);
        Settings.setRecepients(mRecepientList);
        mAdapterRecipients.notifyItemRemoved(position);
        checkSendButtonVisibility();
    }


    @Override
    public void onAddRecepient(View v, int position, int view_id) {
        if (view_id == R.id.card_email_add_new_recipient_save){
            mRecepientList.add(v.getTag().toString());
        }
        removeRecipient(position);
    }

    @Override
    public void onDeleteRecepient(int position, int view_id) {
        removeRecipient(position);
    }

    @Override
    public void onDeleteAttachment(int position, int view_id) {
        mAttachmentList.remove(position);
        Settings.setAttachments(mAttachmentList);
        mAdapterAttachments.notifyItemRemoved(position);
    }

    @Override
    public void onAddAttachment() {

    }
}
