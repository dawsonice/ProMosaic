package me.dawson.promosaic;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class AboutMe extends Activity {
	private Button btQQ;
	private Button btEmail;
	private Button btGithub;

	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);

		setContentView(R.layout.about_me);

		btQQ = (Button) findViewById(R.id.bt_qq);
		btEmail = (Button) findViewById(R.id.bt_email);
		btGithub = (Button) findViewById(R.id.bt_github);
		btQQ.setOnClickListener(cl);
		btEmail.setOnClickListener(cl);
		btGithub.setOnClickListener(cl);
	}

	private OnClickListener cl = new OnClickListener() {

		@Override
		public void onClick(View view) {
			if (view.equals(btGithub)) {
				String url = "https://github.com/coderkiss";
				Intent intent = new Intent();
				intent.setAction("android.intent.action.VIEW");
				Uri content_url = Uri.parse(url);
				intent.setData(content_url);
				try {
					startActivity(intent);
				} catch (Exception ex) {
				}
			} else if (view.equals(btQQ)) {
			} else if (view.equals(btEmail)) {
				Intent intent = new Intent(Intent.ACTION_SEND);
				intent.setType("message/rfc822");
				intent.putExtra(Intent.EXTRA_EMAIL,
						new String[] { "coder.kiss@gmail.com" });
				try {
					startActivity(intent);
				} catch (Exception ex) {
				}
			}
		}
	};
}
