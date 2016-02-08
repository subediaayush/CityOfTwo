package com.messenger.cityoftwo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

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
        Conversation currentConv = ConversationList.get(position);

        if (currentConv.getType() == Conversation.Type.RECEIVED)
            return 1;
        else if (currentConv.getType() == Conversation.Type.SENT)
            return 0;

        return 0;
    }


    @Override
    public ConversationListViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater li = LayoutInflater.from(context);

        View view = null;

        if (viewType == 1)
            view = li.inflate(R.layout.layout_msg_received, null);
        else if (viewType == 0)
            view = li.inflate(R.layout.layout_msg_sent, null);

        return new ConversationListViewHolder(view);
    }

    @Override
    public void onBindViewHolder(ConversationListViewHolder holder, int position) {
        Conversation currentConv = ConversationList.get(position);
        final String BufferText = currentConv.getText();

        holder.TextContainer.setText(BufferText);
        holder.BackgroundView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // TODO Implement click based event
//                Toast.makeText(context, BufferText, Toast.LENGTH_SHORT).show();
            }
        });
    }

    public void addItem(Conversation conv){
        ConversationList.add(conv);
        notifyItemInserted(getItemCount());
    }

    @Override
    public int getItemCount() {
        return ConversationList.size();
    }

    class ConversationListViewHolder extends RecyclerView.ViewHolder {
        TextView TextContainer;
        View BackgroundView;

        public ConversationListViewHolder(View itemView) {
            super(itemView);
            TextContainer = (TextView) itemView.findViewById(R.id.text);
            BackgroundView = itemView;
        }
    }
}
