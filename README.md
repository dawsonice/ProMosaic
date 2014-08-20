## ProMosaic

===

Make mosaic for image on android.

## Features

===
Select Mode

* Follow finger
* Select rectangle

Effect Mode

* `Grid` color based on original image
* `Blur` Image
* `Pure` grey color

## Usage

===
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

===

![](http://ww2.sinaimg.cn/large/70489561gw1ejit88zg5vj20u01hcae9.jpg)
![](http://ww2.sinaimg.cn/large/70489561gw1ejit92a87pj20u01hc0xv.jpg)
![](http://ww4.sinaimg.cn/large/70489561gw1ejit9ho3a4j20u01hctdv.jpg)
![](http://ww4.sinaimg.cn/large/70489561gw1ejit9zrvu3j20u01hc0y7.jpg)

## Contact me
===
Any further question?

[email](mailto:coder.kiss@gmail.com) me please!