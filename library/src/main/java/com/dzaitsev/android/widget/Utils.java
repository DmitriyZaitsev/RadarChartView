package com.dzaitsev.android.widget;

import android.content.res.Resources;
import android.graphics.Paint;
import android.support.annotation.NonNull;
import android.util.DisplayMetrics;
import android.util.TypedValue;

import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.red;
import static android.graphics.Paint.Style.STROKE;
import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.sin;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-29, 20:32
 */
@SuppressWarnings("NumericCastThatLosesPrecision") //
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


  @NonNull static float[] createPoint(float radius, double alpha, float x0, float y0) {
    final float[] point = new float[2];
    point[0] = (float) (radius * cos(alpha) + x0);
    point[1] = (float) (radius * sin(alpha) + y0);
    return point;
  }

  @NonNull static float[] createPoints(int count, float radius, float x0, float y0) {
    final int length = count + count;
    final float[] points = new float[length];
    final double angle = 2 * PI / count;
    int j = 0;
    for (int i = 0; i < length; i += 2) {
      final double alpha = angle * j++ - PI / 2;
      final float[] point = createPoint(radius, alpha, x0, y0);
      points[i] = point[0];
      points[i + 1] = point[1];
    }
    return points;
  }

  static int dp(float dp, DisplayMetrics metrics) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, metrics);
  }

  static int sp(float sp, DisplayMetrics metrics) {
    return (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, sp, metrics);
  }

  private static int between(int startColor, int endColor, int factor, int steps) {
    final float ratio = (float) factor / steps;
    return (int) (endColor * ratio + startColor * (1 - ratio));
  }
}
