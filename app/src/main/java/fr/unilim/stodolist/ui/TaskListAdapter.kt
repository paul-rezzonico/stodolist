@file:Suppress("DEPRECATION")

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
import fr.unilim.stodolist.viewmodel.AndroidTask
import fr.unilim.stodolist.viewmodel.TaskStatus
import java.text.SimpleDateFormat
import java.util.*

/**
 * @deprecated This adapter is deprecated. The app now uses Compose UI from the shared module.
 * See [MainActivity] which uses the shared [fr.unilim.stodolist.ui.App] composable.
 * This file is kept for reference only.
 */
@Deprecated(
    message = "Use Compose UI from shared module instead. See MainActivity.",
    level = DeprecationLevel.WARNING
)
class TaskListAdapter : ListAdapter<AndroidTask, TaskListAdapter.TaskViewHolder>(TaskDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val binding = ItemTaskBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return TaskViewHolder(binding)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val task = getItem(position)
        holder.bind(task)
    }

    inner class TaskViewHolder(private val binding: ItemTaskBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(task: AndroidTask) {

            binding.apply {

                tvTitle.text = task.title
                if (task.description != null) {
                    tvDescription.text = "${task.description}\n"
                    tvDescription.visibility = View.VISIBLE
                } else {
                    tvDescription.visibility = View.GONE
                }

                // Status is already computed in the AndroidTask
                val displayStatus = task.status

                tvStatus.text = when (displayStatus) {
                    TaskStatus.TODO -> "À faire"
                    TaskStatus.LATE -> "En retard"
                    TaskStatus.COMPLETED -> "Réalisée"
                }

                task.dueDate?.let { dueDate ->
                    tvDueDate.text =
                        "Date limite : " + SimpleDateFormat(
                            "dd/MM/yyyy",
                            Locale.getDefault()
                        ).format(dueDate)
                } ?: run {
                    tvDueDate.text = "Pas de date limite"
                }

                btnMarkAsCompleted.visibility = if (task.status != TaskStatus.COMPLETED) {
                    btnMarkAsCompleted.setOnClickListener {
                        val updatedTask = task.copy(status = TaskStatus.COMPLETED)
                        onTaskUpdated(updatedTask)
                    }
                    View.VISIBLE
                } else {
                    View.GONE
                }

                btnDeleteTask.setOnClickListener {
                    onTaskDeleted(task)
                }

                // Change the background color based on the task status
                itemView.setBackgroundColor(
                    ContextCompat.getColor(
                        itemView.context,
                        when (displayStatus) {
                            TaskStatus.COMPLETED -> R.color.purple_200
                            TaskStatus.LATE -> R.color.teal_700
                            else -> R.color.white
                        }
                    )
                )
            }
        }
    }

    var onTaskUpdated: (AndroidTask) -> Unit = {}
    var onTaskDeleted: (AndroidTask) -> Unit = {}

    class TaskDiffCallback : DiffUtil.ItemCallback<AndroidTask>() {
        override fun areItemsTheSame(oldItem: AndroidTask, newItem: AndroidTask): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: AndroidTask, newItem: AndroidTask): Boolean {
            return oldItem == newItem
        }
    }
}
