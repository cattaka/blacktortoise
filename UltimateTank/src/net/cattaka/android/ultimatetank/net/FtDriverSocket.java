
package net.cattaka.android.ultimatetank.net;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import jp.ksksue.driver.serial.FTDriver;
import net.cattaka.android.ultimatetank.exception.NotImplementedException;
import net.cattaka.libgeppa.IRawSocket;

public class FtDriverSocket implements IRawSocket {
    private class InputStreamEx extends InputStream {
        private byte[] buf = new byte[1];

        private boolean closed = false;

        @Override
        public int read() throws IOException {
            while (mFtDriver.isConnected() && !closed) {
                if (mFtDriver.read(buf) > 0) {
                    return 0xFF & buf[0];
                }
            }
            return -1;
        }

        @Override
        public int read(byte[] buffer) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public int read(byte[] buffer, int offset, int length) throws IOException {
            throw new NotImplementedException();
        }

        @Override
        public void close() throws IOException {
            super.close();
            closed = true;
        }
    }

    private class OutputStreamEx extends OutputStream {
        private byte[] buf = new byte[1];

        @Override
        public void write(int oneByte) throws IOException {
            buf[0] = (byte)oneByte;
            mFtDriver.write(buf);
        }

        @Override
        public void write(byte[] buffer) throws IOException {
            mFtDriver.write(buffer);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {
            throw new NotImplementedException();
        }
    }

    private FTDriver mFtDriver;

    private InputStreamEx mInputStream;

    private OutputStreamEx mOutputStream;

    public FtDriverSocket(FTDriver ftDriver) {
        super();
        mFtDriver = ftDriver;
        mInputStream = new InputStreamEx();
        mOutputStream = new OutputStreamEx();
    }

    @Override
    public String getLabel() {
        return "FTDriver Socket";
    }

    @Override
    public InputStream getInputStream() {
        return mInputStream;
    }

    @Override
    public OutputStream getOutputStream() {
        return mOutputStream;
    }

    @Override
    public void close() throws IOException {
        mFtDriver.end();
    }

    @Override
    public boolean isConnected() {
        return mFtDriver.isConnected();
    }

}
