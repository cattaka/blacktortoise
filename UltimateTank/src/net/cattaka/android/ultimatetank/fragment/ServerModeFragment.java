
package net.cattaka.android.ultimatetank.fragment;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.cattaka.android.ultimatetank.R;
import net.cattaka.android.ultimatetank.camera.ICameraManager;
import net.cattaka.android.ultimatetank.camera.ICameraManagerAdapter;
import net.cattaka.android.ultimatetank.net.ClientThread;
import net.cattaka.android.ultimatetank.net.ServerThread;
import net.cattaka.android.ultimatetank.net.ServerThread.IServerThreadListener;
import net.cattaka.android.ultimatetank.net.data.SocketState;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.OpCode;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ServerModeFragment extends BaseFragment implements OnClickListener {
    private ServerThread mServerThread;

    private IServerThreadListener mServerThreadListener = new IServerThreadListener() {
        @Override
        public void onClientConnected(ClientThread target) {
            refleshRomoteControllerList();
        }

        @Override
        public void onClientDisconnected(ClientThread target) {
            refleshRomoteControllerList();
        }

        @Override
        public void onSocketStateChanged(SocketState socketState) {
            // none
        }

        /**
         * When receive packet from remote client, pass it to USB device.
         */
        @Override
        public void onReceivePacket(ClientThread from, MyPacket packet) {
            getCommandAdapter().sendPacket(packet);
            if (packet.getOpCode() == OpCode.REQUEST_CAMERA_IMAGE) {
                synchronized (mRequestedCameraImageClients) {
                    mRequestedCameraImageClients.add(from);
                }
            }
        }
    };

    private ListView mConnectedControllerList;

    private SurfaceView mCameraSurfaceView;

    private ImageView mCameraImageView;

    private ICameraManager mCameraManager;

    private Set<ClientThread> mRequestedCameraImageClients;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRequestedCameraImageClients = new HashSet<ClientThread>();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_server_mode, null);

        // Pickup views
        mConnectedControllerList = (ListView)view.findViewById(R.id.connectedControllerList);
        mCameraSurfaceView = (SurfaceView)view.findViewById(R.id.cameraSurfaceView);
        mCameraImageView = (ImageView)view.findViewById(R.id.cameraImageView);

        // Bind event listeners
        mCameraImageView.setOnClickListener(this);

        mCameraManager = createCameraManager(new ICameraManagerAdapter() {
            @Override
            public SurfaceView getSurfaceView() {
                return mCameraSurfaceView;
            }

            @Override
            public void onPictureTaken(Bitmap bitmap, ICameraManager cameraManager) {
                mCameraImageView.setImageBitmap(bitmap);
                synchronized (mRequestedCameraImageClients) {
                    if (mRequestedCameraImageClients.size() > 0) {
                        ByteArrayOutputStream bout = new ByteArrayOutputStream();
                        bitmap.compress(CompressFormat.JPEG, 50, bout);
                        byte[] data = bout.toByteArray();
                        MyPacket packet = new MyPacket(OpCode.CAMERA_IMAGE, data.length, data);
                        for (ClientThread ct : mRequestedCameraImageClients) {
                            ct.sendPacket(packet);
                        }
                        mRequestedCameraImageClients.clear();
                    }
                }
            }
        });
        mCameraManager.setEnablePreview(true);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mServerThread != null) {
            mServerThread.stopThread();
        }
        mServerThread = new ServerThread(5000, mServerThreadListener);
        try {
            mServerThread.startThread();
        } catch (InterruptedException e) {
            // Impossible
            throw new RuntimeException(e);
        }
        mCameraManager.onResume();
        setKeepScreen(true);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mServerThread != null) {
            mServerThread.stopThread();
            mServerThread = null;
        }
        mCameraManager.onPause();
        setKeepScreen(false);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.sendButton) {
            ICommandAdapter adapter = getCommandAdapter();
            if (adapter != null) {
                EditText sendText = (EditText)getView().findViewById(R.id.sendEdit);
                byte[] data = String.valueOf(sendText.getText()).getBytes();
                MyPacket packet = new MyPacket(OpCode.ECHO, data.length, data);
                adapter.sendPacket(packet);
            }
        } else if (v.getId() == R.id.clearButton) {
            TextView receivedText = (TextView)getView().findViewById(R.id.receivedText);
            receivedText.setText("");
        } else if (v.getId() == R.id.cameraImageView) {
            mCameraManager.setEnablePreview(!mCameraManager.isEnablePreview());
        }

    }

    @Override
    public void onReceive(MyPacket packet) {
        super.onReceive(packet);
        if (packet.getOpCode() == OpCode.ECHO) {
            mServerThread.sendPacket(packet);
        }
    }

    public void refleshRomoteControllerList() {
        if (mServerThread != null) {
            List<String> labels = new ArrayList<String>();
            for (ClientThread ct : mServerThread.getClientThreads()) {
                labels.add(ct.getLabel());
            }
            ArrayAdapter<String> adapter = new ArrayAdapter<String>(getContext(),
                    android.R.layout.simple_list_item_1, labels);
            mConnectedControllerList.setAdapter(adapter);
        } else {
            // Impossible
        }

    }
}
