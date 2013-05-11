
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;
import android.content.Context;

public class DummySocketPrepareTask implements IRawSocketPrepareTask {
    public DummySocketPrepareTask() {
        super();
        // none
    }

    public void setup(Context context) {
        // none
    }

    @Override
    public IRawSocket prepareRawSocket() {
        return new DummySocket();
    }
}
