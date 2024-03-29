package com.ivy.sd.png.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.View;

/**
 * Created by dharmapriya.k on 7/28/2016,12:04 PM.
 */
public class SlantView extends View {
    private Context mContext;
    Paint paint;
    Path path;
    private int color = 0;

    public SlantView(Context ctx, AttributeSet attrs) {
        super(ctx, attrs);
        mContext = ctx;
        setWillNotDraw(false);
        paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    }

    @Override
    protected void onDraw(Canvas canvas) {

        int w = getWidth(), h = getHeight();
        paint.setStrokeWidth(2);
        if (color == 0)
            paint.setColor(Color.WHITE);
        else
            paint.setColor(color);
        paint.setStyle(Paint.Style.FILL_AND_STROKE);
        paint.setAntiAlias(true);

        path = new Path();
        path.setFillType(Path.FillType.EVEN_ODD);
        path.moveTo(0, 0);
        path.lineTo(0, h);
        path.lineTo(w, h);
        path.close();
        canvas.drawPath(path, paint);
    }

    public void setColor(int color) {
        this.color = color;
    }
}
