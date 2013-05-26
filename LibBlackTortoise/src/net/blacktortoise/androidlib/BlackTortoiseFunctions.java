
package net.blacktortoise.androidlib;

import android.content.Intent;

public class BlackTortoiseFunctions {
    public static Intent createSelectDeviceActivityIntent() {
        Intent intent = new Intent();
        intent.setClassName(Constants.SELECT_DEVICE_ACTIVITY_PACKAGE,
                Constants.SELECT_DEVICE_ACTIVITY_CLASS);
        return intent;
    }
}
