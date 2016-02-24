package com.example.ivsmirnov.keyregistrator.fragments;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
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
import com.example.ivsmirnov.keyregistrator.adapters.adapter_email_attach;
import com.example.ivsmirnov.keyregistrator.async_tasks.LoadImageFromWeb;
import com.example.ivsmirnov.keyregistrator.async_tasks.Send_Email;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.interfaces.EmailClickItemsInterface;
import com.example.ivsmirnov.keyregistrator.interfaces.Get_Account_Information_Interface;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.example.ivsmirnov.keyregistrator.others.Values;
import com.google.android.gms.auth.UserRecoverableAuthException;
import com.nononsenseapps.filepicker.FilePickerActivity;

import java.util.ArrayList;

/**
 * Created by IVSmirnov on 03.09.2015.
 */
public class Email_Fragment extends Fragment implements Get_Account_Information_Interface, EmailClickItemsInterface{

    public static final String ADD_NEW_RECIPIENT = "add_new_recipient";

    private Context mContext;
    private ImageView mAccountImage;
    private ArrayList<String> mRecepientList, mAttachmentList;
    private ImageView mAddRecipient, mAddAttachment;
    private adapter_email_attach mAdapterRecipients, mAdapterAttachments;
    private RecyclerView mRecipientRecycler, mAttachmentsRecycler;
    private TextInputLayout mInputThemeMessage, mInputBodyMessage;
    private Settings mSettings;
    private FloatingActionButton mSendButton;
    private AccountItem mAccount;


    public static Email_Fragment newInstance(){
        return new Email_Fragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK && requestCode == Values.REQUEST_CODE_SELECT_EMAIL_ATTACHMENT){
            if (data!=null){
                mAttachmentList.add(data.getData().getPath());
                mSettings.setAttachments(mAttachmentList);
                mAdapterAttachments.notifyItemInserted(mAttachmentList.size());
            }
        }
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.layout_email_fragment,container,false);
        mContext = rootView.getContext();
        mSettings = new Settings(mContext);

        mAccountImage = (ImageView)rootView.findViewById(R.id.email_fragment_account_information_image);
        mAddRecipient = (ImageView)rootView.findViewById(R.id.email_fragment_add_recipient);
        mAddAttachment = (ImageView) rootView.findViewById(R.id.email_fragment_add_attachment);

        mInputThemeMessage = (TextInputLayout)rootView.findViewById(R.id.email_fragment_input_message_theme);
        mInputBodyMessage = (TextInputLayout)rootView.findViewById(R.id.email_fragment_input_message_body);

        mSendButton = (FloatingActionButton)rootView.findViewById(R.id.email_fragment_send_fab);

        TextView mAccountName = (TextView)rootView.findViewById(R.id.email_fragment_account_information_name);
        TextView mAccountEmail = (TextView)rootView.findViewById(R.id.email_fragment_account_information_email);

        mAccount = new DataBaseAccount(mContext).getAccount(mSettings.getActiveAccountID());
        if (mAccount!=null){
            mAccountName.setText(mAccount.getLastname());
            mAccountEmail.setText(mAccount.getEmail());
            new LoadImageFromWeb(mAccount.getPhoto(), this).execute();
        }else{
            mAccountName.setText(getResources().getString(R.string.email_fragment_logon));
            mAccountEmail.setText(Values.EMPTY);
            mAccountImage.setImageDrawable(null);
        }

        mInputThemeMessage.getEditText().setText(mSettings.getMessageTheme());
        mInputBodyMessage.getEditText().setText(mSettings.getMessageBody());

        mInputBodyMessage.getEditText().addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
            }
            @Override
            public void afterTextChanged(Editable s) {
                mSettings.setMessageBody(s.toString());
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
                mSettings.setMessageTheme(s.toString());
            }
        });

        mAddRecipient.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mRecepientList.add(ADD_NEW_RECIPIENT);
                mSettings.setRecepients(mRecepientList);
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
                startActivityForResult(intentAddAttachment, Values.REQUEST_CODE_SELECT_EMAIL_ATTACHMENT);
            }
        });

        mRecepientList = mSettings.getRecepients();
        checkSendButtonVisibility();
        mAttachmentList = mSettings.getAttachments();

        mAdapterRecipients = new adapter_email_attach(mContext, this, adapter_email_attach.RECIPIENTS, mRecepientList);
        mAdapterAttachments = new adapter_email_attach(mContext, this, adapter_email_attach.ATTACHMENTS, mAttachmentList);

        mRecipientRecycler = (RecyclerView)rootView.findViewById(R.id.fragment_email_recipients_recycler);
        mRecipientRecycler.setItemAnimator(new DefaultItemAnimator());
        mRecipientRecycler.setLayoutManager(/*new RecyclerWrapContentHeightManager(mContext, LinearLayoutManager.VERTICAL, false)*/new LinearLayoutManager(mContext));
        mRecipientRecycler.setAdapter(mAdapterRecipients);

        mAttachmentsRecycler = (RecyclerView)rootView.findViewById(R.id.email_fagment_attachments_recycler);
        mAttachmentsRecycler.setLayoutManager(/*new RecyclerWrapContentHeightManager(mContext, LinearLayoutManager.VERTICAL, false)*/new LinearLayoutManager(mContext));
        mAttachmentsRecycler.setAdapter(mAdapterAttachments);

        mSendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Send_Email(mContext, Send_Email.DIALOG_ENABLED)
                        .execute(new MailParams()
                        .setTheme(mSettings.getMessageTheme())
                        .setBody(mSettings.getMessageBody())
                        .setAttachments(mAttachmentList)
                        .setRecepients(mRecepientList));
            }
        });

        return rootView;
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
        mSettings.setRecepients(mRecepientList);
        mAdapterRecipients.notifyItemRemoved(position);
        checkSendButtonVisibility();
    }


    @Override
    public void onAddRecepient(View v, int position, int view_id) {
        if (view_id == R.id.card_email_add_new_recepient_save){
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
        mSettings.setAttachments(mAttachmentList);
        mAdapterAttachments.notifyItemRemoved(position);
    }
}
