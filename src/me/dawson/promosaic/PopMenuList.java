package me.dawson.promosaic;

import java.util.List;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.TextView;

public class PopMenuList implements OnItemClickListener {

	public interface ListMenuListener {
		public void onItemClick(int index);
	}

	public static class MenuItem {
		private final int iconId;
		private final Bitmap bitmap;
		private final String title;

		public MenuItem(int iconId, String title) {
			this.iconId = iconId;
			this.title = title;
			this.bitmap = null;
		}

		public MenuItem(Bitmap bitmap, String title) {
			this.iconId = 0;
			this.bitmap = bitmap;
			this.title = title;
		}

		public int getIconId() {
			return iconId;
		}

		public Bitmap getBitmap() {
			return bitmap;
		}

		public String getTitle() {
			return title;
		}
	}

	private List<MenuItem> menuItems;
	private PopupWindow popWindow;
	private ListView listView;
	private ListMenuListener listener;

	public PopMenuList(Context context) {
		this.listView = new ListView(context);
		this.listView.setOnItemClickListener(this);
		init();
	}

	public void setListMenuListener(ListMenuListener listener) {
		this.listener = listener;
	}

	public void setItems(List<MenuItem> items) {
		this.menuItems = items;
		adapter.notifyDataSetChanged();
	}

	protected void init() {
		listView.setBackgroundResource(R.drawable.menu_normal);
		listView.setSelector(R.drawable.menu_selector);
		listView.setDivider(listView.getResources().getDrawable(
				R.drawable.menu_divider));
		listView.setDividerHeight(1);
		listView.setAdapter(adapter);
	}

	public void show(View anchor) {
		if (popWindow != null && popWindow.isShowing()) {
			return;
		}

		if (popWindow == null) {
			int width = Math.round(TypedValue.applyDimension(
					TypedValue.COMPLEX_UNIT_DIP, 160, anchor.getResources()
							.getDisplayMetrics()));
			int height = ViewGroup.LayoutParams.WRAP_CONTENT;
			popWindow = new PopupWindow(listView, width, height, true);

			// close the window when lose focus
			popWindow.setTouchable(true);
			popWindow.setFocusable(true);
			popWindow.setOutsideTouchable(true);

			// remove default black background
			Drawable bg = anchor.getResources().getDrawable(
					R.drawable.transparent);
			popWindow.setBackgroundDrawable(bg);
		}
		popWindow.showAsDropDown(anchor);
	}

	private BaseAdapter adapter = new BaseAdapter() {

		@Override
		public int getCount() {
			if (menuItems != null) {
				return menuItems.size();
			}
			return 0;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				LayoutInflater inflater = LayoutInflater.from(parent
						.getContext());
				convertView = inflater.inflate(R.layout.menu_item, parent,
						false);
			}

			MenuItem item = menuItems.get(position);
			String title = item.getTitle();

			TextView tvName = (TextView) convertView.findViewById(R.id.tv_name);
			tvName.setText(title);
			return convertView;
		}
	};

	@Override
	public void onItemClick(AdapterView<?> parent, View view, int position,
			long id) {
		popWindow.dismiss();
		if (listener != null) {
			listener.onItemClick(position);
		}
	}
}
