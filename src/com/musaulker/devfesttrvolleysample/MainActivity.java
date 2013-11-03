package com.musaulker.devfesttrvolleysample;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.widget.AbsListView;
import android.widget.AbsListView.OnScrollListener;
import android.widget.ListView;

import com.android.volley.Request.Method;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.musaulker.devfesttrvolleysample.models.PicasaArrayAdapter;
import com.musaulker.devfesttrvolleysample.models.PicasaEntry;

public class MainActivity extends Activity {
	private static final int RESULTS_PAGE_SIZE = 20;

	private ListView mLvPicasa;
	private boolean mHasData = false;
	private boolean mInError = false;
	private ArrayList<PicasaEntry> mEntries = new ArrayList<PicasaEntry>();
	private PicasaArrayAdapter mAdapter;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		mLvPicasa = (ListView) findViewById(R.id.lv_picasa);
		mAdapter = new PicasaArrayAdapter(this, 0, mEntries, MyVolley.getImageLoader());
		mLvPicasa.setAdapter(mAdapter);
		mLvPicasa.setOnScrollListener(new EndlessScrollListener());
	}

	@Override
	protected void onResume() {
		super.onResume();

		if (!mHasData && !mInError) {
			loadPage();
		}
	}

	private void loadPage() {
		RequestQueue queue = MyVolley.getRequestQueue();

		int startIndex = 1 + mEntries.size();
		JsonObjectRequest myReq = new JsonObjectRequest(Method.GET, "https://picasaweb.google.com/data/feed/api/all?q=kitten&max-results=" + RESULTS_PAGE_SIZE + "&thumbsize=160&alt=json"
				+ "&start-index=" + startIndex, null, createMyReqSuccessListener(), createMyReqErrorListener());

		queue.add(myReq);
	}

	private Response.Listener<JSONObject> createMyReqSuccessListener() {
		return new Response.Listener<JSONObject>() {
			@Override
			public void onResponse(JSONObject response) {
				try {
					JSONObject feed = response.getJSONObject("feed");
					JSONArray entries = feed.getJSONArray("entry");
					JSONObject entry;
					for (int i = 0; i < entries.length(); i++) {
						entry = entries.getJSONObject(i);

						String url = null;

						JSONObject media = entry.getJSONObject("media$group");
						if (media != null && media.has("media$thumbnail")) {
							JSONArray thumbs = media.getJSONArray("media$thumbnail");
							if (thumbs != null && thumbs.length() > 0) {
								url = thumbs.getJSONObject(0).getString("url");
							}
						}

						mEntries.add(new PicasaEntry(entry.getJSONObject("title").getString("$t"), url));
					}
					mAdapter.notifyDataSetChanged();
				} catch (JSONException e) {
					showErrorDialog();
				}
			}
		};
	}

	private Response.ErrorListener createMyReqErrorListener() {
		return new Response.ErrorListener() {
			@Override
			public void onErrorResponse(VolleyError error) {
				showErrorDialog();
			}
		};
	}

	private void showErrorDialog() {
		mInError = true;

		AlertDialog.Builder b = new AlertDialog.Builder(MainActivity.this);
		b.setMessage("Error occured");
		b.show();
	}

	/**
	 * Detects when user is close to the end of the current page and starts
	 * loading the next page so the user will not have to wait (that much) for
	 * the next entries.
	 * 
	 * @author Ognyan Bankov (ognyan.bankov@bulpros.com)
	 */
	public class EndlessScrollListener implements OnScrollListener {
		// how many entries earlier to start loading next page
		private int visibleThreshold = 5;
		private int currentPage = 0;
		private int previousTotal = 0;
		private boolean loading = true;

		public EndlessScrollListener() {
		}

		public EndlessScrollListener(int visibleThreshold) {
			this.visibleThreshold = visibleThreshold;
		}

		@Override
		public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
			if (loading) {
				if (totalItemCount > previousTotal) {
					loading = false;
					previousTotal = totalItemCount;
					currentPage++;
				}
			}
			if (!loading && (totalItemCount - visibleItemCount) <= (firstVisibleItem + visibleThreshold)) {
				// I load the next page of gigs using a background task,
				// but you can call any function here.
				loadPage();
				loading = true;
			}
		}

		@Override
		public void onScrollStateChanged(AbsListView view, int scrollState) {

		}

		public int getCurrentPage() {
			return currentPage;
		}
	}

}
