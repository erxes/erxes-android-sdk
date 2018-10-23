package com.newmedia.erxeslibrary.ui.login;

import android.support.test.rule.ActivityTestRule;
import android.view.View;

import com.newmedia.erxeslibrary.R;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;

import static org.junit.Assert.*;

public class ErxesActivityTest {
    @Rule
    public ActivityTestRule<ErxesActivity> activityActivityTestRule = new ActivityTestRule<ErxesActivity>(ErxesActivity.class);
    private ErxesActivity erxesActivity;

    @Before
    public void setUp() throws Exception {
        erxesActivity = activityActivityTestRule.getActivity();
    }

    @Test
    public void testLaunch(){

        assertNotNull(erxesActivity.findViewById(R.id.names));
        assertNotNull(erxesActivity.findViewById(R.id.logout));
        assertNotNull(erxesActivity.findViewById(R.id.information));
        assertNotNull(erxesActivity.findViewById(R.id.smsgroup));
        assertNotNull(erxesActivity.findViewById(R.id.phonezurag));
        assertNotNull(erxesActivity.findViewById(R.id.sms_button));
        assertNotNull(erxesActivity.findViewById(R.id.mailgroup));
        assertNotNull(erxesActivity.findViewById(R.id.mail_zurag));
        assertNotNull(erxesActivity.findViewById(R.id.email_button));
        assertNotNull(erxesActivity.findViewById(R.id.email));
        assertNotNull(erxesActivity.findViewById(R.id.phone));
    }

    @After
    public void tearDown() throws Exception {
    }
}