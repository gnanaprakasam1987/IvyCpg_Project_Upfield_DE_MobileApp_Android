package com.ivy.sd.png.asset;

/**
 * Created by rajkumar.s on 11/16/2017.
 */

public interface AssetInteractor {

     interface userActionListener {
      void save(String mModuleCode);
      void loadMasters(String mMenuCode);
      void loadReasonAdapter();
      void loadConditionAdapter();
    }

    interface updateView{
    }
}
