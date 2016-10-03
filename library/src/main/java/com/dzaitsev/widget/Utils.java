package com.dzaitsev.widget;

import android.graphics.Paint;
import android.graphics.PointF;
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

  @NonNull static PointF createPoint(float radius, double alpha, float x0, float y0) {
    return new PointF((float) (radius * cos(alpha) + x0), (float) (radius * sin(alpha) + y0));
  }

  @NonNull static PointF[] createPoints(int amount, float radius, float x0, float y0) {
    final PointF[] points = new PointF[amount];
    final double angle = 2 * PI / amount;
    for (int i = 0; i < amount; i++) {
      final double alpha = angle * i - PI / 2;
      points[i] = createPoint(radius, alpha, x0, y0);
    }
    return points;
  }
}
