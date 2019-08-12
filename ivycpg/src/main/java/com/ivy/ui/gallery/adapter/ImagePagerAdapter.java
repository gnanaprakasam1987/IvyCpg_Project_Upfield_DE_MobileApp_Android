/*
 * Copyright 2018 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.ivy.ui.gallery.adapter;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;

import com.ivy.ui.gallery.model.GalleryBo;
import com.ivy.ui.gallery.view.ImagePagerFragment;

import java.util.ArrayList;

public class ImagePagerAdapter extends FragmentStatePagerAdapter {

    private ArrayList<GalleryBo> galleryBoArrayList;

    public ImagePagerAdapter(FragmentManager fragment, ArrayList<GalleryBo> galleryBoArrayList) {
        super(fragment);
        this.galleryBoArrayList = galleryBoArrayList;
    }

    @Override
    public int getCount() {
        return galleryBoArrayList.size();
    }

    @Override
    public Fragment getItem(int position) {
        return ImagePagerFragment.newInstance(galleryBoArrayList.get(position));
    }
}
