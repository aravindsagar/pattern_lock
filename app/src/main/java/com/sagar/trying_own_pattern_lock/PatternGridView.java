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

    private final float minCellSize = getResources().getDimension(R.dimen.min_cell_size);

    private int mPatternType;

    private int mPaddingLeft, mPaddingRight, mPaddingTop, mPaddingBottom;

    private float mCellHeight, mCellWidth;

    private OnPatternEnteredListener mOnPatternEnteredListener;

    private CellTracker mCellTracker = new CellTracker();

    private Paint mInnerCirclePaint, mOuterCirclePaint, mLinePaint, mTrianglePaint;

    private int[] mRowCenters = new int[NUMBER_OF_ROWS];
    private int[] mColumnCenters = new int[NUMBER_OF_COLUMNS];

    private int mInnerCircleRadius = (int) getResources().getDimension(R.dimen.inner_circle_radius),
            mOuterCircleRadius = (int) getResources().getDimension(R.dimen.outer_circle_radius);

    private boolean isInputEnabled = false, isInitializing = true;

    private enum PatternState{BLANK, IN_PROGRESS, ENTERED};
    private PatternState patternState = PatternState.BLANK;

    public PatternGridView(Context context, AttributeSet attributeSet){
        super(context, attributeSet);
        TypedArray styledAttributes = context.getTheme().obtainStyledAttributes(attributeSet,
                R.styleable.PatternGridView, 0, 0);
        try{
            mPatternType = styledAttributes.getInt(R.styleable.PatternGridView_patternType, 0);
        }finally {
            styledAttributes.recycle();
        }
        setAlpha(0.0f);
//        setTranslationY(getTranslationY() + 50);
        init();
    }

    private void init(){
        mInnerCirclePaint = new Paint(Paint.ANTI_ALIAS_FLAG);
        mInnerCirclePaint.setStyle(Paint.Style.FILL);

        mOuterCirclePaint= new Paint(Paint.ANTI_ALIAS_FLAG);
        mOuterCirclePaint.setStyle(Paint.Style.STROKE);
        mOuterCirclePaint.setStrokeWidth(mInnerCircleRadius);

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
                mInnerCirclePaint.setColor(Color.BLACK);
                mOuterCirclePaint.setColor(Color.BLACK);
                mTrianglePaint.setColor(Color.BLACK);
                mLinePaint.setColor(Color.BLACK);
                break;
            case PATTERN_TYPE_CHECK:
                mInnerCirclePaint.setColor(Color.WHITE);
                mOuterCirclePaint.setColor(Color.WHITE);
                mTrianglePaint.setColor(Color.WHITE);
                mLinePaint.setColor(Color.WHITE);
                break;
        }

        mPaddingLeft = getPaddingLeft();
        mPaddingRight = getPaddingRight();
        mPaddingTop = getPaddingTop();
        mPaddingBottom = getPaddingBottom();
        Log.d(VIEW_LOG_TAG, "Min cell size: " + minCellSize);
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
        return mCellTracker.getCellNumberList();
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
        return (int) (NUMBER_OF_ROWS * minCellSize);
    }

    @Override
    protected int getSuggestedMinimumWidth() {
        return (int) (NUMBER_OF_COLUMNS * minCellSize);
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        int width = w - (mPaddingLeft + mPaddingRight);
        int height = h - (mPaddingTop + mPaddingBottom);

        mCellHeight = height/(float) NUMBER_OF_ROWS;
        mCellWidth = width/(float) NUMBER_OF_COLUMNS;
        mOuterCircleRadius = Math.max ((int) (mCellWidth/4.0) + mInnerCircleRadius, mOuterCircleRadius);
        computePositions();
        super.onSizeChanged(w, h, oldw, oldh);
    }

    private void computePositions(){
        int x = mPaddingLeft;
        Log.d(VIEW_LOG_TAG, "padding = " + mPaddingLeft);
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            mColumnCenters[i] = (int) (x + mCellWidth/2.0);
            x += mCellWidth;
        }
        int y = mPaddingTop;
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            mRowCenters[i] = (int) (y + mCellHeight/2.0);
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
        Log.d(VIEW_LOG_TAG, "View dimension = " + viewDimension);
        setMeasuredDimension(viewDimension, viewDimension);
    }

    private int resolveMeasured(int measureSpec, int desired) {
        int result = 0;
        int specSize = MeasureSpec.getSize(measureSpec);
        switch (MeasureSpec.getMode(measureSpec)) {
            case MeasureSpec.UNSPECIFIED:
                if((float) measureSpec < 1.2 * desired)
                    result = Math.max(specSize, desired);
                else
                    result = desired;
                break;
            case MeasureSpec.AT_MOST:
                result = Math.min(specSize, desired);
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

        public void clear(){
            mCellList.clear();
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(isInitializing){
            animate().alpha(1f).setDuration(750).start();
            isInitializing = false;
            isInputEnabled = true;
        }
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLUMNS; j++) {
                canvas.drawCircle(mColumnCenters[i], mRowCenters[j], mInnerCircleRadius, mInnerCirclePaint);
            }
        }
    }
}
