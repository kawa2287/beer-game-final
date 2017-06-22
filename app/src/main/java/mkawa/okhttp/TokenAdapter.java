package mkawa.okhttp;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.widget.ImageView;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.vstechlab.easyfonts.EasyFonts;

import java.awt.font.TextAttribute;
import java.util.List;

/**
 * Created by mattkawahara on 8/23/16.
 */
public class TokenAdapter extends RecyclerView.Adapter<TokenAdapter.MyViewHolder>{

    private List<Token> tokenList;

    public class MyViewHolder extends RecyclerView.ViewHolder{
        public ImageView name;
        public TextView header;

        public MyViewHolder(View view) {
            super(view);
            name = (ImageView) view.findViewById(R.id.tokenSlide);
            name.setPadding(15,15,15,15);
            header = (TextView) view.findViewById(R.id.pointTitle);
            header.setTypeface(EasyFonts.ostrichRegular(view.getContext()));
            header.setTextSize(20f);

        }
    }

    public TokenAdapter(List<Token> tokenList){
        this.tokenList = tokenList;
    }

    @Override
    public MyViewHolder onCreateViewHolder (ViewGroup parent, int viewType){
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.token_slide, parent, false);

        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        Token token = tokenList.get(position);

        holder.name.setImageResource(token.getName());
        holder.header.setText(token.getTokenTitle());
    }

    @Override
    public int getItemCount(){
        return tokenList.size();
    }
}
