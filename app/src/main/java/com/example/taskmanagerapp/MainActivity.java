package com.example.taskmanagerapp;

import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageButton;
import android.annotation.SuppressLint;
import android.widget.Toast;
import androidx.recyclerview.widget.RecyclerView;
import android.database.sqlite.SQLiteDatabase;
import android.content.ContentValues;
import android.database.Cursor;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.appcompat.app.AlertDialog;
import java.util.Date;
import java.text.SimpleDateFormat;
import java.text.ParseException;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements Adapter.TaskActionListener {
    private static List<Task> TaskList;
    private Adapter adapter;
    private com.example.taskmanagerapp.DBHelper DBHelper;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        ImageButton AddTaskBtn = findViewById(R.id.add_button);

        DBHelper = new DBHelper(this);

        RecyclerView recyclerView = findViewById(R.id.recycler_view);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        TaskList = new ArrayList<>();

        InitTasks();


        adapter = new Adapter(TaskList, this);
        recyclerView.setAdapter(adapter);

        AddTaskBtn.setOnClickListener(v -> AddModDiag(null));
    }

    @Override
    public void onDeleteTask(Task task) {
        DeleteTask(task);
    }

    @Override
    public void onEditTask(Task task) {
        AddModDiag(task);
    }

    private void InitTasks() {
        SQLiteDatabase db = DBHelper.getReadableDatabase();
        Cursor cursor = db.query(com.example.taskmanagerapp.DBHelper.TABLE_TASKS, null, null, null, null, null, null);
        TaskList.clear();

        if (cursor != null && cursor.moveToFirst()) {
            do {
                int id = cursor.getInt(cursor.getColumnIndexOrThrow(com.example.taskmanagerapp.DBHelper.COLUMN_ID));
                String title = cursor.getString(cursor.getColumnIndexOrThrow(com.example.taskmanagerapp.DBHelper.COLUMN_TITLE));
                String description = cursor.getString(cursor.getColumnIndexOrThrow(com.example.taskmanagerapp.DBHelper.COLUMN_DESCRIPTION));
                String dueDate = cursor.getString(cursor.getColumnIndexOrThrow(com.example.taskmanagerapp.DBHelper.COLUMN_DUE_DATE));

                Task task = new Task(id, title, description, dueDate);
                TaskList.add(task);
            } while (cursor.moveToNext());
            cursor.close();
        }
        db.close();
    }

    private void AddModDiag(@Nullable Task task) {
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(this);
        LayoutInflater inflater = this.getLayoutInflater();
        View dialogView = inflater.inflate(R.layout.add_task, null);
        dialogBuilder.setView(dialogView);

        EditText editTextTitle = dialogView.findViewById(R.id.title_text);
        EditText editTextDescription = dialogView.findViewById(R.id.description_text);
        EditText editTextDueDate = dialogView.findViewById(R.id.date_text);
        Button btnSave = dialogView.findViewById(R.id.save_button);

        if (task != null) {
            editTextTitle.setText(task.getTitle());
            editTextDescription.setText(task.getDescription());
            editTextDueDate.setText(task.getDueDate());
        }

        AlertDialog alertDialog = dialogBuilder.create();

        btnSave.setOnClickListener(v -> {
            String title = editTextTitle.getText().toString().trim();
            String description = editTextDescription.getText().toString().trim();
            String dueDate = editTextDueDate.getText().toString().trim();

            if (title.isEmpty() || dueDate.isEmpty()) {
                Toast.makeText(MainActivity.this, "Please fill all the required fields!", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!DateValidityCheck(dueDate)) {
                Toast.makeText(MainActivity.this, "Invalid Date Format (use DD/MM/YYYY)", Toast.LENGTH_SHORT).show();
                return;
            }

            if (task == null) {
                Task newTask = new Task(0, title, description, dueDate);
                addTask(newTask);
            } else {
                task.setTitle(title);
                task.setDescription(description);
                task.setDueDate(dueDate);
                updateTask(task);
            }
            alertDialog.dismiss();
        });
        alertDialog.show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void addTask(Task task) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_TITLE, task.getTitle());
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_DUE_DATE, task.getDueDate());
        long id = db.insert(com.example.taskmanagerapp.DBHelper.TABLE_TASKS, null, values);

        task.setId((int) id);
        db.close();
        TaskList.add(task);
        adapter.notifyDataSetChanged();
    }

    private void updateTask(Task task) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_TITLE, task.getTitle());
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_DESCRIPTION, task.getDescription());
        values.put(com.example.taskmanagerapp.DBHelper.COLUMN_DUE_DATE, task.getDueDate());
        db.update(com.example.taskmanagerapp.DBHelper.TABLE_TASKS, values, com.example.taskmanagerapp.DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();

        int index = TaskList.indexOf(task);
        if (index != -1) {
            TaskList.set(index, task);
            adapter.notifyItemChanged(index);
        }
    }

    @SuppressLint("NotifyDataSetChanged")
    private void DeleteTask(Task task) {
        SQLiteDatabase db = DBHelper.getWritableDatabase();
        db.delete(com.example.taskmanagerapp.DBHelper.TABLE_TASKS, com.example.taskmanagerapp.DBHelper.COLUMN_ID + " = ?", new String[]{String.valueOf(task.getId())});
        db.close();
        TaskList.remove(task);
        adapter.notifyDataSetChanged();
    }

    private boolean DateValidityCheck(String dateStr) {
        SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yyyy", Locale.US);
        sdf.setLenient(false);
        try {
            Date date = sdf.parse(dateStr);
            return date != null;
        } catch (ParseException e) {
            return false;
        }
    }
}