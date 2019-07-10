package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import com.google.android.material.textfield.TextInputEditText;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.widget.Toolbar;
import android.util.AttributeSet;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup.LayoutParams;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.ivy.cpg.view.salesreturn.SalesReturnHelper;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.sd.png.util.DataMembers;
import com.ivy.utils.DateTimeUtils;

import java.io.File;
import java.io.FileOutputStream;

public class CaptureSignatureActivity extends IvyBaseActivityNoActionBar {
    private BusinessModel bmodel;
    LinearLayout mContent;
    signature mSignature;

    private AlertDialog.Builder builder;
    private AlertDialog alertDialog;
    Button mClear, mGetSign, mCancel;
    public int count = 1;
    public String imageName = null;
    private Bitmap mBitmap;
    View mView;
    File mypath;

    private EditText yourName;
    private String invoiceNo;  //Get invoice no
    private String PHOTO_PATH = "";
    private String module = "ORDER";
    private boolean SIGN_CAPTURED = false;
    // Drawer Implimentation
    private DrawerLayout mDrawerLayout;
    private String serverPath = "";
    private Toolbar toolbar;
    TextInputEditText contact_name, contact_no;
    SalesReturnHelper salesReturnHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        //this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_signature);

        bmodel = (BusinessModel) getApplicationContext();
        bmodel.setContext(this);

        toolbar = (Toolbar) findViewById(R.id.toolbar);

		/*drawer layout inialization */
        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        // set a custom shadow that overlays the main content when the drawer
        // opens

        if (toolbar != null)
            setSupportActionBar(toolbar);

        getSupportActionBar().setTitle(
                bmodel.configurationMasterHelper.getSignatureTitle());
        getSupportActionBar().setIcon(R.drawable.ic_action_signature);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.START);
        mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow,
                GravityCompat.END);

        mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);


        PHOTO_PATH = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
                + "/" + DataMembers.photoFolderName + "/";
        ContextWrapper cw = new ContextWrapper(getApplicationContext());
        //File directory = cw.getDir(getResources().getString(R.string.external_dir), Context.MODE_PRIVATE);


        prepareDirectory();

        File directory = new File(PHOTO_PATH);


        Intent intent = getIntent();
        if (intent.getExtras() != null) {
            if (intent.getExtras().getString("fromModule") != null) {
                module = intent.getExtras().getString("fromModule");
            }
        }

        if (getIntent().getStringExtra("From") != null || module.equals("DELIVERY")) {
            ((RelativeLayout) findViewById(R.id.contact_det_rl)).setVisibility(View.VISIBLE);
            contact_no = (TextInputEditText) findViewById(R.id.contact_no);
            contact_name = (TextInputEditText) findViewById(R.id.contact_name);
        }
        if (module.equals("DELIVERY")) {
            imageName = "DV__SGN_" + bmodel.getRetailerMasterBO().getRetailerID() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + ".jpg";
            serverPath = "Delivery/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;
        } else if (module.equals("COL_REF")) {
            imageName = "CSign_" + bmodel.getRetailerMasterBO().getRetailerID() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + ".jpg";
            serverPath = "CollectionSignature/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;
        } else if (module.equals("ORDER")) {
            imageName = "SGN_" + bmodel.getRetailerMasterBO().getRetailerID() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + ".jpg";
            serverPath = "Invoice/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;
        } else if (module.equals("SALES_RETURN")) {
            salesReturnHelper = SalesReturnHelper.getInstance(this);
            imageName = "SR_SGN_" + bmodel.getRetailerMasterBO().getRetailerID() + "_" + DateTimeUtils.now(DateTimeUtils.DATE_TIME_ID_MILLIS) + ".jpg";
            serverPath = "SalesReturn/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + imageName;
        }


        mypath = new File(directory, imageName);


        mContent = (LinearLayout) findViewById(R.id.linearLayout);
        mSignature = new signature(this, null);
        mSignature.setBackgroundColor(Color.WHITE);
        mContent.addView(mSignature, LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT);
        mClear = (Button) findViewById(R.id.clear);
        mGetSign = (Button) findViewById(R.id.getsign);
        mGetSign.setEnabled(false);
        mCancel = (Button) findViewById(R.id.cancel);

        mView = mContent;

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_sign, menu);


        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        boolean drawerOpen = mDrawerLayout.isDrawerOpen(GravityCompat.END);

        menu.findItem(R.id.menu_next).setVisible(!drawerOpen);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            if (mDrawerLayout.isDrawerOpen(GravityCompat.END))
                mDrawerLayout.closeDrawers();
            else
                backButtonClick();
            return true;
        } else if (i == R.id.menu_clear) {
            mSignature.clear();
            SIGN_CAPTURED = false;

        } else if (i == R.id.menu_next) {
            if (bmodel.synchronizationHelper.isExternalStorageAvailable()) {
                if (SIGN_CAPTURED) {
                    new saveSignature().execute("");
                } else {
                    Toast.makeText(CaptureSignatureActivity.this, "Please put your signature here", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(CaptureSignatureActivity.this,
                        "SDCard Not Available.", Toast.LENGTH_SHORT)
                        .show();
            }

        } else {
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onDestroy() {
        Commons.print("GetSignature," + "onDestory");
        super.onDestroy();
    }


    private boolean prepareDirectory() {
        try {
            if (makedirs()) {
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            Commons.printException(e);
            Toast.makeText(this, "Could not initiate File System.. Is Sdcard mounted properly?", Toast.LENGTH_LONG).show();
            return false;
        }
    }

    private boolean makedirs() {
        File tempdir = new File(PHOTO_PATH);
        if (!tempdir.exists())
            tempdir.mkdirs();

        return (tempdir.isDirectory());
    }

    public class signature extends View {
        private static final float STROKE_WIDTH = 15f;
        private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
        private Paint paint = new Paint();
        private Path path = new Path();

        private float lastTouchX;
        private float lastTouchY;
        private final RectF dirtyRect = new RectF();

        public signature(Context context, AttributeSet attrs) {
            super(context, attrs);
            paint.setAntiAlias(true);
            paint.setColor(Color.BLACK);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeJoin(Paint.Join.ROUND);
            paint.setStrokeWidth(STROKE_WIDTH);
        }

        public void save(View v) {

            if (mBitmap == null) {
                mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);

            }
            Canvas canvas = new Canvas(mBitmap);
            try {


                if (module.equals("ORDER")) {
                    bmodel.getOrderHeaderBO().setIsSignCaptured(true);

                    bmodel.getOrderHeaderBO().setSignatureName(imageName);
                    bmodel.getOrderHeaderBO().setSignaturePath(serverPath);
                }
                if (module.equals("SALES_RETURN")) {
                    salesReturnHelper.setIsSignCaptured(true);
                    salesReturnHelper.setSignatureName(imageName);
                    salesReturnHelper.setSignaturePath(serverPath);
                }

                FileOutputStream mFileOutStream = new FileOutputStream(mypath);

                v.draw(canvas);
                mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
                mFileOutStream.flush();
                mFileOutStream.close();

                //String url = Images.Media.insertImage(getContentResolver(), mBitmap, "title", null);
                // Log.v("log_tag","url: " + url);


                //In case you want to delete the file
                //boolean deleted = mypath.delete();
                //Log.v("log_tag","deleted: " + mypath.toString() + deleted);
                //If you want to convert the image to string use base64 converter

            } catch (Exception e) {
                Commons.print("log_tag," + e.toString());
            }
        }

        public void clear() {
            path.reset();
            invalidate();
        }

        @Override
        protected void onDraw(Canvas canvas) {
            canvas.drawPath(path, paint);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            float eventX = event.getX();
            float eventY = event.getY();
            mGetSign.setEnabled(true);
            SIGN_CAPTURED = true;   // if put signature,SIGN_CAPTURED is TRUE

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    path.moveTo(eventX, eventY);
                    lastTouchX = eventX;
                    lastTouchY = eventY;
                    return true;

                case MotionEvent.ACTION_MOVE:

                case MotionEvent.ACTION_UP:

                    resetDirtyRect(eventX, eventY);
                    int historySize = event.getHistorySize();
                    for (int i = 0; i < historySize; i++) {
                        float historicalX = event.getHistoricalX(i);
                        float historicalY = event.getHistoricalY(i);
                        expandDirtyRect(historicalX, historicalY);
                        path.lineTo(historicalX, historicalY);
                    }
                    path.lineTo(eventX, eventY);
                    break;

                default:
                    debug("Ignored touch event: " + event.toString());
                    return false;
            }

            invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                    (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                    (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

            lastTouchX = eventX;
            lastTouchY = eventY;

            return true;
        }

        private void debug(String string) {
        }

        private void expandDirtyRect(float historicalX, float historicalY) {
            if (historicalX < dirtyRect.left) {
                dirtyRect.left = historicalX;
            } else if (historicalX > dirtyRect.right) {
                dirtyRect.right = historicalX;
            }

            if (historicalY < dirtyRect.top) {
                dirtyRect.top = historicalY;
            } else if (historicalY > dirtyRect.bottom) {
                dirtyRect.bottom = historicalY;
            }
        }

        private void resetDirtyRect(float eventX, float eventY) {
            dirtyRect.left = Math.min(lastTouchX, eventX);
            dirtyRect.right = Math.max(lastTouchX, eventX);
            dirtyRect.top = Math.min(lastTouchY, eventY);
            dirtyRect.bottom = Math.max(lastTouchY, eventY);
        }
    }

    /**
     * Method to save Signature in sdCard
     */
    private void saveSignature() {
        boolean error = false;
        if (!error) {
            mView.setDrawingCacheEnabled(true);
            mSignature.save(mView);
            Bundle b = new Bundle();
            b.putString("status", "done");

            if (module.equals("ORDER")) {
               /* Intent intent = new Intent(CaptureSignatureActivity.this, OrderSummary.class);
                intent.putExtra("SIGNATURE", true);
                intent.putExtra("PHOTO_PATH", PHOTO_PATH);
                intent.putExtra("IMAGE_NAME", imageName);
                intent.putExtras(b);
                startActivity(intent);
                setResult(RESULT_OK, intent);*/
                finish();
            } else if (module.equals("DELIVERY") || module.equals("COL_REF")) {

                Intent intent = new Intent();
                intent.putExtra("SIGNATURE", true);
                intent.putExtra("IMAGE_NAME", imageName);
                intent.putExtra("SERVER_PATH", serverPath);
                if (contact_name != null) {
                    intent.putExtra("CONTACTNAME", contact_name.getText().toString());
                    intent.putExtra("CONTACTNO", contact_no.getText().toString());
                }
                setResult(RESULT_OK, intent);
                finish();
            }
        }
    }

    /**
     * Method to call while click home button
     */
    private void backButtonClick() {
        if (module.equals("ORDER")) {
           /* Intent intent = new Intent(CaptureSignatureActivity.this, OrderSummary.class);
            intent.putExtra("SIGNATURE", false);
            intent.putExtra("PHOTO_PATH", "");
            intent.putExtra("IMAGE_NAME", "");
            startActivity(intent);
            setResult(RESULT_OK, intent);*/
            finish();
        } else if (module.equals("DELIVERY") || module.equals("COL_REF") || module.equals("SALES_RETURN")) {
            finish();
        }
    }

    private class saveSignature extends AsyncTask<String, Void, String> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            builder = new AlertDialog.Builder(CaptureSignatureActivity.this);

            customProgressDialog(builder, getResources().getString(R.string.saving));
            alertDialog = builder.create();
            alertDialog.show();
/*            pd = ProgressDialog.show(CaptureSignatureActivity.this, DataMembers.SD,
                    getResources().getString(R.string.saving), true, false);*/
        }

        @Override
        protected void onPostExecute(String result) {
            super.onPostExecute(result);
            // pd.dismiss();
            alertDialog.dismiss();
            if (module.equals("SALES_RETURN")) {
                finish();
            }
        }

        @Override
        protected String doInBackground(String... params) {
            saveSignature();
            return null;
        }

    }


}