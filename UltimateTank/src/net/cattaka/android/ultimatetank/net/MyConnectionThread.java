
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
    public boolean sendMove(float forward, float side) {
        mBuffer[0] = (byte)(0xFF * forward);
        mBuffer[1] = (byte)(0xFF * side);
        MyPacket packet = new MyPacket(OpCode.MOVE, 2, mBuffer);
        return sendPacket(packet);
    }

    @Override
    public boolean sendHead(float yaw, float pitch) {
        mBuffer[0] = (byte)(0xFF * yaw);
        mBuffer[1] = (byte)(0xFF * pitch);
        MyPacket packet = new MyPacket(OpCode.HEAD, 2, mBuffer);
        return sendPacket(packet);
    }

}
