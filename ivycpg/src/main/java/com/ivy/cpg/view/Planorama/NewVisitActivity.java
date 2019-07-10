package com.ivy.cpg.view.Planorama;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;
import com.ivy.utils.FileUtils;
import com.ivy.utils.NetworkUtils;

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
import java.net.URLEncoder;
import java.util.ArrayList;

public class NewVisitActivity extends IvyBaseActivityNoActionBar {

    ImageView imageView_camera,imageView_captured;
    private static final int CAMERA_REQUEST_CODE = 1;
    private String imageName,photoPath;
    private final String moduleName = "PLNO_";
    private BusinessModel bModel;
    private static final String TAG = "Planorama Screen";
    private int IMAGE_MAX_SIZE = 500;
    androidx.appcompat.widget.Toolbar toolbar,toolbar_selection;
    Button button_upload;
    String randomUID;
    ArrayList<String> imageNameList;
    EditText editText_comments;
    private boolean isFirstPicture=true;

    LinearLayout layout_capturedImages;
    private PlanoramaHelper planoramaHelper;
    private View imageViewSelectedToDelete=null;
    boolean isImageSelected=false;
    private LinearLayout layout_toolbar_selection;
    private String mTagOfSelectedImage="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_new_visit);

        bModel=(BusinessModel)getApplicationContext();

        toolbar = findViewById(R.id.toolbar);
        toolbar_selection= findViewById(R.id.toolbar_selection);
        layout_toolbar_selection=findViewById(R.id.layout_toolbar_selection);
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

        toolbar_selection.inflateMenu(R.menu.menu_planorama);
        toolbar_selection.setNavigationIcon(getResources().getDrawable(R.drawable.ok_tick));
        toolbar_selection.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.menu_delete){
                    deleteImage(mTagOfSelectedImage);
                    loadImages();

                    if(!isImageSelected){
                        if(imageNameList!=null&&imageNameList.size()==0) {
                            layout_toolbar_selection.setVisibility(View.GONE);
                            toolbar.setVisibility(View.VISIBLE);
                            isImageSelected=false;
                        }
                    }
                }
                else if(item.getItemId()==R.id.menu_image_full_view){

                    File folder = new File(photoPath+"/"+mTagOfSelectedImage);
                    AppUtils.openImage(folder.getAbsolutePath(),NewVisitActivity.this);
                }


                return false;
            }
        });
        toolbar_selection.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(imageViewSelectedToDelete!=null)
                    imageViewSelectedToDelete.setPadding(0,0,0,0);

                layout_toolbar_selection.setVisibility(View.GONE);
                toolbar.setVisibility(View.VISIBLE);
                isImageSelected=false;
            }
        });

        layout_capturedImages=findViewById(R.id.layout_image_captured);
        editText_comments=findViewById(R.id.edittext_comments);
        imageView_camera=findViewById(R.id.imageview_camera);
        imageView_captured=findViewById(R.id.image);
        photoPath = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName + "/";

       /* UUID uuid=UUID.fromString(moduleName
                +  Commons.now(Commons.DATE_TIME)
                );
        randomUID=uuid.randomUUID().toString();*/


       // calling first time
       callCamera();

        imageView_camera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

              isFirstPicture=false;
              callCamera();


            }
        });

        button_upload=findViewById(R.id.button_upload);
        button_upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                onCreateDialog(2);
            }
        });

        planoramaHelper=PlanoramaHelper.getInstance(this);
        imageNameList=new ArrayList<>();

        //


    }

    private void deleteImage(String imageName){
        if(imageNameList!=null) {
            for (String imgName : imageNameList) {
                if(imageName.equals(imgName))
                bModel.synchronizationHelper.deleteFiles(photoPath,
                        imgName);
            }

            for(int i=0;i<imageNameList.size();i++){
                if(imageName.equals(imageNameList.get(i))){
                imageNameList.remove(i);
                break;}
            }
        }
    }
    private void callCamera(){

        imageName = moduleName
                + bModel.getRetailerMasterBO().getRetailerID()   + "_"
                + Commons.now(Commons.DATE_TIME)
                + "_img.jpg";

        Intent intent = new Intent(NewVisitActivity.this,
                CameraActivity.class);
        intent.putExtra("quality", 40);
        String path = photoPath + "/" + imageName;
        intent.putExtra("path", path);
        startActivityForResult(intent, CAMERA_REQUEST_CODE);

    }
    private void loadImages(){
        layout_capturedImages.removeAllViews();
           for(String imageName:imageNameList){

               File imgFile = new File(photoPath + "/"
                       + imageName);
               if (imgFile.exists()) {
                   try {
                       ImageView imageView=new ImageView(this);
                       imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                       imageView.setAdjustViewBounds(true);
                       imageView.setLongClickable(true);
                       Bitmap myBitmap = FileUtils.decodeFile(imgFile);
                       imageView.setImageBitmap(myBitmap);
                       imageView.setTag(imageName);
                       LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(AppUtils.dpToPx(this,150), AppUtils.dpToPx(this,150));
                       layoutParams.setMargins(10,0,10,0);


                       imageView.setOnClickListener(new View.OnClickListener() {
                           @Override
                           public void onClick(View view) {
                               if(isImageSelected){

                                   if(mTagOfSelectedImage.equals(view.getTag())){
                                       view.setPadding(0,0,0,0);
                                       isImageSelected=false;
                                       layout_toolbar_selection.setVisibility(View.GONE);
                                       toolbar.setVisibility(View.VISIBLE);
                                   }
                                   else {
                                       if(imageViewSelectedToDelete!=null)
                                           imageViewSelectedToDelete.setPadding(0,0,0,0);
                                       selectView(view);
                                   }

                               }
                               else {
                                   selectView(view);
                               }

                           }
                       });

                       layout_capturedImages.addView(imageView,layoutParams);

                   } catch (Exception e) {
                       Commons.printException("" + e);
                   }
               }
           }
    }

    private void selectView(View view){
        layout_toolbar_selection.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        int padding=AppUtils.dpToPx(NewVisitActivity.this,5);
        view.setPadding(padding,padding,padding,padding);
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        imageViewSelectedToDelete=view;
        mTagOfSelectedImage=(String)view.getTag();

        isImageSelected=true;

    }
    private boolean createVisit(){

        String serverUrl="https://api.planorama.com";
        String loginApi="/public/v3/login";
        String createVisitApi="/public/v3/visits";


        String authenticationToken="";
        String visitId="";
        boolean isImageUploaded=false;

        try {
            URL obj = new URL(serverUrl+loginApi);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();

            JSONObject jsonObject=new JSONObject();
            jsonObject.put("username","Ivy01");
            jsonObject.put("password","IvyPOC01");

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
            return false;
        }

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
                jsonObject.put("store","10001");//bModel.getRetailerMasterBO().getRetailerID());
                jsonObject.put("base","5b7141be6efed80001bd8cc0");
                jsonObject.put("isImmediate",true);
                jsonObject.put("comment",editText_comments.getText().toString());

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
                return false;
            }

            if(!visitId.equals("")){
                    File file;
                    for(int i=0;i<imageNameList.size();i++){
                        file=new File(photoPath+"/"+imageNameList.get(i));
                        isImageUploaded=uploadImage(i,serverUrl,authenticationToken,visitId,file);
                    }

            }

            if(isImageUploaded){

                try {
                    String closeVisitApi = "/public/v3/visits/"+URLEncoder.encode(visitId, "UTF-8");
                    URL obj = new URL(serverUrl + closeVisitApi);
                    HttpURLConnection con = (HttpURLConnection) obj.openConnection();
                    con.setRequestMethod("PATCH");
                    con.setRequestProperty("Content-Type", "application/json");
                    con.setRequestProperty("Authorization", authenticationToken);
                    // For POST only - START
                    con.setDoOutput(true);
                    OutputStream os = con.getOutputStream();

                    JSONObject jsonObject = new JSONObject();
                    jsonObject.put("hasAllPhotos", true);

                    os.write(jsonObject.toString().getBytes("UTF-8"));
                    os.flush();
                    os.close();
                    // For POST only - END

                    int responseCode = con.getResponseCode();
                    Commons.print("POST Response Code :: " + responseCode);

                    if (responseCode == HttpURLConnection.HTTP_OK || responseCode == HttpURLConnection.HTTP_NO_CONTENT) { //success
                        Commons.print("visit closed");

                    } else {
                        Commons.print("Visit closing failed");
                    }
                }
                catch (Exception ex){
                    Commons.printException(ex);
                    return false;
                }

            }


        }

        planoramaHelper.saveVisit(this,visitId,editText_comments.getText().toString(),imageNameList.size(),imageNameList);

        return true;
    }


    private boolean uploadImage(int rank,String serverUrl,String authenticationToken,String visitId,File imageFile){

            String createPhotoApi="/public/v3/photos";

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


                FileInputStream fileInputStream = new FileInputStream(imageFile);
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

                dos.writeBytes("Content-Disposition: form-data; name=\"photo\";filename=\"" + imageFile.getName() +"\"" + lineEnd);

                dos.writeBytes("Content-Type: image/jpeg" + lineEnd);
                dos.writeBytes(lineEnd);
                dos.write(buffer);//your image array here buddy
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + lineEnd);
                dos.writeBytes("Content-Disposition: form-data; name=\"data\"" + lineEnd);
                dos.writeBytes(lineEnd);

                JSONObject jsonObject =new JSONObject();
                jsonObject.put("visit",visitId);
                jsonObject.put("rank",rank);
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

                    Commons.print("Image uploaded Response Code :: " + responseCode);

                } else {
                    Commons.print("POST request not worked");
                    return false;
                }

            } catch (Exception e) {
                Commons.printException("" + e);
                return false;
            }


        return true;

    }

    private class CreateVisitAsync extends AsyncTask<String, Void, Boolean> {

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
        protected Boolean doInBackground(String... url) {



            return createVisit();
        }

        @Override
        protected void onPostExecute(Boolean result) {
            super.onPostExecute(result);

            if(result) {
                Toast.makeText(NewVisitActivity.this, getResources().getString(R.string.visit_created_successfully), Toast.LENGTH_LONG).show();

               /* startActivity(new Intent(NewVisitActivity.this,
                        PlanoramaActivity.class));*/
                setResult(RESULT_OK);
                finish();
                overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);

            }
            else {
                Toast.makeText(NewVisitActivity.this, getResources().getString(R.string.error_in_creating_visit), Toast.LENGTH_LONG).show();
            }

            if(alertDialog!=null)
                alertDialog.dismiss();
        }
    }

    @Override
    public void onBackPressed() {
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_planorama, menu);

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {


        if(imageViewSelectedToDelete!=null) {
            menu.findItem(R.id.menu_delete).setVisible(true);
            menu.findItem(R.id.menu_image_full_view).setVisible(true);
        }
        else{ menu.findItem(R.id.menu_delete).setVisible(false);
            menu.findItem(R.id.menu_image_full_view).setVisible(false);}


        return super.onPrepareOptionsMenu(menu);


    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            showDialog(1);

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
                if(imageNameList==null)
                    imageNameList=new ArrayList<>();
                imageNameList.add(imageName);

                loadImages();
/*
                File imgFile = new File(photoPath + "/" + imageName);
                imageView_captured.setImageBitmap(decodeFile(imgFile));
*/

            } else {
                if(isFirstPicture){
                   /* startActivity(new Intent(NewVisitActivity.this,
                            PlanoramaActivity.class));*/
                    finish();
                    overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                }
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



    private void setProfileImage(ImageView imageView,String imageName) {
        try {
            if (imageName != null && !"".equals(imageName)) {
                File imgFile = new File(photoPath + "/"
                        + imageName);
                if (imgFile.exists()) {
                    try {
                        imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                        imageView.setAdjustViewBounds(true);

                        Glide.with(this)
                                .load(imgFile)
                                .centerCrop()
                                .placeholder(R.drawable.no_image_available)
                                .error(R.drawable.no_image_available)
                                .into(imageView);

                    } catch (Exception e) {
                        Commons.printException("" + e);
                    }
                } else {
                    imageView
                            .setImageResource(R.drawable.no_image_available);
                }
            }
        }catch(Exception e){
            Commons.printException(e);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {

        switch (id) {
            case 1:
                AlertDialog.Builder builder1 = new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.doyouwantgoback))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        deleteAllImages();

                                        /*startActivity(new Intent(NewVisitActivity.this,
                                                PlanoramaActivity.class));*/
                                        finish();
                                        overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bModel.applyAlertDialogTheme(builder1);
                break;

            case 2:
                AlertDialog.Builder builder2 = new AlertDialog.Builder(this)
                        .setIcon(null)
                        .setCancelable(false)
                        .setTitle(
                                getResources().getString(
                                        R.string.are_you_sure_you_want_to_submit))
                        .setPositiveButton(getResources().getString(R.string.ok),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {

                                        if(NetworkUtils.isNetworkConnected(NewVisitActivity.this.getApplicationContext()))
                                        new CreateVisitAsync().execute();
                                        else Toast.makeText(NewVisitActivity.this,getResources().getString(R.string.please_connect_to_internet),Toast.LENGTH_LONG).show();
                                    }
                                })
                        .setNegativeButton(
                                getResources().getString(R.string.cancel),
                                new DialogInterface.OnClickListener() {
                                    public void onClick(DialogInterface dialog,
                                                        int whichButton) {
                                    }
                                });
                bModel.applyAlertDialogTheme(builder2);
                break;

            default:
                break;
        }

        return null;
    }

    private void deleteAllImages(){

        if(imageNameList!=null) {
            for (String imageName : imageNameList) {
                bModel.synchronizationHelper.deleteFiles(photoPath,
                        imageName);
            }

            imageNameList.clear();
        }


    }
}
