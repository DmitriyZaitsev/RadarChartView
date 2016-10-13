package com.dzaitsev.radarchartview;

import android.graphics.Paint;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import com.dzaitsev.android.widget.RadarChartView;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;

import static java.lang.Math.min;
import static java.util.Arrays.asList;

public class DemoActivity extends AppCompatActivity {

  static final         String[] KEYS      = { "WI", "CA", "ID", "NY", "NM", "MN", "PA", "IA", "OH", "VT" };
  private static final float    MAX_VALUE = 2855.681F;
  static final         float[]  VALUES    = {
      MAX_VALUE, 2312.895F, 871.640F, 751.280F, 661.293F, 661.293F, 426.985F, 267.249F, 196.676F, 127.346F
  };
  int            position;
  RadarChartView chartView;
  private final SeekBar.OnSeekBarChangeListener axisTickBarListener = new OnProgressChangedListener() {
    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      if (progress > 0) {
        chartView.setAxisTick(progress * 10);
      }
    }
  };
  SeekBar        valueBar;
  ArrayAdapter<String> adapter;
  private final SeekBar.OnSeekBarChangeListener valueBarListener = new OnProgressChangedListener() {
    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
      chartView.addOrReplace(KEYS[min(VALUES.length - 1 - position, valueBar.getMax())], progress << 1);
    }
  };
  private SeekBar axisTickBar;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);

    chartView = (RadarChartView) findViewById(R.id.radar_chart);
    final Map<String, Float> axis = new LinkedHashMap<>(VALUES.length); // in 1,000 pounds (Sep 19, 2016)
    axis.put(KEYS[9], VALUES[9]);
    axis.put(KEYS[8], VALUES[8]);
    axis.put(KEYS[7], VALUES[7]);
    axis.put(KEYS[6], VALUES[6]);
    axis.put(KEYS[5], VALUES[5]);
    axis.put(KEYS[4], VALUES[4]);
    axis.put(KEYS[3], VALUES[3]);
    axis.put(KEYS[2], VALUES[2]);
    axis.put(KEYS[1], VALUES[1]);
    axis.put(KEYS[0], VALUES[0]);
    chartView.setAxis(axis);

    axisTickBar = (SeekBar) findViewById(R.id.axisTick);
    axisTickBar.setOnSeekBarChangeListener(axisTickBarListener);

    valueBar = (SeekBar) findViewById(R.id.value);
    valueBar.setOnSeekBarChangeListener(valueBarListener);

    final Spinner spinner = (Spinner) findViewById(R.id.spinner);
    adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(asList(KEYS)));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        DemoActivity.this.position = position;
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    final SeekBar axisBar = (SeekBar) findViewById(R.id.axis);
    axisBar.setMax(VALUES.length);
    axisBar.setOnSeekBarChangeListener(new OnProgressChangedListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        chartView.clearAxis();
        adapter.clear();
        final int capacity = VALUES.length - progress;
        final Map<String, Float> map = new LinkedHashMap<>(capacity);
        for (int i = VALUES.length - 1; i >= capacity; i--) {
          final String key = KEYS[i];
          map.put(key, VALUES[i]);
          adapter.add(key);
        }
        chartView.setAxis(map);
        adapter.notifyDataSetChanged();
        updateSeekBars();
      }
    });

    ((CompoundButton) findViewById(R.id.circlesOnly)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        chartView.setCirclesOnly(isChecked);
      }
    });
    ((CompoundButton) findViewById(R.id.autoSize)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        chartView.setAutoSize(isChecked);
      }
    });
    ((CompoundButton) findViewById(R.id.fillStroke)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        chartView.setChartStyle(isChecked ? Paint.Style.FILL : Paint.Style.STROKE);
      }
    });

    updateSeekBars();
  }

  @SuppressWarnings("NumericCastThatLosesPrecision") //
  final void updateSeekBars() {
    final float max = chartView.getAxisMax();
    axisTickBar.setOnSeekBarChangeListener(null);
    axisTickBar.setMax((int) (max / 10));
    axisTickBar.setOnSeekBarChangeListener(axisTickBarListener);

    valueBar.setOnSeekBarChangeListener(null);
    valueBar.setMax((int) max);
    valueBar.setOnSeekBarChangeListener(valueBarListener);
  }

  private static class OnProgressChangedListener implements SeekBar.OnSeekBarChangeListener {
    OnProgressChangedListener() {
    }

    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override public final void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override public final void onStopTrackingTouch(SeekBar seekBar) {
    }
  }
}
