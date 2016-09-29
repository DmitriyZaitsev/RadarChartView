package com.dzaitsev.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;
import java.util.LinkedHashMap;
import java.util.Map;

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
  private final Map<String, Float> mSectors;
  private       int                mStartColor;
  private       int                mEndColor;
  private       int                mAxisColor;
  private       Paint[]            mPaints;
  private       Path[]             mPaths;
  private       float              mAxisMax;
  private       float              mAxisTick;
  private       int                mTextSize;
  private       int                mCenterX;
  private       int                mCenterY;
  private       int                mParts;
  private       Paint              mAxisPaint;
  private       Ring[]             mRings;

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
    values.recycle();

    mAxisPaint = createPaint(mAxisColor);

    mParts = ((int) mAxisMax / (int) mAxisTick) + ((int) mAxisMax % (int) mAxisTick > 0 ? 1 : 0);
    setParts(mParts);
  }

  public void addOrReplace(String sector, float value) {
    mSectors.put(sector, value);
    invalidate();
  }

  public void remove(String sector) {
    mSectors.remove(sector);
    invalidate();
  }

  public void setParts(int parts) {
    mParts = parts;
    mPaints = new Paint[parts];
    mPaths = new Path[parts];
    mRings = new Ring[parts];

    final int size = parts - 1;
    if (size == 0) {
      mPaints[0] = createPaint(mStartColor);
      mPaths[0] = new Path();
      mRings[0] = new Ring(mAxisMax, mAxisMax);
    } else {
      for (int i = 0; i <= size; i++) {
        final int alpha = color(alpha(mStartColor), alpha(mEndColor), size, i);
        final int red = color(red(mStartColor), red(mEndColor), size, i);
        final int green = color(green(mStartColor), green(mEndColor), size, i);
        final int blue = color(blue(mStartColor), blue(mEndColor), size, i);

        mPaints[i] = createPaint(argb(alpha, red, green, blue));
        mPaths[i] = new Path();
        mRings[i] = new Ring(mAxisTick * (i + 1), mAxisTick);
      }
      mRings[size] = new Ring(mAxisMax, mAxisMax - mRings[size - 1].radius);
    }
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    mCenterX = (getRight() - getLeft()) / 2;
    mCenterY = (getBottom() - getTop()) / 2;

    final int size = mSectors.size();
    if (size < 3) {
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
    for (int i = 0; i < mParts; i++) {
      final Path path = mPaths[i];
      final Ring ring = mRings[i];

      path.moveTo(mCenterX, mCenterY);
      path.addCircle(mCenterX, mCenterY, ring.fixedRadius, CW);
      path.close();

      final Paint paint = mPaints[i];
      paint.setStrokeWidth(ring.width);
      canvas.drawPath(path, paint);
    }
  }

  public void clearSectors() {
    mSectors.clear();
    invalidate();
  }

  private void drawPolygons(Canvas canvas, int size) {
    for (int i = 0; i < mParts; i++) {
      final Path path = mPaths[i];
      final Ring ring = mRings[i];
      final Point[] points = createPoints(size, ring.fixedRadius, mCenterX, mCenterY);
      final Point start = points[0];
      path.moveTo(start.x, start.y);
      for (int j = 1; j < size; j++) {
        final Point to = points[j];
        path.lineTo(to.x, to.y);
      }
      path.lineTo(start.x, start.y);
      path.close();

      final Paint paint = mPaints[i];
      paint.setStrokeWidth(ring.width);
      canvas.drawPath(path, paint);
    }
  }

  private static class Ring {
    final float width;
    final float radius;
    final float fixedRadius;

    Ring(float radius, float width) {
      this.radius = radius;
      this.width = width;
      fixedRadius = radius - width / 2;
    }
  }
}
