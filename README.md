# Android view (widget) for rendering radial diagrams


## Demo
<img src="/images/demo_01.gif" width="33.3%"><img src="/images/demo_02.gif" width="33.3%"><img src="/images/demo_03.gif" width="33.3%">
## How to start

From code
```java
// Prepare the data. We're going to show the top ten cheese producing U.S. states in 2013 (in 1,000 pounds)
final Map<String, Float> axis = new LinkedHashMap<>(10);
axis.put("CA", 2312.895F);
axis.put("ID", 871.640F);
axis.put("NY", 751.280F);
axis.put("NM", 661.293F);
axis.put("MN", 661.293F);
axis.put("PA", 426.985F);
axis.put("IA", 267.249F);
axis.put("OH", 196.676F);
axis.put("VT", 127.346F);

// Set your data to the view
final RadarChartView chartView = (RadarChartView) findViewById(R.id.chartView);
chartView.setAxis(axis);

chartView.setAxisMax(2855.681F);         // set max value for the chart
chartView.addOrReplace("WI", 2855.681F); // add new axis
chartView.addOrReplace("OH", 281.59F);   // change the existing value
chartView.setAutoSize(true);             // auto balance the chart
chartView.setCirclesOnly(true);          // if you want circles instead of polygons
chartView.setChartStyle(FILL);           // chart drawn with this style will be filled not stroked
// ...
// and many other attributes..
```

From XML
```xml
<com.dzaitsev.android.widget.RadarChartView
  android:id="@+id/radar_chart"
  android:layout_width="match_parent"
  android:layout_height="wrap_content"
  android:background="#fff"
  android:padding="30dp"
  app:axisColor="#22737b"
  app:axisMax="280"
  app:axisTick="75"
  app:axisWidth="1dp"
  app:chartColor="#C8FF4081"
  app:chartStyle="stroke"
  app:chartWidth="3dp"
  app:circlesOnly="false"
  app:endColor="#c3e3e5"
  app:startColor="#5f9ca1"
  app:textSize="12sp" />
```

## Download

Grab via Gradle:
```groovy
// add this repository to your project
allprojects {
  repositories {
    // ...
    maven { url 'https://dl.bintray.com/dmitriyzaitsev/maven' }
    // ...
  }
}
```

```groovy
compile 'com.dzaitsev.android.widget:radarchartview:0.1.0'
```

Maven:

```xml
<dependency>
    <groupId>com.dzaitsev.android.widget</groupId>
    <artifactId>radarchartview</artifactId>
    <version>0.1.0</version>
</dependency>
```

## License

```
Copyright 2016 Dmytro Zaitsev

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
```