## ProMosaic

Make mosaic for image on android.

## Features

Select Mode

* Follow finger
* Select rectangle

Effect Mode

* `Grid` color based on original image
* `Blur` Image
* `Pure` grey color

## Usage

Use MosaicView as normal android view:

```
<me.dawson.promosaic.MosaicView
        android:id="@+id/iv_content"
        android:layout_width="match_parent"
        android:layout_height="match_parent"
        android:layout_below="@+id/ll_options"
        android:background="@color/grey_3"
        android:contentDescription="@string/app_name" />
```

Set properties in java code

```
mvImage.setSrcPath(filePath);
mvImage.clear();
mvImage.setEffect(Effect.BLUR);
mvImage.setMosaicColor(0xFF4D4D4D);
mvImage.setMode(Mode.PATH);
```

## Demo

![](http://ww2.sinaimg.cn/large/70489561gw1ejit88zg5vj20u01hcae9.jpg)
![](http://ww2.sinaimg.cn/large/70489561gw1ejit92a87pj20u01hc0xv.jpg)
![](http://ww4.sinaimg.cn/large/70489561gw1ejit9ho3a4j20u01hctdv.jpg)
![](http://ww4.sinaimg.cn/large/70489561gw1ejit9zrvu3j20u01hc0y7.jpg)

## Contact me

Any further question?

[Email](mailto:coder.kiss@gmail.com) me please!

License

        Copyright 2014 Dawson Dong
        
        Licensed under the Apache License, Version 2.0 (the "License");
        you may not use this file except in compliance with the License.
        You may obtain a copy of the License at
        
        http://www.apache.org/licenses/LICENSE-2.0
        
        Unless required by applicable law or agreed to in writing, software
        distributed under the License is distributed on an "AS IS" BASIS,
        WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
        See the License for the specific language governing permissions and
        limitations under the License.
