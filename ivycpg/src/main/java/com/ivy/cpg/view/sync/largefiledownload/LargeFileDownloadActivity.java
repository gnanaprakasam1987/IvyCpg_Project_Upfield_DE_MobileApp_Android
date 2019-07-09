package com.ivy.cpg.view.sync.largefiledownload;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.appcompat.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.util.Commons;
import com.ivy.utils.NetworkUtils;

import java.util.ArrayList;

public class LargeFileDownloadActivity extends IvyBaseActivityNoActionBar {

    private ArrayList<DigitalContentModel> digitalContentBOS =  new ArrayList<>();
    private VideoDownloadListAdapter videoDownloadListAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_large_file_download);

        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle("File Download");
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setDisplayUseLogoEnabled(false);
        }

        digitalContentBOS.addAll(FileDownloadProvider.getInstance(getApplicationContext()).getDigitalDownloadList(getApplicationContext()));

        LocalBroadcastManager.getInstance(this).registerReceiver(broadcastReceiver,
                new IntentFilter("ProgressUpdate"));

        initializeVideoAdapter();
    }

    @Override
    protected void onResume() {
        super.onResume();
        refresh();
    }

    private void initializeVideoAdapter() {

        RecyclerView videoRecyclerView = findViewById(R.id.video_pending_recycler_view);

        LinearLayoutManager layout = new LinearLayoutManager(getApplicationContext(), LinearLayoutManager.VERTICAL,
                false);
        videoRecyclerView.setLayoutManager(layout);

        videoDownloadListAdapter =
                new VideoDownloadListAdapter(getApplicationContext(),digitalContentBOS);
        videoRecyclerView.setAdapter(videoDownloadListAdapter);
        videoRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(this));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_seller_perrpt, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        return super.onPrepareOptionsMenu(menu);
    }

    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {

            case android.R.id.home:
                finish();
                break;
            case R.id.menu_refresh:
                if (NetworkUtils.isNetworkConnected(this)) {
                    refresh();
                }else
                    Toast.makeText(this, getResources().getString(R.string.please_connect_to_internet), Toast.LENGTH_SHORT).show();
                break;
        }
        return true;
    }

    /**
     * Will Resume the download if Process stopped
     * Will not allow refresh if already download in progress
     */
    private void refresh(){

        if(FileDownloadIntentService.isServiceRunning){
            Toast.makeText(this, getResources().getString(R.string.download_progress), Toast.LENGTH_SHORT).show();
        }else if (FileDownloadProvider.getInstance(this).getDigitalContentList() != null
                && FileDownloadProvider.getInstance(this).getDigitalContentList().size() != 0){
            Toast.makeText(this, getResources().getString(R.string.resume_download), Toast.LENGTH_SHORT).show();
            FileDownloadProvider.getInstance(getApplicationContext()).callFileDownload(getApplicationContext());
        }else{
            Toast.makeText(this, getResources().getString(R.string.files_downloaded), Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Update the download progress in list
     */
    private void updateProgress(int id, String downloadDetail,int percent,int digitalId){

        for (DigitalContentModel digitalContentModel : digitalContentBOS){
            if (digitalContentModel.getImageID() == digitalId){
                digitalContentModel.setDownloadDetail(downloadDetail);
                digitalContentModel.setPercent(percent);
                digitalContentModel.setDownloadId(id);
                break;
            }
        }

        videoDownloadListAdapter.notifyDataSetChanged();

    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent ) {
            long bytesCurrent = intent.getLongExtra("bytesCurrent",0);
            long bytesTotal = intent.getLongExtra("bytesTotal",0);
            int downloadId = intent.getIntExtra("downloadId",0);
            int digitalId = intent.getIntExtra("DigitalId",0);
            int percent = intent.getIntExtra("Percent",0);
            String downloadDetail = intent.getStringExtra("DownloadDetail");
            Commons.print("Received data : "+bytesCurrent+"--"+bytesTotal+"---"+downloadId);

            updateProgress(downloadId, downloadDetail, percent,digitalId);
        }
    };

    public class SimpleDividerItemDecoration extends RecyclerView.ItemDecoration {
        private Drawable mDivider;

        private SimpleDividerItemDecoration(Context context) {
            mDivider = ContextCompat.getDrawable(context,R.drawable.line_divider);
        }

        @Override
        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

}
