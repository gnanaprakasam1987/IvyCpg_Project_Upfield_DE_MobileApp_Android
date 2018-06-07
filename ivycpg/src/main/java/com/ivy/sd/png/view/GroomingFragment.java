package com.ivy.sd.png.view;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.BitmapShader;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Shader;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.request.target.BitmapImageViewTarget;
import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.StandardListBO;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.CommonDialog;
import com.ivy.sd.png.util.Commons;

import java.util.ArrayList;

/**
 * Created by mayuri.v on 6/8/2017.
 */
public class GroomingFragment extends IvyBaseFragment implements View.OnClickListener {

    private BusinessModel bmodel;
    private RecyclerView thumbnailList;
    private ImageView capturedImg, addImg, dummy_capturedImg;
    private String _imagename = "";
    private static final int CAMERA_REQUEST_CODE = 1;
    private ArrayList<String> imgList = new ArrayList<>();
    private AlertDialog objDialog = null;
    private boolean hide_selectuser_icon = false;
    String _path;
    ImageAdapter imgAdapter;
    String captured_image_path = "";
    private TextView no_pic_text;
    BitmapFactory.Options options;
    private ArrayList<StandardListBO> childList;
    private ArrayAdapter<String> mChildUserNameAdapter;
    private int mSelectedIdIndex = -1;
    private String childUserName = "";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        View view = inflater.inflate(R.layout.fragment_gromming, container, false);

        if (((AppCompatActivity) getActivity()).getSupportActionBar() != null) {
            ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(null);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);
        }


        setScreenTitle(bmodel.getMenuName("MENU_GROOM_CS"));

        LinearLayoutManager layoutManager = new LinearLayoutManager(getActivity(), LinearLayoutManager.HORIZONTAL, false);
        thumbnailList = (RecyclerView) view.findViewById(R.id.thumbnailList);
        thumbnailList.setLayoutManager(layoutManager);
        capturedImg = (ImageView) view.findViewById(R.id.capturedImg);
        addImg = (ImageView) view.findViewById(R.id.addImg);
        no_pic_text = (TextView) view.findViewById(R.id.no_pic_text);
        dummy_capturedImg = (ImageView) view.findViewById(R.id.dummy_capturedImg);
//        remove_btn.setOnDragListener(new MyDragListener());
//        view.setOnDragListener(new MyDragListener());
        addImg.setOnClickListener(this);
        imgAdapter = new ImageAdapter();
        thumbnailList.setAdapter(imgAdapter);

        options = new BitmapFactory.Options();
        options.inSampleSize = 0;
        return view;

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
    }

    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_nonfield_two, menu);

    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);
        //condition to check CNT01
        if (bmodel.configurationMasterHelper.IS_CNT01) {
            if (hide_selectuser_icon) {
                menu.findItem(R.id.menu_select).setVisible(false);
            } else
                menu.findItem(R.id.menu_select).setVisible(true);
        } else {
            menu.findItem(R.id.menu_select).setVisible(false);
        }
        menu.findItem(R.id.menu_add).setVisible(false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i1 = item.getItemId();
        if (i1 == android.R.id.home) {
            Intent i = new Intent(getActivity(), HomeScreenActivity.class);
            startActivity(i);
            getActivity().finish();
            return true;
        } else if (i1 == R.id.menu_select) {
            showUserDialog();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();

        if (bmodel.configurationMasterHelper.IS_CNT01) {
            //if CNT01 is enabled
            if (objDialog != null) {
                if (!objDialog.isShowing()) {
//                    showUserDialog();
                }
            } else {
                showUserDialog();
            }
        } else {
            //if CNT01 is disabled
            loadListData();
        }


    }

    private void showUserDialog() {
        childList = bmodel.mAttendanceHelper.loadChildUserList(getActivity());
        if (childList != null && childList.size() > 0) {
            if (childList.size() > 1) {
                showDialog();
            } else if (childList.size() == 1) {
                hide_selectuser_icon = true;
                bmodel.setSelectedUserId(childList.get(0).getChildUserId());
                loadListData();
            }
        } else {
            hide_selectuser_icon = true;
            bmodel.setSelectedUserId(bmodel.userMasterHelper.getUserMasterBO().getUserid());
            loadListData();
        }

    }

    private void showDialog() {
        mChildUserNameAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.select_dialog_singlechoice);

        for (StandardListBO temp : childList)
            mChildUserNameAdapter.add(temp.getChildUserName());

        AlertDialog.Builder builder;

        builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Select User");
//        int itemid=-1;
//        if(bmodel.getSelectedUserId()!=null)
        builder.setSingleChoiceItems(mChildUserNameAdapter, mSelectedIdIndex,
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int item) {
                        mSelectedIdIndex = item;
                        bmodel.setSelectedUserId(childList.get(item).getChildUserId());
                        childUserName = childList.get(item).getChildUserName();
                        setScreenTitle(bmodel.getMenuName("MENU_GROOM_CS") + " (" +
                                childUserName + ")");
                        hide_selectuser_icon = false;
                        loadListData();
                        dialog.dismiss();
                    }
                });

        objDialog = bmodel.applyAlertDialogTheme(builder);
        objDialog.setCancelable(false);
    }

    private void loadListData() {
        imgList = bmodel.groomingHelper.loadGroomingImage();

        if (imgList != null && imgList.size() > 0) {
            no_pic_text.setVisibility(View.GONE);
            thumbnailList.setVisibility(View.VISIBLE);
            capturedImg.setVisibility(View.VISIBLE);
            capturedImg.setImageBitmap(BitmapFactory.decodeFile(imgList.get(0), options));
            dummy_capturedImg.setVisibility(View.GONE);

            imgAdapter.notifyDataSetChanged();
        } else {
            no_pic_text.setVisibility(View.VISIBLE);
            thumbnailList.setVisibility(View.GONE);
            capturedImg.setVisibility(View.GONE);
            dummy_capturedImg.setVisibility(View.VISIBLE);
        }
//        if (imgList != null && imgList.size() > 0)
//        imgAdapter.notifyDataSetChanged();

    }

    private void loadListDataDeletion() {
        imgList = bmodel.groomingHelper.loadGroomingImage();
        if (imgList != null && imgList.size() > 0) {
            no_pic_text.setVisibility(View.GONE);
            thumbnailList.setVisibility(View.VISIBLE);
            capturedImg.setVisibility(View.VISIBLE);
//            capturedImg.setImageBitmap(BitmapFactory.decodeFile(imgList.get(0), options));
            dummy_capturedImg.setVisibility(View.GONE);

        } else {
            no_pic_text.setVisibility(View.VISIBLE);
            thumbnailList.setVisibility(View.GONE);
            capturedImg.setVisibility(View.GONE);
            capturedImg.setImageResource(0);
            dummy_capturedImg.setVisibility(View.VISIBLE);
        }
        imgAdapter.notifyDataSetChanged();
    }

    @Override
    public void onClick(View v) {
        if (v == addImg) {
            try {

                if (bmodel.groomingHelper.loadGroomingImage().size() < 4) {// allows upto 4 image only
                    int userid = bmodel.userMasterHelper.getUserMasterBO().getUserid();
                    if (bmodel.configurationMasterHelper.IS_CNT01) {
                        userid = bmodel.getSelectedUserId();
                    }
                    Intent intent = new Intent(getActivity(), CameraActivity.class);
                    _imagename = "GROM_"
                            + bmodel.getRetailerMasterBO()
                            .getRetailerID() + "_" + userid + "_"
                            + Commons.now(Commons.DATE_TIME)
                            + "_img.jpg";
                    _path = HomeScreenFragment.photoPath + "/"
                            + _imagename;
//                intent.putExtra("quality", 40);
                    intent.putExtra("path", _path);
                    startActivityForResult(intent, CAMERA_REQUEST_CODE);
                } else {
                    bmodel.showAlert(getResources().getString(R.string.maximum_count_is_exceed), 0);
                }
            } catch (Exception ex) {
                Commons.printException("grooming camera action", ex);
            }
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == CAMERA_REQUEST_CODE) {
            String aws_path = "Grooming/"
                    + bmodel.userMasterHelper.getUserMasterBO().getDownloadDate()
                    .replace("/", "") + "/"
                    + bmodel.userMasterHelper.getUserMasterBO().getUserid() + "/" + _imagename;
            if (resultCode == 1) {
                int header_count = bmodel.groomingHelper.loadGroomingHeader();
                if (header_count > 0) {
                    bmodel.groomingHelper.saveGroomingDetail(_path, aws_path);
                } else {
                    bmodel.groomingHelper.saveGroomingHeader();
                    bmodel.groomingHelper.saveGroomingDetail(_path, aws_path);
                }
                loadListData();
                capturedImg.setVisibility(View.VISIBLE);
                capturedImg.setImageBitmap(BitmapFactory.decodeFile(_path, options));
            }

        }
    }


    public class ImageAdapter extends RecyclerView.Adapter<ImageAdapter.MyViewHolder> {

        public class MyViewHolder extends RecyclerView.ViewHolder {
            public ImageView img, deleteImg;
            String path;
            Integer position;

            public MyViewHolder(View view) {
                super(view);
                img = (ImageView) view.findViewById(R.id.img);
                deleteImg = (ImageView) view.findViewById(R.id.deleteImg);

            }
        }


        @Override
        public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View itemView = LayoutInflater.from(parent.getContext())
                    .inflate(R.layout.grooming_recycler_img, parent, false);

            return new MyViewHolder(itemView);
        }

        @Override
        public void onBindViewHolder(final MyViewHolder holder, int position) {
//            holder.img.setImageBitmap(BitmapFactory.decodeFile(imgList.get(position), options));

            if (imgList.get(position) != null && !imgList.get(position).equals("")) {
                Bitmap defaultIcon = BitmapFactory.decodeResource(getResources(), R.drawable.ic_photo_camera);
                Glide.with(getActivity()).load(imgList.get(position)).asBitmap().centerCrop().placeholder(R.drawable.ic_photo_camera).into(new BitmapImageViewTarget(holder.img) {
                    @Override
                    protected void setResource(Bitmap resource) {
                        holder.img.setImageDrawable(new BitmapDrawable(getResources(), resource));
                    }
                });
            }
            holder.path = imgList.get(position);
            holder.position = position;
            holder.img.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    capturedImg.setVisibility(View.VISIBLE);
                    capturedImg.setImageBitmap(BitmapFactory.decodeFile(holder.path, options));
                    captured_image_path = holder.path;
                }
            });
            holder.img.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View v) {
                    final View view = v;
                    new CommonDialog(bmodel, getActivity(), "", "Image would be deleted", false, "OK", "Cancel", new CommonDialog.positiveOnClickListener() {
                        @Override
                        public void onPositiveButtonClick() {
                            bmodel.groomingHelper.deleteGroomingEntry(holder.path);
                            if (captured_image_path.equalsIgnoreCase(holder.path)) {
                                loadListData();
                            } else {
                                loadListDataDeletion();
                            }
                            Toast.makeText(getActivity(),
                                    "Image deleted successfully",
                                    Toast.LENGTH_SHORT).show();
                        }
                    }, new CommonDialog.negativeOnClickListener() {
                        @Override
                        public void onNegativeButtonClick() {
                        }

                    }).show();
                    return false;
                }
            });
        }

        @Override
        public int getItemCount() {
            return imgList.size();
        }

    }

    private Bitmap getCircularBitmapFrom(Bitmap source) {
        if (source == null || source.isRecycled()) {
            return null;
        }
        float radius = source.getWidth() > source.getHeight() ? ((float) source
                .getHeight()) / 2f : ((float) source.getWidth()) / 2f;
        Bitmap bitmap = Bitmap.createBitmap(source.getWidth(),
                source.getHeight(), Bitmap.Config.ARGB_8888);

        Paint paint = new Paint();
        BitmapShader shader = new BitmapShader(source, Shader.TileMode.CLAMP,
                Shader.TileMode.CLAMP);
        paint.setShader(shader);
        paint.setAntiAlias(true);

        Canvas canvas = new Canvas(bitmap);
        canvas.drawCircle(source.getWidth() / 2, source.getHeight() / 2,
                radius, paint);

        return bitmap;
    }

}
