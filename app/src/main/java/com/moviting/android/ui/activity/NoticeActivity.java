package com.moviting.android.ui.activity;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.ValueEventListener;
import com.moviting.android.R;
import com.moviting.android.model.Notice;

import java.util.ArrayList;

public class NoticeActivity extends BaseActivity {

    private static final String TAG = "NoticeActivity";
    private ListView noticeList;
    private NoticeAdapter noticeAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_notice);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        noticeList = (ListView) findViewById(R.id.notice_list);
        noticeList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(WebViewActivity.createIntent(NoticeActivity.this, noticeAdapter.getNotice(i).link));
            }
        });
        getNoticeList();
    }

    private void getNoticeList() {
        getFirebaseDatabaseReference().child("notice").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                ArrayList<Notice> noticeItemList = new ArrayList<>();
                for(DataSnapshot child : dataSnapshot.getChildren()) {
                    noticeItemList.add(child.getValue(Notice.class));
                }
                noticeAdapter = new NoticeAdapter(noticeItemList);
                noticeList.setAdapter(noticeAdapter);
                noticeAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
                if(getBaseContext() != null) {
                    Toast.makeText(getBaseContext(), databaseError.getMessage(), Toast.LENGTH_SHORT).show();
                }
                Log.w(TAG, databaseError.getDetails());
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        super.onOptionsItemSelected(item);
        switch(item.getItemId()) {
            case android.R.id.home:
                NoticeActivity.this.onBackPressed();
                break;
        }
        return true;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    public static Intent createIntent(Context context) {
        Intent in = new Intent();
        in.setClass(context, NoticeActivity.class);
        return in;
    }

    public class NoticeAdapter extends BaseAdapter {
        private ArrayList<Notice> noticeList;

        public NoticeAdapter(ArrayList<Notice> noticeList) {
            this.noticeList = noticeList;
        }

        public Notice getNotice(int i) {
            return noticeList.get(i);
        }

        @Override
        public int getCount() {
            return noticeList.size();
        }

        @Override
        public Object getItem(int i) {
            return null;
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            NoticeViewHolder noticeViewHolder;
            if(view == null) {
                noticeViewHolder = new NoticeViewHolder();

                LayoutInflater inflater = (LayoutInflater) getBaseContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                view = inflater.inflate(R.layout.notice_item, null);

                noticeViewHolder.title = (TextView) view.findViewById(R.id.title);

                view.setTag(noticeViewHolder);
            } else {
                noticeViewHolder = (NoticeViewHolder) view.getTag();
            }

            noticeViewHolder.title.setText(noticeList.get(i).title);

            return view;
        }

        class NoticeViewHolder {
            TextView title;
        }
    }
}
