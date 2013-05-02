
package net.cattaka.android.ultimatetank.net;

import net.cattaka.android.ultimatetank.net.data.MyPacket;
import net.cattaka.android.ultimatetank.net.data.OpCode;
import net.cattaka.android.ultimatetank.usb.ICommandAdapter;
import net.cattaka.libgeppa.data.IPacketFactory;
import net.cattaka.libgeppa.thread.ConnectionThread;
import net.cattaka.libgeppa.thread.IConnectionThreadListener;

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

}
