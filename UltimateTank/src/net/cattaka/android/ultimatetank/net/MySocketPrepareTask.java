
package net.cattaka.android.ultimatetank.net;

import jp.ksksue.driver.serial.FTDriver;
import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;

public class MySocketPrepareTask implements IRawSocketPrepareTask {
    private FTDriver mFtDriver;

    public MySocketPrepareTask(FTDriver ftDriver) {
        super();
        mFtDriver = ftDriver;
    }

    @Override
    public IRawSocket prepareRawSocket() {
        if (mFtDriver.begin(FTDriver.BAUD115200)) {
            return new FtDriverSocket(mFtDriver);
        } else {
            return null;
        }
    }

}
