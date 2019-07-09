package com.ivy.cpg.view.roadactivity;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.sd.camera.CameraActivity;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.commons.IvyBaseFragment;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.util.Commons;
import com.ivy.cpg.view.homescreen.HomeScreenActivity;
import com.ivy.utils.FileUtils;

import java.util.Vector;

@SuppressLint("NewApi")
public class RoadFragment extends IvyBaseFragment {

    private Spinner type;
    private Spinner product;
    private Spinner location1;
    private Spinner location2;
    private EditText remarks;
    private ArrayAdapter<RoadActivityBO> location2Adapter;
    private BusinessModel bmodel;
    private String categoryname, LocationName1, LocationName2;
    private String mImageName;
    private static final int RESULT_SAVE = 1;
    private int perPhotoCount;
    private Vector<String> imageNames = new Vector<>();
    int moduleMaxCount = 0;
    private View view;
    private RoadActivityHelper roadActivityHelper;
    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());
        roadActivityHelper = RoadActivityHelper.getInstance(getContext());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        view = inflater.inflate(R.layout.fragment_road, container, false);
        bmodel = (BusinessModel) getActivity().getApplicationContext();
        bmodel.setContext(getActivity());

        perPhotoCount = 0;

        ActionBar actionBar = ((AppCompatActivity) getActivity()).getSupportActionBar();
        if (actionBar != null) {


            actionBar.setDisplayShowTitleEnabled(false);
            actionBar.setIcon(null);
            actionBar.setElevation(0);
        }
        setScreenTitle(getArguments().getString("screentitle"));

        TextView tv_roadact_category =  view.findViewById(R.id.tv_roadact_category);
        TextView tv_roadact_location1 =  view.findViewById(R.id.tv_roadact_location1);
        TextView tv_roadact_location2 =  view.findViewById(R.id.tv_roadact_location2);

        imageNames = new Vector<>();

        type =  view.findViewById(R.id.type);
        product =  view.findViewById(R.id.category);
        location1 =  view.findViewById(R.id.province);
        location2 =  view.findViewById(R.id.district);
        remarks =  view.findViewById(R.id.remarks);

        categoryname = roadActivityHelper.getProductName();
        tv_roadact_category.setText(getResources().getString(R.string.category));
        LocationName1 = roadActivityHelper.getLocationName1();
        tv_roadact_location1.setText(getResources().getString(R.string.province));
        LocationName2 = roadActivityHelper.getLocationName2();
        tv_roadact_location2.setText(getResources().getString(R.string.district));

        ArrayAdapter<RoadActivityBO> typeAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        typeAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<RoadActivityBO> productAdapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        productAdapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        ArrayAdapter<RoadActivityBO> location1Adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        location1Adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        location2Adapter = new ArrayAdapter<>(getActivity(),
                android.R.layout.simple_spinner_item);
        location2Adapter
                .setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        typeAdapter.addAll(roadActivityHelper.getTypeSpinnerData());
        type.setAdapter(typeAdapter);
        productAdapter.addAll(roadActivityHelper
                .getProductSpinnerData());
        product.setAdapter(productAdapter);


        moduleMaxCount = bmodel.configurationMasterHelper.raPhotoCount;

        if (bmodel.configurationMasterHelper.photocount != 0 && (bmodel.configurationMasterHelper.photocount < bmodel.configurationMasterHelper.raPhotoCount)) {
            moduleMaxCount = bmodel.configurationMasterHelper.photocount;
        }

        location1Adapter.addAll(roadActivityHelper
                .getLocation1SpinnerData());
        location1.setAdapter(location1Adapter);
        location1.setSelection(0);
        location1.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {

                RoadActivityBO roadBO = (RoadActivityBO) parent
                        .getSelectedItem();
                loadLoc2Spinner(roadBO.getId());
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        type.setSelection(0);
        type.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });
        product.setSelection(0);
        product.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        location2.setSelection(0);
        location2.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {

            @Override
            public void onItemSelected(AdapterView<?> parent, View view,
                                       int position, long id) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // TODO Auto-generated method stub

            }
        });

        return view;
    }

    public void loadLoc2Spinner(int parentId) {
        location2Adapter.clear();
        Vector<RoadActivityBO> listBo = new Vector<>();
        for (RoadActivityBO roadBO : roadActivityHelper
                .getLocation2SpinnerData()) {
            if (roadBO.getPid() == parentId || roadBO.getPid() == 0) {
                listBo.add(roadBO);
            }
        }

        location2Adapter.addAll(listBo);
        location2.setAdapter(location2Adapter);

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_roadactivity, menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int i = item.getItemId();
        if (i == android.R.id.home) {
            startActivity(new Intent(getActivity(),
                    HomeScreenActivity.class));
            getActivity().finish();
            return true;
        } else if (i == R.id.menu_gallery) {
            Intent mIntent = new Intent(getActivity(),
                    AdhocGallery.class);
            startActivity(mIntent);
            return true;
        } else if (i == R.id.menu_photo) {
            if (perPhotoCount == 5) {
                Toast.makeText(getActivity(),
                        String.format(
                                getResources()
                                        .getString(
                                                R.string.you_reached_limit),
                                perPhotoCount),
                        Toast.LENGTH_SHORT).show();
            } else {
                int maxImgCount = bmodel.getAdhocimgCount() + imageNames.size();

                if (moduleMaxCount <= maxImgCount) {
                    Toast.makeText(getActivity(),
                            String.format(
                                    getResources()
                                            .getString(
                                                    R.string.kindlyuploadorsync),
                                    moduleMaxCount),
                            Toast.LENGTH_SHORT).show();
                } else {
                    if (FileUtils.isExternalStorageAvailable(10)) {
                        mImageName = "RA_"
                                + bmodel.userMasterHelper.getUserMasterBO().getUserid()
                                + "_" + Commons.now(Commons.DATE_TIME)
                                + "_img.jpg";

                        Intent intent = new Intent(getActivity(), CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String mImagePath = FileUtils.photoFolderPath + "/" + mImageName;
                        Commons.print("photoPath : " + mImagePath);
                        intent.putExtra(CameraActivity.PATH, mImagePath);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);

                        return true;

                    } else {
                        Toast.makeText(getActivity(),
                                R.string.sdcard_is_not_ready_to_capture_img,
                                Toast.LENGTH_SHORT).show();
                    }
                }
            }
            return true;
        } else if (i == R.id.menu_roadact_save) {
            if (validate()) {
                roadActivityHelper.saveRoadActivity(((RoadActivityBO) type.getSelectedItem()).getId() + "", ((RoadActivityBO) product.getSelectedItem()).getId() + "",
                        ((RoadActivityBO) location2.getSelectedItem()).getId() + "", remarks.getText().toString(), imageNames);
                perPhotoCount = 0;
                type.setSelection(0);
                product.setSelection(0);
                location1.setSelection(0);
                location2.setSelection(0);
                remarks.setText("");
                mImageName = null;
                imageNames.removeAllElements();
                Toast.makeText(getActivity(),
                        getResources().getString(R.string.saved_successfully),
                        Toast.LENGTH_SHORT).show();
            }

            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        unbindDrawables(view.findViewById(R.id.root));
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

    public boolean validate() {
        boolean bool = true;
        String value = null;
        if (type.getSelectedItem().toString()
                .equalsIgnoreCase(getResources().getString(R.string.select))) {
            bool = false;
            value = getResources().getString(R.string.choose) + " "
                    + getResources().getString(R.string.type);
        } else if (product.getSelectedItem().toString()
                .equalsIgnoreCase(getResources().getString(R.string.select))) {
            bool = false;
            value = getResources().getString(R.string.choose) + " "
                    + categoryname;
        } else if (location1.getSelectedItem().toString()
                .equalsIgnoreCase(getResources().getString(R.string.select))) {
            bool = false;
            value = getResources().getString(R.string.choose) + " "
                    + LocationName1;
        } else if (location2.getSelectedItem().toString()
                .equalsIgnoreCase(getResources().getString(R.string.select))) {
            bool = false;
            value = getResources().getString(R.string.choose) + " "
                    + LocationName2;
        } else if (remarks.getText().toString().length() == 0) {
            bool = false;
            value = getResources().getString(R.string.enter) + " "
                    + getResources().getString(R.string.remarks_label);
        } else if (imageNames.size() == 0) {
            bool = false;
            value = getResources().getString(R.string.capture) + " "
                    + getResources().getString(R.string.photo);
        }

        if (!bool) {
            Toast.makeText(getActivity(), value, Toast.LENGTH_SHORT)
                    .show();
        }

        return bool;

    }

    protected Dialog onCreateDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity())
                .setIcon(null)
                .setCancelable(false)
                .setTitle(
                        getResources().getString(R.string.doyouwantgoback))
                .setPositiveButton("Ok",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {

                                startActivity(new Intent(getActivity(),
                                        HomeScreenActivity.class));
                                getActivity().finish();

                            }
                        })
                .setNegativeButton("Cancel",
                        new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog,
                                                int whichButton) {
                            }
                        });
        bmodel.applyAlertDialogTheme(builder);
        return null;
    }

    public void showFileDeleteAlert(final String imageNameStarts) {

        int mImageCount = 1;
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("");
        builder.setMessage(getResources().getString(R.string.word_already)
                + mImageCount
                + getResources().getString(
                R.string.word_photocaptured_delete_retake));

        builder.setPositiveButton(getResources().getString(R.string.yes),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {

                        bmodel.deleteFiles(FileUtils.photoFolderPath,
                                imageNameStarts);
                        dialog.dismiss();
                        Intent intent = new Intent(getActivity(),
                                CameraActivity.class);
                        intent.putExtra(CameraActivity.QUALITY, 40);
                        String _path = FileUtils.photoFolderPath + "/" + mImageName;
                        Commons.print("PhotoPAth:  -      " + _path);
                        intent.putExtra(CameraActivity.PATH, _path);
                        startActivityForResult(intent,
                                bmodel.CAMERA_REQUEST_CODE);
                    }
                });

        builder.setNegativeButton(getResources().getString(R.string.no),
                new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

        builder.setCancelable(false);
        bmodel.applyAlertDialogTheme(builder);
    }

    public Vector<String> getImageNames() {
        return imageNames;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        // check if the request code is same as what is passed here it is 2
        if (requestCode == RESULT_SAVE) {
            if (resultCode == 1) {
                perPhotoCount = perPhotoCount + 1;
                getImageNames().add(mImageName);

                for (int i = 0; i < getImageNames().size(); i++) {
                    Commons.print("IMAGE NAME:"
                            + getImageNames().get(i));
                }
            } else {
                Commons.print("IMAGE NAME:," + "Camers Activity : Canceled");
            }
        }
    }
}
