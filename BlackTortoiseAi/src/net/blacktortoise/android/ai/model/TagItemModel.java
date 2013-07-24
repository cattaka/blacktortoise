
package net.blacktortoise.android.ai.model;

import java.util.List;

import net.blacktortoise.android.ai.coder.BitmapArrayCoder;
import net.blacktortoise.android.ai.coder.BitmapCoder;
import net.cattaka.util.gendbhandler.Attribute;
import net.cattaka.util.gendbhandler.Attribute.FieldType;
import net.cattaka.util.gendbhandler.GenDbHandler;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;

@GenDbHandler(find = {
        "id", ":id"
})
public class TagItemModel {
    @Attribute(primaryKey = true)
    private Long id;

    private String name;

    private String label;

    @Attribute(customDataType = FieldType.BLOB, customCoder = BitmapCoder.class)
    private Bitmap thumbnail;

    @Attribute(customDataType = FieldType.BLOB, customCoder = BitmapArrayCoder.class)
    private List<Bitmap> bitmaps;

    public void updateThumbnail() {
        if (bitmaps != null && bitmaps.size() > 0) {
            Bitmap bt = bitmaps.get(0);
            thumbnail = Bitmap.createBitmap(72, 72, Bitmap.Config.ARGB_8888);
            float scale = Math.min((float)thumbnail.getWidth() / (float)bt.getWidth(),
                    (float)thumbnail.getHeight() / (float)bt.getHeight());
            Matrix m = new Matrix();
            m.setScale(scale, scale);
            Canvas canvas = new Canvas(thumbnail);
            canvas.drawBitmap(bt, m, null);
        } else {
            thumbnail = null;
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public Bitmap getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(Bitmap thumbnail) {
        this.thumbnail = thumbnail;
    }

    public List<Bitmap> getBitmaps() {
        return bitmaps;
    }

    public void setBitmaps(List<Bitmap> bitmaps) {
        this.bitmaps = bitmaps;
    }

}
