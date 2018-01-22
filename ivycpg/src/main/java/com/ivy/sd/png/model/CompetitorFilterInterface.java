package com.ivy.sd.png.model;

import com.ivy.sd.png.bo.CompetitorFilterLevelBO;

import java.util.HashMap;
import java.util.Vector;

/**
 * Created by dharmapriya.k on 11/10/2017,12:55 PM.
 */
public interface CompetitorFilterInterface {
    void updateCompetitorProducts(Vector<CompetitorFilterLevelBO> parentIdList, HashMap<Integer, Integer> mSelectedIdByLevelId, String filterText );
}
