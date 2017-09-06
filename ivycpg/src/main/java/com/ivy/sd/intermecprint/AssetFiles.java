package com.ivy.sd.intermecprint;

import android.app.Activity;

import com.ivy.sd.png.util.Commons;

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by hgode on 07.04.2014.
 */
public class AssetFiles {
    ArrayList<String> _files;
    Activity _activity;
    final String TAG="AssetFiles";

    public AssetFiles(Activity activity) {
        _activity = activity;
        _files=new ArrayList<String>();
        listAssetFiles("");
        String[] list;
        String path = ""; //no subdir
        listAssetFiles(path);
        dumpList();
    }//empty constructor

    private void dumpList(){
        Commons.print(TAG+ ",file list: ");
        for(String s : _files)
            Commons.print(TAG+","+s);
    }
    private void listAssetFiles(String path) {
        String[] list;
        try {
            list = _activity.getAssets().list(path);
            if (list.length > 0) {
                // This is a folder
                for (String file : list) {
                    // This is a file
                    // TODO: add file name to an array list
                    if(file.endsWith(".prn"))
                        _files.add(file);
                }
            }
        }
        catch(IOException e){
            Commons.printException(TAG+ ",IOException for list: '" + path + "'");// return false;
        }
    }
}