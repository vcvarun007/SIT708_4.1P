package com.example.taskmanagerapp;

import android.view.LayoutInflater;
import android.view.View;
import android.annotation.SuppressLint;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.TextView;
import androidx.annotation.NonNull;

public class Adapter extends RecyclerView.Adapter<Adapter.TaskViewHolder> {
    private List<Task> taskList;
    private static TaskActionListener taskActionListener;

    public Adapter(List<Task> taskList, TaskActionListener taskActionListener) {
        this.taskList = taskList;
        this.taskActionListener = taskActionListener;
    }

    @NonNull
    @Override
    public TaskViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.task_item, parent, false);
        return new TaskViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull TaskViewHolder holder, int position) {
        Task task = taskList.get(position);
        holder.bind(task);
    }

    @Override
    public int getItemCount() {
        return taskList.size();
    }

    public interface TaskActionListener {
        void onDeleteTask(Task task);
        void onEditTask(Task task);
    }

    public static class TaskViewHolder extends RecyclerView.ViewHolder {
        private TextView textViewTaskTitle;
        private TextView textViewDueDate;
        private ImageView imageViewMoreOptions;

        public TaskViewHolder(@NonNull View itemView) {
            super(itemView);
            textViewTaskTitle = itemView.findViewById(R.id.textViewTaskTitle);
            textViewDueDate = itemView.findViewById(R.id.textViewDueDate);
            imageViewMoreOptions = itemView.findViewById(R.id.imageViewMoreOptions);
        }

        @SuppressLint("NonConstantResourceId")
        public void bind(Task task) {
            textViewTaskTitle.setText(task.getTitle());
            textViewDueDate.setText("Due Date: " + task.getDueDate());

            imageViewMoreOptions.setOnClickListener(v -> {
                PopupMenu popMenu = new PopupMenu(v.getContext(), v);
                popMenu.inflate(R.menu.delete_menu);
                popMenu.setOnMenuItemClickListener(item -> {
                    if (item.getItemId() == R.id.menu_delete) {
                        taskActionListener.onDeleteTask(task);
                        return true;
                    }
                    return false;
                });
                popMenu.show();
            });
            itemView.setOnClickListener(v -> {
                taskActionListener.onEditTask(task);
            });
        }
    }
}