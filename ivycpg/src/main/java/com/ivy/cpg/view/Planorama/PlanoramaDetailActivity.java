package com.ivy.cpg.view.Planorama;

import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import androidx.appcompat.widget.Toolbar;
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
import com.ivy.utils.FileUtils;
import com.ivy.utils.NetworkUtils;

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
    private Toolbar toolbar,toolbar_selection;
    private Button button_loadAnalysis;

    private LinearLayout layout_toolbar_selection;
    private String mTagOfSelectedImage="";
    boolean isImageSelected=false;
    private View imageViewSelectedToDelete=null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planorama_detail);

        toolbar = findViewById(R.id.toolbar);
        toolbar_selection= findViewById(R.id.toolbar_selection);
        layout_toolbar_selection=findViewById(R.id.layout_toolbar_selection);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Planorama Visit");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        mBModel=(BusinessModel)getApplicationContext();
        mBModel.setContext(this);

        toolbar_selection.inflateMenu(R.menu.menu_planorama);
        toolbar_selection.getMenu().findItem(R.id.menu_delete).setVisible(false);
        toolbar_selection.setNavigationIcon(getResources().getDrawable(R.drawable.ok_tick));
        toolbar_selection.setOnMenuItemClickListener(new androidx.appcompat.widget.Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                if(item.getItemId()==R.id.menu_image_full_view){

                    try {
                        File folder = new File(photoPath + "/" + mTagOfSelectedImage);
                        AppUtils.openImage(folder.getAbsolutePath(), PlanoramaDetailActivity.this);
                    }
                    catch (Exception ex){
                        Commons.printException(ex);
                    }
                }


                return false;
            }
        });
        toolbar_selection.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
               unSelectView();
            }
        });


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

                String result =planoramaHelper.fetchLocalAnalysisResult(PlanoramaDetailActivity.this,visitId);

                if(!result.equals("")){
                   new LoadLOcalAnalysisResult().execute();
                }
                else {
                    if (NetworkUtils.isNetworkConnected(PlanoramaDetailActivity.this.getApplicationContext()))
                        new LoadVisitAnalysis().execute();
                    else
                        Toast.makeText(PlanoramaDetailActivity.this, getResources().getString(R.string.please_connect_to_internet), Toast.LENGTH_LONG).show();
                }
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
                    Bitmap myBitmap = FileUtils.decodeFile(imgFile);
                    imageView.setImageBitmap(myBitmap);
                    imageView.setTag(imageName);
                    LinearLayout.LayoutParams layoutParams=new LinearLayout.LayoutParams(AppUtils.dpToPx(this,150), AppUtils.dpToPx(this,150));

                    if(position!=0) {
                        layoutParams.setMargins(AppUtils.dpToPx(this, 5), 0, 0, 0);
                    }
                    position+=1;


                    imageView.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View view) {
                            if(isImageSelected){

                                if(mTagOfSelectedImage.equals(view.getTag())){
                                    unSelectView();
                                }
                                else {

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

    private void selectView(View view){

        // Un selecting last selected image
        if(imageViewSelectedToDelete!=null)
            imageViewSelectedToDelete.setPadding(0,0,0,0);

        layout_toolbar_selection.setVisibility(View.VISIBLE);
        toolbar.setVisibility(View.GONE);

        int padding=AppUtils.dpToPx(PlanoramaDetailActivity.this,5);
        view.setPadding(padding,padding,padding,padding);
        view.setBackgroundColor(getResources().getColor(R.color.colorPrimary));

        imageViewSelectedToDelete=view;
        mTagOfSelectedImage=(String)view.getTag();

        isImageSelected=true;

    }

    private void unSelectView(){

        if(imageViewSelectedToDelete!=null)
            imageViewSelectedToDelete.setPadding(0,0,0,0);

        isImageSelected=false;
        layout_toolbar_selection.setVisibility(View.GONE);
        toolbar.setVisibility(View.VISIBLE);




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
                planoramaHelper.prepareProductList(this,responseOutput.toString());
                planoramaHelper.updateProductAvailability(responseOutput.toString());
                planoramaHelper.preparePlanoramaSOSList(this,responseOutput.toString());

                planoramaHelper.saveAnalysisResult(this,responseOutput.toString(),visitId);
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

            try {
                String token = authenticate();
                if (!token.equals("")) {
                    String status=downloadVisitAnalysis(token, visitId);

                    if(status.equals("0")) {
                        //Analysis ready, so updating existing stock values..
                        if (mBModel.hasAlreadyStockChecked(mBModel.getRetailerMasterBO()
                                .getRetailerID())) {
                            mBModel.setEditStockCheck(true);
                            planoramaHelper.loadStockCheckedProducts(PlanoramaDetailActivity.this, mBModel
                                    .getRetailerMasterBO().getRetailerID());

                        }



                    }

                    return status;

                } else return "1";


            }
            catch (Exception ex){
                Commons.printException(ex);
                return "3";
            }

        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(alertDialog!=null)
                alertDialog.dismiss();

            if(result.equals("1"))
            Toast.makeText(PlanoramaDetailActivity.this,getResources().getString(R.string.authentication_error),Toast.LENGTH_LONG).show();
            else if(result.equals("2"))
                Toast.makeText(PlanoramaDetailActivity.this,"Analysis result is not ready.",Toast.LENGTH_LONG).show();
            else if(result.equals("3"))
                Toast.makeText(PlanoramaDetailActivity.this,"Error in fetching analysis result",Toast.LENGTH_LONG).show();
            else  startActivity(new Intent(PlanoramaDetailActivity.this,PlanoramaAnalysisActivty.class));







        }
    }

    private class LoadLOcalAnalysisResult extends AsyncTask<String, Void, String> {

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

            try {

                String result =planoramaHelper.fetchLocalAnalysisResult(PlanoramaDetailActivity.this,visitId);

                if(!result.equals("")) {
                    planoramaHelper.prepareProductList(PlanoramaDetailActivity.this, result);
                    planoramaHelper.updateProductAvailability(result);
                    planoramaHelper.preparePlanoramaSOSList(PlanoramaDetailActivity.this, result);

                    return "0";
                }


            }
            catch (Exception ex){
                Commons.printException(ex);
            }

            return "1";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);

            if(alertDialog!=null)
                alertDialog.dismiss();

            if(result.equals("0")) {
                if (mBModel.hasAlreadyStockChecked(mBModel.getRetailerMasterBO()
                        .getRetailerID())) {
                    mBModel.setEditStockCheck(true);
                    planoramaHelper.loadStockCheckedProducts(PlanoramaDetailActivity.this, mBModel
                            .getRetailerMasterBO().getRetailerID());

                }
                startActivity(new Intent(PlanoramaDetailActivity.this, PlanoramaAnalysisActivty.class));
            }
            else Toast.makeText(PlanoramaDetailActivity.this,"Error in fetching analysis result",Toast.LENGTH_LONG).show();







        }
    }

}
