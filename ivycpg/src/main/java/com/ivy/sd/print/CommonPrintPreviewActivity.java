package com.ivy.sd.print;

import android.annotation.TargetApi;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import androidx.core.content.FileProvider;
import androidx.appcompat.widget.Toolbar;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.fragment.app.FragmentManager;

import com.aem.api.AEMPrinter;
import com.aem.api.AEMScrybeDevice;
import com.aem.api.IAemScrybe;
import com.bixolon.printer.BixolonPrinter;
import com.itextpdf.text.BadElementException;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import com.ivy.cpg.view.collection.CollectionHelper;
import com.ivy.cpg.view.order.OrderHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.sd.png.asean.view.BuildConfig;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.sd.png.util.StandardListMasterConstants;
import com.ivy.cpg.view.collection.CollectionScreen;
import com.ivy.sd.png.view.EmailDialog;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.DateTimeUtils;
import com.zebra.sdk.comm.BluetoothConnection;
import com.zebra.sdk.comm.Connection;
import com.zebra.sdk.comm.ConnectionException;
import com.zebra.sdk.graphics.ZebraImageFactory;
import com.zebra.sdk.graphics.ZebraImageI;
import com.zebra.sdk.printer.ZebraPrinter;
import com.zebra.sdk.printer.ZebraPrinterFactory;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.activation.CommandMap;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.activation.FileDataSource;
import javax.activation.MailcapCommandMap;
import javax.mail.BodyPart;
import javax.mail.PasswordAuthentication;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;

import mmsl.DeviceUtility.DeviceBluetoothCommunication;
import mmsl.DeviceUtility.DeviceCallBacks;

public class CommonPrintPreviewActivity extends IvyBaseActivityNoActionBar implements DeviceCallBacks, EmailDialog.onSendButtonClickListnor {
    private TextView mPrinterStatusTV;
    private Spinner mPrintCountSpinner;
    private TextView mPreviewTV;
    private ImageView mDistLogoIV, imageView_signature;
    private ArrayAdapter<CharSequence> mSpinnerAdapter;

    private BluetoothAdapter mBluetoothAdapter;

    private BixolonPrinter bixolonPrinterApi = null;

    private Connection zebraPrinterConnection;

    private BusinessModel bmodel;

    private int mPrintCount;
    private int mPrintCountInput = 1;

    private boolean zebraPrinter = false;
    private boolean bixPrinter = true;

    private int mImagePrintCount = 0;
    private int mDataPrintCount = 0;
    private int mTotalNumbersPrinted = 0;

    private boolean isFromOrder;
    private boolean isFromCollection;
    private boolean isUpdatePrintCount;
    private boolean isHomeBtnEnable;
    private boolean isPrintClicked;
    private boolean isHidePrintBtn;
    private boolean isFromInvoice;
    private boolean isFromEOD;
    private boolean isFromVanUnload;
    private int widthImage, heightImage;
    private String PRINT_STATE = "";
    private Toolbar toolbar;
    Bitmap screen;
    private OrderHelper orderHelper;
    DeviceBluetoothCommunication bluetoothCommunication;
    CommonDialog commonDialog;
    private String sendMailAndLoadClass;

    private static final UUID SPP_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_common_print_preview);


        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);
        orderHelper = OrderHelper.getInstance(this);

        mPrinterStatusTV = (TextView) findViewById(R.id.printer_status);
        mPrintCountSpinner = (Spinner) findViewById(R.id.print_count);
        mDistLogoIV = (ImageView) findViewById(R.id.dist_logo);
        imageView_signature = findViewById(R.id.imageView_signature);
        mPreviewTV = (TextView) findViewById(R.id.preView);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        mPreviewTV.setTypeface(Typeface.createFromAsset(getAssets(), "font/consola.ttf"));


        isFromOrder = getIntent().getExtras().getBoolean("IsFromOrder", false);
        isUpdatePrintCount = getIntent().getExtras().getBoolean("IsUpdatePrintCount", false);
        isHomeBtnEnable = getIntent().getExtras().getBoolean("isHomeBtnEnable", false);
        isFromCollection = getIntent().getExtras().getBoolean("isFromCollection", false);
        isHidePrintBtn = getIntent().getExtras().getBoolean("isHidePrintBtn", false);
        isFromInvoice = getIntent().getExtras().getBoolean("isFromInvoice", false);
        isFromEOD = getIntent().getExtras().getBoolean("isFromEOD", false);
        sendMailAndLoadClass = getIntent().getExtras().getString("sendMailAndLoadClass");
        isFromVanUnload = getIntent().getExtras().getBoolean("isFromVanUnload", false);
        if (isHomeBtnEnable) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            setScreenTitle(getResources().getString(R.string.print_preview));
        }

        widthImage = bmodel.mCommonPrintHelper.width_image;
        heightImage = bmodel.mCommonPrintHelper.height_image;

        onScreenPreparation();

        if (bmodel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {
            try {

                BluetoothAdapter mBluetoothAdapter = BluetoothAdapter
                        .getDefaultAdapter();
                BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(getMacAddressFieldText().toUpperCase());
                bluetoothCommunication = new DeviceBluetoothCommunication();
                bluetoothCommunication.StartConnection(device, this);
                mPrinterStatusTV.setText("Connected");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_print_preview, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (!bmodel.configurationMasterHelper.IS_SHARE_INVOICE) {
            menu.findItem(R.id.menu_share_pdf).setVisible(false);
        }
        if (isHidePrintBtn) {
            menu.findItem(R.id.menu_print).setVisible(false);
        }

        if (isHidePrintBtn) {
            menu.findItem(R.id.menu_print).setVisible(false);
        }

        if (!bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
            menu.findItem(R.id.menu_email_print).setVisible(false);
        }

        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                if (isFromOrder) {
                    bmodel.productHelper.clearOrderTable();
                    bmodel.setOrderHeaderBO(null);

                    if (bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
                        bmodel.resetSRPvalues();
                    }
                    Intent i;
                    if (getIntent().getStringExtra("From") == null) {
                        i = new Intent(
                                CommonPrintPreviewActivity.this,
                                HomeScreenTwo.class);
                        Bundle extras = getIntent().getExtras();
                        if (extras != null) {
                            i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                            i.putExtra("CurrentActivityCode", OrderSummary.mCurrentActivityCode);
                        }
                    } else {
                        i = new Intent(CommonPrintPreviewActivity.this, HomeScreenActivity.class);
                    }
                    startActivity(i);
                } else if (isFromCollection) {
                    CollectionHelper.getInstance(CommonPrintPreviewActivity.this).downloadCollectionMethods();

                    Intent intent = new Intent(CommonPrintPreviewActivity.this,
                            CollectionScreen.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                    intent.putExtra("screentitle", "MENU_COLLECTION");
                    startActivity(intent);
                }
                finish();
                return true;
            case R.id.menu_print:
                if (!isPrintClicked) {
                    isPrintClicked = true;
                    callPrinter();
                }
                break;
            case R.id.menu_share_pdf:

                try {
                    LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
                    RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_common_print_preview, null); //RelativeLayout is root view of my UI(xml) file.
                    root.setDrawingCacheEnabled(true);
                    screen = getBitmapFromView(this.getWindow().findViewById(R.id.root_print));
                    new CreatePdf().execute();
                } catch (Exception ex) {
                    Commons.printException(ex);
                }
                break;
            case R.id.menu_email_print:
                try {
                    prepareEmailData();
                } catch (Exception e) {

                }
                break;
        }
        return false;
    }


    private void callPrinter() {
        if (isHomeBtnEnable)
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        if (bmodel.configurationMasterHelper.COMMON_PRINT_ZEBRA || bmodel.configurationMasterHelper.SHOW_ZEBRA_TITAN
                || bmodel.configurationMasterHelper.SHOW_ZEBRA_UNIPAL) {
            new Print().execute("1");
        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_BIXOLON) {
            doConnectionBixolon();
        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_SCRYBE) {
            doConnectionScrybe();
        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_LOGON) {
            new Print().execute("2");
        } else if (bmodel.configurationMasterHelper.COMMON_PRINT_MAESTROS)
            doMaestroPrintNew();
        else if (bmodel.configurationMasterHelper.COMMON_PRINT_INTERMEC) {
            new Print().execute("3");
        }
    }

    private class CreatePdf extends AsyncTask<Integer, Integer, Boolean> {
        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(CommonPrintPreviewActivity.this);
            customProgressDialog(builder, getResources().getString(R.string.preparing_pdf));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected Boolean doInBackground(Integer... params) {

            return createPdf(StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber
            );
        }

        @Override
        protected void onPostExecute(Boolean result) {
            alertDialog.dismiss();

            try {
                if (result) {

                    Intent sharingIntent = new Intent(Intent.ACTION_SEND_MULTIPLE);
                    ArrayList<Uri> uriList = new ArrayList<>();
                    File newFile = new File(Environment.getExternalStorageDirectory().getPath() + "/IvyInvoice/"
                            , StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber + ".pdf");

                    if (Build.VERSION.SDK_INT >= 24) {
                        uriList.add(FileProvider.getUriForFile(CommonPrintPreviewActivity.this, BuildConfig.APPLICATION_ID + ".provider", newFile));

                    } else {
                        uriList.add(Uri.fromFile(newFile));
                    }

                    sharingIntent.putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList);
                    sharingIntent.setType("application/pdf");
                    sharingIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                    sharingIntent.addFlags(Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                    startActivity(Intent.createChooser(sharingIntent, getResources().getString(R.string.share_invoice_using)));

                } else {
                    Toast.makeText(CommonPrintPreviewActivity.this, getResources().getString(R.string.error_in_creating_pdf), Toast.LENGTH_LONG).show();
                }
            } catch (Exception ex) {
                Commons.printException(ex);
            }

        }

    }


    private void prepareEmailData() {

        if (bmodel.mCommonPrintHelper.getInvoiceData().toString().length() > 0) {

            if (bmodel.configurationMasterHelper.IS_ORDER_SUMMERY_EXPORT_AND_EMAIL) {
                FragmentManager ft = getSupportFragmentManager();
                EmailDialog dialog = new EmailDialog(CommonPrintPreviewActivity.this, bmodel.getRetailerMasterBO().getEmail());
                dialog.setCancelable(false);
                dialog.show(ft, "MENU_STK_ORD");
            }
        } else {
            Toast.makeText(bmodel, "No data to store", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void setEmailAddress(String value) {
        new SendMail(this, orderHelper.getOrderId() != null && !orderHelper.getOrderId().equals("") ? "OrderId-" + orderHelper.getOrderId() + " " + bmodel.getRetailerMasterBO().getRetailerName() : "Read", "Test", value).execute();
    }


    public class SendMail extends AsyncTask<Void, Void, Boolean> {

        // private final String emailId = "";//Change this field value
        Session session;
        Context mContext;
        ProgressDialog progressDialog;
        private String subject;
        private String body;
        private String additionalEmail;

        public SendMail(Context ctx, String subject, String message, String additionalEmail) {
            this.mContext = ctx;

            this.subject = subject;
            this.body = message;
            this.additionalEmail = additionalEmail;

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            progressDialog = ProgressDialog.show(mContext, getResources().getString(R.string.sending_email), getResources().getString(R.string.please_wait_some_time), false);
        }

        @Override
        protected Boolean doInBackground(Void... voids) {

            HashMap<String, String> mUserCredentials = bmodel.downloadEmailAccountCredentials();
            final String emailId = mUserCredentials.get("EMAILID");
            final String password = mUserCredentials.get("PASSWORD");
            final String type = mUserCredentials.get("TYPE");

            Properties props = System.getProperties();// new Properties();

            if(type.equalsIgnoreCase("office365")) {
                //Properties for Office365
                props.put("mail.smtp.host", "smtp.office365.com");
            } else {
                //Configuring properties for GMAIL
                props.put("mail.smtp.host", "smtp.gmail.com");
                props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
            }

            props.put("mail.smtp.socketFactory.port", "587");
            props.put("mail.smtp.auth", "true");
            props.put("mail.smtp.port", "587");
            props.put("mail.smtp.starttls.enable", "true");

            //Creating a new session
            session = Session.getDefaultInstance(props,
                    new javax.mail.Authenticator() {
                        //Authenticating the password
                        protected PasswordAuthentication getPasswordAuthentication() {
                            return new PasswordAuthentication(emailId, password);
                        }
                    });

            try {
                javax.mail.Message message = new MimeMessage(session);
                message.setFrom(new InternetAddress(emailId));
                if (!TextUtils.isEmpty(bmodel.getRetailerMasterBO().getEmail()) && isValidEmail(bmodel.getRetailerMasterBO().getEmail())) {
                    if (!additionalEmail.isEmpty() && isValidEmail(additionalEmail)) {
                        InternetAddress[] recipientAddress = new InternetAddress[2];
                        recipientAddress[0] = new InternetAddress(bmodel.getRetailerMasterBO().getEmail().trim());
                        recipientAddress[1] = new InternetAddress(additionalEmail.trim());
                        message.setRecipients(javax.mail.Message.RecipientType.TO, recipientAddress);
                    } else if (!additionalEmail.isEmpty() && !isValidEmail(additionalEmail)) {
                        return false;
                    } else
                        message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(bmodel.getRetailerMasterBO().getEmail().trim()));

                } else if (!additionalEmail.isEmpty() && isValidEmail(additionalEmail))
                    message.setRecipient(javax.mail.Message.RecipientType.TO, new InternetAddress(additionalEmail));
                else {
                    return false;
                }


                message.setSubject(subject);
                message.setText(body);
                //  mm.setContent(message,"text/html; charset=utf-8");

                BodyPart bodyPart = new MimeBodyPart();
                bodyPart.setText(body);
                //Attachment
                DataSource source;
                if (bmodel.configurationMasterHelper.IS_ATTACH_PDF) {
                    // LayoutInflater inflater = (LayoutInflater) CommonPrintPreviewActivity.this.getSystemService(LAYOUT_INFLATER_SERVICE);
                    // RelativeLayout root = (RelativeLayout) inflater.inflate(R.layout.activity_common_print_preview, null); //RelativeLayout is root view of my UI(xml) file.
                    // root.setDrawingCacheEnabled(true);
                    screen = getBitmapFromView(CommonPrintPreviewActivity.this.getWindow().findViewById(R.id.root_print));
                    createPdf(StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber);
                   /* File newFile = new File(Environment.getExternalStorageDirectory().getPath() + "/IvyInvoice/"
                            , StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber + ".pdf");*/
                    source = new FileDataSource(Environment.getExternalStorageDirectory().getPath() + "/" + "IvyInvoice" + "/" +
                            StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber + ".pdf");
                    bodyPart.setDataHandler(new DataHandler(source));
                    bodyPart.setFileName("OrderDetails" + ".pdf");
                } else {
                    if (sendMailAndLoadClass.equalsIgnoreCase("PRINT_FILE_ORDER") ||
                            sendMailAndLoadClass.equalsIgnoreCase("HomeScreenTwoPRINT_FILE_ORDER")) {
                        source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                                StandardListMasterConstants.PRINT_FILE_ORDER + orderHelper.getOrderId().replaceAll("\'", "") + ".txt");
                        bodyPart.setDataHandler(new DataHandler(source));
                        bodyPart.setFileName("OrderDetails" + ".txt");
                    }
                    if (sendMailAndLoadClass.equalsIgnoreCase("PRINT_FILE_INVOICE")) {
                        source = new FileDataSource(getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.IVYDIST_PATH + "/" +
                                StandardListMasterConstants.PRINT_FILE_INVOICE + bmodel.invoiceNumber + ".txt");
                        bodyPart.setDataHandler(new DataHandler(source));
                        bodyPart.setFileName("InvoiceDetails" + ".txt");
                    }
                }


                MimeMultipart multiPart = new MimeMultipart();
                multiPart.addBodyPart(bodyPart);
                message.setContent(multiPart);

                Thread.currentThread().setContextClassLoader(getClassLoader());

                MailcapCommandMap mc = (MailcapCommandMap) CommandMap.getDefaultCommandMap();
                mc.addMailcap("text/html;; x-java-content-handler=com.sun.mail.handlers.text_html");
                mc.addMailcap("text/xml;; x-java-content-handler=com.sun.mail.handlers.text_xml");
                mc.addMailcap("text/plain;; x-java-content-handler=com.sun.mail.handlers.text_plain");
                mc.addMailcap("multipart/*;; x-java-content-handler=com.sun.mail.handlers.multipart_mixed");
                mc.addMailcap("message/rfc822;; x-java-content- handler=com.sun.mail.handlers.message_rfc822");

                //sending mail
                Transport.send(message);

            } catch (Exception ex) {
                Commons.printException(ex);
                return false;
            }

            return true;
        }

        @Override
        protected void onPostExecute(Boolean isSent) {
            super.onPostExecute(isSent);

            progressDialog.dismiss();

            if (isSent) {
                Toast.makeText(CommonPrintPreviewActivity.this, getResources().getString(R.string.email_sent),
                        Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(CommonPrintPreviewActivity.this, getResources().getString(R.string.error_in_sending_email),
                        Toast.LENGTH_SHORT).show();
            }

        }
    }


    public boolean createPdf(String pdfFileName) {

        try {

            if (bmodel.isExternalStorageAvailable()) {
                File folder;
                folder = new File(Environment.getExternalStorageDirectory().getPath()
                        + "/IvyInvoice/");
                if (!folder.exists()) {
                    folder.mkdir();
                }

                String path = folder + "";
                File SDPath = new File(path);
                if (!SDPath.exists()) {
                    SDPath.mkdir();
                }


              /*  Document document = new Document(PageSize.A4);
                PdfWriter.getInstance(document, new FileOutputStream(path + "/" + pdfFileName + ".pdf"));
                document.open();
                Font normal = new Font(Font.FontFamily.TIMES_ROMAN, 12);
                Paragraph p = new Paragraph(content, normal);
                document.add(p);
                document.close();*/


                //Create a directory for your PDF
                File f = new File(path, (pdfFileName).replace("'", "") + ".pdf");

                Document document = new Document();
                PdfWriter.getInstance(document, new FileOutputStream(f));
                document.open();
                ByteArrayOutputStream stream = new ByteArrayOutputStream();
                screen.compress(Bitmap.CompressFormat.PNG, 80, stream);
                byte[] byteArray = stream.toByteArray();
                addImage(document, byteArray);
                document.close();


            }

        } catch (Exception ex) {
            Commons.printException(ex);
            return false;
        }
        return true;
    }

    public Bitmap getBitmapFromView(View view) {
        //Define a bitmap with the same size as the view
        Bitmap returnedBitmap = Bitmap.createBitmap(view.getWidth(), view.getHeight(), Bitmap.Config.ARGB_8888);
        //Bind a canvas to it
        Canvas canvas = new Canvas(returnedBitmap);
        //Get the view's background
        Drawable bgDrawable = view.getBackground();
        if (bgDrawable != null)
            //has background drawable, then draw it on the canvas
            bgDrawable.draw(canvas);
        else
            //does not have background drawable, then draw white background on the canvas
            canvas.drawColor(Color.WHITE);
        // draw the view on the canvas
        view.draw(canvas);
        //return the bitmap
        return returnedBitmap;
    }

    private static void addImage(Document document, byte[] byteArray) {
        Image image = null;
        try {
            image = Image.getInstance(byteArray);
        } catch (BadElementException e) {
            Commons.print("exception => " + e);
        } catch (MalformedURLException e) {
            Commons.print("exception => " + e);
        } catch (IOException e) {
            Commons.print("exception => " + e);
        }
        try {
            image.setAlignment(Image.MIDDLE);
            image.scaleToFit(PageSize.A4.getWidth(), PageSize.A4.getHeight());
            image.setAbsolutePosition((float) ((document.getPageSize().getWidth() / 2) - (image.getScaledWidth() / 2)), 0);
            document.add(image);
        } catch (DocumentException e) {
            Commons.print("exception => " + e);
        }
    }

    private void onScreenPreparation() {
        try {

            mPrintCount = orderHelper.getPrint_count();

            mSpinnerAdapter = new ArrayAdapter<CharSequence>(this,
                    android.R.layout.simple_spinner_item);
            mSpinnerAdapter
                    .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
            if (bmodel.configurationMasterHelper.printCount > 1) {
                for (int i = 1; i <= bmodel.configurationMasterHelper.printCount; ++i)
                    mSpinnerAdapter.add(i + "");
                mPrintCountSpinner.setAdapter(mSpinnerAdapter);
            } else {
                mPrintCountSpinner.setVisibility(View.GONE);
            }

            if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT) {
                mPrintCountSpinner.setVisibility(View.GONE);
            }

            mPrintCountSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view,
                                           int pos, long id) {
                    mPrintCountInput = SDUtil.convertToInt((String) parent.getSelectedItem());
                }

                @Override
                public void onNothingSelected(AdapterView<?> arg0) {

                }

            });

            if (bmodel.mCommonPrintHelper.isLogoEnabled) {
                //InputStream inputStream = getAssets().open("logo.9.png");
                Bitmap bmp = BitmapFactory.decodeStream(getLogoIS());
                mDistLogoIV.setImageBitmap(bmp);
            }

            if (bmodel.mCommonPrintHelper.isSignatureEnabled
                    && bmodel.mCommonPrintHelper.signatureName != null
                    && !bmodel.mCommonPrintHelper.signatureName.equals("")) {
                imageView_signature.setVisibility(View.VISIBLE);
                Bitmap bmp = BitmapFactory.decodeStream(getSignature());
                imageView_signature.setImageBitmap(bmp);
            }


            mPreviewTV.setText(bmodel.mCommonPrintHelper.getInvoiceData().toString()
                    .replace("#B#", "")
                    .replace("print_type", "")
                    .replace("print_sub_type", "")
                    .replace("print_no", "")
                    .replace("print_title", "")
                    .replace("duplicate_print_count", ""));

        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    public void updateStatus(final String statusMessage) {
        try {
            runOnUiThread(new Runnable() {
                public void run() {
                    mPrinterStatusTV.setText(statusMessage);
                }
            });
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private InputStream getLogoIS() {
        InputStream xmlFile = null;
        try {
            File f = new File(
                    getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                            + "/"
                            + bmodel.userMasterHelper.getUserMasterBO()
                            .getUserid() + "APP");
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith("client_banner");
                    }
                });

                for (File temp : files) {
                    xmlFile = new FileInputStream(temp);
                    break;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        return xmlFile;
    }

    private InputStream getSignature() {
        InputStream xmlFile = null;
        try {

            String path = getExternalFilesDir(Environment.DIRECTORY_PICTURES) + "/" + DataMembers.photoFolderName;

            File f = new File(path);
            if (f.isDirectory()) {
                File files[] = f.listFiles(new FilenameFilter() {
                    public boolean accept(File directory, String fileName) {
                        return fileName.startsWith(bmodel.mCommonPrintHelper.signatureName);
                    }
                });

                for (File temp : files) {
                    xmlFile = new FileInputStream(temp);
                    break;
                }
            }
        } catch (Exception e) {
            Commons.printException(e);
        }

        return xmlFile;
    }

    class Print extends AsyncTask<String, Void, Boolean> {
        protected void onPreExecute() {
            updateStatus("Connecting...");
        }

        @Override
        protected Boolean doInBackground(String... params) {
            if (params[0].equals("1"))
                doZebraPrintNew(getMacAddressFieldText());
            if (params[0].equals("2"))
                doLogonPrintNew(getMacAddressFieldText());
            if (params[0].equals("3"))
                doInterMecPrint(getMacAddressFieldText());

            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            showAlert();
        }

    }


    public String getMacAddressFieldText() {
        String macAddress = null;
        try {
            // String macAddress = "00:22:58:3A:CD:46";
            SharedPreferences pref = this.getSharedPreferences("PRINT",
                    MODE_PRIVATE);
            macAddress = pref.getString("MAC", "");
            SharedPreferences.Editor editor = pref.edit();
            editor.putString("MAC", macAddress);
            editor.commit();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return macAddress;
    }

    private void doZebraPrintNew(String macAddress) {
        ZebraPrinter zebraPrinter = null;
        //InputStream inputStream;
        ZebraImageI zebraImageI = null;
        ZebraImageI zebraSignatureImage = null;
        try {

            if (macAddress.equals("")) {
                updateStatus("Mac address is empty...");
                return;
            }

            zebraPrinterConnection = new BluetoothConnection(macAddress);
            zebraPrinterConnection.open();

            if (bmodel.mCommonPrintHelper.isLogoEnabled) {
                zebraPrinter = ZebraPrinterFactory.getInstance(zebraPrinterConnection);
                Bitmap bitmap = BitmapFactory.decodeStream(getLogoIS());
                if (bitmap != null) {
                    Bitmap resizeBitamp = Bitmap.createScaledBitmap(bitmap, widthImage, heightImage, false);
                    zebraImageI = ZebraImageFactory.getImage(resizeBitamp);
                }
            }

            if (bmodel.mCommonPrintHelper.isSignatureEnabled) {
                if (zebraPrinter == null)
                    zebraPrinter = ZebraPrinterFactory.getInstance(zebraPrinterConnection);

                Bitmap bitmap = BitmapFactory.decodeStream(getSignature());
                if (bitmap != null) {
                    Bitmap resizeBitMap = Bitmap.createScaledBitmap(bitmap, widthImage, heightImage, false);
                    zebraSignatureImage = ZebraImageFactory.getImage(resizeBitMap);
                }
            }

            if (zebraPrinterConnection.isConnected())
                updateStatus("Printing...");

            if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT)
                mPrintCountInput = 2;

            for (int i = 0; i < mPrintCountInput; i++) {
                if (zebraPrinterConnection.isConnected()) {
                    if (bmodel.mCommonPrintHelper.isLogoEnabled) {
                        zebraPrinterConnection.write("! UTILITIES\r\nIN-MILLIMETERS\r\nSETFF 1 0\r\nPRINT\r\n".getBytes());
                        //arg : image,x,y,width,height
                        // - default height and width will be taken if it mentioned as "-1"
                        if (zebraImageI != null) {
                            zebraPrinter.printImage(zebraImageI, 230, 0, -1, -1, false);
                        }
                    }

                    zebraPrinterConnection.write(getDataZebra());

                    if (bmodel.mCommonPrintHelper.isSignatureEnabled) {
                        zebraPrinterConnection.write("! UTILITIES\r\nIN-MILLIMETERS\r\nSETFF 1 0\r\nPRINT\r\n".getBytes());
                        //arg : image,x,y,width,height
                        // - default height and width will be taken if it mentioned as "-1"
                        if (zebraSignatureImage != null) {
                            zebraPrinter.printImage(zebraSignatureImage, 230, 0, -1, -1, false);
                        }
                    }

                    if (bmodel.mCommonPrintHelper.isLogoEnabled || bmodel.mCommonPrintHelper.isSignatureEnabled) {
                        mImagePrintCount++;

                    }

                    mDataPrintCount++;
                    if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT) {
                        if (i == mPrintCountInput - 1) {
                            mPrintCount++;
                            mTotalNumbersPrinted++;
                        }
                    } else {
                        mPrintCount++;
                        mTotalNumbersPrinted++;
                    }

                }
            }

            if (zebraPrinterConnection != null) {
                zebraPrinterConnection.close();
            }

        } catch (ConnectionException e) {
            Commons.printException(e);
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    private byte[] getDataInterMec() {

        try {

            StringBuilder tempsb = new StringBuilder();


            String[] lines = bmodel.mCommonPrintHelper.getInvoiceData().toString().split("\\r?\\n");
            for (String s : lines) {

                if (s.contains("print_type")) {
                    if (mPrintCount == 0) {
                        String primaryLabel = (bmodel.labelsMasterHelper.applyLabels("print_type_primary"));
                        s = s.replace("print_type", (primaryLabel != null ? primaryLabel : "Original"));
                    } else {
                        String secondaryLabel = bmodel.labelsMasterHelper.applyLabels("print_type_secondary");
                        s = s.replace("print_type", (secondaryLabel != null ? secondaryLabel : "Duplicate"));
                    }

                }

                if (s.contains("duplicate_print_count")) {
                    if (mPrintCount == 0)
                        s = s.replace("duplicate_print_count", "");
                    else
                        s = s.replace("duplicate_print_count", " " + mPrintCount);

                }

                if (s.contains("print_title")) {

                    int count;
                    if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT)
                        count = mDataPrintCount;
                    else
                        count = mPrintCount;

                    if (count == 0) {
                        String primaryLabel = (bmodel.labelsMasterHelper.applyLabels("print_title_primary"));
                        s = s.replace("print_title", (primaryLabel != null ? primaryLabel : ""));
                    } else {
                        String secondaryLabel = bmodel.labelsMasterHelper.applyLabels("print_title_secondary");
                        s = s.replace("print_title", (secondaryLabel != null ? secondaryLabel : ""));
                    }
                }

                if (s.contains("print_no")) {
                    s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
                }

                if (s.contains("#B#")) {
                    //Can't able to set bold in intermec, so just replacing with empty string
                    s = s.replace("#B#", "");
                    tempsb.append(s);
                    tempsb.append("\n\r");

                } else {
                    tempsb.append(s);
                    tempsb.append("\n\r");

                }
            }


            byte[] result;
            result = String.valueOf(tempsb).getBytes();


            return result;

        } catch (Exception e) {
            Commons.printException(e);
        }

        return new byte[0];

    }

    private byte[] getDataZebra() {

        try {

            StringBuilder tempsb = new StringBuilder();

            if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                tempsb.append("! U1 SETLP ANG12PT.CPF 0 26 \n");
            }

            String[] lines = bmodel.mCommonPrintHelper.getInvoiceData().toString().split("\\r?\\n");
            for (String s : lines) {


                if (s.contains("print_sub_type")) {

                    if (mDataPrintCount == 0) {
                        String primaryLabel = (bmodel.labelsMasterHelper.applyLabels("print_sub_type_primary"));
                        s = s.replace("print_sub_type", (primaryLabel != null ? primaryLabel : "Customer"));
                    } else {
                        String secondaryLabel = bmodel.labelsMasterHelper.applyLabels("print_sub_type_secondary");
                        s = s.replace("print_sub_type", (secondaryLabel != null ? secondaryLabel : "Company"));
                    }
                }

                if (s.contains("print_type")) {
                    if (mPrintCount == 0) {
                        String primaryLabel = (bmodel.labelsMasterHelper.applyLabels("print_type_primary"));
                        s = s.replace("print_type", (primaryLabel != null ? primaryLabel : "Original"));
                    } else {
                        String secondaryLabel = bmodel.labelsMasterHelper.applyLabels("print_type_secondary");
                        s = s.replace("print_type", (secondaryLabel != null ? secondaryLabel : "Duplicate"));
                    }

                }

                if (s.contains("duplicate_print_count")) {
                    if (mPrintCount == 0)
                        s = s.replace("duplicate_print_count", "");
                    else
                        s = s.replace("duplicate_print_count", " " + mPrintCount);

                }

                if (s.contains("print_title")) {

                    int count;
                    if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT)
                        count = mDataPrintCount;
                    else
                        count = mPrintCount;

                    if (count == 0) {
                        String primaryLabel = (bmodel.labelsMasterHelper.applyLabels("print_title_primary"));
                        s = s.replace("print_title", (primaryLabel != null ? primaryLabel : ""));
                    } else {
                        String secondaryLabel = bmodel.labelsMasterHelper.applyLabels("print_title_secondary");
                        s = s.replace("print_title", (secondaryLabel != null ? secondaryLabel : ""));
                    }
                }

                if (s.contains("print_no")) {
                    s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
                }

                if (s.contains("#B#")) {
                    String str = s.replace("#B#", "");
                    int spaceCount = 0;
                    for (char c : str.toCharArray()) {
                        if (c == ' ') {
                            spaceCount++;
                        } else {
                            break;
                        }
                    }

                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                        tempsb.append("! U1 SETBOLD 1");
                        tempsb.append(str.replaceAll(" ", "  ").replaceAll("--", "----").replaceAll(",", ", ").replaceAll("\\.", ". "));
                        tempsb.append("! U1 SETBOLD 0");
                        tempsb.append("\n\r");
                    } else {
                        tempsb.append("! 0 200 200 " + 25 + " 1\r\n" + "LEFT\r\n");
                        tempsb.append("T 5 0 " + (spaceCount * 12) + " 1 " + str.substring(spaceCount, str.length()) + "\r\n");
                        tempsb.append("PRINT\r\n");
                    }

                } else {
                    if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                        tempsb.append(s.replaceAll(" ", "  ").replaceAll("--", "----").replaceAll(",", ", ").replaceAll("\\.", ". "));
                        tempsb.append("\n\r");
                    } else {
                        tempsb.append(s);
                        tempsb.append("\n\r");
                    }
                }
            }

            byte[] result;
            if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
                result = String.valueOf(tempsb).getBytes("ISO-8859-11");
            } else {
                result = String.valueOf(tempsb).getBytes();
            }


            return result;

        } catch (Exception e) {
            // TODO Auto-generated catch block
            Commons.printException(e);
        }

        return new byte[0];

    }


    private void doLogonPrintNew(String macAddress) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice mBluetoothDevice = null;
        BluetoothSocket mBluetoothSocket = null;
        OutputStream mOutputStream = null;

        try {
            if (macAddress.equals(""))
                updateStatus("Mac address is empty...");

            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(getReverseofMacAddress(macAddress));
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            updateStatus("Printing...");

            for (int i = 0; i < mPrintCountInput; i++) {
                mOutputStream = mBluetoothSocket.getOutputStream();
                mOutputStream.write((bmodel.mCommonPrintHelper.getInvoiceData().toString()).getBytes("GBK"));
                mOutputStream.flush();
                mDataPrintCount++;
                mPrintCount++;
                mTotalNumbersPrinted++;
            }
            mOutputStream.close();
            mBluetoothSocket.close();

        } catch (Exception e) {
            Commons.printException(e);
            updateStatus("Connection Failed");
        }
    }

    private void doInterMecPrint(String macAddress) {
        BluetoothAdapter mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
        BluetoothDevice mBluetoothDevice = null;
        BluetoothSocket mBluetoothSocket = null;
        OutputStream mOutputStream = null;
        try {
            if (macAddress.equals(""))
                updateStatus("Mac address is empty...");
            mBluetoothDevice = mBluetoothAdapter.getRemoteDevice(macAddress);
            mBluetoothSocket = mBluetoothDevice.createRfcommSocketToServiceRecord(SPP_UUID);
            mBluetoothSocket.connect();
            updateStatus("Printing...");
            for (int i = 0; i < mPrintCountInput; i++) {
                mOutputStream = mBluetoothSocket.getOutputStream();
                mOutputStream.write(getDataInterMec());
                mOutputStream.flush();
                mDataPrintCount++;
                mPrintCount++;
                mTotalNumbersPrinted++;
            }
            mOutputStream.close();
            mBluetoothSocket.close();
        } catch (Exception e) {
            Commons.printException(e);
            updateStatus("Connection Failed");
        }
    }

    //logon printer self test gives the reverse mac address
    private String getReverseofMacAddress(String macAddress) {
        String mMAcAddress = "";

        String[] split = macAddress.split(":");
        List<String> list = Arrays.asList(split);
        Collections.reverse(list);
        for (int i = 0; i < list.size(); i++) {
            if (i != list.size() - 1)
                mMAcAddress = mMAcAddress + list.get(i) + ":";
            else
                mMAcAddress = mMAcAddress + list.get(i);
        }

        return mMAcAddress;
    }

    public boolean isPrintFileExsist() {
        String path = getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
                + "/" + bmodel.userMasterHelper.getUserMasterBO().getUserid() + DataMembers.PRINTFILE + "/";
        File directory = new File(path);
        File[] contents = directory.listFiles();
        if (contents == null) {
            return false;
        } else if (contents.length == 0) {
            return false;
        } else {
            return true;
        }

    }

    private void showAlert() {

        if (isUpdatePrintCount)
            bmodel.updatePrintCount(mPrintCount);

        boolean isPrintSuccess = false;
        if (mPrintCountInput == mDataPrintCount)
            isPrintSuccess = true;

        if (bmodel.mCommonPrintHelper.isLogoEnabled) {
            if (mPrintCountInput != mImagePrintCount)
                isPrintSuccess = false;
        }

        String msg;
        if (isPrintSuccess) {
            updateStatus("Print completed.");
            msg = getResources().getString(
                    R.string.printed_successfully);
        } else {

            if (getMacAddressFieldText() != null && getMacAddressFieldText().isEmpty())
                updateStatus("Mac address is empty...");
            else
                updateStatus("Printer error.");

            if (!isPrintFileExsist())
                msg = getString(R.string.printFile_missing_error);
            else
                msg = getResources().getString(R.string.error_connecting_printer);

        }
        if (commonDialog != null && commonDialog.isShowing()) {
            commonDialog.dismiss();
            commonDialog.cancel();
            commonDialog = null;
        }

        if (bmodel.configurationMasterHelper.IS_ALLOW_CONTINUOUS_PRINT
                && isPrintSuccess && mTotalNumbersPrinted < bmodel.configurationMasterHelper.printCount && isFromInvoice) {
            msg = getResources().getString(R.string.do_u_want_to_take_one_more_print);

            commonDialog = new CommonDialog(getApplicationContext(), this,
                    "", msg,
                    false, getResources().getString(R.string.ok),
                    getResources().getString(R.string.cancel),
                    new CommonDialog.PositiveClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            mPrintCountInput = 1;
                            mDataPrintCount = 0;
                            callPrinter();

                        }
                    }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                    moveBack();
                }
            });
        } else {
            commonDialog = new CommonDialog(getApplicationContext(), this,
                    "", msg,
                    false, getResources().getString(R.string.ok),
                    null, new CommonDialog.PositiveClickListener() {
                @Override
                public void onPositiveButtonClick() {
                    moveBack();
                }
            }, new CommonDialog.negativeOnClickListener() {
                @Override
                public void onNegativeButtonClick() {
                }
            });
        }
        commonDialog.setCancelable(false);
        commonDialog.show();
    }


    private void moveBack() {
        if (isFromOrder) {
            bmodel.productHelper.clearOrderTable();

            if (bmodel.configurationMasterHelper.IS_REMOVE_TAX_ON_SRP) {
                bmodel.resetSRPvalues();
            }

            Intent i;
            if (getIntent().getStringExtra("From") == null) {
                i = new Intent(
                        CommonPrintPreviewActivity.this,
                        HomeScreenTwo.class);
                Bundle extras = getIntent().getExtras();
                if (extras != null) {
                    i.putExtra("IsMoveNextActivity", bmodel.configurationMasterHelper.MOVE_NEXT_ACTIVITY);
                    i.putExtra("CurrentActivityCode", OrderSummary.mCurrentActivityCode);
                }
            } else {
                i = new Intent(CommonPrintPreviewActivity.this, HomeScreenActivity.class);
            }
            startActivity(i);
        } else if (isFromCollection) {
            CollectionHelper.getInstance(CommonPrintPreviewActivity.this).downloadCollectionMethods();
            bmodel.outletTimeStampHelper.saveTimeStampModuleWise(
                    DateTimeUtils.now(DateTimeUtils.DATE_GLOBAL),
                    DateTimeUtils.now(DateTimeUtils.TIME), "MENU_COLLECTION");

            Intent intent = new Intent(CommonPrintPreviewActivity.this,
                    CollectionScreen.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("screentitle", "MENU_COLLECTION");
            startActivity(intent);
        }
        bmodel.userMasterHelper.downloadDistributionDetails();
        finish();
    }

    /**
     * Bixolon Printer Connection.
     */
    private void doConnectionBixolon() {
        disconnectBixolon();
        bixolonPrinterApi = new BixolonPrinter(this, handler, null);
        bixolonPrinterApi.findBluetoothPrinters();
    }

    /**
     * Bixolon printer call back function.
     */
    private final Handler handler = new Handler(new Handler.Callback() {
        @Override
        public boolean handleMessage(Message msg) {

            switch (msg.what) {
                case BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET:
                    Commons.print("Handler," + "BixolonPrinter.MESSAGE_BLUETOOTH_DEVICE_SET");
                    if (msg.obj == null) {
                        //updateScreenStatus(layoutThereArentPairedPrinters);
                    } else {
                        String addr = "";
                        Set<BluetoothDevice> pairedDevices = (Set<BluetoothDevice>) msg.obj;
                        for (BluetoothDevice device : pairedDevices) {
                            //bixolonPrinterApi.connect(device.getAddress());
                            addr = device.getAddress();
                            break;
                        }
                        try {
                            bixolonPrinterApi.connect(addr);

                        } catch (Exception e) {
                            Commons.printException(e);
                        } finally {
                        }
                    }
                    return true;

                case BixolonPrinter.MESSAGE_STATE_CHANGE:
                    switch (msg.arg1) {
                        case BixolonPrinter.STATE_CONNECTED:

                            PRINT_STATE = "TRUE";
                            String[] lines = bmodel.mCommonPrintHelper.getInvoiceData().toString().split("\\n");
                            updateStatus("Printing...");
                            for (int i = 0; i < mPrintCountInput; i++) {
                                mDataPrintCount++;
                                mPrintCount++;
                                mTotalNumbersPrinted++;
                                doBixDataPrint(lines);
                            }

                            DemoSleeper.sleep(2000 * mDataPrintCount);
                            disconnectBixolon();
                            showAlert();
                            break;

                        case BixolonPrinter.STATE_NONE:
                            PRINT_STATE = "NO_PRINTER";
                            break;

                    }
                    return true;


                case BixolonPrinter.MESSAGE_TOAST:
                    if (!PRINT_STATE.equalsIgnoreCase("TRUE") && !PRINT_STATE.equalsIgnoreCase("NO_PRINTER")) {
                        showAlert();
                    }
                    return true;


                case BixolonPrinter.MESSAGE_PRINT_COMPLETE:
                    return true;

                case BixolonPrinter.MESSAGE_ERROR_OUT_OF_MEMORY:
                    Toast.makeText(getApplicationContext(),
                            getResources().getString(R.string.out_of_memory),
                            Toast.LENGTH_SHORT).show();
                    return true;
            }
            return true;
        }
    });

    /**
     * Disconnect Bixolon printer.
     */
    private void disconnectBixolon() {
        if (bixolonPrinterApi != null) {
            bixolonPrinterApi.disconnect();
        }
    }


    /**
     * Memory clear cache.
     */
    void CheckGC() {
        CheckGC("");
    }

    void CheckGC(String FunctionName) {
        long VmfreeMemory = Runtime.getRuntime().freeMemory();
        long VmmaxMemory = Runtime.getRuntime().maxMemory();
        long VmtotalMemory = Runtime.getRuntime().totalMemory();
        long Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;

        Commons.print(FunctionName + "Before Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");

        // Runtime.getRuntime().gc();
        System.runFinalization();
        System.gc();
        VmfreeMemory = Runtime.getRuntime().freeMemory();
        VmmaxMemory = Runtime.getRuntime().maxMemory();
        VmtotalMemory = Runtime.getRuntime().totalMemory();
        Memorypercentage = ((VmtotalMemory - VmfreeMemory) * 100)
                / VmtotalMemory;
        Commons.print(FunctionName + "_After Memorypercentage"
                + Memorypercentage + "% VmtotalMemory[" + VmtotalMemory + "] "
                + "VmfreeMemory[" + VmfreeMemory + "] " + "VmmaxMemory["
                + VmmaxMemory + "] ");
    }

    /**
     * Bixolon Image print.
     */
    private void doBixImgPrint() {

        try {
            if (bixolonPrinterApi != null) {
                bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO); //It fixes an issue printing special values like �, �����...

                bixolonPrinterApi.lineFeed(2, false); //It's like printing \n\n
                Bitmap fewlapsBitmap = BitmapFactory.decodeStream(getLogoIS());

                Bitmap resizedBitmap = Bitmap.createScaledBitmap(
                        fewlapsBitmap, widthImage, heightImage, false);
                if (resizedBitmap != null) {
                    bixolonPrinterApi.printBitmap(resizedBitmap, BixolonPrinter.ALIGNMENT_RIGHT, 0, 50, false);
                }

                Thread.sleep(100);
                bixolonPrinterApi.lineFeed(1, false);
                mImagePrintCount++;
            }

        } catch (Exception e) {
            Commons.printException("ERROR," + "Printing", e);
        }

    }

    /**
     * Bixolon Data print.
     *
     * @param lines
     */
    private void doBixDataPrint(String[] lines) {
        CheckGC();
        if (bmodel.mCommonPrintHelper.isLogoEnabled) {
            doBixImgPrint();
        }

        if (bmodel.configurationMasterHelper.IS_SHOW_PRINT_LANGUAGE_THAI) {
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_THAI11);
        } else {
            bixolonPrinterApi.setSingleByteFont(BixolonPrinter.CODE_PAGE_858_EURO);
        }

        for (String s : lines) {
            if (s.contains("print_type")) {
                if (mPrintCount == 1) {
                    s = s.replace("print_type", "Original");
                } else {
                    s = s.replace("print_type", "Duplicate");
                }
            }

            if (s.contains("print_no")) {
                s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
            }

            if (s.contains("#B#")) {

                String str = s.replace("#B#", "");
                int spaceCount = 0;
                for (char c : str.toCharArray()) {
                    if (c == ' ') {
                        spaceCount++;
                    } else {
                        break;
                    }
                }

                StringBuilder sbs = new StringBuilder();
                for (int j = 0; j < spaceCount; j++) {
                    sbs.append(" ");
                }
                bixolonPrinterApi.printText(sbs.toString() + str.substring(spaceCount, str.length()) + "\n", BixolonPrinter.ALIGNMENT_LEFT,
                        BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                        BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                                | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);

            } else {
                bixolonPrinterApi.printText(s + "\n", BixolonPrinter.ALIGNMENT_LEFT,
                        BixolonPrinter.TEXT_ATTRIBUTE_FONT_A,
                        BixolonPrinter.TEXT_SIZE_HORIZONTAL1
                                | BixolonPrinter.TEXT_SIZE_VERTICAL1, true);
            }
        }
    }

    /**
     * Scribe Printer Connections
     */
    private void doConnectionScrybe() {
        try {
            new ScrybePrinter(new ScrybeListener() {
                @Override
                public void isScrybeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected) {
                    updateStatus("Printing...");
                    printScrybeData(aemPrinter, aemScrybeDevice, isConnected);
                }
            }).execute();
        } catch (Exception e) {
            Commons.printException(e);
        }
    }

    /**
     * Method used for Scribe Printing
     *
     * @param aemPrinter
     * @param aemScrybeDevice
     * @param isconnected
     */

    @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR1)
    private void printScrybeData(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isconnected) {
        byte fontSize = 26;
        if (isconnected) {
            updateStatus("Connected");
            if (aemPrinter != null) {
                try {
                    for (int i = 0; i < mPrintCountInput; i++) {
                        aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                        aemPrinter.setFontSize(fontSize);
                        aemPrinter.print(getDataScrybe(aemPrinter));
                        aemPrinter.setCarriageReturn();
                        mDataPrintCount++;
                        mPrintCount++;
                        mTotalNumbersPrinted++;
                    }

                    DemoSleeper.sleep(2000 * mDataPrintCount);

                } catch (Exception e) {
                    Commons.print("Print Error :" + e.toString());
                } finally {
                    showAlert();
                    disconnectScrybe(aemScrybeDevice);
                }
            }

        } else {
            updateStatus("Printer not connected.");
            showAlert();
            Toast.makeText(CommonPrintPreviewActivity.this, "Printer not connected ..", Toast.LENGTH_SHORT).show();
            disconnectScrybe(aemScrybeDevice);
        }
    }

    /**
     * Disconnect Scrybe Printer
     *
     * @param aemScrybeDevice
     */
    private void disconnectScrybe(AEMScrybeDevice aemScrybeDevice) {
        if (aemScrybeDevice != null) {
            try {
                aemScrybeDevice.disConnectPrinter();
            } catch (IOException e) {
                Commons.printException(e);
            }
        }
    }


    private String doPrintAddSpace(int space, int maxlenght) {
        StringBuffer sb = new StringBuffer();
        if (space < maxlenght) {
            for (int i = 0; i < maxlenght - space; i++) {
                sb.append(" ");
            }
        }
        return sb.toString();
    }

    /**
     * Scrybe Print data Alignment
     *
     * @param aemPrinter
     * @return
     */
    private String getDataScrybe(AEMPrinter aemPrinter) {

        try {
            StringBuilder tempsb = new StringBuilder();
            String[] lines = bmodel.mCommonPrintHelper.getInvoiceData().toString().split("\\n");
            for (String s : lines) {

                if (s.contains("print_type")) {
                    if (mPrintCount == 0) {
                        s = s.replace("print_type", "Original");
                    } else {
                        s = s.replace("print_type", "Duplicate");
                    }
                }

                if (s.contains("print_no")) {
                    s = s.replace("print_no", (mPrintCount + 1) + " of " + mPrintCountInput);
                }

                if (s.contains("#B#")) {
                    String str = s.replace("#B#", "");
                    int spaceCount = 0;
                    for (char c : str.toCharArray()) {
                        if (c == ' ') {
                            spaceCount++;
                        } else {
                            break;
                        }
                    }

                    String distName = str.substring(spaceCount, str.length());
                    int centerLength = 48 - distName.length();
                    tempsb.append(doPrintAddSpace(0, centerLength / 2));
                    tempsb.append(str.substring(spaceCount, str.length()));

                    aemPrinter.setFontType(AEMPrinter.DOUBLE_HEIGHT);
                    aemPrinter.setFontSize(AEMPrinter.FONT_001);
                    aemPrinter.print(tempsb.toString());

                    tempsb.delete(0, tempsb.length());
                    aemPrinter.setFontType(AEMPrinter.FONT_NORMAL);
                    aemPrinter.setFontSize((byte) 26);

                } else {
                    tempsb.append(s);
                    aemPrinter.print(tempsb.toString());
                    tempsb.delete(0, tempsb.length());
                }
            }

            return tempsb.toString();

        } catch (Exception e) {
            Commons.printException(e);
        }

        return "";
    }


    /**
     * Scrybe printer connection call back
     */
    public class ScrybePrinter extends AsyncTask<Void, Void, Boolean> implements IAemScrybe {

        private ScrybeListener onScrybeListener;
        private AEMScrybeDevice m_AemScrybeDevice;
        private AEMPrinter m_AemPrinter = null;

        public ScrybePrinter(ScrybeListener onScrybeListener) {
            this.onScrybeListener = onScrybeListener;
            this.m_AemScrybeDevice = new AEMScrybeDevice(ScrybePrinter.this);
        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            ArrayList<String> aemPrinterList = m_AemScrybeDevice.getPairedPrinters();
            if (aemPrinterList != null) {
                for (int i = 0; i < aemPrinterList.size(); i++) {
                    Log.v("Check", aemPrinterList.get(i));
                }
                if (aemPrinterList.size() > 0)
                    m_AemPrinter = connect(aemPrinterList.get(0).toString());
            }

            if (m_AemPrinter != null) {
                return true;
            }

            return false;
        }

        @Override
        protected void onPostExecute(Boolean printerStatus) {
            super.onPostExecute(printerStatus);
            onScrybeListener.isScrybeResponse(m_AemPrinter, m_AemScrybeDevice, printerStatus);
        }

        @Override
        public void onDiscoveryComplete(ArrayList<String> arrayList) {

        }

        private AEMPrinter connect(String printerName) {
            try {
                Log.v("Check", "connecting");
                m_AemScrybeDevice.connectToPrinter(printerName);
                m_AemPrinter = m_AemScrybeDevice.getAemPrinter();
                Log.v("Check", "connected");

            } catch (IOException e) {
                Commons.printException(e);
                disConnectScrybe();
                m_AemPrinter = null;
                return m_AemPrinter;
            }

            return m_AemPrinter;
        }

        private void disConnectScrybe() {
            if (m_AemScrybeDevice != null) {
                try {
                    m_AemScrybeDevice.disConnectPrinter();
                } catch (IOException e) {
                    Commons.printException(e);
                }
            }
        }

    }

    public interface ScrybeListener {
        void isScrybeResponse(AEMPrinter aemPrinter, AEMScrybeDevice aemScrybeDevice, boolean isConnected);
    }


    class PrintMaestro extends AsyncTask<Void, Void, Boolean> {
        protected void onPreExecute() {
            updateStatus("Connecting...");
        }

        @Override
        protected Boolean doInBackground(Void... params) {
            try {
                if (!getMacAddressFieldText().equals("")) {
                    mPrinterStatusTV.setText("Printing");
                    bluetoothCommunication.SendData(getDataZebra());
                    mDataPrintCount++;
                    mPrintCount++;
                    mTotalNumbersPrinted++;
                } else {
                    updateStatus("Mac address is empty...");
                }
                DemoSleeper.sleep(2000);
            } catch (Exception ex) {
                Commons.printException(ex);
            }
            return true;
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);
            showAlert();
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (bmodel.configurationMasterHelper.COMMON_PRINT_MAESTROS) {
            if (bluetoothCommunication != null)
                bluetoothCommunication.StopConnection();
        }
    }

    private void doMaestroPrintNew() {
        try {
            new PrintMaestro().execute();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //Overriding Maestro Printer interface methods
    @Override
    public void onBatteryStatus(byte[] bytes) {
    }

    @Override
    public void onNoSmartCardFound() {
    }

    @Override
    public void onSmartCardPresent() {
    }

    @Override
    public void onFalseFingerDetected() {
    }

    @Override
    public void onCancelledCommand() {
    }

    @Override
    public void onCorruptDataRecieved() {
    }

    @Override
    public void onCorruptDataSent() {
    }

    @Override
    public void onInternalFPModuleCommunicationerror() {
    }

    @Override
    public void onParameterOutofRange() {
    }

    @Override
    public void onFingerPrintTimeout() {
    }

    @Override
    public void onWSQCOMLETE(int i) {
    }

    @Override
    public void onConnectComplete() {
    }

    @Override
    public void onConnectionFailed() {
    }

    @Override
    public void onPlaceFinger() {
    }

    @Override
    public void onMoveFingerUP() {
    }

    @Override
    public void onMoveFingerDown() {
    }

    @Override
    public void onMoveFingerRight() {
    }

    @Override
    public void onMoveFingerLeft() {
    }

    @Override
    public void onPressFingerHard() {
    }

    @Override
    public void onLatentFingerHard(String s) {
    }

    @Override
    public void onRemoveFinger() {
    }

    @Override
    public void onWSQFingerReceived(byte[] bytes) {
    }

    @Override
    public void onFingeracquisitioncompeted(String s) {
    }

    @Override
    public void onFingerScanStarted(int i) {
    }

    @Override
    public void onFingerTooMoist() {
    }

    @Override
    public void onNoResponseFromCard() {
    }

    @Override
    public void onCardNotSupported() {
    }

    @Override
    public void onCommandNotSupported() {
    }

    @Override
    public void onInvalidCommand() {
    }

    @Override
    public void onErrorOccured() {
    }

    @Override
    public void onVerificationSuccessful(int i) {
    }

    @Override
    public void onSerialNumber(byte[] bytes) {
    }

    @Override
    public void onVersionNumberReceived(byte[] bytes) {
    }

    @Override
    public void onVerificationfailed() {
    }

    @Override
    public void onFingerImageRecieved(byte[] bytes) {
    }

    @Override
    public void onCommandRecievedWhileProcessing() {
    }

    @Override
    public void onCommandRecievedWhileAnotherRunning() {
    }

    @Override
    public void onCryptographicError() {
    }

    @Override
    public void onOperationNotSupported() {
    }

    @Override
    public void onTemplateRecieved(byte[] bytes) {
    }

    @Override
    public void onNFIQ(int i) {
    }

    @Override
    public void onOutofPaper() {
    }

    @Override
    public void onPlatenOpen() {
    }

    @Override
    public void onHighHeadTemperature() {
    }

    @Override
    public void onLowHeadTemperature() {
    }

    @Override
    public void onImproperVoltage() {
    }

    @Override
    public void onSuccessfulPrintIndication() {
    }

    @Override
    public void onSmartCardDataRecieved(byte[] bytes) {
    }

    @Override
    public void onCPUSmartCardCommandDataRecieved(byte[] bytes) {
    }

    @Override
    public void onMSRDataRecieved(String s) {
    }

    @Override
    public void onNoData() {
    }

    @Override
    public void onImproveSwipe() {
    }

    @Override
    public void onSameFinger() {
    }

    @Override
    public void onWriteToSmartCardSuccessful() {
    }

    @Override
    public void onErrorReadingSmartCard() {
    }

    @Override
    public void onErrorOccuredWhileProccess() {
    }

    @Override
    public void onErrorWritingSmartCard() {
    }

    public boolean isValidEmail(CharSequence target) {
        return !TextUtils.isEmpty(target) && android.util.Patterns.EMAIL_ADDRESS.matcher(target).matches();
    }
}