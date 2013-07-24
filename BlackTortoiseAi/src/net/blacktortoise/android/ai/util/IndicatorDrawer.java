
package net.blacktortoise.android.ai.util;

import net.blacktortoise.android.ai.action.ConsoleDto;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Paint.Style;

public class IndicatorDrawer {
    private Paint mFillPaint;

    private Paint mStrokePaint;

    public IndicatorDrawer() {
        mFillPaint = new Paint();
        mStrokePaint = new Paint();

        mFillPaint.setColor(0xFFFFFFFF);
        mFillPaint.setStyle(Style.FILL);
        mStrokePaint.setColor(0xFF000000);
        mStrokePaint.setStyle(Style.STROKE);
        mStrokePaint.setStrokeWidth(3);
    }

    public void drawMove(Bitmap dst, ConsoleDto dto) {
        float r = (dst.getWidth() + dst.getWidth()) / 5;
        float x = ((dto.getLastTurn() + 1f) / 2f) * dst.getWidth();
        float y = ((dto.getLastForward() + 1f) / 2f) * dst.getWidth();
        Canvas canvas = new Canvas(dst);
        canvas.drawRect(0, 0, dst.getWidth(), dst.getHeight(), mFillPaint);

        canvas.drawCircle(x, y, r, mStrokePaint);
    }

    public void drawHead(Bitmap dst, ConsoleDto dto) {
        float r = (dst.getWidth() + dst.getWidth()) / 5;
        float x = ((dto.getLastYaw() + 1f) / 2f) * dst.getWidth();
        float y = ((dto.getLastPitch() + 1f) / 2f) * dst.getWidth();
        Canvas canvas = new Canvas(dst);
        canvas.drawRect(0, 0, dst.getWidth(), dst.getHeight(), mFillPaint);

        canvas.drawCircle(x, y, r, mStrokePaint);
    }
}
