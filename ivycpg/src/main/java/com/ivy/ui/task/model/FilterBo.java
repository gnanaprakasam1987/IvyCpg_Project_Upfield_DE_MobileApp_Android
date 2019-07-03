package com.ivy.ui.task.model;

import android.os.Parcel;
import android.os.Parcelable;

import org.jetbrains.annotations.NotNull;

public class FilterBo implements Parcelable {

    private int filterId;
    private String filterName;
    private int ProdLevelId;
    private boolean isChecked;

    public FilterBo() {
    }

    public FilterBo(int filterId, String filterName) {
        this.filterId = filterId;
        this.filterName = filterName;
    }

    protected FilterBo(Parcel in) {
        filterId = in.readInt();
        filterName = in.readString();
        isChecked = in.readByte() != 0;
    }

    public static final Creator<FilterBo> CREATOR = new Creator<FilterBo>() {
        @Override
        public FilterBo createFromParcel(Parcel in) {
            return new FilterBo(in);
        }

        @Override
        public FilterBo[] newArray(int size) {
            return new FilterBo[size];
        }
    };

    public int getFilterId() {
        return filterId;
    }

    public void setFilterId(int filterId) {
        this.filterId = filterId;
    }

    public String getFilterName() {
        return filterName;
    }

    public void setFilterName(String filterName) {
        this.filterName = filterName;
    }

    public int getProdLevelId() {
        return ProdLevelId;
    }

    public void setProdLevelId(int prodLevelId) {
        ProdLevelId = prodLevelId;
    }

    public boolean isChecked() {
        return isChecked;
    }

    public void setChecked(boolean checked) {
        isChecked = checked;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeInt(filterId);
        parcel.writeString(filterName);
        parcel.writeByte((byte) (isChecked ? 1 : 0));
    }


    @NotNull
    @Override
    public String toString() {
        return filterName;
    }
}
