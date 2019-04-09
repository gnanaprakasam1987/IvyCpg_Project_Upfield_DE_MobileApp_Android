package com.ivy.ui.offlineplan.map.di;

import android.content.Context;

import com.ivy.ui.offlineplan.map.MapViewContract;
import com.ivy.utils.rx.AppSchedulerProvider;
import com.ivy.utils.rx.SchedulerProvider;

import dagger.Module;
import dagger.Provides;
import io.reactivex.disposables.CompositeDisposable;

@Module
public class MapViewModule {

    private MapViewContract.MapView mView;
    private Context mContext;

    public MapViewModule(MapViewContract.MapView mView, Context context) {
        this.mView = mView;
        this.mContext = context;
    }

    @Provides
    CompositeDisposable provideCompositeDisposable() {
        return new CompositeDisposable();
    }

    @Provides
    SchedulerProvider provideSchedulerProvider() {
        return new AppSchedulerProvider();
    }

    @Provides
    public MapViewContract.MapView provideView() {
        return mView;
    }

}
