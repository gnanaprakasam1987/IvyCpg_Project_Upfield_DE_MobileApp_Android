package com.ivy.cpg.view.Planorama;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.supervisor.customviews.recyclerviewpager.RecyclerViewPager;
import com.ivy.cpg.view.supervisor.mvp.outletmapview.OutletMapViewPresenter;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.AppUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Iterator;


public class PlanoramaDetailActivity extends IvyBaseActivityNoActionBar {

    private RecyclerViewPager sellerListRecyclerView;
    private OutletMapViewPresenter outletMapViewPresenter;
    private String visitId,comments;
    private PlanoramaHelper planoramaHelper;
    private ArrayList<String> imageNameList;
    private LinearLayout layout_capturedImages;
    private String photoPath;
    private BusinessModel mBModel;
    private TextView textView_comments,textView_no_of_photos;
    private Toolbar toolbar;
    private Button button_loadAnalysis;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planorama_detail);

        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Planorama Visit");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mBModel=(BusinessModel)getApplicationContext();
        mBModel.setContext(this);
        layout_capturedImages=findViewById(R.id.layout_image_captured);
        textView_comments=findViewById(R.id.textView_comments);
        textView_no_of_photos=findViewById(R.id.label_no_of_photos);
        planoramaHelper=PlanoramaHelper.getInstance(this);
        if(getIntent().getExtras()!=null){
            visitId=getIntent().getExtras().getString("visitid");
            comments=getIntent().getExtras().getString("comment");

        }
        photoPath = getExternalFilesDir(
                Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName + "/";


        textView_comments.setText((comments!=null&&!comments.equals(""))?comments:"-");

        imageNameList=planoramaHelper.getImageNameListByVistId().get(visitId);
        if(imageNameList!=null) {
            textView_no_of_photos.setText(String.valueOf(imageNameList.size()));
            loadImages();
        }
        else textView_no_of_photos.setVisibility(View.GONE);

        button_loadAnalysis=findViewById(R.id.button_load_analysis);
        button_loadAnalysis.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LoadVisitAnalysis().execute();
            }
        });

    }


    private void loadImages(){
        layout_capturedImages.removeAllViews();
        int position=0;
        for(String imageName:imageNameList){

            File imgFile = new File(photoPath + "/"
                    + imageName);
            if (imgFile.exists()) {
                try {
                    ImageView imageView=new ImageView(this);
                    imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                    imageView.setAdjustViewBounds(true);
                    Bitmap myBitmap = mBModel.decodeFile(imgFile);
                    imageView.setImageBitmap(myBitmap);
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(AppUtils.dpToPx(this,150), AppUtils.dpToPx(this,150));

                    if(position!=0) {
                        layoutParams.setMargins(AppUtils.dpToPx(this, 5), 0, 0, 0);
                    }
                    position+=1;


                    layout_capturedImages.addView(imageView,layoutParams);

                } catch (Exception e) {
                    Commons.printException("" + e);
                }
            }
            else {
                // If any image is not available then showing default image..

                ImageView imageView=new ImageView(this);
                imageView.setScaleType(ImageView.ScaleType.FIT_XY);
                imageView.setAdjustViewBounds(true);
                imageView.setImageDrawable(getResources().getDrawable(R.drawable.no_image_available));
                LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(AppUtils.dpToPx(this,150), AppUtils.dpToPx(this,150));
                layoutParams.setMargins(10,0,10,0);
                layout_capturedImages.addView(imageView,layoutParams);

                break;
            }
        }
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

    private String authenticate() {

        String serverUrl = "https://api.planorama.com";
        String loginApi = "/public/v3/login";

        String authenticationToken = "";
        try {
            URL obj = new URL(serverUrl + loginApi);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("POST");
            con.setRequestProperty("Content-Type", "application/json");
            // For POST only - START
            con.setDoOutput(true);
            OutputStream os = con.getOutputStream();

            JSONObject jsonObject = new JSONObject();
            jsonObject.put("username", "Ivy01");
            jsonObject.put("password", "IvyPOC01");

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

                String output = response.toString();

                JSONObject json = new JSONObject(output);
                authenticationToken = ((JSONObject) json.get("data")).get("token").toString();

            } else {
                Commons.print("POST request not worked");
            }

        } catch (Exception e) {
            Commons.printException("" + e);
        }

        return authenticationToken;
    }

    private String downloadVisitAnalysis(String authenticationToken,String visitId){

        String serverUrl = "https://api.planorama.com";

        try {
            String loadAllVisits = "/public/v3/plaj/"+URLEncoder.encode(visitId, "UTF-8");


            URL obj = new URL(serverUrl + loadAllVisits);
            HttpURLConnection con = (HttpURLConnection) obj.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-Type", "application/json");
            con.setRequestProperty("Authorization",authenticationToken);
            con.setDoOutput(false);

            int responseCode = con.getResponseCode();
            StringBuilder responseOutput = new StringBuilder();

            if (responseCode == HttpURLConnection.HTTP_OK||responseCode==HttpURLConnection.HTTP_CREATED) {

                InputStream inputStream = con.getInputStream();
                InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
                BufferedReader bufferedReader = new BufferedReader(inputStreamReader);
                String line ;

                while ((line = bufferedReader.readLine()) != null) {
                    responseOutput.append(line);
                }

                if (bufferedReader != null) {
                    bufferedReader.close();
                    bufferedReader = null;
                }

                if (inputStreamReader != null) {
                    inputStreamReader.close();
                    inputStreamReader = null;
                }

                if (con != null) {
                    con.disconnect();
                    con = null;
                }
            }

            if(planoramaHelper.isAnalysisReady(responseOutput.toString())) {
                planoramaHelper.prepareProductList(responseOutput.toString());
                planoramaHelper.updateProductAvailability(responseOutput.toString());
                planoramaHelper.preparePlanoramaSOSList(responseOutput.toString());
            }
            else{
                planoramaHelper.getmProductList().clear();
                planoramaHelper.getmSOSList().clear();

                return "2";}




        } catch (Exception e) {
            Commons.printException("" + e);
            return "3";
        }



        return "0";
    }



    private class LoadVisitAnalysis extends AsyncTask<String, Void, String> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(PlanoramaDetailActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            String token=authenticate();
            if(!token.equals("")){
                return downloadVisitAnalysis(token,visitId);
            }
            else return "1";

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(result.equals("1"))
            Toast.makeText(PlanoramaDetailActivity.this,getResources().getString(R.string.authentication_error),Toast.LENGTH_LONG).show();
            else if(result.equals("2"))
                Toast.makeText(PlanoramaDetailActivity.this,"Analysis result is not ready.",Toast.LENGTH_LONG).show();
            else if(result.equals("3"))
                Toast.makeText(PlanoramaDetailActivity.this,"Error in fetching analysis result",Toast.LENGTH_LONG).show();



                startActivity(new Intent(PlanoramaDetailActivity.this,PlanoramaAnalysisActivty.class));


            if(alertDialog!=null)
                alertDialog.dismiss();
        }
    }

}
