package com.dzaitsev.radarchartview;

import android.content.Context;
import android.util.AttributeSet;
import com.dzaitsev.widget.RadarChartView;
import java.util.Random;

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
    final Random random = new Random(System.nanoTime());
    for (int i = 0; i < random.nextInt(9) + 3; i++) {
      final int value = Math.min((random.nextInt(270) + 10), 280);
      addOrReplace("Item " + i, value);
    }
  }

  public ChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
  }
}
