package com.messenger.cityoftwo; /**
 * Copyright (C) 2015 Wasabeef
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import android.support.v4.view.ViewCompat;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.animation.OvershootInterpolator;

import jp.wasabeef.recyclerview.animators.BaseItemAnimator;

public class OvershootInUpAnimator extends BaseItemAnimator {

    private final float mTension;

    public OvershootInUpAnimator() {
        mTension = 2.0f;
    }

    public OvershootInUpAnimator(float mTension) {
        this.mTension = mTension;
    }

    @Override
    protected void preAnimateAddImpl(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        View view = holder.itemView;
        ViewCompat.setScaleX(view, .01f);
        ViewCompat.setScaleY(view, .01f);
        try {
            if (viewType == 1)
                ViewCompat.setPivotX(view, view.getWidth());
            else if (viewType == 0)
                ViewCompat.setPivotX(view, 0);
        } catch (Exception e) {
            Log.e(e.toString, "Cannot determine the view type of the view");
        }
        ViewCompat.setPivotY(view, view.getHeight());
    }

    @Override
    protected void preAnimateRemoveImpl(RecyclerView.ViewHolder holder) {
        int viewType = holder.getItemViewType();
        View view = holder.itemView;
        ViewCompat.setScaleX(view, 1);
        ViewCompat.setScaleY(view, 1);
        try {
            if (viewType == 1)
                ViewCompat.setPivotX(view, view.getWidth());
            else if (viewType == 0)
                ViewCompat.setPivotX(view, 0);
        } catch (Exception e) {
            Log.e(e.toString, "Cannot determine the view type of the view");
        }
        ViewCompat.setPivotY(view, view.getHeight());
    }

    @Override
    protected void animateAddImpl(final RecyclerView.ViewHolder holder) {
        ViewCompat.animate(holder.itemView)
                .scaleX(1)
                .scaleY(1)
                .alpha(1)
                .setDuration(getAddDuration())
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
                .setDuration(getRemoveDuration())
                .setListener(new DefaultRemoveVpaListener(holder))
                .start();
    }
}
