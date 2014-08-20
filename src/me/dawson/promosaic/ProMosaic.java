package me.dawson.promosaic;

import java.util.LinkedList;
import java.util.List;

import me.dawson.promosaic.MosaicView.Effect;
import me.dawson.promosaic.MosaicView.Mode;
import me.dawson.promosaic.PopMenuList.ListMenuListener;
import me.dawson.promosaic.PopMenuList.MenuItem;
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

	private MosaicView mvImage;

	private Button btClear;
	private Button btLoad;
	private Button btSave;
	private Button btEffect;
	private Button btMode;
	private Button btAbout;
	private Button btErase;

	private PopMenuList effectList;
	private PopMenuList modeList;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.pro_mosaic);

		mvImage = (MosaicView) findViewById(R.id.iv_content);

		btLoad = (Button) findViewById(R.id.bt_load);
		btClear = (Button) findViewById(R.id.bt_clear);
		btSave = (Button) findViewById(R.id.bt_save);
		btAbout = (Button) findViewById(R.id.bt_about);
		btMode = (Button) findViewById(R.id.bt_mode);
		btEffect = (Button) findViewById(R.id.bt_effect);
		btErase = (Button) findViewById(R.id.bt_erase);
		btLoad.setOnClickListener(cl);
		btClear.setOnClickListener(cl);
		btSave.setOnClickListener(cl);
		btAbout.setOnClickListener(cl);
		btEffect.setOnClickListener(cl);
		btMode.setOnClickListener(cl);
		btErase.setOnClickListener(cl);
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
				mvImage.clear();
				mvImage.setErase(false);
			} else if (view.equals(btSave)) {
				boolean succced = mvImage.save();
				String text = "save image "
						+ (succced ? " succeed" : " failed");
				Toast.makeText(view.getContext(), text, Toast.LENGTH_SHORT)
						.show();
			} else if (view.equals(btEffect)) {
				initEffectList();
				effectList.show(btEffect);
			} else if (view.equals(btMode)) {
				initModeList();
				modeList.show(btMode);
			} else if (view.equals(btAbout)) {
				Intent intent = new Intent(ProMosaic.this, AboutMe.class);
				startActivity(intent);
			} else if (view.equals(btErase)) {
				mvImage.setErase(true);
			}
		}
	};

	private void initEffectList() {
		if (effectList != null) {
			return;
		}
		effectList = new PopMenuList(this);
		List<MenuItem> items = new LinkedList<MenuItem>();
		items.add(new MenuItem(null, getResources().getString(
				R.string.effect_grid)));
		items.add(new MenuItem(null, getResources().getString(
				R.string.effect_blur)));
		items.add(new MenuItem(null, getResources().getString(
				R.string.effect_color)));
		effectList.setItems(items);
		effectList.setListMenuListener(el);
	}

	private ListMenuListener el = new ListMenuListener() {

		@Override
		public void onItemClick(int index) {
			if (index == 0) {
				mvImage.setEffect(Effect.GRID);
			} else if (index == 1) {
				mvImage.setEffect(Effect.BLUR);
			} else if (index == 2) {
				mvImage.setMosaicColor(0xFF4D4D4D);
				mvImage.setEffect(Effect.COLOR);
			}
		}
	};

	private void initModeList() {
		if (modeList != null) {
			return;
		}
		modeList = new PopMenuList(this);
		List<MenuItem> items = new LinkedList<MenuItem>();
		items.add(new MenuItem(null, getResources().getString(
				R.string.mode_path)));
		items.add(new MenuItem(null, getResources().getString(
				R.string.mode_grid)));
		modeList.setItems(items);
		modeList.setListMenuListener(ml);
	}

	private ListMenuListener ml = new ListMenuListener() {

		@Override
		public void onItemClick(int index) {
			if (index == 0) {
				mvImage.setMode(Mode.PATH);
			} else if (index == 1) {
				mvImage.setMode(Mode.GRID);
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
			mvImage.setSrcPath(filePath);
		}
	}
}
