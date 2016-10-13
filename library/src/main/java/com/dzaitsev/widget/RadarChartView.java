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
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Path.Direction.CW;
import static com.dzaitsev.widget.Utils.createPaint;
import static com.dzaitsev.widget.Utils.createPointF;
import static com.dzaitsev.widget.Utils.createPointFs;
import static com.dzaitsev.widget.Utils.gradient;
import static com.dzaitsev.widget.Utils.mutatePaint;
import static java.lang.Math.PI;
import static java.lang.Math.ceil;
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
  private final Map<String, Float>           mReadOnly  = Collections.unmodifiableMap(mAxis);
  private final Rect                         mRect      = new Rect();
  private final Path                         mPath      = new Path();
  private final TextPaint                    mTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
  private final Paint                        mPaint     = createPaint(BLACK);

  private int      mStartColor;
  private int      mEndColor;
  private int      mAxisColor;
  private int      mGraphColor;
  private float    mAxisMax;
  private float    mAxisTick;
  private float    mAxisWidth;
  private float    mGraphWidth;
  private int      mGraphStyle;
  private int      mCenterX;
  private int      mCenterY;
  private Ring[]   mRings;
  private boolean  mCirclesOnly;
  private boolean  mAutoSize;
  private boolean  mSmoothGradient;
  private PointF[] mVertices;

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
    mSmoothGradient = values.getBoolean(R.styleable.RadarChartView_smoothGradient, false);
    values.recycle();

    mTextPaint.setTextSize(textSize);
    mTextPaint.density = getResources().getDisplayMetrics().density;
  }

  public void addOrReplace(String axis, float value) {
    mAxis.put(axis, value);
    onAxisChanged();
  }

  public void clearAxis() {
    mAxis.clear();
    onAxisChanged();
  }

  public Map<String, Float> getAxis() {
    return mReadOnly;
  }

  public void setAxis(Map<String, Float> axis) {
    mAxis.clear();
    mAxis.putAll(axis);
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

    if (mAutoSize && !mAxis.isEmpty()) {
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

  public boolean isSmoothGradient() {
    return mSmoothGradient;
  }

  public void setSmoothGradient(boolean smoothGradient) {
    mSmoothGradient = smoothGradient;
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
      calculateCenter();
      buildVertices();
    }

    final int count = mAxis.size();
    if (count < 3 || mCirclesOnly) {
      drawCircles(canvas);
    } else {
      drawPolygons(canvas, count);
    }
    drawValues(canvas, count);
    drawAxis(canvas);
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int width = MeasureSpec.getSize(widthMeasureSpec);
    final int height = MeasureSpec.getSize(heightMeasureSpec);
    if (width != height) {
      final int size = MeasureSpec.makeMeasureSpec(min(width, height), MeasureSpec.EXACTLY);
      super.onMeasure(size, size);
    }
    calculateCenter();
    buildRings();
  }

  private float axisMax() {
    return min(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), getMeasuredHeight() - getPaddingBottom() - getPaddingTop())
        * .5F;
  }

  private float axisTick() {
    return mAxisTick * ratio();
  }

  private void buildRings() {
    final float axisTick = axisTick();
    final float axisMax = axisMax();
    final int ringsCount = (int) max(ceil(axisMax / axisTick), 1);

    mRings = new Ring[ringsCount];
    if (ringsCount == 1) {
      mRings[0] = new Ring(axisMax, axisMax, mStartColor);
    } else {
      for (int i = 0; i < ringsCount; i++) {
        mRings[i] = new Ring(axisTick * (i + 1), axisTick, gradient(mStartColor, mEndColor, i, ringsCount));
      }
      mRings[ringsCount - 1] = new Ring(axisMax, axisMax - mRings[ringsCount - 2].radius, mEndColor);
    }

    buildVertices();
  }

  private void buildVertices() {
    final int count = mAxis.size();
    for (Ring ring : mRings) {
      ring.vertices = createPointFs(count, ring.fixedRadius, mCenterX, mCenterY);
    }
    mVertices = createPointFs(count, axisMax(), mCenterX, mCenterY);
  }

  private void calculateCenter() {
    mCenterX = getMeasuredWidth() / 2 + getPaddingLeft() - getPaddingRight();
    mCenterY = getMeasuredHeight() / 2 + getPaddingTop() - getPaddingBottom();
  }

  private void drawAxis(Canvas canvas) {
    final Iterator<String> axis = (mAxis.keySet()).iterator();
    mutatePaint(mPaint, mAxisColor, mAxisWidth, STROKE);
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

      mutatePaint(mPaint, ring.color, ring.width + 2, STROKE);
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

      mutatePaint(mPaint, ring.color, (float) (ring.width * cos(PI / count)) + 2, STROKE);
      canvas.drawPath(mPath, mPaint);
    }
  }

  private void drawValues(Canvas canvas, int count) {
    if (count == 0) {
      return;
    }

    mPath.reset();

    Float[] values = new Float[count];
    values = (mAxis.values()).toArray(values);
    if (count > 0) {
      final float ratio = ratio();
      final PointF first = createPointF(values[0] * ratio, -PI / 2, mCenterX, mCenterY);
      if (count == 1) {
        mPath.moveTo(mCenterX, mCenterY);
      } else {
        mPath.moveTo(first.x, first.y);
        for (int i = 1; i < count; i++) {
          final PointF point = createPointF(values[i] * ratio, (2 * PI / count) * i - PI / 2, mCenterX, mCenterY);
          mPath.lineTo(point.x, point.y);
        }
      }
      mPath.lineTo(first.x, first.y);
    }
    mPath.close();

    mutatePaint(mPaint, mGraphColor, mGraphWidth, Paint.Style.values()[mGraphStyle]);
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

  private float ratio() {
    final float axisMax = axisMax();
    return axisMax > 0 ? axisMax / mAxisMax : 0;
  }

  private void setAxisMaxInternal(float axisMax) {
    mAxisMax = axisMax;
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
