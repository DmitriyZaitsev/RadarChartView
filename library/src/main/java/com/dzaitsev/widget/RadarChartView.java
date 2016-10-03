package com.dzaitsev.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static android.graphics.Color.parseColor;
import static android.graphics.Path.Direction.CW;
import static com.dzaitsev.widget.Utils.createPaint;
import static com.dzaitsev.widget.Utils.createPoints;
import static com.dzaitsev.widget.Utils.gradient;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.min;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-28, 14:15
 */
public class RadarChartView extends View {
  private final LinkedHashMap<String, Float> mSectors   = new LinkedHashMap<>();
  private final Rect                         mRect      = new Rect();
  private final Path                         mPath      = new Path();
  private final TextPaint                    mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);

  private int     mStartColor;
  private int     mEndColor;
  private int     mAxisColor;
  private float   mAxisMax;
  private float   mAxisTick;
  private Paint   mAxisPaint;
  private int     mCenterX;
  private int     mCenterY;
  private Ring[]  mRings;
  private boolean mCirclesOnly;

  public RadarChartView(Context context) {
    this(context, null);
  }

  public RadarChartView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RadarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
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
    final int textSize = values.getDimensionPixelSize(R.styleable.RadarChartView_textSize, 15);
    mCirclesOnly = values.getBoolean(R.styleable.RadarChartView_circlesOnly, false);
    values.recycle();

    mAxisPaint = createPaint(mAxisColor);
    mTextPaint.setTextSize(textSize);
    mTextPaint.density = getResources().getDisplayMetrics().density;

    buildRings();
  }

  public void addOrReplace(String sector, float value) {
    mSectors.put(sector, value);
    fillRingsWithPoints();
    invalidate();
  }

  public void clearSectors() {
    mSectors.clear();
    fillRingsWithPoints();
    invalidate();
  }

  public void remove(String sector) {
    mSectors.remove(sector);
    fillRingsWithPoints();
    invalidate();
  }

  public int getAxisColor() {
    return mAxisColor;
  }

  public void setAxisColor(int axisColor) {
    mAxisColor = axisColor;
    invalidate();
  }

  public float getAxisMax() {
    return mAxisMax;
  }

  public void setAxisMax(float axisMax) {
    mAxisMax = axisMax;
    buildRings();
    invalidate();
  }

  public float getAxisTick() {
    return mAxisTick;
  }

  public void setAxisTick(float axisTick) {
    mAxisTick = axisTick;
    buildRings();
    invalidate();
  }

  public int getEndColor() {
    return mEndColor;
  }

  public void setEndColor(int endColor) {
    mEndColor = endColor;
    invalidate();
    invalidate();
  }

  public int getStartColor() {
    return mStartColor;
  }

  public void setStartColor(int startColor) {
    mStartColor = startColor;
    invalidate();
  }

  public boolean isCirclesOnly() {
    return mCirclesOnly;
  }

  public void setCirclesOnly(boolean circlesOnly) {
    mCirclesOnly = circlesOnly;
    invalidate();
  }

  public void setTextSize(float textSize) {
    mTextPaint.setTextSize(textSize);
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    if (isInEditMode()) {
      calculateCenter();
    }

    final int size = mSectors.size();
    if (size < 3 || mCirclesOnly) {
      drawCircles(canvas);
    } else {
      drawPolygons(canvas, size);
    }
    drawAxis(canvas, size);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int width = getMeasuredWidth();
    final int height = getMeasuredHeight();
    if (width != height) {
      final int size = min(width, height);
      setMeasuredDimension(size, size);
    }
    calculateCenter();
  }

  private void buildRings() {
    final float fParts = mAxisMax / mAxisTick;
    final int iParts = (int) fParts;
    final int ringsCount = iParts + (fParts - iParts > 0 ? 1 : 0);

    mRings = new Ring[ringsCount];
    if (ringsCount == 1) {
      mRings[0] = new Ring(mAxisMax, mAxisMax, mStartColor);
    } else {
      for (int i = 0; i < ringsCount; i++) {
        if (i == ringsCount - 1) {
          mRings[i] = new Ring(mAxisMax, mAxisMax - mRings[ringsCount - 2].radius, mEndColor);
        } else {
          mRings[i] = new Ring(mAxisTick * (i + 1), mAxisTick, gradient(mStartColor, mEndColor, i, ringsCount));
        }
      }
    }

    fillRingsWithPoints();
  }

  private void calculateCenter() {
    mCenterX = (getRight() - getLeft()) / 2 + getPaddingLeft() - getPaddingRight();
    mCenterY = (getBottom() - getTop()) / 2 + getPaddingTop() - getPaddingBottom();

    fillRingsWithPoints();
  }

  private void fillRingsWithPoints() {
    for (Ring ring : mRings) {
      ring.points = createPoints(mSectors.size(), ring.fixedRadius, mCenterX, mCenterY);
    }
  }

  private void drawAxis(Canvas canvas, int size) {
    final PointF[] points = createPoints(size, mAxisMax, mCenterX, mCenterY);
    final Iterator<String> sectors = mSectors.keySet()
        .iterator();
    for (final PointF point : points) {
      mPath.reset();
      mPath.moveTo(mCenterX, mCenterY);
      mPath.lineTo(point.x, point.y);
      mPath.close();
      canvas.drawPath(mPath, mAxisPaint);

      final String title = sectors.next();
      mTextPaint.getTextBounds(title, 0, title.length(), mRect);
      float x = point.x > mCenterX ? point.x : point.x - mRect.width();
      float y = point.y > mCenterY ? point.y + mRect.height() : point.y;
      canvas.drawText(title, x, y, mTextPaint);
    }
  }

  private void drawCircles(Canvas canvas) {
    for (final Ring ring : mRings) {
      mPath.reset();
      mPath.moveTo(mCenterX, mCenterY);
      mPath.addCircle(mCenterX, mCenterY, ring.fixedRadius, CW);
      mPath.close();

      ring.paint.setStrokeWidth(ring.width + 2);
      canvas.drawPath(mPath, ring.paint);
    }
  }

  private void drawPolygons(Canvas canvas, int size) {
    for (final Ring ring : mRings) {
      final PointF[] points = ring.points;
      final PointF start = points[0];

      mPath.reset();
      mPath.moveTo(start.x, start.y);
      for (int j = 1; j < size; j++) {
        final PointF to = points[j];
        mPath.lineTo(to.x, to.y);
      }
      mPath.lineTo(start.x, start.y);
      mPath.close();

      ring.paint.setStrokeWidth((float) (ring.width * cos(PI / size)) + 2);
      canvas.drawPath(mPath, ring.paint);
    }
  }

  private static class Ring {
    final float width;
    final float radius;
    final float fixedRadius;
    final Paint paint;
    PointF[] points;

    Ring(float radius, float width, int color) {
      this.radius = radius;
      this.width = width;
      paint = createPaint(color);
      fixedRadius = radius - width / 2;
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
