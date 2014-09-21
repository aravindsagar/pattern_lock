package com.sagar.trying_own_pattern_lock;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.util.AttributeSet;
import android.view.MotionEvent;
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
    private int[] mRowUpperBound = new int[NUMBER_OF_COLUMNS];
    private int[] mColumnUpperBound = new int[NUMBER_OF_COLUMNS];
    private int[] mRowLowerBound = new int[NUMBER_OF_COLUMNS];
    private int[] mColumnLowerBound = new int[NUMBER_OF_COLUMNS];

    private int mInnerCircleRadius = (int) getResources().getDimension(R.dimen.inner_circle_radius),
            mOuterCircleRadius = (int) getResources().getDimension(R.dimen.outer_circle_radius);

    private boolean mIsInputEnabled = false;
    private boolean mIsInitializing = true;

    private enum PatternState{BLANK, IN_PROGRESS, ENTERED};
    private PatternState mPatternState = PatternState.BLANK;

    private Path mPath = new Path();

    private int mCurrentX, mCurrentY;

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
        mLinePaint.setDither(true);
        mLinePaint.setStrokeJoin(Paint.Join.ROUND);
        mLinePaint.setStrokeCap(Paint.Cap.ROUND);
        mLinePaint.setStrokeWidth(mInnerCircleRadius);

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

        mLinePaint.setAlpha(100);

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

    public boolean isInputEnabled(){
        return mIsInputEnabled;
    }

    public void setInputEnabled(boolean mIsInputEnabled){
        this.mIsInputEnabled = mIsInputEnabled;
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
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            mColumnCenters[i] = (int) (x + mCellWidth/2.0);
            mColumnLowerBound[i] = mColumnCenters[i] - mOuterCircleRadius;
            mColumnUpperBound[i] = mColumnCenters[i] + mOuterCircleRadius;
            x += mCellWidth;
        }
        int y = mPaddingTop;
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            mRowCenters[i] = (int) (y + mCellHeight/2.0);
            mRowLowerBound[i] = mRowCenters[i] - mOuterCircleRadius;
            mRowUpperBound[i] = mRowCenters[i] + mOuterCircleRadius;
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

        public CellTracker(){
            clear();
        }

        public boolean addCell(int cellNumber){
            if(cellNumber >= mCells || cellNumber < 0){
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
            for (int i = 0; i < mCells; i++) {
                isIncluded[i] = false;
            }
        }

        public boolean isCellIncluded(int cellNumber){
            if(cellNumber >= mCells || cellNumber < 0){
                return false;
            }
            return isIncluded[cellNumber];
        }

        public void setPath(){
            if(mCellList.isEmpty())
                return;
            int cellCenterY = mRowCenters[getRowFromCellNumber(mCellList.get(0))],
                    cellCenterX = mColumnCenters[getColumnFromCellNumber(mCellList.get(0))];
            mPath.moveTo(cellCenterX, cellCenterY);
            for (int i = 1; i < mCellList.size(); i++) {
                cellCenterY = mRowCenters[getRowFromCellNumber(mCellList.get(i))];
                cellCenterX = mColumnCenters[getColumnFromCellNumber(mCellList.get(i))];
                mPath.lineTo(cellCenterX, cellCenterY);
            }
            if(mPatternState == PatternState.IN_PROGRESS)
                mPath.lineTo(mCurrentX, mCurrentY);
        }

        private int getRowFromCellNumber(int cellNumber){
            if(cellNumber >= mCells || cellNumber < 0){
                return -1;
            }
            return cellNumber/NUMBER_OF_COLUMNS;
        }
        private int getColumnFromCellNumber(int cellNumber){
            if(cellNumber >= mCells || cellNumber < 0){
                return -1;
            }
            return cellNumber%3;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        if(mIsInitializing){
            animate().alpha(1f).setDuration(750).start();
            mIsInitializing = false;
            mIsInputEnabled = true;
        }
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            for (int j = 0; j < NUMBER_OF_COLUMNS; j++) {
                canvas.drawCircle(mColumnCenters[j], mRowCenters[i], mInnerCircleRadius, mInnerCirclePaint);
                if(mCellTracker.isCellIncluded(getCellNumberFromRowAndColumn(i,j))){
                    canvas.drawCircle(mColumnCenters[j], mRowCenters[i], mOuterCircleRadius, mOuterCirclePaint);
                }
            }
        }
        if(mPatternState != PatternState.BLANK){
            mPath.rewind();
            mCellTracker.setPath();
            canvas.drawPath(mPath, mLinePaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if(!mIsInputEnabled){
            return false;
        }
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                handleActionDown(event);
                break;
            case MotionEvent.ACTION_MOVE:
                handleActionMove(event);
                break;
            case MotionEvent.ACTION_UP:
                handleActionUp(event);
                break;
            case MotionEvent.ACTION_CANCEL:
                handleActionCancel(event);
        }
        return true;
    }

    private void handleActionDown(MotionEvent event){
        mCellTracker.clear();
        if(mCellTracker.addCell(getCell(event))){
            mPatternState = PatternState.IN_PROGRESS;
        }
        invalidate();
        handleActionMove(event);
    }

    private void handleActionMove(MotionEvent event){
        for(int i=0; i<event.getHistorySize(); i++){
            mCellTracker.addCell(getHistoricalCell(event,i));
        }
        mCellTracker.addCell(getCell(event));
        mCurrentX = (int) event.getX();
        mCurrentY = (int) event.getY();
        invalidate();
    }

    private void handleActionUp(MotionEvent event){
        List<Integer> cellList = mCellTracker.getCellNumberList();
        if(cellList != null && !cellList.isEmpty() && mOnPatternEnteredListener != null){
            mOnPatternEnteredListener.onPatternEntered(cellList);
        }
        mPatternState = PatternState.ENTERED;
        invalidate();
    }

    private void handleActionCancel(MotionEvent event){

    }

    private int getCell(MotionEvent event){
        float x = event.getX(), y = event.getY();
        int row = getRowFromY(y);
        if(row < 0){
            return -1;
        }
        int column = getColumnFromX(x);
        if(column < 0){
            return -1;
        }
        return getCellNumberFromRowAndColumn(row, column);
    }

    private int getHistoricalCell(MotionEvent event, int pos){
        float x = event.getHistoricalX(pos), y = event.getHistoricalY(pos);
        int row = getRowFromY(y);
        if(row < 0){
            return -1;
        }
        int column = getColumnFromX(x);
        if(column < 0){
            return -1;
        }
        return getCellNumberFromRowAndColumn(row, column);
    }

    private int getRowFromY(float y){
        int row = -1;
        for (int i = 0; i < NUMBER_OF_ROWS; i++) {
            if(y >= mRowLowerBound[i] && y <= mRowUpperBound[i]){
                row = i;
                break;
            }
        }
        return row;
    }

    private int getColumnFromX(float x){
        int column = -1;
        for (int i = 0; i < NUMBER_OF_COLUMNS; i++) {
            if(x >= mColumnLowerBound[i] && x <= mColumnUpperBound[i]){
                column = i;
                break;
            }
        }
        return column;
    }

    private int getCellNumberFromRowAndColumn(int row, int column){
        return row * NUMBER_OF_COLUMNS + column;
    }
}
