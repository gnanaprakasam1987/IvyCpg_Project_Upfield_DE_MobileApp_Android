package com.ivy.cpg.view.salesreturn;

import android.os.Bundle;
import androidx.appcompat.widget.Toolbar;

import androidx.fragment.app.FragmentManager;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.model.BrandDialogInterface;
import com.ivy.sd.png.model.FiveLevelFilterCallBack;
import com.ivy.sd.png.model.ProductSearchCallBack;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

public class SalesReturnActivity extends IvyBaseActivityNoActionBar implements BrandDialogInterface,FiveLevelFilterCallBack, SalesReturnAdapter.SalesReturnInterface, ProductSearchCallBack {

    private Toolbar toolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_sales_return);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(null);
            getSupportActionBar().setDisplayShowTitleEnabled(false);
        }
        overridePendingTransition(R.anim.trans_left_in, R.anim.trans_left_out);
    }


    @Override
    public void updateBrandText(String mFilterText, int id) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateBrandText(mFilterText, id);
    }

    @Override
    public void updateGeneralText(String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateGeneralText(mFilterText);
    }

    @Override
    public void updateCancel() {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateCancel();
    }

    @Override
    public void updateFromFiveLevelFilter(int mProductId, HashMap<Integer, Integer> mSelectedIdByLevelId, ArrayList<Integer> mAttributeProducts, String mFilterText) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.updateFromFiveLevelFilter(mProductId, mSelectedIdByLevelId, mAttributeProducts, mFilterText);
    }

    @Override
    public void onListItemSelected(String pid) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.onListItemSelected(pid);
    }

    @Override
    public void showSalesReturnDialog(String pid) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.showSalesReturnDialog(pid);
    }

    @Override
    public void productSearchResult(Vector<ProductMasterBO> searchedList) {
        FragmentManager fm = getSupportFragmentManager();
        SalesReturnFragment fragment = (SalesReturnFragment) fm
                .findFragmentById(R.id.sales_return_fragment);
        fragment.productSearchResult(searchedList);
    }
}
