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

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import jp.wasabeef.recyclerview.animators.BaseItemAnimator;

public class ChatItemAnimator extends BaseItemAnimator {

    private final float mTension;
    private final int mAddDuration;
    private final int mRemoveDuration;

    public ChatItemAnimator() {
        mTension = 2.0f;
        mAddDuration = 200;
        mRemoveDuration = 200;
    }

    public ChatItemAnimator(float mTension, int mDuration) {
        this.mTension = mTension;
        this.mAddDuration = mDuration;
        this.mRemoveDuration = mDuration;
    }

    public ChatItemAnimator(float mTension, int mAddDuration, int mRemoveDuration) {
        this.mTension = mTension;
        this.mAddDuration = mAddDuration / 2;
        this.mRemoveDuration = mRemoveDuration;
    }

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        View view = holder.itemView;

        try {
            if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
                ViewCompat.setPivotX(view, 0);
                ViewCompat.setPivotY(view, 0);

                ViewCompat.setScaleX(view, .01f);
                ViewCompat.setScaleY(view, .01f);
            } else if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
                ViewCompat.setPivotX(view, view.getWidth());
                ViewCompat.setPivotY(view, view.getHeight());

                ViewCompat.setScaleX(view, .01f);
                ViewCompat.setScaleY(view, .01f);
            } else if ((viewType & CityOfTwo.FLAG_AD) == CityOfTwo.FLAG_AD) {
                ViewCompat.setTranslationY(view, view.getHeight());
            }
        } catch (Exception e) {
            Log.e(e.toString(), "Cannot determine the view type of the view");
        }
    }

    @Override
    protected void preAnimateRemoveImpl(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        View view = holder.itemView;
        try {
            if ((viewType & CityOfTwo.FLAG_RECEIVED) == CityOfTwo.FLAG_RECEIVED) {
                ViewCompat.setPivotX(view, 0);
                ViewCompat.setPivotY(view, 0);
            } else if ((viewType & CityOfTwo.FLAG_SENT) == CityOfTwo.FLAG_SENT) {
                ViewCompat.setPivotX(view, view.getWidth());
                ViewCompat.setPivotY(view, view.getHeight());
            }
        } catch (Exception e) {
            Log.e(e.toString(), "Cannot determine the view type of the view");
        }
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .translationX(0)
                .translationY(0)
                .setDuration(mAddDuration)
                .setInterpolator(new OvershootInterpolator(mTension))
                .setListener(new DefaultAddVpaListener(holder))
                .start();
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
