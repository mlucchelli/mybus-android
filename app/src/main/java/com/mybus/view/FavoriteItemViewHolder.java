package com.mybus.view;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.mybus.R;
import com.mybus.listener.FavoriteListItemListener;

public class FavoriteItemViewHolder extends RecyclerView.ViewHolder {

    public TextView favName;
    public TextView favAddress;
    public ImageView favoriteEditIcon;
    public ImageView favoriteDeleteIcon;
    private FavoriteListItemListener mFavoriteEditListener;

    public FavoriteItemViewHolder(View itemView, FavoriteListItemListener favoriteEditListener) {
        super(itemView);
        this.mFavoriteEditListener = favoriteEditListener;
        favName = (TextView) itemView.findViewById(R.id.favorite_name);
        favAddress = (TextView) itemView.findViewById(R.id.favorite_address);
        favoriteEditIcon = (ImageView) itemView.findViewById(R.id.favorite_edit);
        favoriteDeleteIcon = (ImageView) itemView.findViewById(R.id.favorite_delete);
        favoriteDeleteIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFavoriteEditListener.onFavoriteItemDelete(getAdapterPosition());
            }
        });
        favoriteEditIcon.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mFavoriteEditListener.onFavoriteItemEdit(getAdapterPosition());
            }
        });
    }
}