package com.dzaitsev.radarchartview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.SeekBar;
import android.widget.Spinner;
import com.dzaitsev.widget.RadarChartView;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static java.lang.Math.min;
import static java.util.Arrays.asList;

public class DemoActivity extends AppCompatActivity {

  private static final float    MAX_VALUE = 2855.681F;
  private static final String[] KEYS      = new String[] { "WI", "CA", "ID", "NY", "NM", "MN", "PA", "IA", "OH", "VT" };
  private static final float[]  VALUES    =
      new float[] { MAX_VALUE, 2312.895F, 871.640F, 751.280F, 661.293F, 661.293F, 426.985F, 267.249F, 196.676F, 127.346F };
  private int            mPosition;
  private RadarChartView mChart;
  private SeekBar        mAxisTick;
  private SeekBar        mValue;

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_demo);

    mChart = (RadarChartView) findViewById(R.id.radar_chart);
    mChart.setAxis(new HashMap<String, Float>() {{ // in 1,000 pounds (Sep 19, 2016)
      put(KEYS[0], VALUES[0]);
      put(KEYS[1], VALUES[1]);
      put(KEYS[2], VALUES[2]);
      put(KEYS[3], VALUES[3]);
      put(KEYS[4], VALUES[4]);
      put(KEYS[5], VALUES[5]);
      put(KEYS[6], VALUES[6]);
      put(KEYS[7], VALUES[7]);
      put(KEYS[8], VALUES[8]);
      put(KEYS[9], VALUES[9]);
    }});

    mAxisTick = (SeekBar) findViewById(R.id.axisTick);
    mAxisTick.setOnSeekBarChangeListener(new OnProgressChangedListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress > 0) {
          mChart.setAxisTick(progress * 10);
        }
      }
    });

    mValue = (SeekBar) findViewById(R.id.value);
    mValue.setOnSeekBarChangeListener(new OnProgressChangedListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mChart.addOrReplace(KEYS[min(mPosition, mValue.getMax())], progress * 2);
      }
    });

    updateSeekBars();

    final Spinner spinner = (Spinner) findViewById(R.id.spinner);
    final ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, new ArrayList<>(asList(KEYS)));
    adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
    spinner.setAdapter(adapter);
    spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
      @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        mPosition = position;
      }

      @Override public void onNothingSelected(AdapterView<?> parent) {
      }
    });

    final SeekBar axis = (SeekBar) findViewById(R.id.axis);
    axis.setMax(VALUES.length);
    axis.setOnSeekBarChangeListener(new OnProgressChangedListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        mChart.clearAxis();
        adapter.clear();
        Map<String, Float> map = new HashMap<>(progress);
        for (int i = 0; i < progress; i++) {
          final String key = KEYS[i];
          map.put(key, VALUES[i]);
          adapter.add(key);
        }
        mChart.setAxis(map);
        adapter.notifyDataSetChanged();
        updateSeekBars();
      }
    });

    ((CheckBox) findViewById(R.id.circlesOnly)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mChart.setCirclesOnly(isChecked);
      }
    });
    ((CheckBox) findViewById(R.id.autoSize)).setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
      @Override public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        mChart.setAutoSize(isChecked);
      }
    });
  }

  private void updateSeekBars() {
    final float max = mChart.getAxisMax();
    mAxisTick.setMax((int) (max / 10));
    mValue.setMax((int) max);
  }

  private static abstract class OnProgressChangedListener implements SeekBar.OnSeekBarChangeListener {
    @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
    }

    @Override public final void onStartTrackingTouch(SeekBar seekBar) {
    }

    @Override public final void onStopTrackingTouch(SeekBar seekBar) {
    }
  }
}
