package org.servalproject.batphone;

import org.servalproject.R;
import org.servalproject.ServalBatPhoneApplication;
import org.servalproject.servald.DnaResult;
import org.servalproject.servald.LookupResults;
import org.servalproject.servald.ServalD;

import android.app.ListActivity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;

public class CallDirector extends ListActivity {

	String dialed_number;
	ArrayAdapter<DnaResult> adapter;
	private boolean searching = false;
	Button cancel;
	Button search;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		this.setContentView(R.layout.call_director);
		Intent intent = this.getIntent();

		dialed_number = intent.getStringExtra("phone_number");

		adapter = new ArrayAdapter<DnaResult>(this,
				android.R.layout.simple_list_item_1);
		setListAdapter(adapter);

		TextView call = (TextView) this.findViewById(R.id.call);
		call.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				BatPhone.call(dialed_number);
				finish();
			}
		});

		cancel = (Button) this.findViewById(R.id.cancel);
		cancel.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				finish();
			}
		});

		search = (Button) this.findViewById(R.id.search);
		search.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				searchMesh();
			}
		});
	}

	@Override
	protected void onResume() {
		super.onResume();
		searchMesh();
	}

	private void searchMesh() {
		if (searching)
			return;

		search.setEnabled(false);
		search.setText("Searching");
		searching = true;
		adapter.notifyDataSetChanged();

		new AsyncTask<String, DnaResult, Void>() {
			@Override
			protected void onProgressUpdate(DnaResult... values) {
				if (adapter.getPosition(values[0]) < 0)
					adapter.add(values[0]);
			}

			@Override
			protected void onPostExecute(Void result) {
				search.setEnabled(true);
				search.setText("Search");
				searching = false;
				adapter.notifyDataSetChanged();

				// TODO Auto-generated method stub
				super.onPostExecute(result);
			}

			@Override
			protected Void doInBackground(String... params) {
				ServalD.dnaLookup(new LookupResults() {
					@Override
					public void result(DnaResult result) {
						publishProgress(result);
					}
				}, params[0]);
				return null;
			}

		}.execute(dialed_number);
	}

	@Override
	protected void onListItemClick(ListView l, View v, int position, long id) {
		try {
			BatPhone.callPeer(adapter.getItem(position));
			finish();
		} catch (Exception e) {
			ServalBatPhoneApplication.context.displayToastMessage(e
					.getMessage());
			Log.e("BatPhone", e.getMessage(), e);
		}
	}

}
