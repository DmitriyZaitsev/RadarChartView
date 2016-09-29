package com.dzaitsev.radarchartview;

import android.content.Context;
import android.util.AttributeSet;
import com.dzaitsev.widget.RadarChartView;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-29, 15:08
 */
public final class ChartView extends RadarChartView {
  public ChartView(Context context) {
    super(context);
  }

  public ChartView(Context context, AttributeSet attrs) {
    super(context, attrs);
    addOrReplace("Item 1", 42);
    addOrReplace("Item 2", 11);
    addOrReplace("Item 3", 33);
    addOrReplace("Item 4", 26);
    //addOrReplace("Item 5", 5);
    //addOrReplace("Item 6", 16);
    //addOrReplace("Item 7", 30);
    //addOrReplace("Item 8", 30);
    //addOrReplace("Item 9", 30);
    //addOrReplace("Item 10", 30);
    //addOrReplace("Item 11", 30);
    //addOrReplace("Item 12", 30);
  }

  public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
