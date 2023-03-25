package fr.unilim.stodolist.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import fr.unilim.stodolist.R
import fr.unilim.stodolist.databinding.ItemTaskBinding
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

class TaskListAdapter : ListAdapter<Task, TaskListAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(task: Task) {
            binding.apply {
                tvTitle.text = task.title
                tvStatus.text = when (task.status) {
                    TaskStatus.TODO -> "À faire"
                    TaskStatus.LATE -> "En retard"
                    TaskStatus.COMPLETED -> "Réalisée"
                }
                task.dueDate?.let { dueDate ->
                    tvDueDate.text = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(dueDate);
                } ?: run {
                    tvDueDate.text = "Pas de date limite"
                }

                if (task.status == TaskStatus.COMPLETED) {
                    btnMarkAsCompleted.visibility = View.GONE
                } else {
                    btnMarkAsCompleted.setOnClickListener {
                        val updatedTask = task.copy(status = TaskStatus.COMPLETED)
                        onTaskUpdated(updatedTask)
                    }
                }

                // Change the background color based on the task status
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        if (task.status == TaskStatus.COMPLETED) R.color.purple_200 else R.color.white
                    )
                )
            }
        }
    }

    var onTaskUpdated: (Task) -> Unit = {}

    class TaskDiffCallback : DiffUtil.ItemCallback<Task>() {
        override fun areItemsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Task, newItem: Task): Boolean {
            return oldItem == newItem
        }
    }
}
