package com.adgad.kboard;

/*
 * Copyright (C) 2015 Paul Burke
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

import android.content.Context;
import android.content.SharedPreferences;
import android.util.TypedValue;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

class RecyclerListAdapter extends RecyclerView.Adapter<ItemViewHolder> {

    private final List<UserKey> mItems = new ArrayList<UserKey>();
    private final ItemViewHolder.ItemClickListener mListener;
    private final SharedPreferences mPreferences;
    private int selectableItemBackground;
    private int defaultTextColor = 0xDE000000;
    private boolean themeResolved;

    public RecyclerListAdapter(ArrayList<UserKey> items, SharedPreferences sharedPrefs, ItemViewHolder.ItemClickListener listener) {
        mItems.addAll(items);
        mListener = listener;
        mPreferences = sharedPrefs;
    }

    @NonNull
    @Override
    public ItemViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_main, parent, false);
        return new ItemViewHolder(view, mListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemViewHolder holder, int position) {
        resolveTheme(holder.textView.getContext());
        UserKey item = mItems.get(position);
        holder.textView.setText(item.text);
        if (item.color != null) {
            holder.textView.setBackgroundColor(item.color);
            holder.textView.setTextColor(ColorContrast.textColorFor(item.color));
        } else {
            if (selectableItemBackground != 0) {
                holder.textView.setBackgroundResource(selectableItemBackground);
            }
            holder.textView.setTextColor(defaultTextColor);
        }
    }


    private void resolveTheme(Context context) {
        if (themeResolved) {
            return;
        }
        TypedValue value = new TypedValue();
        if (context.getTheme().resolveAttribute(android.R.attr.selectableItemBackground, value, true)) {
            selectableItemBackground = value.resourceId;
        }
        if (context.getTheme().resolveAttribute(android.R.attr.textColorPrimary, value, true)) {
            if (value.resourceId != 0) {
                defaultTextColor = ContextCompat.getColor(context, value.resourceId);
            } else if (value.type >= TypedValue.TYPE_FIRST_COLOR_INT && value.type <= TypedValue.TYPE_LAST_COLOR_INT) {
                defaultTextColor = value.data;
            }
        }
        themeResolved = true;
    }

    private void updateWords() {
        SharedPreferences.Editor editor = mPreferences.edit();
        editor.putString(KboardIME.Keys.STORAGE_KEY, KeyStore.toJson(mItems));
        editor.apply();
    }

    public void add(UserKey item) {
        mItems.add(item);
        notifyItemInserted(mItems.size());
        updateWords();
    }

    public void set(int position, UserKey item) {
        mItems.set(position, item);
        notifyItemChanged(position);
        updateWords();
    }

    public void addAll(Collection<UserKey> items) {
        int start = mItems.size();
        mItems.addAll(items);
        notifyItemRangeInserted(start, mItems.size());
        updateWords();
    }

    public void remove(int position) {
        mItems.remove(position);
        notifyItemRemoved(position);
        updateWords();
    }

    public UserKey get(int position) {
        return mItems.get(position);
    }

    public void clear() {
        int size = mItems.size();
        mItems.clear();
        notifyItemRangeChanged(0, size);
        updateWords();
    }

    public void swap(int fromPosition, int toPosition) {
        UserKey text = mItems.get(fromPosition);
        mItems.remove(fromPosition);
        mItems.add(toPosition, text);
        notifyItemMoved(fromPosition, toPosition);
        updateWords();
    }


    @Override
    public int getItemCount() {
        return mItems.size();
    }

}