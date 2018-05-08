package com.ivy.cpg.view.supervisor;

import com.google.firebase.database.DataSnapshot;

import java.util.HashMap;
import java.util.List;

public interface Seller {

    void setSellerMarker(DataSnapshot dataSnapshot);
    void setMarker(DataSnapshot dataSnapshot);
    void updateSellerInfo(DataSnapshot dataSnapshot);

}
