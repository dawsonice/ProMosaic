package me.dawson.promosaic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class MosaicImage extends ViewGroup {
	public static final String TAG = "MosaicImage";

	private static final int GRID_WIDTH = 5;

	private static final int STROKE_COLOR = 0xff2a5caa;

	private static final int STROKE_WIDTH = 6;

	private int mImageWidth;
	private int mImageHeight;

	private Bitmap bmOrigin;
	private Bitmap bmMosaic;

	private Point startPoint;
	private Rect mEventRect;
	private Paint mPaint;

	private int mGridWidth;
	private int mStrokeWidth;
	private int mStrokeColor;

	private String inPath;
	private String outPath;

	private Rect mImageRect;

	public MosaicImage(Context context) {
		super(context);
		initImage();
	}

	public MosaicImage(Context context, AttributeSet attrs) {
		super(context, attrs);
		initImage();
	}

	private void initImage() {
		mStrokeWidth = STROKE_WIDTH;
		mStrokeColor = STROKE_COLOR;
		mGridWidth = dp2px(GRID_WIDTH);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setColor(mStrokeColor);

		startPoint = new Point();
		mImageRect = new Rect();

		setWillNotDraw(false);
	}

	public void setSrcPath(String absPath) {
		File file = new File(absPath);
		if (file == null || !file.exists()) {
			Log.w(TAG, "invalid file path " + absPath);
			return;
		}

		inPath = absPath;
		String fileName = file.getName();
		String parent = file.getParent();
		int index = fileName.lastIndexOf(".");
		String stem = fileName.substring(0, index);
		String newStem = stem + "_mosaic";
		fileName = fileName.replace(stem, newStem);
		outPath = parent + "/" + fileName;

		getImageSize(inPath);

		if (bmMosaic != null) {
			bmMosaic.recycle();
			bmMosaic = null;
		}

		if (bmOrigin != null) {
			bmOrigin.recycle();
			bmOrigin = null;
		}

		bmOrigin = getImage(absPath);
		requestLayout();
		invalidate();
	}

	public boolean isSaved() {
		return (bmMosaic == null);
	}

	public void setOutPath(String absPath) {
		this.outPath = absPath;
	}

	public void setGridWidth(int width) {
		this.mGridWidth = dp2px(width);
	}

	public int getGridWidth() {
		return this.mGridWidth;
	}

	public void setStrokeColor(int color) {
		this.mStrokeColor = color;
		mPaint.setColor(mStrokeColor);
	}

	public int getStrokeColor() {
		return this.mStrokeColor;
	}

	public void setStrokeWidth(int width) {
		this.mStrokeWidth = width;
		mPaint.setStrokeWidth(mStrokeWidth);
	}

	public int getStrokeWidth() {
		return this.mStrokeWidth;
	}

	public void clear() {
		if (bmMosaic == null) {
			return;
		}

		bmMosaic.recycle();
		bmMosaic = null;
		invalidate();
	}

	public boolean save() {
		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bmOrigin, 0, 0, null);
		canvas.drawBitmap(bmMosaic, 0, 0, null);
		canvas.save();

		try {
			FileOutputStream fos = new FileOutputStream(outPath);
			bitmap.compress(Bitmap.CompressFormat.JPEG, 100, fos);
			fos.flush();
			fos.close();
		} catch (IOException e) {
			e.printStackTrace();
			Log.e(TAG, "failed to write image content");
			return false;
		}
		return true;
	}

	public boolean dispatchTouchEvent(MotionEvent event) {
		super.dispatchTouchEvent(event);

		int action = event.getAction();
		int x = (int) event.getX();
		int y = (int) event.getY();
		Log.d(TAG, "action " + action + " x " + x + " y " + y);
		if (action == MotionEvent.ACTION_DOWN) {
			startPoint.set(x, y);
			mEventRect = new Rect();
		} else if (action == MotionEvent.ACTION_MOVE) {
			int left = startPoint.x < x ? startPoint.x : x;
			int top = startPoint.y < y ? startPoint.y : y;
			int right = x > startPoint.x ? x : startPoint.x;
			int bottom = y > startPoint.y ? y : startPoint.y;

			if (left < mImageRect.left) {
				left = mImageRect.left;
			}
			if (top < mImageRect.top) {
				top = mImageRect.top;
			}
			if (right > mImageRect.right) {
				right = mImageRect.right;
			}
			if (bottom > mImageRect.bottom) {
				bottom = mImageRect.bottom;
			}

			mEventRect.set(left, top, right, bottom);

			invalidate();
		} else if (action == MotionEvent.ACTION_UP) {
			if (mImageWidth <= 0 || mImageHeight <= 0) {
				return true;
			}
			if (bmMosaic == null) {
				bmMosaic = Bitmap.createBitmap(mImageWidth, mImageHeight,
						Config.ARGB_8888);
			}
			Canvas canvas = new Canvas(bmMosaic);

			int viewWidth = mImageRect.right - mImageRect.left;
			int viewHeight = mImageRect.bottom - mImageRect.top;

			int left = (mImageWidth * (mEventRect.left - mImageRect.left))
					/ viewWidth;
			int top = (mImageHeight * (mEventRect.top - mImageRect.top))
					/ viewHeight;
			int right = (mImageWidth * (mEventRect.right - mImageRect.left))
					/ viewWidth;
			int bottom = (mImageHeight * (mEventRect.bottom - mImageRect.top))
					/ viewHeight;

			int horCount = (int) Math.ceil((right - left) / (float) mGridWidth);
			int verCount = (int) Math.ceil((bottom - top) / (float) mGridWidth);

			Paint paint = new Paint();
			paint.setAntiAlias(true);

			for (int horIndex = 0; horIndex < horCount; ++horIndex) {
				for (int verIndex = 0; verIndex < verCount; ++verIndex) {
					int l = left + mGridWidth * horIndex;
					int t = top + mGridWidth * verIndex;
					int r = l + mGridWidth;
					int b = t + mGridWidth;
					int color = bmOrigin.getPixel(l, t);
					if (r > right) {
						r = right;
					}
					if (b > bottom) {
						b = bottom;
					}

					Rect rect = new Rect(l, t, r, b);

					paint.setColor(color);
					canvas.drawRect(rect, paint);
				}
			}

			canvas.save();
			// ivMosaic.setImageBitmap(bmMosaic);
			mEventRect = null;
			startPoint.set(-1, -1);
			invalidate();
		}

		return true;
	}

	public void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		int viewWidth = right - left;
		int viewHeight = bottom - top;
		float widthRatio = viewWidth / ((float) mImageWidth);
		float heightRatio = viewHeight / ((float) mImageHeight);
		float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
		int realWidth = (int) (mImageWidth * ratio);
		int realHeight = (int) (mImageHeight * ratio);

		int imageLeft = (viewWidth - realWidth) / 2;
		int imageTop = (viewHeight - realHeight) / 2;
		int imageRight = imageLeft + realWidth;
		int imageBottom = imageTop + realHeight;
		mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "onDraw canvas " + canvas);

		if (bmOrigin != null) {
			canvas.drawBitmap(bmOrigin, null, mImageRect, null);
		}

		if (bmMosaic != null) {
			canvas.drawBitmap(bmMosaic, null, mImageRect, null);
		}

		if (mEventRect != null) {
			canvas.drawRect(mEventRect, mPaint);
		}
	}

	private Bitmap getImage(String absPath) {
		Bitmap bitmap = BitmapFactory.decodeFile(absPath);
		return bitmap;
	}

	private void getImageSize(String absPath) {
		Options options = new Options();
		options.inPreferredConfig = Config.ALPHA_8;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(absPath, options);
		mImageWidth = options.outWidth;
		mImageHeight = options.outHeight;
		Log.d(TAG, "image width " + mImageWidth + " height " + mImageHeight);
	}

	private int dp2px(int dip) {
		Context context = this.getContext();
		Resources resources = context.getResources();
		int px = Math
				.round(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
						dip, resources.getDisplayMetrics()));
		return px;
	}
}
