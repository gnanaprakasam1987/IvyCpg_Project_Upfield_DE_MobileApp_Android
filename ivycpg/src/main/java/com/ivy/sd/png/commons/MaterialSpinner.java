package com.ivy.sd.png.commons;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.Typeface;
import androidx.core.content.ContextCompat;
import androidx.appcompat.widget.AppCompatSpinner;
import android.text.Layout;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.SpinnerAdapter;
import android.widget.TextView;

import com.ivy.sd.png.asean.view.R;
import com.ivy.sd.png.model.BusinessModel;
import com.ivy.sd.png.provider.ConfigurationMasterHelper;

/**
 * Created by hanifa.m on 11/3/2016.
 */

public class MaterialSpinner extends AppCompatSpinner implements ValueAnimator.AnimatorUpdateListener {


    public static final int DEFAULT_ARROW_WIDTH_DP = 12;

    private static final String TAG = MaterialSpinner.class.getSimpleName();

    private Context mContext;

    //Paint objects
    private Paint paint;
    private TextPaint textPaint;
    private StaticLayout staticLayout;


    private Path selectorPath;
    private Point[] selectorPoints;

    //Inner padding = "Normal" android padding
    private int innerPaddingLeft;
    private int innerPaddingRight;
    private int innerPaddingTop;
    private int innerPaddingBottom;

    //Private padding to add space for FloatingLabel and ErrorLabel
    private int extraPaddingTop;
    private int extraPaddingBottom;

    //@see dimens.xml
    private int underlineTopSpacing;
    private int underlineBottomSpacing;
    private int errorLabelSpacing;
    private int floatingLabelTopSpacing;
    private int floatingLabelBottomSpacing;
    private int floatingLabelInsideSpacing;
    private int rightLeftSpinnerPadding;
    private int minContentHeight;

    //Properties about Error Label
    private int lastPosition;
    private int errorLabelPosX;
    private int minNbErrorLines;
    private float currentNbErrorLines;


    //Properties about Floating Label (
    private float floatingLabelPercent;
    private ObjectAnimator floatingLabelAnimator;
    private boolean isSelected;
    private boolean floatingLabelVisible;
    private int baseAlpha;


    //AttributeSet
    private int baseColor;
    private int highlightColor;
    private int errorColor;
    private int disabledColor;
    private CharSequence error;
    private CharSequence floatingLabelText;
    private int floatingLabelColor;
    private boolean multiline;
    private Typeface typeface;
    private float thickness;
    private float thicknessError;
    private int arrowColor;
    private float arrowSize;
    private boolean enableErrorLabel;
    private boolean enableFloatingLabel;
    private String typefacePath;

    private HintAdapter hintAdapter;
    BusinessModel bmodel;

    /*
    * **********************************************************************************
    * CONSTRUCTORS
    * **********************************************************************************
    */

    public MaterialSpinner(Context context) {
        super(context);
        init(context, null);
    }

    public MaterialSpinner(Context context, AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);

    }

    public MaterialSpinner(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }


    /*
    * **********************************************************************************
    * INITIALISATION METHODS
    * **********************************************************************************
    */

    private void init(Context context, AttributeSet attrs) {
        bmodel = (BusinessModel) context.getApplicationContext();
        initAttributes(context, attrs);
        initPaintObjects();
        initDimensions();
        initPadding();
        initFloatingLabelAnimator();
        initOnItemSelectedListener();
        setMinimumHeight(getPaddingTop() + getPaddingBottom() + minContentHeight);
        //Erase the drawable selector not to be affected by new size (extra Padding's) and this is mandatory
        setBackgroundResource(R.drawable.custspinner_background);

    }

    private void initAttributes(Context context, AttributeSet attrs) {

        TypedArray defaultArray = context.obtainStyledAttributes(new int[]{R.attr.colorControlNormal, R.attr.colorAccent});
        int defaultBaseColor = ContextCompat.getColor(context, R.color.gray_text);
        int defaultHighlightColor = defaultArray.getColor(1, 0);
        int defaultErrorColor = context.getResources().getColor(R.color.RED);
        int defaultfloatinglabelColor = context.getResources().getColor(R.color.gray_text);
        defaultArray.recycle();

        TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.MaterialSpinner);
        baseColor = array.getColor(R.styleable.MaterialSpinner_ms_baseColor, defaultBaseColor);
        highlightColor = array.getColor(R.styleable.MaterialSpinner_ms_highlightColor, defaultHighlightColor);
        errorColor = array.getColor(R.styleable.MaterialSpinner_ms_error_Color, defaultErrorColor);
        disabledColor = ContextCompat.getColor(context, R.color.gray_text);
        floatingLabelText = array.getString(R.styleable.MaterialSpinner_ms_floatingLabelText);
        floatingLabelColor = array.getColor(R.styleable.MaterialSpinner_ms_floatingLabelColor, defaultfloatinglabelColor);
//        multiline = true;
        arrowColor = array.getColor(R.styleable.MaterialSpinner_ms_arrowColor, baseColor);
        arrowSize = array.getDimension(R.styleable.MaterialSpinner_ms_arrowSize, dpToPx(DEFAULT_ARROW_WIDTH_DP));
        enableFloatingLabel = array.getBoolean(R.styleable.MaterialSpinner_ms_enableFloatingLabel, true);

        typefacePath = array.getString(R.styleable.MaterialSpinner_ms_typeface);
        if (typefacePath != null) {
            typeface = Typeface.createFromAsset(getContext().getAssets(), typefacePath);

        }

        array.recycle();

        floatingLabelPercent = 0f;
        errorLabelPosX = 0;
        thickness = (float) 1.2;
        thicknessError = 2;
        isSelected = false;
        floatingLabelVisible = false;
        lastPosition = 0;
        minNbErrorLines = 1;
        currentNbErrorLines = minNbErrorLines;
        enableErrorLabel = true;
        enableFloatingLabel = true;


    }


    @Override
    public void setSelection(final int position) {
        this.post(new Runnable() {
            @Override
            public void run() {
                MaterialSpinner.super.setSelection(position);
            }
        });
    }

    private void initPaintObjects() {

        int labelTextSize = getResources().getDimensionPixelSize(R.dimen.dimen_12dp);

        paint = new Paint(Paint.ANTI_ALIAS_FLAG);

        textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
        textPaint.setTextSize(labelTextSize);
        if (typeface != null) {
            textPaint.setTypeface(typeface);
        }
        textPaint.setColor(baseColor);
        baseAlpha = textPaint.getAlpha();

        selectorPath = new Path();
        selectorPath.setFillType(Path.FillType.EVEN_ODD);

        selectorPoints = new Point[3];
        for (int i = 0; i < 3; i++) {
            selectorPoints[i] = new Point();
        }
    }

    @Override
    public int getSelectedItemPosition() {
        return super.getSelectedItemPosition();
    }

    private void initPadding() {

        innerPaddingTop = getPaddingTop();
        innerPaddingLeft = getPaddingLeft();
        innerPaddingRight = getPaddingRight();
        innerPaddingBottom = getPaddingBottom();

        extraPaddingTop = enableFloatingLabel ? floatingLabelTopSpacing + floatingLabelInsideSpacing + floatingLabelBottomSpacing : floatingLabelBottomSpacing;
        updateBottomPadding();

    }

    private void updateBottomPadding() {
        Paint.FontMetrics textMetrics = textPaint.getFontMetrics();
        extraPaddingBottom = underlineTopSpacing + underlineBottomSpacing;
        if (enableErrorLabel) {
            extraPaddingBottom += (int) ((textMetrics.descent - textMetrics.ascent) * currentNbErrorLines);
        }
        updatePadding();
    }

    private void initDimensions() {
        underlineTopSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_1dp);
        underlineBottomSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_2dp);
        //floatingLabelTopSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_4dp);
        floatingLabelTopSpacing = 0;
        floatingLabelBottomSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_6dp);
        //floatingLabelBottomSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_8dp);
        //rightLeftSpinnerPadding = alignLabels ? getResources().getDimensionPixelSize(R.dimen.right_left_spinner_padding) : 0;
        floatingLabelInsideSpacing = getResources().getDimensionPixelSize(R.dimen.dimen_10dp);
        errorLabelSpacing = (int) getResources().getDimension(R.dimen.card_margin);
        minContentHeight = (int) getResources().getDimension(R.dimen.dimen_18dp);
    }

    private void initOnItemSelectedListener() {
        setOnItemSelectedListener(null);
    }

    /*
    * **********************************************************************************
    * ANIMATION METHODS FOR FLOATING lABEL TEXT
    * **********************************************************************************
    */

    private void initFloatingLabelAnimator() {
        if (floatingLabelAnimator == null) {
            floatingLabelAnimator = ObjectAnimator.ofFloat(this, "floatingLabelPercent", 0f, 1f);
            floatingLabelAnimator.addUpdateListener(this);
        }
    }

    private void showFloatingLabel() {
        if (floatingLabelAnimator != null) {
            floatingLabelVisible = true;
            if (floatingLabelAnimator.isRunning()) {
                floatingLabelAnimator.reverse();
            } else {
                floatingLabelAnimator.start();
            }
        }
    }

    private void hideFloatingLabel() {
        if (floatingLabelAnimator != null) {
            floatingLabelVisible = false;
            floatingLabelAnimator.reverse();
        }
    }


    /*
     * **********************************************************************************
     * UTILITY METHODS
     * **********************************************************************************
    */

    /*
    This method is available in AppUtil
     */
    @Deprecated
    private int dpToPx(float dp) {
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        float px = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, displayMetrics);
        return Math.round(px);
    }

    private float pxToDp(float px) {
        final DisplayMetrics displayMetrics = getContext().getResources().getDisplayMetrics();
        return px * displayMetrics.density;
    }

    private void updatePadding() {
        int left = innerPaddingLeft;
        int top = innerPaddingTop + extraPaddingTop;
        int right = innerPaddingRight;
        int bottom = innerPaddingBottom + extraPaddingBottom;
        super.setPadding(left, top, right, bottom);
        setMinimumHeight(top + bottom + minContentHeight);
    }


    private int prepareBottomPadding() {  //THIS MEHOD USED FOR DISPALYING THE ERROR LABEL

        int targetNbLines = minNbErrorLines;

        if (error != null) {
            staticLayout = new StaticLayout(error, textPaint, getWidth() - getPaddingRight() - getPaddingLeft(), Layout.Alignment.ALIGN_NORMAL, 1.0f, 0.0f, true);
            int nbErrorLines = staticLayout.getLineCount();
            targetNbLines = Math.max(minNbErrorLines, nbErrorLines);

        }
        return targetNbLines;
    }

    private boolean isSpinnerEmpty() {
        return (hintAdapter.getCount() == 0);
    }

    /*
     * **********************************************************************************
     * DRAWING METHODS
     * **********************************************************************************
    */


    @Override
    protected void onDraw(Canvas canvas) {

        super.onDraw(canvas);
        int startX = 0;
        int endX = getWidth();
        int lineHeight;

        int startYLine = getHeight() - getPaddingBottom() + underlineTopSpacing;
        int startYFloatingLabel = (int) (getPaddingTop() - floatingLabelPercent * floatingLabelBottomSpacing);


        if (error != null && enableErrorLabel) {
            lineHeight = dpToPx(thicknessError);
            int startYErrorLabel = startYLine + errorLabelSpacing + lineHeight;
            paint.setColor(errorColor);
            textPaint.setColor(errorColor);
            //Error Label Drawing

            canvas.save();
            canvas.translate(startX, startYErrorLabel - errorLabelSpacing);
            staticLayout.draw(canvas);
            canvas.restore();

        } else {
            lineHeight = dpToPx(thickness);
            if (isSelected && hasFocus()) {
                paint.setColor(highlightColor);
            } else {
                paint.setColor(baseColor);
            }
        }

        // Underline Drawing
        canvas.drawRect(startX, startYLine, endX, startYLine + lineHeight, paint);

        //Floating Label Drawing
        if ((floatingLabelText != null) && enableFloatingLabel) {

            if (isSelected && hasFocus()) {
                textPaint.setColor(highlightColor);
            } else {
                textPaint.setColor(isEnabled() ? disabledColor : disabledColor);
                //paint.setColor(disabledColor);
            }
            if (floatingLabelAnimator.isRunning() || !floatingLabelVisible) {
                textPaint.setAlpha((int) ((0.8 * floatingLabelPercent + 0.2) * baseAlpha * floatingLabelPercent));

            }
            String textToDraw = floatingLabelText.toString();

            canvas.drawText(textToDraw, startX, startYFloatingLabel, textPaint);

        }

        drawSelector(canvas, getWidth(), getPaddingTop() + dpToPx(8));


    }
/*
 *    drawSelector method used for create customize spinner
 */

    private void drawSelector(Canvas canvas, int posX, int posY) {
        if (error != null && enableErrorLabel) {
            paint.setColor(errorColor);
        } else {
            if (isSelected && hasFocus()) {
                paint.setColor(highlightColor);
            } else {
                paint.setColor(isEnabled() ? arrowColor : disabledColor);
            }
        }


        Point point1 = selectorPoints[0];
        Point point2 = selectorPoints[1];
        Point point3 = selectorPoints[2];

        point1.set(posX, posY);
        point2.set((int) (posX - (arrowSize)), posY);
        point3.set((int) (posX - (arrowSize / 2)), (int) (posY + (arrowSize / 2)));

        selectorPath.reset();
        selectorPath.moveTo(point1.x, point1.y);
        selectorPath.lineTo(point2.x, point2.y);
        selectorPath.lineTo(point3.x, point3.y);
        selectorPath.close();
        canvas.drawPath(selectorPath, paint);
    }

    /*
     * **********************************************************************************
     * LISTENER METHODS
     * **********************************************************************************
    */
//    public static void hideSoftKeyboard(View v) {
//        InputMethodManager in = (InputMethodManager) v.getContext()
//                .getSystemService(Context.INPUT_METHOD_SERVICE);
//        in.hideSoftInputFromWindow(v.getApplicationWindowToken(),
//                InputMethodManager.HIDE_NOT_ALWAYS);
//    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (isEnabled()) {
            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    if (getAdapter() != null)
                        isSelected = true;
                    break;

                case MotionEvent.ACTION_UP:
                    if (isSelected || hasFocus() && getAdapter() != null) {
                        InputMethodManager in = (InputMethodManager) mContext
                                .getSystemService(Context.INPUT_METHOD_SERVICE);
                        in.hideSoftInputFromWindow(getWindowToken(),
                                InputMethodManager.HIDE_NOT_ALWAYS);
                    }
                    break;
                case MotionEvent.ACTION_CANCEL:
                    isSelected = false;
                    break;
            }
            invalidate();
        }
        return super.onTouchEvent(event);
    }

    @Override
    public void setOnItemSelectedListener(final OnItemSelectedListener listener) {

        final OnItemSelectedListener onItemSelectedListener = new OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {


                if (floatingLabelText != null) {
                    if (!floatingLabelVisible && position != 0) {
                        showFloatingLabel();
                    } else if (!floatingLabelVisible && position == 0 && !mSpinnerAdapter.getItem(position).toString().contains(getResources().getString(R.string.select_str))) {
                        showFloatingLabel();
                    } else if (floatingLabelVisible && position == 0 && mSpinnerAdapter.getItem(position).toString().contains(getResources().getString(R.string.select_str))) {
                        hideFloatingLabel();
                    }
                }

                if (position != lastPosition && error != null) {
                    setError(null);
                }
                lastPosition = position;

                if (listener != null) {
//                    position = floatingLabelText != null ? 0 : position;
                    listener.onItemSelected(parent, view, position, id);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                if (listener != null) {
                    listener.onNothingSelected(parent);
                }
            }
        };

        super.setOnItemSelectedListener(onItemSelectedListener);
    }

    @Override
    public void onAnimationUpdate(ValueAnimator animation) {
        invalidate();
    }


    /*
    * **********************************************************************************
    * GETTERS AND SETTERS
    * **********************************************************************************
    */


    public Typeface getTypeface() {
        return typeface;
    }

    public void setTypeface(Typeface typeface) {
        this.typeface = typeface;
    }

    public int getBaseColor() {
        return baseColor;
    }

    public void setBaseColor(int baseColor) {
        this.baseColor = baseColor;
        textPaint.setColor(baseColor);
        baseAlpha = textPaint.getAlpha();
        invalidate();
    }


    public int getErrorColor() {
        return errorColor;
    }

    public void setErrorColor(int errorColor) {
        this.errorColor = errorColor;
        invalidate();
    }

    public void setFloatingLabelText(CharSequence floatingLabelText) {
        this.floatingLabelText = floatingLabelText;
        invalidate();
    }

    public void setFloatingLabelText(int resid) {
        String floatingLabelText = getResources().getString(resid);
        setFloatingLabelText(floatingLabelText);
    }

    public CharSequence getFloatingLabelText() {
        return this.floatingLabelText;
    }

    public void setError(CharSequence error) {
        this.error = error;
        prepareBottomPadding();

        requestLayout();
    }

    public void setError(int resid) {
        CharSequence error = getResources().getString(resid);
        setError(error);
    }

    @Override
    public void setEnabled(boolean enabled) {
        if (!enabled) {
            isSelected = false;
            invalidate();
        }
        super.setEnabled(enabled);
    }

    public CharSequence getError() {
        return this.error;
    }


    /**
     * @deprecated {use @link #setPaddingSafe(int, int, int, int)} to keep internal computation OK
     */
    @Deprecated
    @Override
    public void setPadding(int left, int top, int right, int bottom) {
        super.setPadding(left, top, right, bottom);
    }


    public void setPaddingSafe(int left, int top, int right, int bottom) {
        innerPaddingRight = right;
        innerPaddingLeft = left;
        innerPaddingTop = top;
        innerPaddingBottom = bottom;
        updatePadding();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public void setAdapter(SpinnerAdapter adapter) {

        hintAdapter = new HintAdapter(adapter, getContext());
        super.setAdapter(hintAdapter);
    }

    @Override
    public SpinnerAdapter getAdapter() {
        return hintAdapter != null ? hintAdapter.getWrappedAdapter() : null;
    }

    private float getFloatingLabelPercent() {
        return floatingLabelPercent;
    }

    private void setFloatingLabelPercent(float floatingLabelPercent) {
        this.floatingLabelPercent = floatingLabelPercent;
    }

    private int getErrorLabelPosX() {
        return errorLabelPosX;
    }

    private void setErrorLabelPosX(int errorLabelPosX) {
        this.errorLabelPosX = errorLabelPosX;
    }

    private float getCurrentNbErrorLines() {
        return currentNbErrorLines;
    }

    private void setCurrentNbErrorLines(float currentNbErrorLines) {
        this.currentNbErrorLines = currentNbErrorLines;
        updateBottomPadding();
    }

    @Override
    public Object getItemAtPosition(int position) {
        return position;
    }

    @Override
    public long getItemIdAtPosition(int position) {
        return position;
    }


    /*
     * **********************************************************************************
     * INNER CLASS
     * **********************************************************************************
     */
    private SpinnerAdapter mSpinnerAdapter;

    private class HintAdapter extends BaseAdapter {


        public HintAdapter(SpinnerAdapter spinnerAdapter, Context context) {
            mSpinnerAdapter = spinnerAdapter;
            mContext = context;
        }


        @Override
        public int getItemViewType(int position) {


            return position;
        }

        @Override
        public int getCount() {
            return mSpinnerAdapter.getCount();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }


        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return buildView(position, convertView, parent, false);
        }

        @Override
        public View getDropDownView(int position, View convertView, ViewGroup parent) {
            return buildView(position, convertView, parent, true);
        }

        private View buildView(int position, View convertView, ViewGroup parent, boolean isDropDownView) {


            final LayoutInflater inflater = LayoutInflater.from(mContext);
            final int resid = isDropDownView ? R.layout.spinner_new_retailer_text_list_item : R.layout.spinner_new_retailer_text_list_item;
            final TextView textView = (TextView) inflater.inflate(resid, parent, false);
            textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
            textView.setTypeface(typeface);
            textView.setText(mSpinnerAdapter.getItem(position).toString());
            if (position == 0) {
                // Set the hint text color for 0th position of mSpinneradapter

                textView.setTextColor(ContextCompat.getColor(mContext, R.color.gray_text));
            } else {

                textView.setTextColor(ContextCompat.getColor(mContext, R.color.filer_level_text_color));
                textView.setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(R.dimen.font_small));
                textView.setTypeface(bmodel.configurationMasterHelper.getFontRoboto(ConfigurationMasterHelper.FontType.LIGHT));

            }

            return textView;
        }

        private SpinnerAdapter getWrappedAdapter() {
            return mSpinnerAdapter;
        }
    }
}
