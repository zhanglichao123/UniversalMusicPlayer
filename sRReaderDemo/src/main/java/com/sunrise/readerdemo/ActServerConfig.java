package com.sunrise.readerdemo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.*;

import java.util.ArrayList;

public class ActServerConfig extends Activity {

	private String server_address;
	private int server_port;

	EditText server_address_tv;
	EditText server_port_tv;

	Button button_ok;
	Button button_cancel;

	private long exitTime = 0;

	private RecyclerView recyclerView;
	private ArrayList<String> serverListData = new ArrayList<String>();

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.server_set);

		Bundle bundle = getIntent().getExtras();
		if(!bundle.isEmpty()){
			serverListData = bundle.getStringArrayList("server");
		}

		recyclerView = (RecyclerView) findViewById(R.id.server_list);
		server_address_tv = (EditText) findViewById(R.id.orther_ip);
		server_port_tv = (EditText) findViewById(R.id.orther_port);
		button_ok = (Button) findViewById(R.id.button_ok);
		button_cancel = (Button) findViewById(R.id.button_cancel);

		button_ok.setOnClickListener(mylistener);
		button_cancel.setOnClickListener(mylistener);

		recyclerView.setLayoutManager(new LinearLayoutManager(this));
		MyAdapter myAdapter = new MyAdapter();
		myAdapter.initData(serverListData);
		myAdapter.setOnItemClickLitener(new OnItemClickLitener() {
			@Override
			public void onItemClick(View view, int position) {
				String choice = serverListData.get(position);
				String[] choiceArray = choice.split(":");

				server_address_tv.setText(choiceArray[0]);
				server_port_tv.setText(choiceArray[1]);
			}
		});
		recyclerView.setAdapter(myAdapter);
	}

	View.OnClickListener mylistener = new View.OnClickListener() {

		@Override
		public void onClick(View v) {
			switch (v.getId()) {
				case R.id.button_ok:
					buttonok();

					break;
				case R.id.button_cancel:
					finish();
					break;
				default:
					break;
			}
		}
	};

	private void buttonok() {
		saveconfig();

		Log.e("MAIN", "select: " + server_address);
		Log.e("MAIN", "select: " + server_port);

		if (server_address.length() < 0 || server_port == 0) {
			Toast.makeText(ActServerConfig.this, "请选择有效的服务器地址!",
					Toast.LENGTH_LONG).show();
			return;
		}

		Intent intent = new Intent();
		intent.putExtra("address", server_address);
		intent.putExtra("port", server_port);

		// Set result and finish this Activity
		setResult(100, intent);
		finish();
	}

	private void saveconfig() {

		server_address = String.valueOf(server_address_tv.getText());
		if (server_port_tv.getText().length() <= 0) {
			server_port = 0;
		} else {
			server_port = new Integer(
					String.valueOf(server_port_tv.getText()));
		}
	}

	@Override
	public boolean onKeyDown(int keyCode, KeyEvent event) {
		if (keyCode == KeyEvent.KEYCODE_BACK
				&& event.getAction() == KeyEvent.ACTION_DOWN) {

			if ((System.currentTimeMillis() - exitTime) > 2000) {
				Toast.makeText(getApplicationContext(),
						getString(R.string.idPressAgainToExitTest),
						Toast.LENGTH_SHORT).show();
				exitTime = System.currentTimeMillis();

			} else {

				finish();

			}

			return true;
		}

		return super.onKeyDown(keyCode, event);
	}

	public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
		private ArrayList<String> serverList = new ArrayList<String>();

		@Override
		public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int i) {
			View v = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.item, viewGroup, false);
			return new MyViewHolder(v);
		}


		public void onBindViewHolder(final MyViewHolder viewHolder, int i) {
			viewHolder.setText(serverList.get(i).toString());
			if (mOnItemClickLitener != null) {
				viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						int pos = viewHolder.getLayoutPosition();
						mOnItemClickLitener.onItemClick(viewHolder.itemView, pos);
					}
				});
			}
		}

		@Override
		public int getItemCount() {
			return serverList.size();
		}

		public void initData(ArrayList<String> list){
			serverList = list;
		}

		class MyViewHolder extends RecyclerView.ViewHolder{

			TextView ipTV;

			public MyViewHolder(View itemView) {
				super(itemView);

				ipTV = (TextView) itemView.findViewById(R.id.ip);
			}

			public void setText(String s){
				ipTV.setText(s);
			}
		}

		private OnItemClickLitener mOnItemClickLitener;

		public void setOnItemClickLitener(OnItemClickLitener mOnItemClickLitener)
		{
			this.mOnItemClickLitener = mOnItemClickLitener;
		}

	}

	public interface OnItemClickLitener {
		void onItemClick(View view, int position);
	}
}
