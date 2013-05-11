
package net.cattaka.android.ultimatetank.usb;

import net.cattaka.libgeppa.IRawSocket;
import net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask;

public class DummySocketPrepareTask implements IRawSocketPrepareTask {
    public DummySocketPrepareTask() {
        super();
        // none
    }

    @Override
    public IRawSocket prepareRawSocket() {
        return new DummySocket();
    }
}
