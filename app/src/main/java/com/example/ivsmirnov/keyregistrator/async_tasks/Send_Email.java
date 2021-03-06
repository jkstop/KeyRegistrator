package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.widget.Toast;

import com.example.ivsmirnov.keyregistrator.R;
import com.example.ivsmirnov.keyregistrator.others.App;
import com.example.ivsmirnov.keyregistrator.others.SharedPrefs;
import com.google.android.gms.auth.GoogleAuthUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * отправка сообщения
 */
public class Send_Email extends AsyncTask<Void, Void, Exception> {

    public static final boolean DIALOG_ENABLED = true;
    public static final boolean DIALOG_DISABLED = false;

    private Context mContext;
    private boolean isDialogShow;
    private ProgressDialog mProgressDialog;

    public Send_Email(Context c, boolean isDialogShow) {
        this.mContext = c;
        this.isDialogShow = isDialogShow;
    }

    @Override
    protected void onPreExecute() {
        if (isDialogShow){
            mProgressDialog = new ProgressDialog(mContext);
            mProgressDialog.setCancelable(false);
            mProgressDialog.setMessage(mContext.getString(R.string.email_sending));
            mProgressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Exception e) {
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }

        if (e == null){
            Toast.makeText(mContext, mContext.getString(R.string.email_sending_success), Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(mContext, mContext.getString(R.string.email_sending_error), Toast.LENGTH_SHORT).show();
        }

    }

    @Override
    protected Exception doInBackground(Void... params) {

        try {
            String token = GoogleAuthUtil.getToken(mContext, SharedPrefs.getActiveAccountEmail(),"oauth2:https://mail.google.com/");
            String mTheme = mContext.getString(R.string.email_theme);
            String mBody = mContext.getString(R.string.email_message) + SharedPrefs.showDate();
            InternetAddress from = new InternetAddress(SharedPrefs.getActiveAccountEmail(), App.getAppContext().getResources().getString(R.string.app_name));
            ArrayList<String> mAttachments = SharedPrefs.getAttachments();

            Properties mProps = new Properties();
            mProps.put("mail.smtp.ssl.enable", "true");
            mProps.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            Session session = Session.getInstance(mProps);

            MimeMessage mMimeMessage = new MimeMessage(session);

            if (mBody == null) mBody = "";
            DataHandler mHandler = new DataHandler(new ByteArrayDataSource(mBody.getBytes(), "multipart/mixed"));

            mMimeMessage.setSubject(mTheme);
            mMimeMessage.setFrom(from);
            mMimeMessage.setDataHandler(mHandler);

            for (String recepient : SharedPrefs.getRecepients()){
                mMimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            }

            MimeBodyPart mMimeBodyPart = new MimeBodyPart();
            mMimeBodyPart.setText(mBody);

            Multipart mMultiPart = new MimeMultipart();
            mMultiPart.addBodyPart(mMimeBodyPart);
            if (mAttachments.size()!=0){
                for (String s : mAttachments){
                    MimeBodyPart mMimeBodyPartAttach = new MimeBodyPart();
                    mMimeBodyPartAttach.attachFile(new File(s));
                    mMimeBodyPartAttach.setHeader("Content-Type", "text/plain; charset=\"us-ascii\"; name=\"mail.txt\"");
                    mMultiPart.addBodyPart(mMimeBodyPartAttach);
                }
            }

            mMimeMessage.setContent(mMultiPart);

            addMailCap();

            Transport mTransport = session.getTransport("smtp");
            mTransport.connect("imap.gmail.com", SharedPrefs.getActiveAccountEmail(), token);
            mTransport.sendMessage(mMimeMessage, mMimeMessage.getAllRecipients());
            mTransport.close();
            return null;
        } catch (Exception e) {
            e.printStackTrace();
            return e;
        }
    }

    private void addMailCap() {
        MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
        mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
        mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
        mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
        mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
        mc.addMailcap("message/rfc822;; x-java-content-handler=com.sun.mail.handlers.message_rfc822");
        CommandMap.setDefaultCommandMap(mc);
    }

    public class ByteArrayDataSource implements DataSource {
        private byte[] data;
        private String type;

        public ByteArrayDataSource(byte[] data, String type) {
            super();
            this.data = data;
            this.type = type;
        }

        public String getContentType() {
            if (type == null)
                return "application/octet-stream";
            else
                return type;
        }

        public InputStream getInputStream() throws IOException {
            return new ByteArrayInputStream(data);
        }

        public String getName() {
            return "ByteArrayDataSource";
        }

        public OutputStream getOutputStream() throws IOException {
            throw new IOException("Not Supported");
        }
    }


}
