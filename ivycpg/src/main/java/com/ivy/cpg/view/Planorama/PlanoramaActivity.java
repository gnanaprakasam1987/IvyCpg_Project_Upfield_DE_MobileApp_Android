package com.ivy.cpg.view.Planorama;

import android.app.AlertDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DateUtil;
import com.ivy.sd.png.view.HomeScreenTwo;
import com.ivy.utils.FontUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;

public class PlanoramaActivity extends IvyBaseActivityNoActionBar {

    Toolbar toolbar;
    FloatingActionButton floatingActionButton;
    RecyclerView listView_visits;
    ArrayList<PlanoramaBO> list_visits;
    BusinessModel bModel;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_planorama);
        bModel=(BusinessModel)getApplicationContext();
        bModel.setContext(this);
        toolbar = findViewById(R.id.toolbar);
        if (toolbar != null) {

            setSupportActionBar(toolbar);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Planorama");
//            // Used to on / off the back arrow icon
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//           // Used to remove the app logo actionbar icon and set title as home
//          // (title support click)
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        listView_visits=findViewById(R.id.listView_visits);
        listView_visits.setHasFixedSize(true);
        final LinearLayoutManager mLayoutManager = new LinearLayoutManager(this);
        listView_visits.setLayoutManager(mLayoutManager);

        floatingActionButton=findViewById(R.id.fab_new_visit);
        floatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(PlanoramaActivity.this,
                        NewVisitActivity.class));
                //finish();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(this,
                    HomeScreenTwo.class));
            finish();
            overridePendingTransition(R.anim.trans_right_in, R.anim.trans_right_out);
        }
        return super.onOptionsItemSelected(item);
    }

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
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onResume() {
        super.onResume();

        new LoadVisits().execute();
    }

    private class LoadVisits extends AsyncTask<String, Void, String> {

        private AlertDialog.Builder builder;
        private AlertDialog alertDialog;

        @Override
        protected void onPreExecute() {
            super.onPreExecute();

            builder = new AlertDialog.Builder(PlanoramaActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.loading));
            alertDialog = builder.create();
            alertDialog.show();
        }

        @Override
        protected String doInBackground(String... url) {

            String token=authenticate();
            if(!token.equals("")){
                downloadVisits(token);
            }

            return "";
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            Toast.makeText(PlanoramaActivity.this,"Done",Toast.LENGTH_LONG).show();

            if(list_visits!=null){
                RecyclerViewAdapter mAdapter = new RecyclerViewAdapter(list_visits);
                listView_visits.setAdapter(mAdapter);
            }
            if(alertDialog!=null)
                alertDialog.dismiss();
        }
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
            jsonObject.put("username", "Quickstar");
            jsonObject.put("password", "Log-me-in");

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

    private void downloadVisits(String authenticationToken){

        String serverUrl = "https://api.planorama.com";
        String loadAllVisits = "/public/v3/visits";

        try {
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

            updateList(responseOutput.toString());
            // For POST only - END


                /*JSONArray json = new JSONArray(responseOutput);
                for(int i=0;i<json.length();i++){

                    Log.d("tst",json.get(i).toString());
                }*/

                //JSONArray jsonArray = (JSONArray) ((JSONObject) json.get("data")).get("storeInfo");
               // if(jsonArray.get("code").equals())



        } catch (Exception e) {
            Commons.printException("" + e);
        }



    }

    private void updateList(String responseOutput){
        try {
            if (!responseOutput.equals("")) {
                list_visits = new ArrayList<>();

                JSONObject jsonObject=new JSONObject(responseOutput);
                JSONArray jsonArray = jsonObject.getJSONArray("data");
                PlanoramaBO planoramaBO;
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject jsonData=(JSONObject)jsonArray.get(i);
                    planoramaBO=new PlanoramaBO();

                    JSONObject json_StoreInfo=(JSONObject)jsonData.get("storeInfo");
                    planoramaBO.setRetailerName(json_StoreInfo.get("name").toString());
                    planoramaBO.setRetailerCode(json_StoreInfo.get("code").toString());

                    planoramaBO.setVisitedTime(jsonData.get("visitedOn").toString());
                    planoramaBO.setNumbertOfPhotosExpected((int)jsonData.get("expPhotos"));

                    list_visits.add(planoramaBO);
                }

            }
        }
        catch (Exception ex){
            Commons.printException(ex);
        }
    }

    class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.ViewHolder> {

        private ArrayList<PlanoramaBO> items;

        public RecyclerViewAdapter(ArrayList<PlanoramaBO> items) {
            this.items = items;
        }

        @Override
        public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View v = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.row_planorama, parent, false);

            return new ViewHolder(v);
        }

        @Override
        public void onBindViewHolder(final ViewHolder holder, final int position) {

            holder.textView_retailerName.setText(items.get(position).getRetailerName());
            holder.textView_photoCount.setText(String.valueOf(items.get(position).getNumbertOfPhotosExpected()));

           // String date= DateUtil.convertDateObjectToRequestedFormat(DateUtil.fromISO8601UTC(items.get(position).getVisitedTime()),bModel.configurationMasterHelper.outDateFormat);
            holder.textView_visitedTime.setText(items.get(position).getVisitedTime());

        }


        @Override
        public int getItemCount() {
            return items.size();
        }

        public class ViewHolder extends RecyclerView.ViewHolder {
            TextView textView_retailerName;
            TextView textView_visitedTime,textView_photoCount;

            public ViewHolder(View v) {
                super(v);
                textView_retailerName =  v.findViewById(R.id.textView_store_name);
                textView_visitedTime =  v.findViewById(R.id.textView_visit_time);
                textView_photoCount =  v.findViewById(R.id.textView_photoCount);

                textView_retailerName.setTypeface(FontUtils.getFontRoboto(PlanoramaActivity.this, FontUtils.FontType.MEDIUM));
                textView_visitedTime.setTypeface(FontUtils.getFontRoboto(PlanoramaActivity.this, FontUtils.FontType.LIGHT));

            }


        }
    }
}
