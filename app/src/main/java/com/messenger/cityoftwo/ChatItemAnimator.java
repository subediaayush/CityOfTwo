package com.messenger.cityoftwo; /**
 * Copyright (C) 2015 Wasabeef
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.os.Handler;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.AccelerateInterpolator;

import jp.wasabeef.recyclerview.animators.BaseItemAnimator;

public class ChatItemAnimator extends BaseItemAnimator {

    private final float mTension;
    private final int mAddDuration;
    private final int mRemoveDuration;

    private final Handler holderRestoreHandler;

    public ChatItemAnimator() {
        this(2f, 200);
    }

    public ChatItemAnimator(float mTension, int mDuration) {
        this(mTension, mDuration, mDuration);
    }

    public ChatItemAnimator(float mTension, int mAddDuration, int mRemoveDuration) {
        this.mTension = mTension;
        this.mAddDuration = mAddDuration;
        this.mRemoveDuration = mRemoveDuration;
        holderRestoreHandler = new Handler();
    }

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT ||
                (viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE ||
                (viewType & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
            View view = holder.itemView;
            ViewCompat.setTranslationY(view, view.getHeight());
        }
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        if ((viewType & CityOfTwo.FLAG_TEXT) == CityOfTwo.FLAG_TEXT ||
                (viewType & CityOfTwo.FLAG_PROFILE) == CityOfTwo.FLAG_PROFILE ||
                (viewType & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
            final View view = holder.itemView;
            ViewCompat.animate(view)
                    .translationY(0)
                    .setDuration(mAddDuration)
                    .setInterpolator(new AccelerateInterpolator())
                    .setListener(new DefaultAddVpaListener(holder))
                    .start();
        }
    }

    @Override
    protected void animateRemoveImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .scaleX(.1f)
                .scaleY(.1f)
                .alpha(0)
                .setDuration(mRemoveDuration)
                .setListener(new DefaultRemoveVpaListener(holder))
                .start();
    }
}
