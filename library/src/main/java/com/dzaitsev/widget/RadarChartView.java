package com.dzaitsev.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.util.Log;
import android.view.View;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.graphics.Color.RED;
import static android.graphics.Color.alpha;
import static android.graphics.Color.argb;
import static android.graphics.Color.blue;
import static android.graphics.Color.green;
import static android.graphics.Color.parseColor;
import static android.graphics.Color.red;
import static android.graphics.Path.Direction.CW;
import static com.dzaitsev.widget.Utils.color;
import static com.dzaitsev.widget.Utils.createPaint;
import static com.dzaitsev.widget.Utils.createPoints;
import static java.lang.Math.min;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-28, 14:15
 */
public class RadarChartView extends View {
  private static final int MIN_SIZE = 300;
  private static final String TAG = "RadarChartView";
  private final Map<String, Float> mSectors;
  private       int                mStartColor;
  private       int                mEndColor;
  private       int                mAxisColor;
  private       float              mAxisMax;
  private       float              mAxisTick;
  private       int                mTextSize;
  private       int                mCenterX;
  private       int                mCenterY;
  private       Paint              mAxisPaint;
  private       Ring[]             mRings;
  private       boolean            mCirclesOnly;

  public RadarChartView(Context context) {
    this(context, null);
  }

  public RadarChartView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RadarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mSectors = new LinkedHashMap<>();
    final TypedArray colors = context.obtainStyledAttributes(attrs, new int[] {
        R.attr.colorAccent, R.attr.colorPrimary, R.attr.colorPrimaryDark
    }, defStyleAttr, 0);
    final int colorAccent = colors.getColor(0, parseColor("#22737b"));
    final int colorPrimary = colors.getColor(1, parseColor("#c3e3e5"));
    final int colorPrimaryDark = colors.getColor(2, parseColor("#5f9ca1"));
    colors.recycle();

    final TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RadarChartView, defStyleAttr, 0);
    mStartColor = values.getColor(R.styleable.RadarChartView_startColor, colorPrimaryDark);
    mEndColor = values.getColor(R.styleable.RadarChartView_endColor, colorPrimary);
    mAxisColor = values.getColor(R.styleable.RadarChartView_axisColor, colorAccent);
    mAxisMax = values.getFloat(R.styleable.RadarChartView_axisMax, 20);
    mAxisTick = values.getFloat(R.styleable.RadarChartView_axisTick, 5);
    mTextSize = values.getDimensionPixelSize(R.styleable.RadarChartView_textSize, 12);
    mCirclesOnly = values.getBoolean(R.styleable.RadarChartView_circlesOnly, false);
    values.recycle();

    mAxisPaint = createPaint(mAxisColor);

    setAxisTickInternal(mAxisTick);
  }

  public void addOrReplace(String sector, float value) {
    mSectors.put(sector, value);
    invalidate();
  }

  public void clearSectors() {
    mSectors.clear();
    invalidate();
  }

  public void remove(String sector) {
    mSectors.remove(sector);
    invalidate();
  }

  public void setAxisTick(float axisTick) {
    mAxisTick = axisTick;
    setAxisTickInternal(axisTick);
    invalidate();
  }

  public void setCirclesOnly(boolean circlesOnly) {
    mCirclesOnly = circlesOnly;
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    mCenterX = (getRight() - getLeft()) / 2;
    mCenterY = (getBottom() - getTop()) / 2;

    final int size = mSectors.size();
    if (size < 3 || mCirclesOnly) {
      drawCircles(canvas);
    } else {
      drawPolygons(canvas, size);
    }
    drawAxis(canvas, size);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int size = min(resolveSize(MIN_SIZE, widthMeasureSpec), resolveSize(MIN_SIZE, heightMeasureSpec));
    setMeasuredDimension(size, size);
  }

  private void drawAxis(Canvas canvas, int size) {
    final Path path = new Path();
    final Point[] points = createPoints(size, mAxisMax, mCenterX, mCenterY);
    for (int i = 0; i < size; i++) {
      path.moveTo(mCenterX, mCenterY);
      final Point point = points[i];
      path.lineTo(point.x, point.y);
      path.close();
      canvas.drawPath(path, mAxisPaint);
    }
  }

  private void drawCircles(Canvas canvas) {
    for (final Ring ring : mRings) {
      final Path path = ring.path;

      path.reset();
      path.moveTo(mCenterX, mCenterY);
      path.addCircle(mCenterX, mCenterY, ring.fixedRadius, CW);
      path.close();

      canvas.drawPath(path, ring.paint);
    }
  }

  private void drawPolygons(Canvas canvas, int size) {
    for (final Ring ring : mRings) {
      final Path path = ring.path;
      final Point[] points = createPoints(size, ring.fixedRadius, mCenterX, mCenterY);
      final Point start = points[0];

      path.reset();
      path.moveTo(start.x, start.y);
      for (int j = 1; j < size; j++) {
        final Point to = points[j];
        path.lineTo(to.x, to.y);
      }
      path.lineTo(start.x, start.y);
      path.close();

      canvas.drawPath(path, ring.paint);
    }
  }

  private void setAxisTickInternal(float axisTick) {
    Log.i(TAG, "setAxisTick = " + axisTick);
    final int parts = ((int) mAxisMax / (int) axisTick) + ((int) mAxisMax % (int) axisTick > 0 ? 1 : 0);
    Log.i(TAG, "parts       = " + parts);
    mRings = new Ring[parts];

    final int size = parts - 1;
    if (size == 0) {
      mRings[0] = new Ring(mAxisMax, mAxisMax, mStartColor);
    } else {
      for (int i = 0; i <= size; i++) {
        final int alpha = color(alpha(mStartColor), alpha(mEndColor), size, i);
        final int red = color(red(mStartColor), red(mEndColor), size, i);
        final int green = color(green(mStartColor), green(mEndColor), size, i);
        final int blue = color(blue(mStartColor), blue(mEndColor), size, i);
        final int color = argb(alpha, red, green, blue);

        if (i == size) {
          mRings[i] = new Ring(mAxisMax, mAxisMax - mRings[size - 1].radius, color);
        } else {
          mRings[i] = new Ring(axisTick * (i + 1), axisTick, color);
        }
      }
    }
  }

  private static class Ring {
    final float width;
    final float radius;
    final float fixedRadius;
    final Paint paint;
    final Path  path;

    Ring(float radius, float width, int color) {
      this.radius = radius;
      this.width = width;
      paint = createPaint(color);
      paint.setStrokeWidth(width);
      path = new Path();
      fixedRadius = radius - width / 2;
      Log.v(TAG, toString());
    }

    @Override public String toString() {
      return "Ring{" +
          "radius=" + radius +
          ", width=" + width +
          ", fixedRadius=" + fixedRadius +
          '}';
    }
  }
}
