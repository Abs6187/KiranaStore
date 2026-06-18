package com.kirana.store.ui;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.closeSoftKeyboard;
import static androidx.test.espresso.action.ViewActions.typeText;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;

import androidx.test.core.app.ActivityScenario;
import androidx.test.ext.junit.rules.ActivityScenarioRule;
import androidx.test.ext.junit.runners.AndroidJUnit4;
import androidx.test.filters.LargeTest;

import com.kirana.store.R;

import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 * Espresso instrumented test for the Dashboard add-product flow.
 * <p>
 * Adds a product via the bottom-sheet dialog and asserts the product card with
 * its ₹ price appears in the grid. No Firebase / google-services.json required —
 * {@code MainActivity} degrades gracefully when the AI key is absent.
 * <p>
 * Requires an emulator or device ( CAMERA / RECORD_AUDIO runtime permissions
 * are auto-granted via the test runner's grantPermissions flag in newer AGP;
 * if a permission dialog appears, grant it manually on first run).
 */
@RunWith(AndroidJUnit4.class)
@LargeTest
public class DashboardUiTest {

    @Rule
    public ActivityScenarioRule<MainActivity> activityRule =
        new ActivityScenarioRule<>(MainActivity.class);

    @Test
    public void addProduct_appearsOnDashboardWithRupeePrice() {
        // 1. Open the add-product dialog
        onView(withId(R.id.fab_add_product)).perform(click());

        // 2. Fill in name + price (in ₹)
        onView(withId(R.id.edit_product_name))
            .perform(typeText("Mustard Oil"), closeSoftKeyboard());
        onView(withId(R.id.edit_product_price))
            .perform(typeText("175"), closeSoftKeyboard());

        // 3. Save
        onView(withId(R.id.btn_save_product)).perform(click());

        // 4. The product name and the ₹-formatted price should be visible
        onView(withText("Mustard Oil"))
            .check(matches(isDisplayed()));
        // The card renders the price as "₹175" (currency symbol + integer cast)
        onView(withText("₹175"))
            .check(matches(isDisplayed()));
    }
}
