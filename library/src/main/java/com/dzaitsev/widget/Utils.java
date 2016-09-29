package com.dzaitsev.widget;

import android.content.res.Resources;
import android.graphics.Paint;
import android.graphics.Point;
import android.support.annotation.NonNull;

import static android.graphics.Paint.Style.STROKE;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.sin;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-29, 20:32
 */
final class Utils {
  @NonNull static Paint createPaint(int color) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(STROKE);
    paint.setColor(color);
    return paint;
  }

  static int color(int start, int end, int size, int factor) {
    return start + (end - start) / size * factor;
  }

  private static int px(float dp, Resources resources) {
    return (int) (dp * resources.getDisplayMetrics().density + 0.5F);
  }

  @NonNull static Point[] createPoints(int amount, float radius, float x0, float y0) {
    final Point[] points = new Point[amount];
    final double angle = 2 * PI / amount;
    for (int i = 0; i < amount; i++) {
      final double alpha = angle * i - PI / 2;
      points[i] = new Point((int) (radius * cos(alpha) + x0), (int) (radius * sin(alpha) + y0));
    }
    return points;
  }
}
