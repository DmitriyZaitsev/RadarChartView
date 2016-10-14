package com.dzaitsev.android.widget;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.View;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;

import static android.graphics.Color.BLACK;
import static android.graphics.Color.parseColor;
import static android.graphics.Paint.Style.STROKE;
import static com.dzaitsev.android.widget.Utils.createPaint;
import static com.dzaitsev.android.widget.Utils.createPoint;
import static com.dzaitsev.android.widget.Utils.createPoints;
import static com.dzaitsev.android.widget.Utils.dp;
import static com.dzaitsev.android.widget.Utils.gradient;
import static com.dzaitsev.android.widget.Utils.mutatePaint;
import static com.dzaitsev.android.widget.Utils.sp;
import static java.lang.StrictMath.PI;
import static java.lang.StrictMath.ceil;
import static java.lang.StrictMath.cos;
import static java.lang.StrictMath.max;
import static java.lang.StrictMath.min;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-28, 14:15
 */
@SuppressWarnings("ClassWithTooManyFields")
public class RadarChartView extends View {
  private int         startColor;
  private int         endColor;
  private int         axisColor;
  private float       axisMax;
  private float       axisTick;
  private int         axisWidth;
  private int         chartColor;
  private int         chartWidth;
  private Paint.Style chartStyle;
  private boolean     circlesOnly;
  private boolean     autoSize;
  private boolean     smoothGradient;

  private final LinkedHashMap<String, Float> axis;
  private final Rect                         rect;
  private final Path                         path;
  private final TextPaint                    textPaint;
  private final Paint                        paint;
  private       int                          centerX;
  private       int                          centerY;
  private       Ring[]                       rings;
  private       float[]                      vertices;
  private       float                        ratio;
  private       float                        axisMaxInternal;
  private       float                        axisTickInternal;

  public RadarChartView(Context context) {
    this(context, null);
  }

  public RadarChartView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RadarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    axis = new LinkedHashMap<>();
    rect = new Rect();
    path = new Path();
    textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
    paint = createPaint(BLACK);

    final TypedArray colors = context.obtainStyledAttributes(attrs, new int[] {
        R.attr.colorAccent, R.attr.colorPrimary, R.attr.colorPrimaryDark
    }, defStyleAttr, 0);
    final int colorAccent = colors.getColor(0, parseColor("#22737b"));
    final int colorPrimary = colors.getColor(1, parseColor("#c3e3e5"));
    final int colorPrimaryDark = colors.getColor(2, parseColor("#5f9ca1"));
    colors.recycle();

    final TypedArray values = context.obtainStyledAttributes(attrs, R.styleable.RadarChartView, defStyleAttr, 0);
    startColor = values.getColor(R.styleable.RadarChartView_startColor, colorPrimaryDark);
    endColor = values.getColor(R.styleable.RadarChartView_endColor, colorPrimary);
    axisColor = values.getColor(R.styleable.RadarChartView_axisColor, BLACK);
    axisMax = values.getFloat(R.styleable.RadarChartView_axisMax, 20);
    axisTick = values.getFloat(R.styleable.RadarChartView_axisTick, axisMax / 5);
    final DisplayMetrics metrics = getResources().getDisplayMetrics();
    final int textSize = values.getDimensionPixelSize(R.styleable.RadarChartView_textSize, sp(15, metrics));
    circlesOnly = values.getBoolean(R.styleable.RadarChartView_circlesOnly, false);
    autoSize = values.getBoolean(R.styleable.RadarChartView_autoSize, true);
    axisWidth = values.getDimensionPixelSize(R.styleable.RadarChartView_axisWidth, dp(1, metrics));
    chartColor = values.getColor(R.styleable.RadarChartView_chartColor, colorAccent);
    chartWidth = values.getDimensionPixelSize(R.styleable.RadarChartView_chartWidth, dp(3, metrics));
    chartStyle = Paint.Style.values()[values.getInt(R.styleable.RadarChartView_chartStyle, STROKE.ordinal())];
    smoothGradient = values.getBoolean(R.styleable.RadarChartView_smoothGradient, false);
    values.recycle();

    textPaint.setTextSize(textSize);
    textPaint.density = metrics.density;
  }

  public final void addOrReplace(String axisName, float value) {
    axis.put(axisName, value);
    onAxisChanged();
  }

  public final void clearAxis() {
    axis.clear();
    onAxisChanged();
  }

  public final Map<String, Float> getAxis() {
    return Collections.unmodifiableMap(axis);
  }

  public final void setAxis(Map<String, Float> axis) {
    this.axis.clear();
    this.axis.putAll(axis);
    onAxisChanged();
  }

  public final int getAxisColor() {
    return axisColor;
  }

  public final void setAxisColor(int axisColor) {
    this.axisColor = axisColor;
    invalidate();
  }

  public final float getAxisMax() {
    return axisMax;
  }

  public final void setAxisMax(float axisMax) {
    setAutoSize(false);
    setAxisMaxInternal(axisMax);
  }

  public final float getAxisTick() {
    return axisTick;
  }

  public final void setAxisTick(float axisTick) {
    this.axisTick = axisTick;
    calcAxisTickInternal();

    buildRings();
    invalidate();
  }

  public final float getAxisWidth() {
    return axisWidth;
  }

  public final void setAxisWidth(int axisWidth) {
    this.axisWidth = axisWidth;
    invalidate();
  }

  public final int getChartColor() {
    return chartColor;
  }

  public final void setChartColor(int chartColor) {
    this.chartColor = chartColor;
    invalidate();
  }

  public final Paint.Style getChartStyle() {
    return chartStyle;
  }

  public final void setChartStyle(Paint.Style chartStyle) {
    this.chartStyle = chartStyle;
    invalidate();
  }

  public final float getChartWidth() {
    return chartWidth;
  }

  public final void setChartWidth(int chartWidth) {
    this.chartWidth = chartWidth;
    invalidate();
  }

  public final int getEndColor() {
    return endColor;
  }

  public final void setEndColor(int endColor) {
    this.endColor = endColor;
    invalidate();
    invalidate();
  }

  public final int getStartColor() {
    return startColor;
  }

  public final void setStartColor(int startColor) {
    this.startColor = startColor;
    invalidate();
  }

  public final boolean isAutoSize() {
    return autoSize;
  }

  public final void setAutoSize(boolean autoSize) {
    this.autoSize = autoSize;

    if (autoSize && !axis.isEmpty()) {
      setAxisMaxInternal(Collections.max(axis.values()));
    }
  }

  public final boolean isCirclesOnly() {
    return circlesOnly;
  }

  public final void setCirclesOnly(boolean circlesOnly) {
    this.circlesOnly = circlesOnly;
    invalidate();
  }

  public final boolean isSmoothGradient() {
    return smoothGradient;
  }

  public final void setSmoothGradient(boolean smoothGradient) {
    this.smoothGradient = smoothGradient;
    invalidate();
  }

  public final void remove(String axisName) {
    axis.remove(axisName);
    onAxisChanged();
  }

  public final void setTextSize(float textSize) {
    textPaint.setTextSize(textSize);
    invalidate();
  }

  @Override protected void onSizeChanged(int w, int h, int oldw, int oldh) {
    calculateCenter();
    axisMaxInternal =
        max(0, min(getMeasuredWidth() - getPaddingRight() - getPaddingLeft(), getMeasuredHeight() - getPaddingBottom() - getPaddingTop()))
            * 0.5F;
    calcRatio();
    calcAxisTickInternal();
    buildRings();
  }

  @Override protected void onDraw(Canvas canvas) {
    final int count = axis.size();
    if (count < 3 || circlesOnly) {
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
  }

  private void buildRings() {
    @SuppressWarnings("NumericCastThatLosesPrecision") //
    final int ringsCount = (int) max(ceil(axisMaxInternal / axisTickInternal), 1);
    if (ringsCount == 0) {
      return;
    }

    rings = new Ring[ringsCount];
    if (ringsCount == 1) {
      rings[0] = new Ring(axisMaxInternal, axisMaxInternal, startColor);
    } else {
      for (int i = 0; i < ringsCount; i++) {
        rings[i] = new Ring(axisTickInternal * (i + 1), axisTickInternal, gradient(startColor, endColor, i, ringsCount));
      }
      rings[ringsCount - 1] = new Ring(axisMaxInternal, axisMaxInternal - rings[ringsCount - 2].radius, endColor);
    }

    buildVertices();
  }

  private void buildVertices() {
    final int count = axis.size();
    for (Ring ring : rings) {
      ring.vertices = createPoints(count, ring.fixedRadius, centerX, centerY);
    }
    vertices = createPoints(count, axisMaxInternal, centerX, centerY);
  }

  private void calcAxisTickInternal() {
    axisTickInternal = axisTick * ratio;
  }

  private void calcRatio() {
    ratio = axisMaxInternal > 0 ? axisMaxInternal / axisMax : 1;
  }

  private void calculateCenter() {
    centerX = (getMeasuredWidth() >> 1) + getPaddingLeft() - getPaddingRight();
    centerY = (getMeasuredHeight() >> 1) + getPaddingTop() - getPaddingBottom();
  }

  private void drawAxis(Canvas canvas) {
    final Iterator<String> axisNames = axis.keySet()
        .iterator();
    mutatePaint(paint, axisColor, axisWidth, STROKE);
    final int length = vertices.length;
    for (int i = 0; i < length; i += 2) {
      path.reset();
      path.moveTo(centerX, centerY);
      final float pointX = vertices[i];
      final float pointY = vertices[i + 1];
      path.lineTo(pointX, pointY);
      path.close();
      canvas.drawPath(path, paint);

      final String axisName = axisNames.next();
      textPaint.getTextBounds(axisName, 0, axisName.length(), rect);
      final float x = pointX > centerX ? pointX : pointX - rect.width();
      final float y = pointY > centerY ? pointY + rect.height() : pointY;
      canvas.drawText(axisName, x, y, textPaint);
    }
  }

  private void drawCircles(Canvas canvas) {
    for (final Ring ring : rings) {
      mutatePaint(paint, ring.color, ring.width + 2, STROKE);
      canvas.drawCircle(centerX, centerY, ring.fixedRadius, paint);
    }
  }

  private void drawPolygons(Canvas canvas, int count) {
    for (final Ring ring : rings) {
      final float[] points = ring.vertices;
      final float startX = points[0];
      final float startY = points[1];

      path.reset();
      path.moveTo(startX, startY);
      path.setLastPoint(startX, startY);
      for (int j = 2; j < count + count; j += 2) {
        path.lineTo(points[j], points[j + 1]);
      }
      path.close();

      //noinspection NumericCastThatLosesPrecision
      mutatePaint(paint, ring.color, (float) (ring.width * cos(PI / count)) + 2, STROKE);
      canvas.drawPath(path, paint);
    }
  }

  private void drawValues(Canvas canvas, int count) {
    if (count == 0) {
      return;
    }

    Float[] values = new Float[count];
    values = axis.values()
        .toArray(values);
    final float[] first = createPoint(values[0] * ratio, -PI / 2, centerX, centerY);
    final float firstX = first[0];
    final float firstY = first[1];
    path.reset();
    path.setLastPoint(firstX, firstY);

    if (count == 1) {
      path.moveTo(centerX, centerY);
      path.lineTo(firstX, firstY);
    } else {
      path.moveTo(firstX, firstY);
      for (int i = 1; i < count; i++) {
        final float[] point = createPoint(values[i] * ratio, (2 * PI / count) * i - PI / 2, centerX, centerY);
        path.lineTo(point[0], point[1]);
      }
    }
    path.close();

    mutatePaint(paint, chartColor, chartWidth, chartStyle);
    canvas.drawPath(path, paint);
  }

  private void onAxisChanged() {
    if (autoSize && !axis.isEmpty()) {
      setAxisMaxInternal(Collections.max(axis.values()));
    } else {
      buildVertices();
      invalidate();
    }
  }

  private void setAxisMaxInternal(float axisMax) {
    this.axisMax = axisMax;
    calcRatio();
    calcAxisTickInternal();
    buildRings();
    invalidate();
  }

  private static class Ring {
    final float width;
    final float radius;
    final float fixedRadius;
    final int   color;
    float[] vertices;

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
