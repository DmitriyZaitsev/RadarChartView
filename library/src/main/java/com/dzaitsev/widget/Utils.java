package com.dzaitsev.widget;

import android.graphics.Paint;
import android.graphics.PointF;
import android.support.annotation.NonNull;

import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
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
    final Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
    paint.setStyle(STROKE);
    paint.setColor(color);
    return paint;
  }

  static void mutatePaint(Paint paint, int color, float strokeWidth, Paint.Style style) {
    paint.setColor(color);
    paint.setStrokeWidth(strokeWidth);
    paint.setStyle(style);
  }

  static int gradient(int startColor, int endColor, int factor, int steps) {
    final int alpha = between(alpha(startColor), alpha(endColor), factor, steps);
    final int red = between(red(startColor), red(endColor), factor, steps);
    final int green = between(green(startColor), green(endColor), factor, steps);
    final int blue = between(blue(startColor), blue(endColor), factor, steps);
    return argb(alpha, red, green, blue);
  }

  @NonNull static PointF createPointF(float radius, double alpha, float x0, float y0) {
    return new PointF((float) (radius * cos(alpha) + x0), (float) (radius * sin(alpha) + y0));
  }

  @NonNull static PointF[] createPointFs(int count, float radius, float x0, float y0) {
    final PointF[] points = new PointF[count];
    final double angle = 2 * PI / count;
    for (int i = 0; i < count; i++) {
      final double alpha = angle * i - PI / 2;
      points[i] = createPointF(radius, alpha, x0, y0);
    }
    return points;
  }

  private static int between(int startColor, int endColor, int factor, int steps) {
    final float ratio = (float) factor / steps;
    return (int) (endColor * ratio + startColor * (1 - ratio));
  }
}
