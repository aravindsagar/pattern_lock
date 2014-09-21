package com.sagar.trying_own_pattern_lock;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ImageView;
import android.widget.TableLayout;
import android.widget.TableRow;

import java.util.ArrayList;


public class MyActivity extends Activity {
    private static final String LOG_TAG = MyActivity.class.getSimpleName();
    private static final int NUMBER_OF_DOTS = 9;
    private static final int NUMBER_OF_ROWS = 3;
    ImageView[] dots = new ImageView[NUMBER_OF_DOTS];
    TableLayout patternLayout;
    TableRow[] tableRows = new TableRow[NUMBER_OF_ROWS];

    ArrayList<Integer> dotsList = new ArrayList<Integer>();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my);
        patternLayout = (TableLayout) findViewById(R.id.pattern_layout);
        dots[0] = (ImageView) findViewById(R.id.dot1);
        dots[1] = (ImageView) findViewById(R.id.dot2);
        dots[2] = (ImageView) findViewById(R.id.dot3);
        dots[3] = (ImageView) findViewById(R.id.dot4);
        dots[4] = (ImageView) findViewById(R.id.dot5);
        dots[5] = (ImageView) findViewById(R.id.dot6);
        dots[6] = (ImageView) findViewById(R.id.dot7);
        dots[7] = (ImageView) findViewById(R.id.dot8);
        dots[8] = (ImageView) findViewById(R.id.dot9);
        tableRows[0] = (TableRow) findViewById(R.id.row1);
        tableRows[1] = (TableRow) findViewById(R.id.row2);
        tableRows[2] = (TableRow) findViewById(R.id.row3);

        /*patternLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
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
                return false;
            }
        });*/
    }

    private void handleActionDown(MotionEvent event){
        int dotNumber = findDot(event);
        dotsList.add(dotNumber);
        dots[dotNumber].setImageResource(R.drawable.aosp_indicator_code_lock_point_area_blue_holo);
        Intent intent = new Intent(this, MyActivity2.class);
        startActivity(intent);
    }

    private void handleActionMove(MotionEvent event){
//        Log.d(LOG_TAG, "Move on " + findDot(event));
        int dotNumber = findDot(event);
        dotsList.add(dotNumber);
        dots[dotNumber].setImageResource(R.drawable.aosp_indicator_code_lock_point_area_blue_holo);
    }

    private void handleActionUp(MotionEvent event){
//        Log.d(LOG_TAG, "Up on " + findDot(event));
    }

    private void handleActionCancel(MotionEvent event){
//        Log.d(LOG_TAG, "Cancel on " + findDot(event));
    }

    private int findDot(MotionEvent event){
        for (int i=0; i<NUMBER_OF_DOTS; i++) {
            float xMin = patternLayout.getX() + dots[i].getX(), yMin = patternLayout.getY();
            float xMax = xMin + dots[i].getWidth(), yMax;
            if(getActionBar() != null){
                yMin += getActionBar().getHeight();
            }
            if(i<3){
                yMin += tableRows[0].getY();
                yMax = yMin + tableRows[0].getHeight();
            }
            else if(i>=3 && i<6){
                yMin += tableRows[1].getY();
                yMax = yMin + tableRows[1].getHeight();
            } else {
                yMin += tableRows[2].getY();
                yMax = yMin + tableRows[2].getHeight();
            }

            if (event.getX() >= xMin && event.getY() >= yMin
                    && event.getX() <= xMax && event.getY() <= yMax) {
                return i;
            }
        }
        return -1;
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.my, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        /*Log.d(LOG_TAG, "Pattern layout: " + patternLayout.getX() + ", " + patternLayout.getY());
        Log.d(LOG_TAG, "Dot 0: " + dots[0].getX() + ", " + dots[0].getY());
        Log.d(LOG_TAG, "Dot 1: " + dots[1].getX() + ", " + dots[1].getY());
        Log.d(LOG_TAG, "Dot 2: " + dots[2].getX() + ", " + dots[2].getY());
        Log.d(LOG_TAG, "Dot 3: " + dots[3].getX() + ", " + dots[3].getY());
        Log.d(LOG_TAG, "Event coord:" + event.getX()*event.getXPrecision() + "," + event.getY()*event.getYPrecision());
        Log.d(LOG_TAG, "Event raw coord:" + event.getX()*event.getRawX() + "," + event.getRawY());*/
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
        return super.onTouchEvent(event);
    }
}
