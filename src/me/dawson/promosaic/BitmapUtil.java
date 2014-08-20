package me.dawson.promosaic;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.BitmapFactory.Options;

public class BitmapUtil {
	public static class Size {
		public int width;
		public int height;

		public Size(int w, int h) {
			this.width = w;
			this.height = h;
		}
	}

	public static Bitmap getImage(String absPath) {
		Bitmap bitmap = BitmapFactory.decodeFile(absPath);
		return bitmap;
	}

	public static Size getImageSize(String absPath) {
		Options options = new Options();
		options.inPreferredConfig = Config.ALPHA_8;
		options.inJustDecodeBounds = true;
		BitmapFactory.decodeFile(absPath, options);
		Size size = new Size(options.outWidth, options.outHeight);
		return size;
	}

	public static Bitmap blur(Bitmap bitmap) {
		int iterations = 1;
		int radius = 8;
		int width = bitmap.getWidth();
		int height = bitmap.getHeight();
		int[] inPixels = new int[width * height];
		int[] outPixels = new int[width * height];
		Bitmap blured = Bitmap.createBitmap(width, height,
				Bitmap.Config.ARGB_8888);
		bitmap.getPixels(inPixels, 0, width, 0, 0, width, height);
		for (int i = 0; i < iterations; i++) {
			blur(inPixels, outPixels, width, height, radius);
			blur(outPixels, inPixels, height, width, radius);
		}
		blured.setPixels(inPixels, 0, width, 0, 0, width, height);
		return blured;
	}

	private static void blur(int[] in, int[] out, int width, int height,
			int radius) {
		int widthMinus1 = width - 1;
		int tableSize = 2 * radius + 1;
		int divide[] = new int[256 * tableSize];

		for (int index = 0; index < 256 * tableSize; index++) {
			divide[index] = index / tableSize;
		}

		int inIndex = 0;

		for (int y = 0; y < height; y++) {
			int outIndex = y;
			int ta = 0, tr = 0, tg = 0, tb = 0;

			for (int i = -radius; i <= radius; i++) {
				int rgb = in[inIndex + clamp(i, 0, width - 1)];
				ta += (rgb >> 24) & 0xff;
				tr += (rgb >> 16) & 0xff;
				tg += (rgb >> 8) & 0xff;
				tb += rgb & 0xff;
			}

			for (int x = 0; x < width; x++) {
				out[outIndex] = (divide[ta] << 24) | (divide[tr] << 16)
						| (divide[tg] << 8) | divide[tb];

				int i1 = x + radius + 1;
				if (i1 > widthMinus1)
					i1 = widthMinus1;
				int i2 = x - radius;
				if (i2 < 0)
					i2 = 0;
				int rgb1 = in[inIndex + i1];
				int rgb2 = in[inIndex + i2];

				ta += ((rgb1 >> 24) & 0xff) - ((rgb2 >> 24) & 0xff);
				tr += ((rgb1 & 0xff0000) - (rgb2 & 0xff0000)) >> 16;
				tg += ((rgb1 & 0xff00) - (rgb2 & 0xff00)) >> 8;
				tb += (rgb1 & 0xff) - (rgb2 & 0xff);
				outIndex += height;
			}
			inIndex += width;
		}
	}

	private static int clamp(int x, int a, int b) {
		return (x < a) ? a : (x > b) ? b : x;
	}

}
