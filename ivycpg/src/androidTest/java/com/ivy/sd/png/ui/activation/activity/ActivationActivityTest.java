package com.ivy.sd.png.ui.activation.activity;


import androidx.test.espresso.action.ViewActions;
import androidx.test.rule.ActivityTestRule;

import com.ivy.sd.png.asean.view.R;
import com.ivy.ui.activation.view.ActivationActivity;

import org.junit.Rule;
import org.junit.Test;


import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;

public class ActivationActivityTest {
    @Rule
    public ActivityTestRule<ActivationActivity> activationActivityActivityTestRule =
            new ActivityTestRule<>(ActivationActivity.class);


    @Test
    public void checkActivateConditions() {

        onView(withId(R.id.activationKey)).check(matches(isDisplayed())).
                perform(ViewActions.typeText("6V82F9MZT60DQKXR"));


        onView(withId(R.id.activate)).check(matches(isDisplayed())).perform(ViewActions.click());
        onView(withId(R.id.tv_already_activated)).check(matches(isDisplayed()));


    }

}
