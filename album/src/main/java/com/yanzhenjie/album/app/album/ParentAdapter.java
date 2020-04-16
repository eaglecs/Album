package com.yanzhenjie.album.app.album;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.v7.widget.AppCompatTextView;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.yanzhenjie.album.AlbumFile;
import com.yanzhenjie.album.AlbumFolder;
import com.yanzhenjie.album.R;
import com.yanzhenjie.album.api.widget.Widget;
import com.yanzhenjie.album.app.Contract;
import com.yanzhenjie.album.impl.OnCheckedClickListener;
import com.yanzhenjie.album.impl.OnItemClickListener;
import com.yanzhenjie.album.widget.divider.Api21ItemDivider;

import java.util.ArrayList;

import static com.yanzhenjie.album.app.album.ParentAdapter.ParentViewHolder;

public class ParentAdapter extends RecyclerView.Adapter<ParentViewHolder> {
    private AlbumFolder albumFolder;
    private Context context;
    private int column = 1;
    private Widget widget;
    private boolean hasCamera;
    private int choiceMode;
    private Contract.AlbumPresenter presenter;

    public ParentAdapter(Context context, int column, Widget widget, boolean hasCamera, int choiceMode, Contract.AlbumPresenter presenter) {
        this.context = context;
        this.column = column;
        this.widget = widget;
        this.hasCamera = hasCamera;
        this.choiceMode = choiceMode;
        this.presenter = presenter;
    }

    public void setAlbumFolder(AlbumFolder albumFolder) {
        this.albumFolder = albumFolder;
    }

    @NonNull
    @Override
    public ParentViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        return new ParentViewHolder(LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.album_parent_item, viewGroup, false));
    }

    @Override
    public void onBindViewHolder(@NonNull ParentViewHolder viewHolder, int position) {
        String title;
        ArrayList<AlbumFile> albumFiles;
        boolean isHasCamera;
        final boolean isSuggestPhoto = position == 0 && !albumFolder.getAlbumFilesSuggest().isEmpty();
        if (position == 0) {
            if (albumFolder.getAlbumFilesSuggest().isEmpty()) {
                isHasCamera = hasCamera;
                albumFiles = albumFolder.getAlbumFiles();
                title = context.getResources().getString(R.string.album_recent);
            } else {
                isHasCamera = false;
                albumFiles = albumFolder.getAlbumFilesSuggest();
                title = context.getResources().getString(R.string.album_text_suggest);
            }
        } else {
            isHasCamera = hasCamera;
            albumFiles = albumFolder.getAlbumFiles();
            title = context.getResources().getString(R.string.album_recent);
        }
        viewHolder.tvTitle.setText(title);
        GridLayoutManager mLayoutManager = new GridLayoutManager(context, column, getOrientation(), false);
        viewHolder.rvAlbum.setLayoutManager(mLayoutManager);
//        viewHolder.rvAlbum.addItemDecoration(new Api21ItemDivider(Color.TRANSPARENT, 5, 5));
        AlbumAdapter mAdapter = new AlbumAdapter(context, isHasCamera, choiceMode, widget.getMediaItemCheckSelector());
        mAdapter.setAddClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                presenter.clickCamera(view);
            }
        });
        mAdapter.setCheckedClickListener(new OnCheckedClickListener() {
            @Override
            public void onCheckedClick(CompoundButton button, int position) {

                presenter.tryCheckItem(button, position, isSuggestPhoto);
            }
        });
        mAdapter.setItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(View view, int position) {
                presenter.tryPreviewItem(position);
            }
        });
        viewHolder.rvAlbum.setAdapter(mAdapter);
        mAdapter.setAlbumFiles(albumFiles);
        mAdapter.notifyDataSetChanged();
    }

    @RecyclerView.Orientation
    private int getOrientation() {
        Configuration config = context.getResources().getConfiguration();
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
    public int getItemCount() {
        if (albumFolder == null) {
            return 0;
        } else {
            if (albumFolder.getAlbumFilesSuggest().isEmpty()) {
                return 1;
            } else {
                return 2;
            }
        }
    }

    public void notifyInsertItem(AlbumFile albumFile) {
        if (albumFolder != null) {
            albumFolder.addAlbumFile(0, albumFile);
            notifyDataSetChanged();
        }
    }

    static class ParentViewHolder extends RecyclerView.ViewHolder {
        private AppCompatTextView tvTitle;
        private RecyclerView rvAlbum;

        public ParentViewHolder(@NonNull View itemView) {
            super(itemView);
            tvTitle = itemView.findViewById(R.id.tvTitle);
            rvAlbum = itemView.findViewById(R.id.rvAlbum);
        }
    }
}
