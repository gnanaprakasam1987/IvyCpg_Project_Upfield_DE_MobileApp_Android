package com.ivy.ui.survey.data;

import io.reactivex.Single;

public interface SurveyDataManager {

    Single<Boolean> isSurveyAvailableForRetailer(String retailerId);

    Single<Boolean> deleteNewRetailerSurvey(String retailerId);
}
