package com.example.ivsmirnov.keyregistrator.async_tasks;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.ivsmirnov.keyregistrator.activities.Launcher;
import com.example.ivsmirnov.keyregistrator.databases.DataBaseAccount;
import com.example.ivsmirnov.keyregistrator.items.AccountItem;
import com.example.ivsmirnov.keyregistrator.items.MailParams;
import com.example.ivsmirnov.keyregistrator.others.Settings;
import com.google.android.gms.auth.GoogleAuthException;
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
import javax.mail.MessagingException;
import javax.mail.Multipart;
import javax.mail.NoSuchProviderException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

/**
 * Created by IVSmirnov on 26.08.2015.
 */
public class Send_Email extends AsyncTask<MailParams, Void, Void> {

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
            mProgressDialog.setMessage("Отправка сообщения");
            mProgressDialog.show();
        }
    }

    @Override
    protected void onPostExecute(Void aVoid) {
        if (mProgressDialog!=null && mProgressDialog.isShowing()){
            mProgressDialog.cancel();
        }
    }

    @Override
    protected Void doInBackground(MailParams... params) {

        try {

            AccountItem accountItem = DataBaseAccount.getAccount(Settings.getActiveAccountID());

            String token = GoogleAuthUtil.getToken(mContext, accountItem.getEmail(),"oauth2:https://mail.google.com/");
            String mTheme = params[0].getTheme();
            String mBody = params[0].getBody();
            ArrayList<String> mAttachments = params[0].getAttachments();

            Properties props = new Properties();
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.auth.mechanisms", "XOAUTH2");
            Session session = Session.getInstance(props);

            MimeMessage mimeMessage = new MimeMessage(session);
            DataHandler handler = new DataHandler(new ByteArrayDataSource(mBody.getBytes(), "multipart/mixed"));

            mimeMessage.setSubject(mTheme);
            mimeMessage.setDataHandler(handler);

            for (String recepient : params[0].getRecepients()){
                mimeMessage.addRecipient(Message.RecipientType.TO, new InternetAddress(recepient));
            }

            MimeBodyPart mimeBodyPartText = new MimeBodyPart();
            mimeBodyPartText.setText(mBody);

            Multipart multipart = new MimeMultipart();
            multipart.addBodyPart(mimeBodyPartText);
            if (mAttachments.size()!=0){
                for (String s : mAttachments){
                    MimeBodyPart mimeBodyPartAttach = new MimeBodyPart();
                    mimeBodyPartAttach.attachFile(new File(s));
                    mimeBodyPartAttach.setHeader("Content-Type", "text/plain; charset=\"us-ascii\"; name=\"mail.txt\"");
                    multipart.addBodyPart(mimeBodyPartAttach);
                }
            }

            mimeMessage.setContent(multipart);

            addMailCap();

            Transport transport = session.getTransport("smtp");
            transport.connect("imap.gmail.com",accountItem.getEmail(),token);
            transport.sendMessage(mimeMessage,mimeMessage.getAllRecipients());
            transport.close();

        } catch (Exception e) {
            e.printStackTrace();
        }

        return null;
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

        public ByteArrayDataSource(byte[] data) {
            super();
            this.data = data;
        }

        public void setType(String type) {
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
