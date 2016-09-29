package com.dzaitsev.widget;

import android.content.Context;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Path;
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
import static java.lang.Math.min;

/**
 * ~ ~ ~ ~ Description ~ ~ ~ ~
 *
 * @author Dmytro Zaitsev
 * @since 2016-Sep-28, 14:15
 */
public final class RadarChartView extends View {
  private static final int MIN_SIZE = 300;
  private final Map<String, Float> mSectors;
  private       int                mStartColor;
  private       int                mEndColor;
  private       int                mParts;
  private       Paint[]            mPaints;
  private       Path[]             mPaths;
  private       float              mAxisMax;
  private       float              mAxisTickInterval;
  private       int                mTextSize;
  private       int                mCenterX;
  private       int                mCenterY;
  private       int                mRingWidth;
  private       int                mOffset;

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
    mAxisTickInterval = array.getFloat(R.styleable.RadarChartView_axisTickInterval, 5);

    mParts = (int) (mAxisMax / mAxisTickInterval) + ((int) mAxisMax % (int) mAxisTickInterval);
    mPaints = new Paint[mParts];
    mPaths = new Path[mParts];

    if (mParts == 1) {
      mPaints[0] = createPaint(mStartColor);
      mPaths[0] = new Path();
    } else {
      final int size = mParts - 1;
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
    if (isInEditMode()) {
      calculateFields();
    }
    for (int i = 0; i < mParts; i++) {
      final Path path = mPaths[i];

      path.moveTo(mCenterX, mCenterY);
      final int radius = mRingWidth * i + mOffset;
      path.addCircle(mCenterX, mCenterY, radius, CW);
      path.close();

      final Paint paint = mPaints[i];
      paint.setStrokeWidth(mRingWidth);
      canvas.drawPath(path, paint);
    }
  }

  @Override protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
    final int size = min(resolveSize(MIN_SIZE, widthMeasureSpec), resolveSize(MIN_SIZE, heightMeasureSpec));
    setMeasuredDimension(size, size);
    calculateFields();
  }

  private void calculateFields() {
    mCenterX = (getRight() - getLeft()) / 2;
    mCenterY = (getBottom() - getTop()) / 2;
    mRingWidth = mCenterX / mParts;
    mOffset = mRingWidth / 2;
  }
}
