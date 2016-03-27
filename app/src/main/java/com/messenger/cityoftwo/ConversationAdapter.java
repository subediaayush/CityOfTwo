package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Created by Aayush on 1/15/2016.
 */
public class ConversationAdapter extends RecyclerView.Adapter<ConversationAdapter.ConversationListViewHolder> {

    private List<Conversation> ConversationList;
    private Context context;

    public ConversationAdapter(Context context, List<Conversation> conversationList) {
        ConversationList = conversationList;
        this.context = context;
    }

    @Override
    public int getItemViewType(int position) {
        return ConversationList.get(position).getType();
    }


    @Override
    public ConversationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(context);

        View view;

        if (viewType == CityOfTwo.RECEIVED)
            view = li.inflate(R.layout.layout_msg_received, null);
        else if (viewType == CityOfTwo.SENT)
            view = li.inflate(R.layout.layout_msg_sent, null);
        else if (viewType == CityOfTwo.START)
            view = li.inflate(R.layout.layout_msg_start, null);
        else
            view = li.inflate(R.layout.layout_msg_end, null);

        return new ConversationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ConversationListViewHolder holder, final int position) {
        Conversation currentConv = ConversationList.get(position);

        if (currentConv.getType() != CityOfTwo.START &&
                currentConv.getType() != CityOfTwo.END) {
            final String BufferText = currentConv.getText();

            String Time = new SimpleDateFormat("hh:mm a").format(currentConv.getTime());

            holder.textContainer.setText(BufferText);
            holder.dateContainer.setText(Time);
            holder.textContainer.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    toggleVisibility(holder.dateContainer);
                    ConversationAdapter.this.notifyItemChanged(position);
                }
            });
        }
    }

    private void toggleVisibility(View view) {
        if (view.getVisibility() == View.VISIBLE)
            view.setVisibility(View.GONE);
        else
            view.setVisibility(View.VISIBLE);
    }

    @Override
    public int getItemCount() {
        return ConversationList.size();
    }

    class ConversationListViewHolder extends RecyclerView.ViewHolder {
        TextView dateContainer;
        TextView textContainer;

        public ConversationListViewHolder(View itemView) {
            super(itemView);
            textContainer = (TextView) itemView.findViewById(R.id.text);
            dateContainer = (TextView) itemView.findViewById(R.id.time);
        }
    }
}
