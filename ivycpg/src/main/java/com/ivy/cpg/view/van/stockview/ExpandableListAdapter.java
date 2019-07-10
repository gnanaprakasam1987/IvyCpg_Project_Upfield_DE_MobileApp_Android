package com.ivy.cpg.view.van.stockview;

import android.content.Context;
import androidx.core.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.bo.LoadManagementBO;
import com.ivy.sd.png.commons.SDUtil;
import com.ivy.sd.png.model.BusinessModel;

import java.util.ArrayList;
import java.util.HashMap;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
    private Context context;
    private ArrayList<LoadManagementBO> listDataHeader; // header titles
    // child data in format of header title, child title
    private HashMap<String, ArrayList<LoadManagementBO>> listDataChild;
    private BusinessModel bModel;
    private StockViewInterface stockViewInterface;

    ExpandableListAdapter(Context context, ArrayList<LoadManagementBO> listDataHeader,
                          HashMap<String, ArrayList<LoadManagementBO>> listChildData, StockViewInterface stockViewInterface) {
        this.context = context;
        this.listDataHeader = listDataHeader;
        this.listDataChild = listChildData;
        bModel = (BusinessModel) context.getApplicationContext();
        this.stockViewInterface = stockViewInterface;

    }

    @Override
    public int getGroupCount() {
        return this.listDataHeader.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        String proid = String.valueOf(this.listDataHeader.get(groupPosition).getProductid());
        if (this.listDataHeader.get(groupPosition).getIsFree() == 1)
            proid = proid + "F";
        return this.listDataChild.get(proid)
                .size();
    }

    @Override
    public Object getGroup(int groupPosition) {
        return this.listDataHeader.get(groupPosition);
    }

    @Override
    public Object getChild(int groupPosition, int childPosition) {
        String keyPid = this.listDataHeader.get(groupPosition).getProductid() + "";
        if (this.listDataHeader.get(groupPosition).getIsFree() == 1)
            keyPid = keyPid + "F";
        return this.listDataChild.get(keyPid)
                .get(childPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(final int groupPosition, final boolean isExpanded, final View convertView, final ViewGroup parent) {
        LoadManagementBO groupBoObj;
        String tv;
        groupBoObj = (LoadManagementBO) getGroup(groupPosition);
        final GroupViewHolder holder;
        View row = convertView;

        if (row == null) {
            LayoutInflater inflater = ((StockViewActivity) context).getLayoutInflater();
            row = inflater
                    .inflate(R.layout.row_stock_report, parent, false);
            holder = new GroupViewHolder();

            holder.psname = row.findViewById(R.id.orderPRODNAME);
            holder.psname.setMaxLines(bModel.configurationMasterHelper.MAX_NO_OF_PRODUCT_LINES);
            holder.sihCase = row.findViewById(R.id.sih_case);
            holder.sihOuter = row.findViewById(R.id.sih_outer);
            holder.sih = row.findViewById(R.id.sih);
            holder.prodcode = row.findViewById(R.id.prdcode);

            if (bModel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.sihCase.setVisibility(View.GONE);
                if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.sihOuter.setVisibility(View.GONE);
                if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.sih.setVisibility(View.GONE);
            } else {
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
            }

            if (!bModel.configurationMasterHelper.IS_SHOW_SKU_CODE)
                holder.prodcode.setVisibility(View.GONE);


            row.setOnClickListener(new View.OnClickListener() {
                public void onClick(View v) {
                    stockViewInterface.onRowClick(holder.pname);
                }
            });
            row.setTag(holder);
        } else {
            holder = (GroupViewHolder) row.getTag();
        }

        holder.psname.setText(groupBoObj.getProductshortname());

        if (groupBoObj.getIsFree() == 1)
            holder.psname.setTextColor(ContextCompat.getColor(context,
                    R.color.colorAccent));
        else
            holder.psname.setTextColor(ContextCompat.getColor(context,
                    android.R.color.black));

        holder.pname = groupBoObj.getProductname();
        if (bModel.configurationMasterHelper.IS_SHOW_SKU_CODE) {
            String prodCode = context.getString(R.string.prod_code) + ": " +
                    groupBoObj.getProductCode() + " ";
            if (bModel.labelsMasterHelper.applyLabels(holder.prodcode.getTag()) != null)
                prodCode = bModel.labelsMasterHelper
                        .applyLabels(holder.prodcode.getTag()) + ": " +
                        groupBoObj.getProductCode() + " ";
            holder.prodcode.setText(prodCode);
        }

        if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_OU ||
                bModel.configurationMasterHelper.CONVERT_STOCK_SIH_CS ||
                bModel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
            holder.sihCase.setVisibility(View.GONE);
            holder.sihOuter.setVisibility(View.GONE);
            if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_OU) {
                if (groupBoObj.getOuterSize() != 0) {
                    tv = SDUtil.mathRoundoff((double) groupBoObj.getStocksih() / groupBoObj.getOuterSize()) + "";
                    holder.sih.setText(tv);
                } else {
                    tv = groupBoObj.getStocksih() + "";
                    holder.sih.setText(tv);

                }
            } else if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_CS) {
                if (groupBoObj.getCaseSize() != 0) {
                    tv = SDUtil.mathRoundoff((double) groupBoObj.getStocksih() / groupBoObj.getCaseSize()) + "";
                    holder.sih.setText(tv);
                } else {
                    tv = groupBoObj.getStocksih() + "";
                    holder.sih.setText(tv);

                }
            } else {
                tv = groupBoObj.getStocksih() + "";
                holder.sih.setText(tv);

            }
        } else if (bModel.configurationMasterHelper.SHOW_SIH_SPLIT) {
            if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (groupBoObj.getSih() == 0) {
                    holder.sihCase.setText("0");
                    holder.sihOuter.setText("0");
                    holder.sih.setText("0");
                } else if (groupBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                    if (groupBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                        tv = groupBoObj.getSih() + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                        tv = groupBoObj.getSih()
                                % groupBoObj.getOuterSize() + "";
                        holder.sih.setText(tv);
                    }
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    if (groupBoObj.getOuterSize() > 0
                            && (groupBoObj.getSih() % groupBoObj.getCaseSize()) >= groupBoObj
                            .getOuterSize()) {
                        tv = (groupBoObj.getSih() % groupBoObj
                                .getCaseSize())
                                / groupBoObj.getOuterSize()
                                + "";
                        holder.sihOuter.setText(tv);
                        tv = (groupBoObj.getSih() % groupBoObj
                                .getCaseSize())
                                % groupBoObj.getOuterSize()
                                + "";
                        holder.sih.setText(tv);
                    } else {
                        holder.sihOuter.setText("0");
                        tv = groupBoObj.getSih()
                                % groupBoObj.getCaseSize() + "";
                        holder.sih.setText(tv);
                    }
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (groupBoObj.getSih() == 0) {
                    holder.sihCase.setText("0");
                    holder.sihOuter.setText("0");
                } else if (groupBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                    if (groupBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                    } else {
                        tv = groupBoObj.getSih()
                                / groupBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                    }
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    if (groupBoObj.getOuterSize() > 0
                            && (groupBoObj.getSih() % groupBoObj.getCaseSize()) >= groupBoObj
                            .getOuterSize()) {
                        tv = (groupBoObj.getSih() % groupBoObj
                                .getCaseSize())
                                / groupBoObj.getOuterSize()
                                + "";
                        holder.sihOuter.setText(tv);
                    } else {
                        holder.sihOuter.setText("0");
                    }
                }
            } else if (bModel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (groupBoObj.getSih() == 0) {
                    holder.sih.setText("0");
                    holder.sihOuter.setText("0");
                } else if (groupBoObj.getOuterSize() == 0) {
                    tv = groupBoObj.getSih() + "";
                    holder.sih.setText(tv);
                    holder.sihOuter.setText("0");
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getOuterSize() + "";
                    holder.sihOuter.setText(tv);
                    tv = groupBoObj.getSih()
                            % groupBoObj.getOuterSize() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (groupBoObj.getSih() == 0) {
                    holder.sih.setText("0");
                    holder.sihCase.setText("0");
                } else if (groupBoObj.getCaseSize() == 0) {
                    tv = groupBoObj.getSih() + "";
                    holder.sih.setText(tv);
                    holder.sihCase.setText("0");
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    tv = groupBoObj.getSih()
                            % groupBoObj.getCaseSize() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                if (groupBoObj.getSih() == 0) {
                    holder.sihCase.setText("0");
                } else if (groupBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (groupBoObj.getSih() == 0) {
                    holder.sihOuter.setText("0");
                } else if (groupBoObj.getOuterSize() == 0) {
                    holder.sihOuter.setText("0");
                } else {
                    tv = groupBoObj.getSih()
                            / groupBoObj.getOuterSize() + "";
                    holder.sihOuter.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                tv = groupBoObj.getSih() + "";
                holder.sih.setText(tv);
            }
        } else {
            tv = groupBoObj.getSih() + "";
            holder.sih.setText(tv);
        }


        return row;
    }

    @Override
    public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {

        LoadManagementBO childBoObj;
        String tv;
        childBoObj = (LoadManagementBO) getChild(groupPosition, childPosition);
        final ViewHolder holder;
        View row = convertView;
        if (row == null) {

            LayoutInflater inflater = ((StockViewActivity) context).getLayoutInflater();
            row = inflater
                    .inflate(R.layout.custom_child_listitem, parent, false);
            holder = new ViewHolder();

            holder.batchNo = row.findViewById(R.id.batch_no);
            holder.sihCase = row.findViewById(R.id.sih_case);
            holder.sihOuter = row.findViewById(R.id.sih_outer);
            holder.sih = row.findViewById(R.id.sih);

            if (bModel.configurationMasterHelper.SHOW_SIH_SPLIT) {
                if (!bModel.configurationMasterHelper.SHOW_ORDER_CASE)
                    holder.sihCase.setVisibility(View.GONE);
                if (!bModel.configurationMasterHelper.SHOW_OUTER_CASE)
                    holder.sihOuter.setVisibility(View.GONE);
                if (!bModel.configurationMasterHelper.SHOW_ORDER_PCS)
                    holder.sih.setVisibility(View.GONE);
            } else {
                holder.sihCase.setVisibility(View.GONE);
                holder.sihOuter.setVisibility(View.GONE);
            }


            row.setTag(holder);
        } else {
            holder = (ViewHolder) row.getTag();
        }
        tv = context.getString(
                R.string.batch_no)
                + ": " + childBoObj.getBatchNo() + "";
        holder.batchNo.setText(tv);
        if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_OU ||
                bModel.configurationMasterHelper.CONVERT_STOCK_SIH_CS ||
                bModel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
            holder.sihCase.setVisibility(View.GONE);
            holder.sihOuter.setVisibility(View.GONE);
            if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_OU) {
                if (childBoObj.getOuterSize() != 0) {
                    tv = SDUtil.mathRoundoff((double) childBoObj.getStocksih() / childBoObj.getOuterSize()) + "";
                    holder.sih.setText(tv);
                } else {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_CS) {
                if (childBoObj.getCaseSize() != 0) {
                    tv = SDUtil.mathRoundoff((double) childBoObj.getStocksih() / childBoObj.getCaseSize()) + "";
                    holder.sih.setText(tv);
                } else {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.CONVERT_STOCK_SIH_PS) {
                tv = childBoObj.getStocksih() + "";
                holder.sih.setText(tv);
            }

        } else if (bModel.configurationMasterHelper.SHOW_SIH_SPLIT) {
            if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sihCase.setText("0");
                    holder.sihOuter.setText("0");
                    holder.sih.setText("0");
                } else if (childBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                    if (childBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                        tv = childBoObj.getStocksih() + "";
                        holder.sih.setText(tv);
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                        tv = childBoObj.getStocksih()
                                % childBoObj.getOuterSize() + "";
                        holder.sih.setText(tv);
                    }
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    if (childBoObj.getOuterSize() > 0
                            && (childBoObj.getStocksih() % childBoObj.getCaseSize()) >= childBoObj
                            .getOuterSize()) {
                        tv = (childBoObj.getStocksih() % childBoObj
                                .getCaseSize())
                                / childBoObj.getOuterSize()
                                + "";
                        holder.sihOuter.setText(tv);
                        tv = (childBoObj.getStocksih() % childBoObj
                                .getCaseSize())
                                % childBoObj.getOuterSize()
                                + "";
                        holder.sih.setText(tv);
                    } else {
                        holder.sihOuter.setText("0");
                        tv = childBoObj.getStocksih()
                                % childBoObj.getCaseSize() + "";
                        holder.sih.setText(tv);
                    }
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sihCase.setText("0");
                    holder.sihOuter.setText("0");
                } else if (childBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                    if (childBoObj.getOuterSize() == 0) {
                        holder.sihOuter.setText("0");
                    } else {
                        tv = childBoObj.getStocksih()
                                / childBoObj.getOuterSize() + "";
                        holder.sihOuter.setText(tv);
                    }
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    if (childBoObj.getOuterSize() > 0
                            && (childBoObj.getStocksih() % childBoObj.getCaseSize()) >= childBoObj
                            .getOuterSize()) {
                        tv = (childBoObj.getStocksih() % childBoObj
                                .getCaseSize())
                                / childBoObj.getOuterSize()
                                + "";
                        holder.sihOuter.setText(tv);
                    } else {
                        holder.sihOuter.setText("0");
                    }
                }
            } else if (bModel.configurationMasterHelper.SHOW_OUTER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sih.setText("0");
                    holder.sihOuter.setText("0");
                } else if (childBoObj.getOuterSize() == 0) {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                    holder.sihOuter.setText("0");
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getOuterSize() + "";
                    holder.sihOuter.setText(tv);
                    tv = childBoObj.getStocksih()
                            % childBoObj.getOuterSize() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE
                    && bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sih.setText("0");
                    holder.sihCase.setText("0");
                } else if (childBoObj.getCaseSize() == 0) {
                    tv = childBoObj.getStocksih() + "";
                    holder.sih.setText(tv);
                    holder.sihCase.setText("0");
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                    tv = childBoObj.getStocksih()
                            % childBoObj.getCaseSize() + "";
                    holder.sih.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_CASE) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sihCase.setText("0");
                } else if (childBoObj.getCaseSize() == 0) {
                    holder.sihCase.setText("0");
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getCaseSize() + "";
                    holder.sihCase.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_OUTER_CASE) {
                if (childBoObj.getStocksih() == 0) {
                    holder.sihOuter.setText("0");
                } else if (childBoObj.getOuterSize() == 0) {
                    holder.sihOuter.setText("0");
                } else {
                    tv = childBoObj.getStocksih()
                            / childBoObj.getOuterSize() + "";
                    holder.sihOuter.setText(tv);
                }
            } else if (bModel.configurationMasterHelper.SHOW_ORDER_PCS) {
                tv = childBoObj.getStocksih() + "";
                holder.sih.setText(tv);
            }
        } else {
            tv = childBoObj.getStocksih() + "";
            holder.sih.setText(tv);
        }

        return row;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return false;
    }


    private class GroupViewHolder {
        private String pname;
        private TextView psname;
        private TextView sih;
        private TextView sihCase;
        private TextView sihOuter;
        private TextView prodcode;
    }


    class ViewHolder {
        private TextView sih;
        private TextView sihCase;
        private TextView sihOuter;
        private TextView batchNo;

    }
}
