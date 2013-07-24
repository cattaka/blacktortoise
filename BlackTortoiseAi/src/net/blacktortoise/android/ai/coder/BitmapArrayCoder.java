
package net.blacktortoise.android.ai.coder;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;
import android.graphics.BitmapFactory;

public class BitmapArrayCoder {
    public static byte[] encode(List<Bitmap> src) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        DataOutputStream out = new DataOutputStream(bout);
        if (src == null) {
            return null;
        } else {
            try {
                out.writeInt(src.size());
                for (Bitmap bitmap : src) {
                    bitmap.compress(CompressFormat.PNG, 100, out);
                }
                out.flush();
                return bout.toByteArray();
            } catch (IOException e) {
                // Impossible
                throw new RuntimeException();
            }
        }
    }

    public static List<Bitmap> decode(byte[] src) {
        if (src == null) {
            return null;
        }
        try {
            List<Bitmap> results = new ArrayList<Bitmap>();
            DataInputStream in = new DataInputStream(new ByteArrayInputStream(src));
            int n = in.readInt();
            if (n > 0) {
                for (int i = 0; i < n; i++) {
                    Bitmap bitmap = BitmapFactory.decodeStream(in);
                    if (bitmap != null) {
                        results.add(bitmap);
                    }
                }
            }
            return results;
        } catch (IOException e) {
            // Impossible
            throw new RuntimeException();
        }
    }
}
