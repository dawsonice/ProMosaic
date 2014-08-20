package me.dawson.promosaic;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.dawson.promosaic.BitmapUtil.Size;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.CornerPathEffect;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.Point;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.util.Log;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.ViewGroup;

public class MosaicView extends ViewGroup {
	public static final String TAG = "MosaicView";

	public static enum Effect {
		GRID, COLOR, BLUR,
	};

	public static enum Mode {
		GRID, PATH,
	}

	// default image inner padding, in dip pixels
	private static final int INNER_PADDING = 6;

	// default grid width, in dip pixels
	private static final int GRID_WIDTH = 5;

	// default grid width, in dip pixels
	private static final int PATH_WIDTH = 20;

	// default stroke rectangle color
	private static final int STROKE_COLOR = 0xff2a5caa;

	// default stroke width, in pixels
	private static final int STROKE_WIDTH = 6;

	private int mImageWidth;
	private int mImageHeight;

	private Bitmap bmBaseLayer;
	private Bitmap bmCoverLayer;
	private Bitmap bmMosaicLayer;

	private Point startPoint;

	private int mGridWidth;
	private int mPathWidth;

	private int mStrokeWidth;

	private int mStrokeColor;

	private String inPath;
	private String outPath;

	private Effect mEffect;
	private Mode mMode;

	private Rect mImageRect;

	private Paint mPaint;

	private Rect mTouchRect;
	private List<Rect> mTouchRects;

	private Path mTouchPath;
	private List<Rect> mEraseRects;

	private int mMosaicColor;
	private int mPadding;

	private List<Path> mTouchPaths;
	private List<Path> mErasePaths;

	private boolean mMosaic;

	public MosaicView(Context context) {
		super(context);
		initImage();
	}

	public MosaicView(Context context, AttributeSet attrs) {
		super(context, attrs);
		initImage();
	}

	private void initImage() {
		mMosaic = true;

		mTouchRects = new ArrayList<Rect>();
		mEraseRects = new ArrayList<Rect>();

		mTouchPaths = new ArrayList<Path>();
		mErasePaths = new ArrayList<Path>();

		mStrokeWidth = STROKE_WIDTH;
		mStrokeColor = STROKE_COLOR;

		mPadding = dp2px(INNER_PADDING);

		mPathWidth = dp2px(PATH_WIDTH);
		mGridWidth = dp2px(GRID_WIDTH);

		mPaint = new Paint();
		mPaint.setAntiAlias(true);
		mPaint.setStyle(Paint.Style.STROKE);
		mPaint.setStrokeWidth(mStrokeWidth);
		mPaint.setColor(mStrokeColor);

		mImageRect = new Rect();

		setWillNotDraw(false);

		mMode = Mode.PATH;
		mEffect = Effect.GRID;
	}

	public void setSrcPath(String absPath) {
		File file = new File(absPath);
		if (file == null || !file.exists()) {
			Log.w(TAG, "invalid file path " + absPath);
			return;
		}

		reset();

		inPath = absPath;
		String fileName = file.getName();
		String parent = file.getParent();
		int index = fileName.lastIndexOf(".");
		String stem = fileName.substring(0, index);
		String newStem = stem + "_mosaic";
		fileName = fileName.replace(stem, newStem);
		outPath = parent + "/" + fileName;

		Size size = BitmapUtil.getImageSize(inPath);
		mImageWidth = size.width;
		mImageHeight = size.height;

		bmBaseLayer = BitmapUtil.getImage(absPath);

		bmCoverLayer = getCoverLayer();
		bmMosaicLayer = null;

		requestLayout();
		invalidate();
	}

	public void setEffect(Effect effect) {
		if (mEffect == effect) {
			Log.d(TAG, "duplicated effect " + effect);
			return;
		}

		this.mEffect = effect;
		if (bmCoverLayer != null) {
			bmCoverLayer.recycle();
		}

		bmCoverLayer = getCoverLayer();
		if (mMode == Mode.GRID) {
			updateGridMosaic();
		} else if (mMode == Mode.PATH) {
			updatePathMosaic();
		}

		invalidate();
	}

	public void setMode(Mode mode) {
		if (mMode == mode) {
			Log.d(TAG, "duplicated mode " + mode);
			return;
		}

		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
			bmMosaicLayer = null;
		}

		this.mMode = mode;

		invalidate();
	}

	private Bitmap getCoverLayer() {
		Bitmap bitmap = null;
		if (mEffect == Effect.GRID) {
			bitmap = getGridMosaic();
		} else if (mEffect == Effect.COLOR) {
			bitmap = getColorMosaic();
		} else if (mEffect == Effect.BLUR) {
			bitmap = getBlurMosaic();
		}
		return bitmap;
	}

	private Bitmap getColorMosaic() {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		Rect rect = new Rect(0, 0, mImageWidth, mImageHeight);
		Paint paint = new Paint();
		paint.setColor(mMosaicColor);
		canvas.drawRect(rect, paint);
		canvas.save();
		return bitmap;
	}

	private Bitmap getBlurMosaic() {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return null;
		}

		if (bmBaseLayer == null) {
			return null;
		}
		Bitmap bitmap = BitmapUtil.blur(bmBaseLayer);
		return bitmap;
	}

	private Bitmap getGridMosaic() {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return null;
		}

		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);

		int horCount = (int) Math.ceil(mImageWidth / (float) mGridWidth);
		int verCount = (int) Math.ceil(mImageHeight / (float) mGridWidth);

		Paint paint = new Paint();
		paint.setAntiAlias(true);

		for (int horIndex = 0; horIndex < horCount; ++horIndex) {
			for (int verIndex = 0; verIndex < verCount; ++verIndex) {
				int l = mGridWidth * horIndex;
				int t = mGridWidth * verIndex;
				int r = l + mGridWidth;
				if (r > mImageWidth) {
					r = mImageWidth;
				}
				int b = t + mGridWidth;
				if (b > mImageHeight) {
					b = mImageHeight;
				}
				int color = bmBaseLayer.getPixel(l, t);
				Rect rect = new Rect(l, t, r, b);
				paint.setColor(color);
				canvas.drawRect(rect, paint);
			}
		}
		canvas.save();
		return bitmap;
	}

	public boolean isSaved() {
		return (bmCoverLayer == null);
	}

	public void setOutPath(String absPath) {
		this.outPath = absPath;
	}

	public void setGridWidth(int width) {
		this.mGridWidth = dp2px(width);
	}

	public void setPathWidth(int width) {
		this.mPathWidth = dp2px(width);
	}

	public int getGridWidth() {
		return this.mGridWidth;
	}

	public void setStrokeColor(int color) {
		this.mStrokeColor = color;
		mPaint.setColor(mStrokeColor);
	}

	public void setMosaicColor(int color) {
		this.mMosaicColor = color;
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

	public void setErase(boolean erase) {
		this.mMosaic = !erase;
	}

	public void clear() {
		mTouchRects.clear();
		mEraseRects.clear();

		mTouchPaths.clear();
		mErasePaths.clear();

		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
			bmMosaicLayer = null;
		}

		invalidate();
	}

	public boolean reset() {
		if (bmCoverLayer != null) {
			bmCoverLayer.recycle();
			bmCoverLayer = null;
		}
		if (bmBaseLayer != null) {
			bmBaseLayer.recycle();
			bmBaseLayer = null;
		}
		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
			bmMosaicLayer = null;
		}
		mTouchRects.clear();
		mEraseRects.clear();

		mTouchPaths.clear();
		mErasePaths.clear();
		return true;
	}

	public boolean save() {
		if (mTouchRects.isEmpty() || bmMosaicLayer == null) {
			return false;
		}

		Bitmap bitmap = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		canvas.drawBitmap(bmBaseLayer, 0, 0, null);
		canvas.drawBitmap(bmMosaicLayer, 0, 0, null);
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
		if (mMode == Mode.GRID) {
			onGridEvent(action, x, y);
		} else if (mMode == Mode.PATH) {
			onPathEvent(action, x, y);
		}
		return true;
	}

	private void onGridEvent(int action, int x, int y) {
		if (x >= mImageRect.left && x <= mImageRect.right
				&& y >= mImageRect.top && y <= mImageRect.bottom) {
			int left = x;
			int right = x;
			int top = y;
			int bottom = y;
			if (startPoint == null) {
				startPoint = new Point();
				startPoint.set(x, y);
				mTouchRect = new Rect();
			} else {
				left = startPoint.x < x ? startPoint.x : x;
				top = startPoint.y < y ? startPoint.y : y;
				right = x > startPoint.x ? x : startPoint.x;
				bottom = y > startPoint.y ? y : startPoint.y;
			}
			mTouchRect.set(left, top, right, bottom);
		}

		if (action == MotionEvent.ACTION_UP) {
			if (mMosaic) {
				mTouchRects.add(mTouchRect);
			} else {
				mEraseRects.add(mTouchRect);
			}
			mTouchRect = null;
			startPoint = null;
			updateGridMosaic();
		}

		invalidate();
	}

	private void onPathEvent(int action, int x, int y) {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		if (x < mImageRect.left || x > mImageRect.right || y < mImageRect.top
				|| y > mImageRect.bottom) {
			return;
		}

		float ratio = (mImageRect.right - mImageRect.left)
				/ (float) mImageWidth;
		x = (int) ((x - mImageRect.left) / ratio);
		y = (int) ((y - mImageRect.top) / ratio);

		if (action == MotionEvent.ACTION_DOWN) {
			mTouchPath = new Path();
			mTouchPath.moveTo(x, y);
			if (mMosaic) {
				mTouchPaths.add(mTouchPath);
			} else {
				mErasePaths.add(mTouchPath);
			}
		} else if (action == MotionEvent.ACTION_MOVE) {
			mTouchPath.lineTo(x, y);
			updatePathMosaic();
			invalidate();
		}
	}

	private void updatePathMosaic() {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		long time = System.currentTimeMillis();
		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
		}
		bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);

		Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);

		Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
		paint.setStyle(Paint.Style.STROKE);
		paint.setAntiAlias(true);
		paint.setStrokeJoin(Paint.Join.ROUND);
		paint.setStrokeCap(Paint.Cap.ROUND);
		paint.setPathEffect(new CornerPathEffect(10));
		paint.setStrokeWidth(mPathWidth);
		paint.setColor(Color.BLUE);

		Canvas canvas = new Canvas(bmTouchLayer);

		for (Path path : mTouchPaths) {
			canvas.drawPath(path, paint);
		}

		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		for (Path path : mErasePaths) {
			canvas.drawPath(path, paint);
		}

		canvas.setBitmap(bmMosaicLayer);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawBitmap(bmCoverLayer, 0, 0, null);

		paint.reset();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(bmTouchLayer, 0, 0, paint);
		paint.setXfermode(null);
		canvas.save();

		bmTouchLayer.recycle();
		Log.d(TAG, "updatePathMosaic " + (System.currentTimeMillis() - time));
	}

	private void updateGridMosaic() {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		long time = System.currentTimeMillis();
		if (bmMosaicLayer != null) {
			bmMosaicLayer.recycle();
		}
		bmMosaicLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);

		float ratio = (mImageRect.right - mImageRect.left)
				/ (float) mImageWidth;
		Bitmap bmTouchLayer = Bitmap.createBitmap(mImageWidth, mImageHeight,
				Config.ARGB_8888);

		Canvas canvas = null;
		canvas = new Canvas(bmTouchLayer);
		Paint paint = new Paint();
		paint.setStyle(Paint.Style.FILL);
		paint.setColor(mStrokeColor);

		for (Rect rect : mTouchRects) {
			int left = (int) ((rect.left - mImageRect.left) / ratio);
			int right = (int) ((rect.right - mImageRect.left) / ratio);
			int top = (int) ((rect.top - mImageRect.top) / ratio);
			int bottom = (int) ((rect.bottom - mImageRect.top) / ratio);
			canvas.drawRect(left, top, right, bottom, paint);
		}

		paint.setColor(Color.TRANSPARENT);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
		for (Rect rect : mEraseRects) {
			int left = (int) ((rect.left - mImageRect.left) / ratio);
			int right = (int) ((rect.right - mImageRect.left) / ratio);
			int top = (int) ((rect.top - mImageRect.top) / ratio);
			int bottom = (int) ((rect.bottom - mImageRect.top) / ratio);
			canvas.drawRect(left, top, right, bottom, paint);
		}

		canvas.setBitmap(bmMosaicLayer);
		canvas.drawARGB(0, 0, 0, 0);
		canvas.drawBitmap(bmCoverLayer, 0, 0, null);

		paint.reset();
		paint.setAntiAlias(true);
		paint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.DST_IN));
		canvas.drawBitmap(bmTouchLayer, 0, 0, paint);
		paint.setXfermode(null);
		canvas.save();

		bmTouchLayer.recycle();
		Log.d(TAG, "updateGridMosaic " + (System.currentTimeMillis() - time));
	}

	public void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		Log.d(TAG, "onDraw canvas " + canvas + " mTouchRect " + mTouchRect);

		if (bmBaseLayer != null) {
			canvas.drawBitmap(bmBaseLayer, null, mImageRect, null);
		}

		if (bmMosaicLayer != null) {
			canvas.drawBitmap(bmMosaicLayer, null, mImageRect, null);
		}

		if (mTouchRect != null) {
			canvas.drawRect(mTouchRect, mPaint);
		}
	}

	protected void onLayout(boolean changed, int left, int top, int right,
			int bottom) {
		if (mImageWidth <= 0 || mImageHeight <= 0) {
			return;
		}

		int contentWidth = right - left;
		int contentHeight = bottom - top;
		int viewWidth = contentWidth - mPadding * 2;
		int viewHeight = contentHeight - mPadding * 2;
		float widthRatio = viewWidth / ((float) mImageWidth);
		float heightRatio = viewHeight / ((float) mImageHeight);
		float ratio = widthRatio < heightRatio ? widthRatio : heightRatio;
		int realWidth = (int) (mImageWidth * ratio);
		int realHeight = (int) (mImageHeight * ratio);

		int imageLeft = (contentWidth - realWidth) / 2;
		int imageTop = (contentHeight - realHeight) / 2;
		int imageRight = imageLeft + realWidth;
		int imageBottom = imageTop + realHeight;
		mImageRect.set(imageLeft, imageTop, imageRight, imageBottom);
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
