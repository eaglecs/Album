/*
 * Copyright 2018 Yan Zhenjie
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.yanzhenjie.album.app.album;

import android.app.Activity;
import android.content.res.Configuration;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.R;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.impl.DoubleClickWrapper;
import com.yanzhenjie.album.impl.OnCheckedClickListener;
import com.yanzhenjie.album.impl.OnItemClickListener;
import com.yanzhenjie.album.util.AlbumUtils;
import com.yanzhenjie.album.util.SystemBar;
import com.yanzhenjie.album.widget.ColorProgressBar;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;

import java.util.ArrayList;

/**
 * Created by YanZhenjie on 2018/4/7.
 */
class AlbumView extends Contract.AlbumView implements View.OnClickListener {

    private Activity mActivity;

    private Toolbar mToolbar;
    private MenuItem mCompleteMenu;

    private RecyclerView mRecyclerView;
    private AppCompatTextView tvTitleSuggest;
    private AppCompatTextView tvTitleRecent;
    private RecyclerView rvSuggest;
    private GridLayoutManager mLayoutManager;
    private GridLayoutManager mLayoutManagerSuggest;
    private AlbumAdapter mAdapter;
    private AlbumAdapter mAdapterSuggest;

    private Button mBtnPreview;
    private Button mBtnSwitchFolder;

    private LinearLayout mLayoutLoading;
    private ColorProgressBar mProgressBar;
    private int radius = 0;
    private Double lat = 0.0;
    private Double lng = 0.0;
    private Boolean hasCamera = false;

    public AlbumView(Activity activity, Contract.AlbumPresenter presenter) {
        super(activity, presenter);
        this.mActivity = activity;

        this.mToolbar = activity.findViewById(R.id.toolbar);
        this.mRecyclerView = activity.findViewById(R.id.rvPhoto);
        this.tvTitleSuggest = activity.findViewById(R.id.tvTitleSuggest);
        this.tvTitleRecent = activity.findViewById(R.id.tvTitleRecent);
        this.rvSuggest = activity.findViewById(R.id.rvSuggest);

        this.mBtnSwitchFolder = activity.findViewById(R.id.btn_switch_dir);
        this.mBtnPreview = activity.findViewById(R.id.btn_preview);

        this.mLayoutLoading = activity.findViewById(R.id.layout_loading);
        this.mProgressBar = activity.findViewById(R.id.progress_bar);

        this.mToolbar.setOnClickListener(new DoubleClickWrapper(this));
        this.mBtnSwitchFolder.setOnClickListener(this);
        this.mBtnPreview.setOnClickListener(this);
    }

    @Override
    protected void onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.album_menu_album, menu);
        mCompleteMenu = menu.findItem(R.id.album_menu_finish);
    }

    @Override
    protected void onOptionsItemSelected(MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.album_menu_finish) {
            getPresenter().complete();
        }
    }

    @Override
    public void setupViews(Widget widget, int column, boolean hasCamera, int choiceMode, int radius, Double lat, Double lng) {
        this.radius = radius;
        this.lat = lat;
        this.lng = lng;
        this.hasCamera = hasCamera;
        SystemBar.setNavigationBarColor(mActivity, widget.getNavigationBarColor());
        int statusBarColor = widget.getStatusBarColor();
        if (widget.getUiStyle() == Widget.STYLE_LIGHT) {
            if (SystemBar.setStatusBarDarkFont(mActivity, true)) {
                SystemBar.setStatusBarColor(mActivity, statusBarColor);
            } else {
                SystemBar.setStatusBarColor(mActivity, getColor(R.color.albumColorPrimaryBlack));
            }

            mProgressBar.setColorFilter(getColor(R.color.albumLoadingDark));

            Drawable navigationIcon = getDrawable(R.drawable.album_ic_back_white);
            AlbumUtils.setDrawableTint(navigationIcon, getColor(R.color.albumIconDark));
            setHomeAsUpIndicator(navigationIcon);

            Drawable completeIcon = mCompleteMenu.getIcon();
            AlbumUtils.setDrawableTint(completeIcon, getColor(R.color.albumIconDark));
            mCompleteMenu.setIcon(completeIcon);
        } else {
            mProgressBar.setColorFilter(widget.getToolBarColor());
            SystemBar.setStatusBarColor(mActivity, statusBarColor);
            setHomeAsUpIndicator(R.drawable.album_ic_back_white);
        }
        mToolbar.setBackgroundColor(widget.getToolBarColor());

        Configuration config = mActivity.getResources().getConfiguration();
        mLayoutManager = new GridLayoutManager(getContext(), column, getOrientation(config), false);
        mLayoutManagerSuggest = new GridLayoutManager(getContext(), column, getOrientation(config), false);
        mRecyclerView.setLayoutManager(mLayoutManager);
        rvSuggest.setLayoutManager(mLayoutManagerSuggest);
        int dividerSize = getResources().getDimensionPixelSize(R.dimen.album_dp_4);
        mRecyclerView.addItemDecoration(new Api21ItemDivider(Color.TRANSPARENT, dividerSize, dividerSize));
        rvSuggest.addItemDecoration(new Api21ItemDivider(Color.TRANSPARENT, dividerSize, dividerSize));
        mAdapterSuggest = new AlbumAdapter(getContext(), false, choiceMode, widget.getMediaItemCheckSelector());
        mAdapterSuggest.setCheckedClickListener(new OnCheckedClickListener() {
            @Override
            public void onCheckedClick(CompoundButton button, int position) {
                getPresenter().tryCheckItem(button, position, true);
            }
        });
        mAdapterSuggest.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getPresenter().tryPreviewItem(position);
            }
        });
        rvSuggest.setAdapter(mAdapterSuggest);


        mAdapter = new AlbumAdapter(getContext(), hasCamera, choiceMode, widget.getMediaItemCheckSelector());
        mAdapter.setAddClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getPresenter().clickCamera(view);
            }
        });
        mAdapter.setCheckedClickListener(new OnCheckedClickListener() {
            @Override
            public void onCheckedClick(CompoundButton button, int position) {
                getPresenter().tryCheckItem(button, position, false);
            }
        });
        mAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                getPresenter().tryPreviewItem(position);
            }
        });
        mRecyclerView.setAdapter(mAdapter);
    }

    @Override
    public void setLoadingDisplay(boolean display) {
        mLayoutLoading.setVisibility(display ? View.VISIBLE : View.GONE);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        int position = mLayoutManager.findFirstVisibleItemPosition();
        mLayoutManager.setOrientation(getOrientation(newConfig));
        mRecyclerView.setAdapter(mAdapter);
        mLayoutManager.scrollToPosition(position);

        int positionSuggest = mLayoutManagerSuggest.findFirstVisibleItemPosition();
        mLayoutManagerSuggest.setOrientation(getOrientation(newConfig));
        rvSuggest.setAdapter(mAdapterSuggest);
        mLayoutManagerSuggest.scrollToPosition(positionSuggest);
    }

    @RecyclerView.Orientation
    private int getOrientation(Configuration config) {
        switch (config.orientation) {
            case Configuration.ORIENTATION_PORTRAIT: {
                return LinearLayoutManager.VERTICAL;
            }
            case Configuration.ORIENTATION_LANDSCAPE: {
                return LinearLayoutManager.HORIZONTAL;
            }
            default: {
                throw new AssertionError("This should not be the case.");
            }
        }
    }

    @Override
    public void setCompleteDisplay(boolean display) {
        mCompleteMenu.setVisible(display);
    }

    @Override
    public void bindAlbumFolder(AlbumFolder albumFolder) {
        mBtnSwitchFolder.setText(albumFolder.getName());
        mAdapterSuggest.setAlbumFiles(albumFolder.getAlbumFilesSuggest());
        mAdapterSuggest.notifyDataSetChanged();
        if (albumFolder.getAlbumFilesSuggest().isEmpty()) {
            rvSuggest.setVisibility(View.GONE);
            tvTitleSuggest.setVisibility(View.GONE);
        } else {
            rvSuggest.setVisibility(View.VISIBLE);
            rvSuggest.scrollToPosition(0);
            tvTitleSuggest.setVisibility(View.VISIBLE);
        }

        mAdapter.setAlbumFiles(albumFolder.getAlbumFiles());
        mAdapter.notifyDataSetChanged();
        if (!albumFolder.getAlbumFiles().isEmpty() || hasCamera) {
            mRecyclerView.scrollToPosition(0);
        }
    }

    @Override
    public void notifyInsertItem(int position) {
        mAdapter.notifyItemInserted(position);

    }

    @Override
    public void notifyInsertItem(int position, AlbumFile albumFile) {
        mAdapter.addItem(albumFile);
        mAdapter.notifyItemInserted(position);
    }

    @Override
    public void notifyItem(int position) {
        mAdapter.notifyItemChanged(position);
    }

    @Override
    public void setCheckedCount(int count) {
        mBtnPreview.setText(" (" + count + ")");
    }

    @Override
    public void onClick(View v) {
        if (v == mToolbar) {
            mRecyclerView.smoothScrollToPosition(0);
        } else if (v == mBtnSwitchFolder) {
            getPresenter().clickFolderSwitch();
        } else if (v == mBtnPreview) {
            getPresenter().tryPreviewChecked();
        }
    }
}