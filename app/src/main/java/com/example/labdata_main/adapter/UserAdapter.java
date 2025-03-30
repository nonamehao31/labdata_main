package com.example.labdata_main.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.labdata_main.R;
import com.example.labdata_main.model.User;

import java.util.List;

/**
 * 用户列表适配器
 * 用于显示单位内所有用户
 */
public class UserAdapter extends RecyclerView.Adapter<UserAdapter.UserViewHolder> {
    private List<User> users;
    private OnUserClickListener onUserClickListener;

    public interface OnUserClickListener {
        void onUserClick(User user, int position);
    }

    public UserAdapter(List<User> users) {
        this.users = users;
    }

    public void setOnUserClickListener(OnUserClickListener listener) {
        this.onUserClickListener = listener;
    }

    @NonNull
    @Override
    public UserViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_user, parent, false);
        return new UserViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull UserViewHolder holder, int position) {
        User user = users.get(position);
        
        // 设置用户信息
        holder.tvUserName.setText(user.getName());
        holder.tvUserEmail.setText(user.getEmail());
        
        // 设置用户类型
        boolean isAdmin = user.getUserType() == 1;
        holder.tvUserType.setText(isAdmin ? "管理员" : "实验员");
        
        // 根据用户类型设置标签背景颜色
        holder.tvUserType.setBackgroundResource(
            isAdmin ? R.drawable.rounded_tag_background : R.drawable.rounded_tag_background_secondary
        );

        // 设置点击事件
        holder.itemView.setOnClickListener(v -> {
            if (onUserClickListener != null) {
                onUserClickListener.onUserClick(user, position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return users != null ? users.size() : 0;
    }

    /**
     * 更新用户列表数据
     * @param newUsers 新的用户列表数据
     */
    public void updateUsers(List<User> newUsers) {
        this.users = newUsers;
        notifyDataSetChanged();
    }

    /**
     * 用户ViewHolder
     */
    static class UserViewHolder extends RecyclerView.ViewHolder {
        ImageView ivUserAvatar;
        TextView tvUserName;
        TextView tvUserEmail;
        TextView tvUserType;

        UserViewHolder(@NonNull View itemView) {
            super(itemView);
            ivUserAvatar = itemView.findViewById(R.id.ivUserAvatar);
            tvUserName = itemView.findViewById(R.id.tvUserName);
            tvUserEmail = itemView.findViewById(R.id.tvUserEmail);
            tvUserType = itemView.findViewById(R.id.tvUserType);
        }
    }
}
