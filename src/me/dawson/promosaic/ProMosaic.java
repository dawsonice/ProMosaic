package me.dawson.promosaic;

import android.app.Activity;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.Toast;

public class ProMosaic extends Activity {
	public static final String TAG = "ProMosaic";
	private static final int REQ_PICK_IMAGE = 1984;

	private MosaicImage miImage;

	private Button btClear;
	private Button btLoad;
	private Button btSave;
	private Button btAbout;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.pro_mosaic);

		miImage = (MosaicImage) findViewById(R.id.iv_content);

		btLoad = (Button) findViewById(R.id.bt_load);
		btClear = (Button) findViewById(R.id.bt_clear);
		btSave = (Button) findViewById(R.id.bt_save);
		btAbout = (Button) findViewById(R.id.bt_about);
		btLoad.setOnClickListener(cl);
		btClear.setOnClickListener(cl);
		btSave.setOnClickListener(cl);
		btAbout.setOnClickListener(cl);
	}

	private OnClickListener cl = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view.equals(btLoad)) {
				Intent intent = new Intent();
				intent.setType("image/*");
				intent.setAction(Intent.ACTION_PICK);
				String title = getResources().getString(R.string.choose_image);
				Intent chooser = Intent.createChooser(intent, title);
				startActivityForResult(chooser, REQ_PICK_IMAGE);
			} else if (view.equals(btClear)) {
				miImage.clear();
			} else if (view.equals(btSave)) {
				boolean succced = miImage.save();
				String text = "save image "
						+ (succced ? " succeed" : " failed");
				Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT)
						.show();
			}
		}
	};

	public void onActivityResult(int reqCode, int resultCode, Intent data) {
		super.onActivityResult(reqCode, resultCode, data);

		// user cancelled
		if (resultCode != Activity.RESULT_OK) {
			Log.d(TAG, "user cancelled");
			return;
		}

		if (reqCode == REQ_PICK_IMAGE) {
			Uri selectedImage = data.getData();
			String[] filePathColumn = { MediaStore.Images.Media.DATA };

			Cursor cursor = getContentResolver().query(selectedImage,
					filePathColumn, null, null, null);
			cursor.moveToFirst();

			int columnIndex = cursor.getColumnIndex(filePathColumn[0]);
			String filePath = cursor.getString(columnIndex);
			cursor.close();
			miImage.setSrcPath(filePath);
		}
	}
}
