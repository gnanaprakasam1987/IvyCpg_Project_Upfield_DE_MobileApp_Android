package com.ivy.cpg.view.Planorama;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;
import android.widget.Toolbar;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.ivy.cpg.view.reports.performancereport.SellerMapViewReportFragment;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.model.MyHttpConnectionNew;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.util.UUID;
import java.util.Vector;

public class NewVisitActivity extends IvyBaseActivityNoActionBar {

    ImageView imageView_camera,imageView_captured;
    private static final int CAMERA_REQUEST_CODE = 1;
    private String imageName,photoPath;
    private final String moduleName = "PLNO_";
    private BusinessModel bModel;
    private static final String TAG = "Planorama Screen";
    private int IMAGE_MAX_SIZE = 500;
    android.support.v7.widget.Toolbar toolbar;
    Button button_upload;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_visit);

        bModel=(BusinessModel)getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("New Visit");
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        imageView_camera=findViewById(R.id.imageview_camera);
        imageView_captured=findViewById(R.id.image);
        photoPath = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName + "/";

        imageView_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {



                imageName = moduleName
                        + bModel.getRetailerMasterBO()
                        .getRetailerID()   + "_"
                        + Commons.now(Commons.DATE_TIME)
                        + "_img.jpg";

                Intent intent = new Intent(NewVisitActivity.this,
                        CameraActivity.class);
                intent.putExtra("quality", 40);
                String path = photoPath + "/" + imageName;
                intent.putExtra("path", path);
                startActivityForResult(intent, CAMERA_REQUEST_CODE);

            }
        });

        button_upload=findViewById(R.id.button_upload);
        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new CreateVisitAsync().execute();
            }
        });
    }

    private void createVisit(){

        String serverUrl="https://api.planorama.com";
        String loginApi="/public/v3/login";
        String createVisitApi="/public/v3/visits";
        String createPhotoApi="/public/v3/photos";

        String authenticationToken="";
        try {
            URL obj = new URL(serverUrl+loginApi);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("username","Quickstar");
            jsonObject.put("password","Log-me-in");

            os.write(jsonObject.toString().getBytes("UTF-8"));
            os.flush();
            os.close();
            // For POST only - END

            int responseCode = con.getResponseCode();
            Commons.print("POST Response Code :: " + responseCode);

            if (responseCode == HttpURLConnection.HTTP_OK) { //success
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        con.getInputStream()));
                String inputLine;
                StringBuilder response = new StringBuilder();
                while ((inputLine = in.readLine()) != null) {
                    response.append(inputLine);
                }
                in.close();

                String output=response.toString();

                JSONObject json = new JSONObject(output);
                authenticationToken=((JSONObject)json.get("data")).get("token").toString();

            } else {
                Commons.print("POST request not worked");
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        String visitId="";
        if(!authenticationToken.equals("")){

            try {
                URL obj = new URL(serverUrl+createVisitApi);
                HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                con.setRequestMethod("POST");
                con.setRequestProperty("Content-Type", "application/json");
                con.setRequestProperty("Authorization", authenticationToken);
                // For POST only - START
                con.setDoOutput(true);
                OutputStream os = con.getOutputStream();

                JSONObject jsonObject=new JSONObject();
                jsonObject.put("store","5678");//bModel.getRetailerMasterBO().getRetailerID());
                jsonObject.put("base","5b60a8bddad4ec000126a959");
                jsonObject.put("isImmediate",true);

                os.write(jsonObject.toString().getBytes("UTF-8"));
                os.flush();
                os.close();
                // For POST only - END

                int responseCode = con.getResponseCode();
                Commons.print("POST Response Code :: " + responseCode);

                if (responseCode == HttpURLConnection.HTTP_OK||responseCode==HttpURLConnection.HTTP_CREATED) { //success
                    BufferedReader in = new BufferedReader(new InputStreamReader(
                            con.getInputStream()));
                    String inputLine;
                    StringBuilder response = new StringBuilder();
                    while ((inputLine = in.readLine()) != null) {
                        response.append(inputLine);
                    }
                    in.close();

                    String output=response.toString();

                    JSONObject json = new JSONObject(output);
                    visitId=((JSONObject)json.get("data")).get("id").toString();

                } else {
                    Commons.print("POST request not worked");
                }

            } catch (Exception e) {
                Commons.printException("" + e);
            }

            if(!visitId.equals("")){

                try {
                    String twoHyphens = "--";
                    String boundary =  "*****"+Long.toString(System.currentTimeMillis())+"*****";
                    String lineEnd = "\r\n";

                    int bytesRead, bytesAvailable, bufferSize;
                    byte[] buffer;
                    int maxBufferSize = 1*1024*1024;

                    URL obj = new URL(serverUrl+createPhotoApi);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("POST");
                    con.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                    con.setRequestProperty("Authorization", authenticationToken);

                    DataOutputStream dos  =new DataOutputStream(con.getOutputStream());
                    dos.writeBytes(twoHyphens + boundary + lineEnd);

                    File file=new File(photoPath+"/"+imageName);
                    FileInputStream fileInputStream = new FileInputStream(file);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    buffer = new byte[bufferSize];

                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    while(bytesRead > 0) {
                        //outputStream.write(buffer, 0, bufferSize);
                        bytesAvailable = fileInputStream.available();
                        bufferSize = Math.min(bytesAvailable, maxBufferSize);
                        bytesRead = fileInputStream.read(buffer, 0, bufferSize);
                    }

                    dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"" + file.getName() +"\"" + lineEnd);

                    dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                    dos.writeBytes(lineEnd);
                    dos.write(buffer);//your image array here buddy
                    dos.writeBytes(lineEnd);
                    dos.writeBytes(twoHyphens + boundary + lineEnd);
                    dos.writeBytes("Content-Disposition: form-data; name=\"data\"" + lineEnd);
                    dos.writeBytes(lineEnd);

                    JSONObject jsonObject =new JSONObject();
                    jsonObject.put("visit",visitId);
                    JSONArray jsonArray=new JSONArray();
                    jsonArray.put("tag1");
                    jsonArray.put("tag2");
                    jsonObject.put("tags",jsonArray);
                    dos.writeBytes(jsonObject.toString());//your parameter value
                    dos.writeBytes(lineEnd); //to add multiple parameters write Content-Disposition: form-data; name=\"your parameter name\"" + crlf again and keep repeating till here :)
                    dos.writeBytes(twoHyphens + boundary + twoHyphens);
                    dos.flush();
                    dos.close();

                    // For POST only - END

                    int responseCode = con.getResponseCode();
                    Commons.print("POST Response Code :: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK||responseCode==HttpURLConnection.HTTP_CREATED) { //success
                        BufferedReader in = new BufferedReader(new InputStreamReader(
                                con.getInputStream()));
                        String inputLine;
                        StringBuilder response = new StringBuilder();
                        while ((inputLine = in.readLine()) != null) {
                            response.append(inputLine);
                        }
                        in.close();

                        String output=response.toString();

                        JSONObject json = new JSONObject(output);
                        Commons.print("POST Response Code :: " + responseCode);

                       // visitId=((JSONObject)json.get("data")).get("id").toString();

                    } else {
                        Commons.print("POST request not worked");
                    }

                } catch (Exception e) {
                    Commons.printException("" + e);
                }

            }
            else {
               // Toast.makeText(NewVisitActivity.this,"Error in Visit Creation",Toast.LENGTH_LONG).show();
            }

        }
        else {
           // Toast.makeText(NewVisitActivity.this,"Error in Authentication",Toast.LENGTH_LONG).show();
        }
    }

    private class CreateVisitAsync extends AsyncTask<String, Void, String> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(NewVisitActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {


            createVisit();
            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(NewVisitActivity.this,"Done",Toast.LENGTH_LONG).show();

            if(alertDialog!=null)
                alertDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {

            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unbindDrawables(findViewById(R.id.root));
    }
    private void unbindDrawables(View view) {
        if (view != null) {
            if (view.getBackground() != null) {
                view.getBackground().setCallback(null);
            }
            if (view instanceof ViewGroup) {
                for (int i = 0; i < ((ViewGroup) view).getChildCount(); i++) {
                    unbindDrawables(((ViewGroup) view).getChildAt(i));
                }
                try {
                    ((ViewGroup) view).removeAllViews();
                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == CAMERA_REQUEST_CODE) {
            if (resultCode == 1) {
                // Photo saved successfully
                Commons.print(TAG + "," +
                        "Camera Activity : Successfully Captured.");
                Toast.makeText(this,"Successfully Captured.",Toast.LENGTH_LONG).show();
                File imgFile = new File(photoPath + "/" + imageName);
                imageView_captured.setImageBitmap(decodeFile(imgFile));

            } else {
                Commons.print(TAG + "," + "Camera Activity : Canceled");
                Toast.makeText(this,"Cancelled.",Toast.LENGTH_LONG).show();
            }
        }

    }

    /**
     * DecodeFile is convert the large size image to fixed size which mentioned
     * above
     */
    private Bitmap decodeFile(File f) {
        Bitmap b = null;
        try {
            // Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;

            FileInputStream fis = new FileInputStream(f);
            BitmapFactory.decodeStream(fis, null, o);
            fis.close();

            int scale = 1;
            if (o.outHeight > IMAGE_MAX_SIZE || o.outWidth > IMAGE_MAX_SIZE) {
                scale = (int) Math.pow(
                        2,
                        (int) Math.ceil(Math.log(IMAGE_MAX_SIZE
                                / (double) Math.max(o.outHeight, o.outWidth))
                                / Math.log(0.5)));
            }

            // Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            fis = new FileInputStream(f);
            b = BitmapFactory.decodeStream(fis, null, o2);
            fis.close();
        } catch (Exception e) {
            Commons.printException(e);
        }
        return b;
    }

}
