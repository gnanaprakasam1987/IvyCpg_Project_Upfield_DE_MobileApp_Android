package com.ivy.sd.png.view;

import android.support.test.espresso.Espresso;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import com.ivy.cpg.view.order.scheme.SchemeApply;
import com.ivy.sd.png.asean.view.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import static android.support.test.espresso.action.ViewActions.click;
import static android.support.test.espresso.assertion.ViewAssertions.matches;
import static android.support.test.espresso.matcher.ViewMatchers.isEnabled;
import static android.support.test.espresso.matcher.ViewMatchers.withId;

@RunWith(AndroidJUnit4.class)
public class SchemeApplyTest {

    @Rule
    public ActivityTestRule<SchemeApply> mActivityRule =
            new ActivityTestRule<>(SchemeApply.class);

    @Test
   public void ensureFreeButtonClickWithoutChecking(){

     //  Espresso.onView(withId(R.id.btn_show_free_products)).perform(click());

       Espresso.onData(withId(R.id.btn_show_free_products)).perform(click()).check(matches(isEnabled()));

       Espresso.onData(withId(R.id.cb_quantity)).perform(click());


       Espresso.onData(withId(R.id.btn_show_free_products)).perform(click()).check(matches(isEnabled()));

   }
}