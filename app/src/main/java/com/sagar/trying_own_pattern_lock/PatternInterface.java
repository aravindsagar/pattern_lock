package com.sagar.trying_own_pattern_lock;

import android.graphics.Color;

import java.util.List;

/**
 * Created by aravind on 20/9/14.
 */
public interface PatternInterface {
    public void clearPattern();
    public List<Integer> getPattern();
    public void setRingColor(Color color);
    public void setOnPatternEnteredListener(PatternGridView.OnPatternEnteredListener listener);
}
