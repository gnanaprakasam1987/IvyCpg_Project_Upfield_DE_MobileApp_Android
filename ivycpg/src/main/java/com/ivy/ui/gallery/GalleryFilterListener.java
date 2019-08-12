package com.ivy.ui.gallery;

import java.util.ArrayList;

public interface GalleryFilterListener {

    void apply(ArrayList<String> sectionMenuList);

    void clearAll();
}
