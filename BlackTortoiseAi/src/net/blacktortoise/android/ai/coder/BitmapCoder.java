
package net.blacktortoise.android.ai.coder;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class BitmapCoder {
    public static byte[] encode(Bitmap src) {
        if (src == null) {
            return null;
        } else {
            try {
                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                src.compress(CompressFormat.PNG, 100, bout);
                bout.flush();
                return bout.toByteArray();
            } catch (IOException e) {
                // Impossible
                throw new RuntimeException();
            }
        }
    }

    public static Bitmap decode(byte[] src) {
        if (src == null) {
            return null;
        }
        Bitmap bitmap = BitmapFactory.decodeByteArray(src, 0, src.length);
        return bitmap;
    }
}
