package com.ivy.sd.png.view;

import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Message;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3Object;
import com.ivy.lib.existing.DBUtil;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.MVPBadgeBO;
import com.ivy.sd.png.bo.MvpBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by rajesh.k on 10-12-2015.
 */
public class MVPBadgeFragment extends IvyBaseFragment{
    private BusinessModel bmodel;
    private TextView txt_name,rank, points,txtDesignation;

    LinearLayout linlaHeaderProgress,badgeView;
    private ListView lvBadges;
    private List<MvpBO> mvpData = new ArrayList<>();
    private List<MVPBadgeBO> mMVPBadgesurlList;
    private View view;
    private static final int DOWNLOAD_BUFFER_SIZE = 4096;

    private List<MvpBO> mvpTeamBadgeData = new ArrayList<>();
    private List<MVPBadgeBO> mvpBadgeInfo = new ArrayList<>();

    private String name;
    private int parentID;

    Button mMVPBadgeMoreImgBTN;
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        view=inflater.inflate(R.layout.nav_header, container,
                false);

        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        txt_name = (TextView) view.findViewById(R.id.name);
        txtDesignation=(TextView)view.findViewById(R.id.txtdesig);
        rank = (TextView) view.findViewById(R.id.txtRank);
        points = (TextView) view.findViewById(R.id.txtPoints);
        lvBadges=(ListView)view.findViewById(R.id.lv_batch_update);
       linlaHeaderProgress = (LinearLayout) view.findViewById(R.id.linlaHeaderProgress);
        badgeView = (LinearLayout) view.findViewById(R.id.ll_badge);

        mMVPBadgesurlList=bmodel.mvpHelper.getMVPBadgeUrlList();

        if(bmodel.configurationMasterHelper.IS_TEAMLEAD){
            bmodel.mvpHelper.downloadMVPIdBySuperwisorId();
            List<Integer> mvpIdList = bmodel.mvpHelper.getMvpUserIdList();
            if (mvpIdList != null && mvpIdList.size() > 0) {

                new DownloadDatas().execute(mvpIdList.get(0));

            }
        }else{
            new DownloadDatas().execute(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        }
       /* bmodel.mvpHelper.loadMVPData(bmodel.userMasterHelper.getUserMasterBO().getUserid());
        mvpData = bmodel.mvpHelper.getMvpDataList();*/

        if(mvpBadgeInfo!=null&&mvpBadgeInfo.size()>3){

            mMVPBadgeMoreImgBTN=(Button)view.findViewById(R.id.btn_more_images);
            mMVPBadgeMoreImgBTN.setVisibility(View.VISIBLE);
            mMVPBadgeMoreImgBTN.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    MVPDialogFragment dialog = new MVPDialogFragment();
                    dialog.show(getActivity().getSupportFragmentManager(), "MyDialogFragment");
                }
            });
        }

        for (MvpBO mvp : mvpData) {
            if (mvp.getUserID() == bmodel.userMasterHelper.getUserMasterBO().getUserid()) {
                txt_name.setText(mvp.getUsername()+"");
                rank.setText(mvp.getRank() + "");
                points.setText(mvp.getTotalScore() + "");
                txtDesignation.setText(mvp.getEntitylevel()+"");


            }
        }



    }

    @Override
    public void onResume() {
        super.onResume();
        Commons.print("MVP badge Fragemtn, "+" on Resume called ");

    }
    class DownloadDatas extends AsyncTask<Integer,String,Integer>{

        protected void onPreExecute() {

           linlaHeaderProgress.setVisibility(View.VISIBLE);
        }
        @Override
        protected Integer doInBackground(Integer... params) {
            if(mMVPBadgesurlList!=null&&mMVPBadgesurlList.size()>0) {
                downloadImagesFromAmazon();
            }

            bmodel.mvpHelper.loadMVPData(params[0]);
            mvpData = bmodel.mvpHelper.getMvpDataList();

            mvpTeamBadgeData = bmodel.mvpHelper.getMvpBadgeDataList();

            mvpBadgeInfo=bmodel.mvpHelper.getMvpBadgeInfoList();

            return params[0];
        }

        @Override
        protected void onPostExecute(Integer s) {
            super.onPostExecute(s);
            linlaHeaderProgress.setVisibility(View.GONE);
            badgeView.removeAllViews();
            if(!bmodel.configurationMasterHelper.IS_TEAMLEAD) {
                getBadgeViews(s);
            }
            lvBadges.setAdapter(new MyAdapter(mMVPBadgesurlList));
        }
    }

    private void downloadImagesFromAmazon(){
        {
            Message msg;

            try {
                boolean isAmazonUpload = false;
                DBUtil db = new DBUtil(getActivity(), DataMembers.DB_NAME,
                        DataMembers.DB_PATH);
                db.createDataBase();
                db.openDataBase();
                Cursor c = db
                        .selectSQL("SELECT flag FROM HHTModuleMaster where hhtCode = 'ISAMAZON_IMGUPLOAD' and flag = 1");
                if (c != null) {
                    while (c.moveToNext()) {
                        isAmazonUpload = true;
                    }
                }
                c.close();
                db.closeDB();



                BufferedInputStream inStream;
                BufferedOutputStream outStream;
                File outFile;
                FileOutputStream fileStream;
                AmazonS3Client s3 = null;

                    System.setProperty("org.xml.sax.driver", "org.xmlpull.v1.sax2.Driver");

                    org.xml.sax.XMLReader reader = org.xml.sax.helpers.XMLReaderFactory.createXMLReader();

//                    Commons.print(ConfigurationMasterHelper.ACCESS_KEY_ID);
//                    Commons.print(ConfigurationMasterHelper.SECRET_KEY);
//                    Commons.print("Buket>>>>" + DataMembers.S3_BUCKET);
                    BasicAWSCredentials myCredentials = new BasicAWSCredentials(ConfigurationMasterHelper.ACCESS_KEY_ID,
                            ConfigurationMasterHelper.SECRET_KEY);
                    s3 = new AmazonS3Client(myCredentials);
                    s3.setEndpoint(DataMembers.S3_BUCKET_REGION);



                for (MVPBadgeBO mvpBadgeBO:mMVPBadgesurlList) {

                    String imagurl = mvpBadgeBO.getBadgeURL();



                        Commons.print("Image url>>>>" + imagurl);
                        S3Object object = s3.getObject(new GetObjectRequest(DataMembers.S3_BUCKET, imagurl));
//                           Commons.print("Content-Type: " + object.getObjectMetadata().getContentType());
                        inStream = new BufferedInputStream(object.getObjectContent(), DOWNLOAD_BUFFER_SIZE);
                        Bitmap bmp = BitmapFactory.decodeStream(inStream);
                        mvpBadgeBO.setBadgeBitmap(bmp);














                }

            } catch (Exception e) {
                Commons.printException(e);


            }
        }
    }

    public void getBadgeViews(int userid) {

        try {
            for (MvpBO mvp : mvpData) {
                if (mvp.getUserID() == userid) {
                    name = mvp.getUsername();
                    rank.setText(mvp.getRank() + "");
                    points.setText(mvp.getTotalScore() + "");
                    txtDesignation.setText(mvp.getEntitylevel()+"");
                    txt_name.setText(mvp.getUsername()+"");
                    parentID = mvp.getParentPosID();
                    for (Integer key : mvp.getBadgeList().keySet()) {
                        for (MVPBadgeBO mvpBadgeBO : mvpBadgeInfo) {
                            if (key == mvpBadgeBO.getBadgeID()) {
                                if (mvp.getBadgeList().get(key) != null) {
                                    mvpBadgeBO.setBadgeCount(mvp.getBadgeList().get(key) + "");
                                }
                            }


                        }
                    }
                    break;


                }
            }

            // add bitmap in MVPBadgeBO object


            int count = 0;
            if (mvpBadgeInfo != null) {
                for (MVPBadgeBO mvpbadge : mvpBadgeInfo) {
                    Bitmap bitmap = getBitMapImage(mvpbadge.getImageName());
                    mvpbadge.setBadgeBitmap(bitmap);

                    if (count > 2) continue;
                    LayoutInflater inflater = getActivity().getLayoutInflater();
                    View view = (View) inflater.inflate(
                            R.layout.mvpbadges_list, null);

                    ImageView iv = (ImageView) view.findViewById(R.id.badge);
                    count++;


                    if (mvpbadge.getBadgeBitmap() != null) {
                        iv.setImageBitmap(mvpbadge.getBadgeBitmap());
                    } else {
                        iv.setImageDrawable(getResources().getDrawable(R.drawable.badge_circle));
                    }


                    ((TextView) view.findViewById(R.id.badge_count)).setText(mvpbadge.getBadgeCount() + "");
                    ((TextView)view.findViewById(R.id.tv_badge_name)).setText(mvpbadge.getBadgeName()+"");
           /* Picasso.with(this)
                    .load(mvp.getBadgeURL()).into(iv);*/
                    badgeView.addView(view);
                }


            }
        }
        catch (Exception ex){

        }
    }
    private Bitmap getBitMapImage(String name){
        File file = new File(
                getActivity().getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS) + "/"
                        + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                        + DataMembers.DIGITAL_CONTENT + "/"
                        + DataMembers.MVP + "/" + name);

        Bitmap bm=decodeFile(file);
        return bm;
    }
    private Bitmap decodeFile(File f){
        try {
            //Decode image size
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f),null,o);

            //The new size we want to scale to
            final int REQUIRED_SIZE=70;

            //Find the correct scale value. It should be the power of 2.
            int width_tmp=o.outWidth, height_tmp=o.outHeight;
            int scale=1;
            while(true){
                if(width_tmp/2<REQUIRED_SIZE || height_tmp/2<REQUIRED_SIZE)
                    break;
                width_tmp/=2;
                height_tmp/=2;
                scale*=2;
            }

            //Decode with inSampleSize
            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize=scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {}
        return null;
    }
    class MyAdapter extends BaseAdapter {
        private List<MVPBadgeBO> items;
        private MyAdapter(List<MVPBadgeBO> mvpBadgeList){
            super();
            this.items=mvpBadgeList;
        }

        @Override
        public int getCount() {
            return items.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            final ViewHolder holder;
            View row = convertView;

            if (row == null) {
                LayoutInflater inflater = getActivity().getLayoutInflater();
                row = inflater
                        .inflate(R.layout.mvpbadges_update_list, parent, false);
                holder = new ViewHolder();
                holder.imgMVPBadgedImage=(ImageView)row.findViewById(R.id.img_badge);
                holder.tvMVPBadgeName=(TextView)row.findViewById(R.id.tv_badge);

                row.setTag(holder);
            }else{
                holder = (ViewHolder) row.getTag();
            }
            holder.mvpBadgeBO=items.get(position);
            if(holder.mvpBadgeBO.getBadgeBitmap()!=null) {
                holder.imgMVPBadgedImage.setImageBitmap(holder.mvpBadgeBO.getBadgeBitmap());
            }
            holder.tvMVPBadgeName.setText(holder.mvpBadgeBO.getBadgeName());


            return row;
        }
    }
    class ViewHolder{
        private MVPBadgeBO mvpBadgeBO;
        private TextView tvMVPBadgeName;
        private ImageView imgMVPBadgedImage;
    }
}
