package com.ivy.cpg.view.order.scheme;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ivy.cpg.view.digitalcontent.DigitalContentActivity;
import com.ivy.cpg.view.digitalcontent.DigitalContentHelper;
import com.ivy.cpg.view.order.OrderSummary;
import com.ivy.cpg.view.order.StockAndOrder;
import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.ProductMasterBO;
import com.ivy.sd.png.bo.SchemeBO;
import com.ivy.sd.png.bo.SchemeProductBO;
import com.ivy.sd.png.commons.IvyBaseActivityNoActionBar;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;
import com.ivy.sd.png.view.BatchAllocation;
import com.ivy.sd.png.view.CrownReturnActivity;
import com.ivy.sd.png.view.InitiativeActivity;
import com.ivy.sd.png.view.OrderDiscount;
import com.ivy.sd.png.view.ProductSchemeDetailsActivity;

import java.util.ArrayList;
import java.util.Vector;

public class UpSellingActivity extends IvyBaseActivityNoActionBar implements View.OnClickListener {

    ArrayList<String> nearestSchemes;
    Vector<ProductMasterBO> productList;
    SchemeDetailsMasterHelper schemeHelper;
    BusinessModel bModel;

    LinearLayout layout_parent;

    private static final String SALES_VALUE = "SV";
    private static final String QUANTITY_TYPE = "QTY";

    Button button_next,button_edit;

    Toolbar toolbar;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_upselling);

        toolbar=findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        if (getSupportActionBar()!=null) {
            getSupportActionBar().setDisplayShowTitleEnabled(false);
            setScreenTitle("Nearest Schemes");
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }

        bModel=(BusinessModel)getApplicationContext();
        layout_parent=findViewById(R.id.layout_parent);
        button_next=findViewById(R.id.btn_next);button_next.setOnClickListener(this);
        button_edit=findViewById(R.id.btn_edit_order);button_edit.setOnClickListener(this);

        schemeHelper=SchemeDetailsMasterHelper.getInstance(this);
        productList=new Vector<>();

        if(getIntent().getExtras().containsKey("nearestSchemes"))
            nearestSchemes=getIntent().getExtras().getStringArrayList("nearestSchemes");

        if(nearestSchemes!=null&&nearestSchemes.size()>0)
          updateView();

    }

    private void updateView(){

        LayoutInflater inflater = LayoutInflater.from(this);
        View view_parent;
        for(String schemeId:nearestSchemes){
            final SchemeBO schemeBO=schemeHelper.getSchemeById().get(schemeId);
            if(schemeBO!=null){

                view_parent = inflater.inflate(R.layout.row_upselling, null);
                TextView text_slabName=view_parent.findViewById(R.id.text_slab_name);
                TextView label_product=view_parent.findViewById(R.id.label_product);updateFont(label_product,0);
                TextView label_ordered=view_parent.findViewById(R.id.label_ordered);updateFont(label_ordered,0);
                TextView label_to_add=view_parent.findViewById(R.id.label_to_add);updateFont(label_to_add,0);
                TextView label_text_any=view_parent.findViewById(R.id.text_any);updateFont(label_text_any,1);
                (view_parent.findViewById(R.id.view_dotted_line)).setLayerType(View.LAYER_TYPE_SOFTWARE, null);
                text_slabName.setText(schemeBO.getScheme());updateFont(text_slabName,0);

                View view=view_parent.findViewById(R.id.image_view_info);
               // view.setTag(1,schemeBO.getSchemeId());
               // view.setTag(2,schemeBO.getBuyingProducts().get(0).getProductId());
                view.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(UpSellingActivity.this, ProductSchemeDetailsActivity.class);
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                        intent.putExtra("slabId",schemeBO.getSchemeId());
                        intent.putExtra("productId",schemeBO.getBuyingProducts().get(0).getProductId());
                        intent.putExtra("isFromUpSelling",true);
                        startActivity(intent);
                    }
                });
               // view.setOnClickListener(this);

                LinearLayout layout_products=view_parent.findViewById(R.id.layout_products);

                for(SchemeProductBO schemeProductBO:schemeBO.getBuyingProducts()) {
                    View view_products = inflater.inflate(R.layout.row_upselling_products, null);

                    TextView text_productName=view_products.findViewById(R.id.text_product);updateFont(text_productName,1);
                    TextView text_ordered=view_products.findViewById(R.id.text_ordered);updateFont(text_ordered,1);
                    TextView text_toAdd=view_products.findViewById(R.id.text_add);updateFont(text_toAdd,1);

                    ProductMasterBO productMasterBO=bModel.productHelper.getProductMasterBOById(schemeProductBO.getProductId());
                    text_productName.setText(productMasterBO.getProductShortName());

                    if(schemeBO.getBuyType().equals(QUANTITY_TYPE)) {
                        int ordered = productMasterBO.getOrderedPcsQty() + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCaseSize()) + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOutersize());
                        text_ordered.setText(String.valueOf(ordered));

                        double toAdd=(schemeProductBO.getBuyQty()-ordered);
                        if(toAdd<0)
                            toAdd=0;
                        text_toAdd.setText(String.valueOf((int)toAdd));

                    }
                    else if(schemeBO.getBuyType().equals(SALES_VALUE)){
                        double ordered = (productMasterBO.getOrderedPcsQty() * productMasterBO.getSrp()) + (productMasterBO.getOrderedCaseQty() * productMasterBO.getCsrp()) + (productMasterBO.getOrderedOuterQty() * productMasterBO.getOsrp());
                        text_ordered.setText(String.valueOf(ordered));

                        double toAdd=(schemeProductBO.getBuyQty()-ordered);
                        if(toAdd<0)
                            toAdd=0;
                        text_toAdd.setText(bModel.formatValue(toAdd));
                    }

                    label_product.setText("Product("+schemeProductBO.getUomDescription()+")");
                   // label_to_add.setText("Add("+schemeProductBO.getUomDescription()+")");
                    if(schemeProductBO.getGroupLogic().equals("ANY")){
                        text_toAdd.setVisibility(View.GONE);
                        label_to_add.setVisibility(View.GONE);
                        label_text_any.setVisibility(View.VISIBLE);

                        if(schemeBO.getBuyType().equals(QUANTITY_TYPE))
                        label_text_any.setText("Need "+(int)schemeProductBO.getBuyQty()+" Quantity");
                        else label_text_any.setText("Need Amount "+(int)schemeProductBO.getBuyQty());
                    }

                    layout_products.addView(view_products);


                }


                layout_parent.addView(view_parent);



            }
        }

    }

    private void updateFont(TextView textView, int flag){

        if(flag==0) {
            textView.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.MEDIUM));
        }
        else  if(flag==1){
            textView.setTypeface(bModel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));
        }

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int i = item.getItemId();
        if (i == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);

    }

    @Override
    public void onClick(View view) {
        if(view.getId()==button_next.getId()){
           setResult(RESULT_OK);
           finish();

        }
        else if(view.getId()==button_edit.getId()){
            finish();
        }
        else if(view.getId()==R.id.image_view_info){
            Intent intent = new Intent(UpSellingActivity.this, ProductSchemeDetailsActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra("slabId",(String)view.getTag(1));
            intent.putExtra("productId",(String)view.getTag(2));
            intent.putExtra("isFromUpSelling",true);
            startActivity(intent);
        }
    }
}
