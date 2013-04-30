
package net.cattaka.android.ultimatetank;

import jp.ksksue.driver.serial.FTDriver;
import net.cattaka.android.ultimatetank.fragment.BaseFragment.IBaseFragmentAdapter;
import net.cattaka.android.ultimatetank.net.MySocketPrepareTask;
import net.cattaka.android.ultimatetank.net.data.MyPacket;
import net.cattaka.android.ultimatetank.net.data.MyPacketFactory;
import net.cattaka.libgeppa.data.ConnectionCode;
import net.cattaka.libgeppa.data.ConnectionState;
import net.cattaka.libgeppa.thread.ConnectionThread;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.app.Activity;
import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.Menu;
import android.widget.Toast;

public class MainActivity extends Activity implements IBaseFragmentAdapter {
    private FTDriver mFtDriver;

    private ConnectionThread<MyPacket> mConnectionThread;

    private UsbDevice mCurrentUsbDevice;

    private IConnectionThreadListener<MyPacket> mConnectionThreadListener = new IConnectionThreadListener<MyPacket>() {

        @Override
        public void onReceive(MyPacket packet) {
            // TODO Auto-generated method stub

        }

        @Override
        public void onConnectionStateChanged(ConnectionState state, ConnectionCode code) {
            Toast.makeText(MainActivity.this, "ConnectionState:" + state, Toast.LENGTH_SHORT)
                    .show();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    protected void onResume() {
        super.onResume();
        UsbManager usbManager = (UsbManager)getSystemService(Context.USB_SERVICE);
        mFtDriver = new FTDriver(usbManager);
        mFtDriver.setRxTimeout(1000); // This is not impossible.

        { // If last connected device exists, start conectionThread again.
            if (mCurrentUsbDevice != null) {
                startConnectionThread(mCurrentUsbDevice);
            }
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stopConnectionThread();
        mFtDriver.end();
    }

    public void startConnectionThread(UsbDevice usbDevice) {
        mCurrentUsbDevice = usbDevice;

        stopConnectionThread();

        MySocketPrepareTask prepareTask = new MySocketPrepareTask(mFtDriver);
        MyPacketFactory packetFactory = new MyPacketFactory();

        mConnectionThread = new ConnectionThread<MyPacket>(prepareTask, packetFactory,
                mConnectionThreadListener);

        try {
            mConnectionThread.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
    }

    public void stopConnectionThread() {
        if (mConnectionThread != null) {
            try {
                mConnectionThread.stopThread();
            } catch (InterruptedException e) {
                // Impossible
                throw new RuntimeException(e);
            }
            mConnectionThread = null;
        }
    }
}
