package com.dzaitsev.radarchartview;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.CheckBox;
import android.widget.SeekBar;
import com.dzaitsev.widget.RadarChartView;
import java.util.Random;

public class MainActivity extends AppCompatActivity {

  @Override protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    final RadarChartView chart = (RadarChartView) findViewById(R.id.radar_chart);

    ((SeekBar) findViewById(R.id.parts)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        if (progress > 0) {
          chart.setAxisTick(progress * 10);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    ((SeekBar) findViewById(R.id.sectors)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
      @Override public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        chart.clearAxis();
        final Random random = new Random(System.nanoTime());
        for (int i = 0; i < progress; i++) {
          final int value = Math.min((random.nextInt(270) + 10), 280);
          chart.addOrReplace("Item " + i, value);
        }
      }

      @Override public void onStartTrackingTouch(SeekBar seekBar) {
      }

      @Override public void onStopTrackingTouch(SeekBar seekBar) {
      }
    });

    ((CheckBox) findViewById(R.id.circlesOnly)).setOnCheckedChangeListener((buttonView, isChecked) -> chart.setCirclesOnly(isChecked));
    ((CheckBox) findViewById(R.id.autoSize)).setOnCheckedChangeListener((buttonView, isChecked) -> chart.setAutoSize(isChecked));
  }
}
