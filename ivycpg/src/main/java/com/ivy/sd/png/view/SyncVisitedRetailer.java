package com.ivy.sd.png.view;

import android.os.Parcel;
import android.os.Parcelable;

import com.ivy.sd.png.bo.SyncRetailerBO;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by mayuri.v on 4/19/2017.
 */
public class SyncVisitedRetailer implements Parcelable {
    private List<SyncRetailerBO> objects;

    public SyncVisitedRetailer(List<SyncRetailerBO> objects) {
        this.objects = objects;
    }

    protected SyncVisitedRetailer(Parcel in) {
        //objects = in.readTypedList(objects,SyncRetailerBO.CREATOR);
        objects = new ArrayList<>();
        in.readTypedList(objects,SyncRetailerBO.CREATOR);
        setObjects(objects);
    }

    public static final Creator<SyncVisitedRetailer> CREATOR = new Creator<SyncVisitedRetailer>() {
        @Override
        public SyncVisitedRetailer createFromParcel(Parcel in) {
            return new SyncVisitedRetailer(in);
        }

        @Override
        public SyncVisitedRetailer[] newArray(int size) {
            return new SyncVisitedRetailer[size];
        }
    };

    public List<SyncRetailerBO> getObjects() {
        return objects;
    }

        public void setObjects(List<SyncRetailerBO> objects) {
            this.objects = objects;
        }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        //dest.writeList(objects);
        dest.writeTypedList(objects);

    }
}
