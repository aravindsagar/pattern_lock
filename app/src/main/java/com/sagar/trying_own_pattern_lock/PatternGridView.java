package com.sagar.trying_own_pattern_lock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by aravind on 20/9/14.
 */
public class PatternGridView extends View implements PatternInterface{

    public static final int PATTERN_TYPE_STORE = 0;
    public static final int PATTERN_TYPE_CHECK = 1;

    private static final int NUMBER_OF_COLUMNS = 3;
    private static final int NUMBER_OF_ROWS = 3;

    private int mPatternType;

    private int mPaddingLeft, mPaddingRight, mPaddingTop, mPaddingBottom;

    private float mCellHeight, mCellWidth;

    private OnPatternEnteredListener mOnPatternEnteredListener;

    private CellTracker mCellTracker = new CellTracker();

    private Paint mDotPaint, mLinePaint, mTrianglePaint;

    private int[][] mCellCenters = new int[NUMBER_OF_ROWS][NUMBER_OF_COLUMNS];

    public PatternGridView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attributeSet,
                R.styleable.PatternGridView, 0, 0);
        try{
            mPatternType = styledAttributes.getInt(R.styleable.PatternGridView_patternType, 0);
        }finally {
            styledAttributes.recycle();
        }
        init();
    }

    private void init(){
        mDotPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mDotPaint.setStyle(Paint.Style.FILL);

        mTrianglePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mTrianglePaint.setStyle(Paint.Style.FILL);

        mLinePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mLinePaint.setStyle(Paint.Style.STROKE);
        mLinePaint.setAlpha(100);
        mLinePaint.setDither(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);

        switch (mPatternType){
            case PATTERN_TYPE_STORE:
                mDotPaint.setColor(Color.BLACK);
                mTrianglePaint.setColor(Color.BLACK);
                mLinePaint.setColor(Color.BLACK);
                break;
            case PATTERN_TYPE_CHECK:
                mDotPaint.setColor(Color.WHITE);
                mTrianglePaint.setColor(Color.WHITE);
                mLinePaint.setColor(Color.WHITE);
                break;
        }

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
    }

    public int getPatternType() {
        return mPatternType;
    }

    public void setPatternType(int mPatternType) {
        this.mPatternType = mPatternType;
        invalidate();
        requestLayout();
    }

    @Override
    public void clearPattern() {

    }

    @Override
    public List<Integer> getPattern() {
        return null;
    }

    @Override
    public void setRingColor(Color color) {

    }

    @Override
    public void setOnPatternEnteredListener(OnPatternEnteredListener listener){
        mOnPatternEnteredListener = listener;
    }

    public static interface OnPatternEnteredListener{
        public void onPatternEntered(List<Integer> pattern);
    }

    @Override
    protected int getSuggestedMinimumHeight() {
        return NUMBER_OF_ROWS * R.dimen.min_cell_size;
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return NUMBER_OF_COLUMNS * R.dimen.min_cell_size;
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int width = w - (mPaddingLeft + mPaddingRight);
        int height = h - (mPaddingTop + mPaddingBottom);

        mCellHeight = h/(float) NUMBER_OF_ROWS;
        mCellWidth = w/(float) NUMBER_OF_COLUMNS;

        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void computePositions(){
        int x, y = mPaddingTop;
        for(int i=0; i<NUMBER_OF_ROWS; i++){
            x = mPaddingLeft;
            for(int j=0; j<NUMBER_OF_COLUMNS; j++){
                
                x+=mCellWidth;
            }
            y += mCellHeight;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        final int minimumWidth = getSuggestedMinimumWidth();
        final int minimumHeight = getSuggestedMinimumHeight();
        int viewWidth = resolveMeasured(widthMeasureSpec, minimumWidth);
        int viewHeight = resolveMeasured(heightMeasureSpec, minimumHeight);

        int viewDimension = Math.min(viewHeight, viewWidth);
        super.onMeasure(viewDimension, viewDimension);
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.max(specSize, desired);
                break;
            case MeasureSpec.EXACTLY:
            default:
                result = specSize;
        }
        return result;
    }

    private class CellTracker {
        private final String LOG_TAG = CellTracker.class.getSimpleName();
        private static final int mCells = NUMBER_OF_ROWS * NUMBER_OF_COLUMNS;

        private boolean[] isIncluded = new boolean[mCells];
        private ArrayList<Integer> mCellList = new ArrayList<Integer>(mCells);

        public boolean addCell(int cellNumber){
            if(cellNumber >= mCells || cellNumber < 0){
                Log.e(LOG_TAG, "Invalid cellNumber: " + cellNumber);
                return false;
            }
            if(isIncluded[cellNumber]){
                return false;
            } else {
                isIncluded[cellNumber] = true;
                mCellList.add(cellNumber);
                return true;
            }
        }

        public List<Integer> getCellNumberList(){
            return mCellList;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

    }
}
