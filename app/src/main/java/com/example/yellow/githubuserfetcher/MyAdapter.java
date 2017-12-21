package com.example.yellow.githubuserfetcher;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Yellow on 2017-12-21.
 */

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder>{
    private List<String> names;
    private List<String> ids;
    private List<String> blogs;

    private OnItemClickListener onItemClickListener;
    //private AdapterView.OnItemLongClickListener

    public MyAdapter(){
        names=new ArrayList<>();
        ids=new ArrayList<>();
        blogs=new ArrayList<>();
/*            names.add("ABCDEFG");
            ids.add("1234567890");
            blogs.add("www.blog.com");*/
    }
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType){
        View view= LayoutInflater.from(viewGroup.getContext())
                .inflate(R.layout.recyclerview_item,viewGroup,false);
        return new MyViewHolder(view);
    }

    public interface OnItemClickListener{
        void onItemClick(View view,int position);
        void onItemLongClick(View view,int position);
    }
    public void setOnItemClickListener(OnItemClickListener clickListener){
        onItemClickListener=clickListener;
    }

    @Override
    public void onBindViewHolder(final MyViewHolder viewHolder, int pos){
        viewHolder.nametext.setText(names.get(pos));
        viewHolder.idtext.setText(ids.get(pos));
        viewHolder.blogtext.setText(blogs.get(pos));

        //若设置了回调，就设置点击事件
        if(onItemClickListener!=null){
            viewHolder.itemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int pos=viewHolder.getLayoutPosition();
                    onItemClickListener.onItemClick(viewHolder.itemView,pos);
                }
            });
            viewHolder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
                @Override
                public boolean onLongClick(View view) {
                    int pos=viewHolder.getLayoutPosition();
                    onItemClickListener.onItemLongClick(viewHolder.itemView,pos);
                    return false;
                }
            });
        }
    }
    @Override
    public int getItemCount(){
        return ids.size();
    }
    public void add(String name,String id,String blog){
        names.add(name);
        ids.add(id);
        blogs.add(blog);
        notifyItemInserted(0);
    }
    public void remove(int pos){
        names.remove(pos);
        ids.remove(pos);
        blogs.remove(pos);
        notifyItemRemoved(pos);
    }
    public void clear(){
        names.clear();
        ids.clear();
        blogs.clear();
        notifyDataSetChanged();
    }
    public String getName(int pos){
        return names.get(pos);
    }
    public String getId(int pos){
        return ids.get(pos);
    }
    public String getBlog(int pos){
        return blogs.get(pos);
    }


    public class MyViewHolder extends RecyclerView.ViewHolder {
        public TextView nametext;
        public TextView idtext;
        public TextView blogtext;

        public MyViewHolder(View view){
            super(view);
            nametext=view.findViewById(R.id.user_display_name);
            idtext=view.findViewById(R.id.id_number);
            blogtext=view.findViewById(R.id.blog_address);
        }
    }
}
