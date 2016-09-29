package com.dzaitsev.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.support.annotation.NonNull;
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
import static android.graphics.Paint.Style.STROKE;
import static android.graphics.Path.Direction.CW;
import static java.lang.Math.PI;
import static java.lang.Math.cos;
import static java.lang.Math.min;
import static java.lang.Math.sin;

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
  private       float[]            mRadiuses;
  private       Paint[]            mPaints;
  private       Paint              mAxesPaint;
  private       Path[]             mPaths;
  private       float              mAxisMax;
  private       float              mAxisTick;
  private       int                mTextSize;
  private       int                mCenterX;
  private       int                mCenterY;
  private       int                mParts;

  public RadarChartView(Context context) {
    this(context, null);
  }

  public RadarChartView(Context context, AttributeSet attrs) {
    this(context, attrs, 0);
  }

  public RadarChartView(Context context, AttributeSet attrs, int defStyleAttr) {
    super(context, attrs, defStyleAttr);
    mSectors = new LinkedHashMap<>();

    // TODO: read attrs
    final TypedArray array = context.obtainStyledAttributes(attrs, R.styleable.RadarChartView, defStyleAttr, 0);
    mStartColor = array.getColor(R.styleable.RadarChartView_startColor, parseColor("#22737b"));
    mEndColor = array.getColor(R.styleable.RadarChartView_endColor, parseColor("#c3e3e5"));
    mAxisMax = array.getFloat(R.styleable.RadarChartView_axisMax, 20);
    mAxisTick = array.getFloat(R.styleable.RadarChartView_axisTick, 5);

    mParts = ((int) mAxisMax / (int) mAxisTick) + ((int) mAxisMax % (int) mAxisTick > 0 ? 1 : 0);
    mPaints = new Paint[mParts];
    mAxesPaint = createPaint((mStartColor + mEndColor) / 2);
    mPaths = new Path[mParts];

    mRadiuses = new float[mParts];
    for (int i = 0; i < mParts - 1; i++) {
      mRadiuses[i] = mAxisTick * (i + 1);
    }
    final int size = mParts - 1;
    mRadiuses[size] = mAxisMax;

    if (mParts == 1) {
      mPaints[0] = createPaint(mStartColor);
      mPaths[0] = new Path();
    } else {
      for (int i = 0; i < mParts; i++) {
        final int alpha = color(alpha(mStartColor), alpha(mEndColor), size, i);
        final int red = color(red(mStartColor), red(mEndColor), size, i);
        final int green = color(green(mStartColor), green(mEndColor), size, i);
        final int blue = color(blue(mStartColor), blue(mEndColor), size, i);
        final Paint paint = createPaint(argb(alpha, red, green, blue));

        mPaints[i] = paint;
        mPaths[i] = new Path();
      }
    }
    mTextSize = array.getDimensionPixelSize(R.styleable.RadarChartView_textSize, 12);

    array.recycle();
  }

  @NonNull private static Paint createPaint(int color) {
    final Paint paint = new Paint();
    paint.setAntiAlias(true);
    paint.setStyle(STROKE);
    paint.setColor(color);
    return paint;
  }

  private static int color(int start, int end, int size, int factor) {
    return start + (end - start) / size * factor;
  }

  private static int px(float dp, Resources resources) {
    return (int) (dp * resources.getDisplayMetrics().density + 0.5F);
  }

  public void addOrReplace(String sector, float value) {
    mSectors.put(sector, value);
    invalidate();
  }

  public void remove(String sector) {
    mSectors.remove(sector);
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
    //drawAxes(canvas);
  }

  private void drawAxes(Canvas canvas) {
    final Path path = new Path();
    for (int i = 0; i < mParts; i++) {
      path.moveTo(mCenterX, mCenterY);
      // ..
      path.close();
      canvas.drawPath(path, new Paint());
    }
  }

  private void drawCircles(Canvas canvas) {
    for (int i = 0; i < mParts; i++) {
      final float ringWidth = i == mParts - 1 ? mRadiuses[i] - mRadiuses[i - 1] : mAxisTick;
      final float offset = ringWidth / 2;
      final float radius = mRadiuses[i] - offset;
      final Path path = mPaths[i];

      path.moveTo(mCenterX, mCenterY);
      path.addCircle(mCenterX, mCenterY, radius, CW);
      path.close();

      final Paint paint = mPaints[i];
      paint.setStrokeWidth(ringWidth);
      canvas.drawPath(path, paint);
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int size = min(resolveSize(MIN_SIZE, widthMeasureSpec), resolveSize(MIN_SIZE, heightMeasureSpec));
    setMeasuredDimension(size, size);
  }

  private void drawPolygons(Canvas canvas, int size) {
    final double angle = 2 * PI / size;
    final Point[] points = new Point[size];

    for (int i = 0; i < mParts; i++) {
      final float ringWidth = i == mParts - 1 ? mRadiuses[i] - mRadiuses[i - 1] : mAxisTick;
      final float offset = ringWidth / 2;
      final float radius = mRadiuses[i] - offset;
      final Path path = mPaths[i];

      for (int j = 0; j < size; j++) {
        final double alpha = angle * j - PI / 2;
        points[j] = new Point((int) (radius * cos(alpha)) + mCenterX, (int) (radius * sin(alpha)) + mCenterY);
      }

      final Point start = points[0];
      path.moveTo(start.x, start.y);
      for (int j = 1; j < size; j++) {
        final Point to = points[j];
        path.lineTo(to.x, to.y);
      }
      path.lineTo(start.x, start.y);
      path.close();

      final Paint paint = mPaints[i];
      paint.setStrokeWidth(ringWidth);
      canvas.drawPath(path, paint);
    }
  }
}
