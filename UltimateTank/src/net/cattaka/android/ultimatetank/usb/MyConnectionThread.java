
package net.cattaka.android.ultimatetank.usb;

import java.io.ByteArrayOutputStream;

import net.cattaka.android.ultimatetank.usb.data.MyPacket;
import net.cattaka.android.ultimatetank.usb.data.OpCode;
import net.cattaka.libgeppa.data.IPacketFactory;
import net.cattaka.libgeppa.thread.ConnectionThread;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;
import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

public class MyConnectionThread extends ConnectionThread<MyPacket> implements ICommandAdapter {
    private byte[] mBuffer = new byte[0x100];

    public MyConnectionThread(
            net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask prepareTask,
            IPacketFactory<MyPacket> packetFactory,
            IConnectionThreadListener<MyPacket> connectionThreadListener,
            boolean useMainLooperForListener) {
        super(prepareTask, packetFactory, connectionThreadListener, useMainLooperForListener);
    }

    public MyConnectionThread(
            net.cattaka.libgeppa.thread.ConnectionThread.IRawSocketPrepareTask prepareTask,
            IPacketFactory<MyPacket> packetFactory,
            IConnectionThreadListener<MyPacket> connectionThreadListener) {
        super(prepareTask, packetFactory, connectionThreadListener);
    }

    @Override
    public boolean sendMove(float leftMotor1, float leftMotor2, float rightMotor1, float rightMotor2) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor1)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * leftMotor2)));
        mBuffer[2] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor1)));
        mBuffer[3] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * rightMotor2)));
        MyPacket packet = new MyPacket(OpCode.MOVE, 4, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        mBuffer[0] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * yaw)));
        mBuffer[1] = (byte)Math.max(0, Math.min(0xFF, (int)(0xFF * pitch)));
        MyPacket packet = new MyPacket(OpCode.HEAD, 2, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public boolean sendRequestCameraImage() {
        MyPacket packet = new MyPacket(OpCode.REQUEST_CAMERA_IMAGE, 0, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public boolean sendCameraImage(Bitmap bitmap) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        bitmap.compress(CompressFormat.JPEG, 0, bout);
        byte[] data = bout.toByteArray();
        MyPacket packet = new MyPacket(OpCode.REQUEST_CAMERA_IMAGE, data.length, data);
        return sendPacket(packet);
    }
}
