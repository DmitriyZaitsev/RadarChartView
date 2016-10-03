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
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Path.Direction.CW;
import static com.dzaitsev.widget.Utils.createPaint;
import static com.dzaitsev.widget.Utils.createPoint;
import static com.dzaitsev.widget.Utils.createPoints;
import static com.dzaitsev.widget.Utils.gradient;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.max;
import static java.lang.Math.min;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-28, 14:15
 */
public class RadarChartView extends View {
  private final LinkedHashMap<String, Float> mAxis      = new LinkedHashMap<>();
  private final Rect                         mRect      = new Rect();
  private final Path                         mPath      = new Path();
  private final TextPaint                    mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  private final Paint                        mPaint     = createPaint(BLACK);

  private int      mStartColor;
  private int      mEndColor;
  private int      mAxisColor;
  private int      mGraphColor;
  private float    mAxisMax;
  private float    mAxisMaxInternal;
  private float    mAxisTick;
  private float    mAxisTickInternal;
  private float    mAxisWidth;
  private float    mGraphWidth;
  private int      mGraphStyle;
  private int      mCenterX;
  private int      mCenterY;
  private Ring[]   mRings;
  private boolean  mCirclesOnly;
  private boolean  mAutoSize;
  private PointF[] mVertices;
  private float    mRatio;

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
    mAxisColor = values.getColor(R.styleable.RadarChartView_axisColor, BLACK);
    mGraphColor = values.getColor(R.styleable.RadarChartView_graphColor, colorAccent);
    mAxisMax = values.getFloat(R.styleable.RadarChartView_axisMax, 20);
    mAxisTick = values.getFloat(R.styleable.RadarChartView_axisTick, mAxisMax / 5);
    final int textSize = values.getDimensionPixelSize(R.styleable.RadarChartView_textSize, 15);
    mCirclesOnly = values.getBoolean(R.styleable.RadarChartView_circlesOnly, false);
    mAutoSize = values.getBoolean(R.styleable.RadarChartView_autoSize, true);
    mAxisWidth = values.getFloat(R.styleable.RadarChartView_axisWidth, 1);
    mGraphWidth = values.getFloat(R.styleable.RadarChartView_graphWidth, 3);
    mGraphStyle = values.getInt(R.styleable.RadarChartView_graphStyle, STROKE.ordinal());
    values.recycle();

    mAxisMaxInternal = mAxisMax;
    mAxisTickInternal = mAxisTick;
    mTextPaint.setTextSize(textSize);
    mTextPaint.density = getResources().getDisplayMetrics().density;

    buildRings();
  }

  public void addOrReplace(String axis, float value) {
    mAxis.put(axis, value);
    onAxisChanged();
  }

  public void clearAxis() {
    mAxis.clear();
    onAxisChanged();
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
    setAutoSize(false);
    setAxisMaxInternal(axisMax);
  }

  public float getAxisTick() {
    return mAxisTick;
  }

  public void setAxisTick(float axisTick) {
    mAxisTick = axisTick;
    calculateRatio();
    buildRings();
    invalidate();
  }

  public float getAxisWidth() {
    return mAxisWidth;
  }

  public void setAxisWidth(float axisWidth) {
    mAxisWidth = axisWidth;
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

  public int getGraphColor() {
    return mGraphColor;
  }

  public void setGraphColor(int graphColor) {
    mGraphColor = graphColor;
    invalidate();
  }

  public int getGraphStyle() {
    return mGraphStyle;
  }

  public void setGraphStyle(int graphStyle) {
    mGraphStyle = graphStyle;
    invalidate();
  }

  public float getGraphWidth() {
    return mGraphWidth;
  }

  public void setGraphWidth(float graphWidth) {
    mGraphWidth = graphWidth;
    invalidate();
  }

  public int getStartColor() {
    return mStartColor;
  }

  public void setStartColor(int startColor) {
    mStartColor = startColor;
    invalidate();
  }

  public boolean isAutoSize() {
    return mAutoSize;
  }

  public void setAutoSize(boolean autoSize) {
    mAutoSize = autoSize;

    if (mAutoSize) {
      setAxisMaxInternal(Collections.max(mAxis.values()));
    }
  }

  public boolean isCirclesOnly() {
    return mCirclesOnly;
  }

  public void setCirclesOnly(boolean circlesOnly) {
    mCirclesOnly = circlesOnly;
    invalidate();
  }

  public void remove(String axis) {
    mAxis.remove(axis);
    onAxisChanged();
  }

  public void setTextSize(float textSize) {
    mTextPaint.setTextSize(textSize);
    invalidate();
  }

  @Override protected void onDraw(Canvas canvas) {
    if (isInEditMode()) {
      calculateRatio();
      calculateCenter();
    }

    final int count = mAxis.size();
    if (count < 3 || mCirclesOnly) {
      drawCircles(canvas);
    } else {
      drawPolygons(canvas, count);
    }
    drawValues(canvas);
    drawAxis(canvas);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    final int width = getMeasuredWidth();
    final int height = getMeasuredHeight();
    if (width != height) {
      final int size = min(width, height);
      setMeasuredDimension(size, size);
    }
    calculateRatio();
    calculateCenter();
  }

  private void buildRings() {
    final float fParts = mAxisMaxInternal / mAxisTickInternal;
    final int iParts = (int) fParts;
    final int ringsCount = max(iParts + (fParts - iParts > 0 ? 1 : 0), 1);

    mRings = new Ring[ringsCount];
    if (ringsCount == 1) {
      mRings[0] = new Ring(mAxisMaxInternal, mAxisMaxInternal, mStartColor);
    } else {
      for (int i = 0; i < ringsCount - 1; i++) {
        mRings[i] = new Ring(mAxisTickInternal * (i + 1), mAxisTickInternal, gradient(mStartColor, mEndColor, i, ringsCount));
      }
      mRings[ringsCount - 1] = new Ring(mAxisMaxInternal, mAxisMaxInternal - mRings[ringsCount - 2].radius, mEndColor);
    }

    buildVertices();
  }

  private void buildVertices() {
    final int count = mAxis.size();
    for (Ring ring : mRings) {
      ring.vertices = createPoints(count, ring.fixedRadius, mCenterX, mCenterY);
    }
    mVertices = createPoints(count, mAxisMaxInternal, mCenterX, mCenterY);
  }

  private void calculateCenter() {
    mCenterX = (getRight() - getLeft()) / 2 + getPaddingLeft() - getPaddingRight();
    mCenterY = (getBottom() - getTop()) / 2 + getPaddingTop() - getPaddingBottom();

    buildVertices();
  }

  private void calculateRatio() {
    float distance = min(getWidth() - getPaddingRight() - getPaddingLeft(), getHeight() - getPaddingBottom() - getPaddingTop()) * .5F;
    mRatio = distance > 0 ? distance / mAxisMax : 1;
    mAxisMaxInternal = mAxisMax * mRatio;
    mAxisTickInternal = mAxisTick * mRatio;
  }

  private void drawAxis(Canvas canvas) {
    final Iterator<String> axis = mAxis.keySet()
        .iterator();
    mPaint.setColor(mAxisColor);
    mPaint.setStrokeWidth(mAxisWidth);
    mPaint.setStyle(STROKE);
    for (final PointF point : mVertices) {
      mPath.reset();
      mPath.moveTo(mCenterX, mCenterY);
      mPath.lineTo(point.x, point.y);
      mPath.close();
      canvas.drawPath(mPath, mPaint);

      final String axisName = axis.next();
      mTextPaint.getTextBounds(axisName, 0, axisName.length(), mRect);
      float x = point.x > mCenterX ? point.x : point.x - mRect.width();
      float y = point.y > mCenterY ? point.y + mRect.height() : point.y;
      canvas.drawText(axisName, x, y, mTextPaint);
    }
  }

  private void drawCircles(Canvas canvas) {
    for (final Ring ring : mRings) {
      mPath.reset();
      mPath.moveTo(mCenterX, mCenterY);
      mPath.addCircle(mCenterX, mCenterY, ring.fixedRadius, CW);
      mPath.close();

      mPaint.setColor(ring.color);
      mPaint.setStrokeWidth(ring.width + 2);
      mPaint.setStyle(STROKE);
      canvas.drawPath(mPath, mPaint);
    }
  }

  private void drawPolygons(Canvas canvas, int count) {
    for (final Ring ring : mRings) {
      final PointF[] points = ring.vertices;
      final PointF start = points[0];

      mPath.reset();
      mPath.moveTo(start.x, start.y);
      for (int j = 1; j < count; j++) {
        final PointF to = points[j];
        mPath.lineTo(to.x, to.y);
      }
      mPath.lineTo(start.x, start.y);
      mPath.close();

      mPaint.setColor(ring.color);
      mPaint.setStrokeWidth((float) (ring.width * cos(PI / count)) + 2);
      mPaint.setStyle(STROKE);
      canvas.drawPath(mPath, mPaint);
    }
  }

  private void drawValues(Canvas canvas) {
    mPaint.setColor(mGraphColor);
    mPaint.setStrokeWidth(mGraphWidth);
    mPaint.setStyle(Paint.Style.values()[mGraphStyle]);
    mPath.reset();

    Float[] values = new Float[mAxis.size()];
    values = mAxis.values()
        .toArray(values);
    final int count = values.length;
    if (count > 0) {
      final PointF first = createPoint(values[0] * mRatio, -PI / 2, mCenterX, mCenterY);
      if (count == 1) {
        mPath.moveTo(mCenterX, mCenterY);
      } else {
        mPath.moveTo(first.x, first.y);
        for (int i = 1; i < count; i++) {
          final PointF point = createPoint(values[i] * mRatio, (2 * PI / count) * i - PI / 2, mCenterX, mCenterY);
          mPath.lineTo(point.x, point.y);
        }
      }
      mPath.lineTo(first.x, first.y);
    }
    mPath.close();
    canvas.drawPath(mPath, mPaint);
  }

  private void onAxisChanged() {
    if (mAutoSize && !mAxis.isEmpty()) {
      setAxisMaxInternal(Collections.max(mAxis.values()));
    } else {
      buildVertices();
      invalidate();
    }
  }

  private void setAxisMaxInternal(float axisMax) {
    mAxisMax = axisMax;
    calculateRatio();
    buildRings();
    invalidate();
  }

  private static class Ring {
    final float width;
    final float radius;
    final float fixedRadius;
    final int   color;
    PointF[] vertices;

    Ring(float radius, float width, int color) {
      this.radius = radius;
      this.width = width;
      this.color = color;
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
