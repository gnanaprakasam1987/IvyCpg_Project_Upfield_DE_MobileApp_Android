package com.ivy.sd.png.interactor;

/**
 * Created by rajkumar.s on 11/16/2017.
 */

public interface AssetInteractor {

     interface userActionListener {
      void save(String mModuleCode);
      void loadMasters(String mMenuCode);
    }
}
