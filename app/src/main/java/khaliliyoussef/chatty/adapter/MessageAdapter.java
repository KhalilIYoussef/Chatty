package khaliliyoussef.chatty.adapter;

import android.content.Context;
import android.support.v7.widget.RecyclerView;

/**
 * Created by Khalil on 7/30/2017.
 */


import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;

import java.util.List;

import butterknife.BindView;
import butterknife.ButterKnife;
import khaliliyoussef.chatty.model.ChattyMessage;
import khaliliyoussef.chatty.R;


public class MessageAdapter extends RecyclerView.Adapter<MessageAdapter.MessageViewHolder> {


    private Context mContext;
    private List<ChattyMessage> mMessages;

    public MessageAdapter(final Context context, List<ChattyMessage> messages )
    {
        //get the context from the activity
        this.mContext = context;
        //get the passed arrayList
        this.mMessages = messages;
    }

    @Override
    public MessageViewHolder onCreateViewHolder(final ViewGroup parent, int viewType)
    {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.message_item, parent, false);

        return new MessageViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(final MessageViewHolder holder, final int position)
    {


        boolean isPhoto = mMessages.get(position).getPhotoUrl() != null;
        if (isPhoto)
        {
           holder.messageTextView.setVisibility(View.GONE);
            holder.photoImageView.setVisibility(View.VISIBLE);
            Glide.with(mContext)
                    .load(mMessages.get(position).getPhotoUrl())
                    .into(holder.photoImageView);
        } else
        {
            holder.messageTextView.setVisibility(View.VISIBLE);
            holder. photoImageView.setVisibility(View.GONE);
            holder.messageTextView.setText(mMessages.get(position).getText());
        }
        holder.authorTextView.setText(mMessages.get(position).getName());


    }

    @Override
    public int getItemCount()
    {
        if (mMessages == null) {
            return 0;
        }
        return mMessages.size();
    }

    class MessageViewHolder extends RecyclerView.ViewHolder
    {

         @BindView(R.id.photoImageView)ImageView photoImageView;
         @BindView(R.id.messageTextView)TextView messageTextView;
         @BindView(R.id.nameTextView)TextView authorTextView;

        public MessageViewHolder(final View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

        }

        }

    }

