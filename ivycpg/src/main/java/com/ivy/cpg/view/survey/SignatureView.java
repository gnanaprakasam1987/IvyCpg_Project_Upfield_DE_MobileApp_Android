package com.ivy.cpg.view.survey;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.LinearLayout;

import com.ivy.sd.png.util.Commons;

import java.io.File;
import java.io.FileOutputStream;

public class SignatureView extends View {

    private static final float STROKE_WIDTH = 15f;
    private static final float HALF_STROKE_WIDTH = STROKE_WIDTH / 2;
    private Paint paint = new Paint();
    private Path path = new Path();

    private float lastTouchX;
    private float lastTouchY;
    private final RectF dirtyRect = new RectF();
    private Bitmap mBitmap;
    private LinearLayout mContent;

    public static boolean signCaptured = false;
    private File mypath;

    public SignatureView(Context context, AttributeSet attrs, LinearLayout mContent, String photoPath, String imageName) {
        super(context, attrs);
        paint.setAntiAlias(true);
        paint.setColor(Color.BLACK);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeWidth(STROKE_WIDTH);
        this.mContent = mContent;
        File directory = new File(photoPath);
        mypath = new File(directory, imageName);
    }

    @SuppressLint("WrongThread")
    public boolean save(View v) {

        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(mContent.getWidth(), mContent.getHeight(), Bitmap.Config.RGB_565);

        }
        Canvas canvas = new Canvas(mBitmap);
        try {

            FileOutputStream mFileOutStream = new FileOutputStream(mypath);

            v.draw(canvas);
            mBitmap.compress(Bitmap.CompressFormat.JPEG, 90, mFileOutStream);
            mFileOutStream.flush();
            mFileOutStream.close();

        } catch (Exception e) {
            Commons.printException(e);
            return false;
        }
        return true;
    }

    public void clear() {
        path.reset();
        invalidate();
        signCaptured = false;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawPath(path, paint);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        float eventX = event.getX();
        float eventY = event.getY();
        signCaptured = true;   // if put signature,SIGN_CAPTURED is TRUE

        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                path.moveTo(eventX, eventY);
                lastTouchX = eventX;
                lastTouchY = eventY;
                return true;

            case MotionEvent.ACTION_MOVE:

            case MotionEvent.ACTION_UP:

                resetDirtyRect(eventX, eventY);
                int historySize = event.getHistorySize();
                for (int i = 0; i < historySize; i++) {
                    float historicalX = event.getHistoricalX(i);
                    float historicalY = event.getHistoricalY(i);
                    expandDirtyRect(historicalX, historicalY);
                    path.lineTo(historicalX, historicalY);
                }
                path.lineTo(eventX, eventY);
                break;

            default:
                return false;
        }

        invalidate((int) (dirtyRect.left - HALF_STROKE_WIDTH),
                (int) (dirtyRect.top - HALF_STROKE_WIDTH),
                (int) (dirtyRect.right + HALF_STROKE_WIDTH),
                (int) (dirtyRect.bottom + HALF_STROKE_WIDTH));

        lastTouchX = eventX;
        lastTouchY = eventY;

        return true;
    }

    private void expandDirtyRect(float historicalX, float historicalY) {
        if (historicalX < dirtyRect.left) {
            dirtyRect.left = historicalX;
        } else if (historicalX > dirtyRect.right) {
            dirtyRect.right = historicalX;
        }

        if (historicalY < dirtyRect.top) {
            dirtyRect.top = historicalY;
        } else if (historicalY > dirtyRect.bottom) {
            dirtyRect.bottom = historicalY;
        }
    }

    private void resetDirtyRect(float eventX, float eventY) {
        dirtyRect.left = Math.min(lastTouchX, eventX);
        dirtyRect.right = Math.max(lastTouchX, eventX);
        dirtyRect.top = Math.min(lastTouchY, eventY);
        dirtyRect.bottom = Math.max(lastTouchY, eventY);
    }
}
