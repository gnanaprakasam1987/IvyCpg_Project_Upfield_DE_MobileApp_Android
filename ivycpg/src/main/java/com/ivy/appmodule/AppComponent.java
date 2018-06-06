package com.ivy.appmodule;

import com.ivy.sd.png.model.BusinessModel;
import javax.inject.Singleton;

import dagger.Component;

/**
 * Created by velraj.p on 5/24/2018.
 */

@Singleton
@Component(modules = {AppModule.class})
public interface AppComponent {
    BusinessModel provideAppContext();
    void inject(BusinessModel main);
}
