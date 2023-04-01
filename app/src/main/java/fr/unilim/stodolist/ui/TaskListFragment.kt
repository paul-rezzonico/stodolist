package fr.unilim.stodolist.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import fr.unilim.stodolist.R
import fr.unilim.stodolist.database.TaskDatabase
import fr.unilim.stodolist.databinding.FragmentTaskListBinding
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.repository.TaskRepository
import fr.unilim.stodolist.viewmodel.TaskViewModel
import fr.unilim.stodolist.viewmodel.TaskViewModelFactory

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskListAdapter: TaskListAdapter

    private var currentStatusFilter: List<TaskStatus> =
        listOf(TaskStatus.TODO, TaskStatus.LATE, TaskStatus.COMPLETED)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentTaskListBinding.inflate(inflater, container, false)

        val taskDatabase = TaskDatabase.getInstance(requireContext())
        val taskDao = taskDatabase.taskDao()
        val taskRepository = TaskRepository(taskDao)
        val viewModelFactory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, viewModelFactory).get(TaskViewModel::class.java)

        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayHomeAsUpEnabled(false)

        setupRecyclerView()

        return binding.root
    }

    private fun setupRecyclerView() {
        taskListAdapter = TaskListAdapter()
        binding.recyclerView.apply {
            adapter = taskListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            taskListAdapter.onTaskDeleted = { deletedTask ->
                taskViewModel.deleteTask(deletedTask)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.task_deleted),
                    Toast.LENGTH_SHORT
                ).show()
                updateTaskList(currentStatusFilter)
            }
        }

        taskListAdapter.onTaskUpdated = { updatedTask ->
            if (updatedTask.status == TaskStatus.COMPLETED) {
                showCompletedTaskDialog()
            }
            taskViewModel.updateTask(updatedTask)
            updateTaskList(currentStatusFilter)
        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        setupBottomNavMenu()
        updateTaskList(currentStatusFilter)
    }

    private fun showCompletedTaskDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.emoji_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        // 1 seconde d'animation
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 1000)
    }

    private fun setupBottomNavMenu() {
        binding.bottomNavigationView.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.action_all -> {
                    updateTaskList(listOf(TaskStatus.TODO, TaskStatus.LATE, TaskStatus.COMPLETED))
                }
                R.id.action_todo -> {
                    updateTaskList(listOf(TaskStatus.TODO, TaskStatus.LATE))
                }
                R.id.action_late -> {
                    updateTaskList(listOf(TaskStatus.LATE))
                }
                R.id.action_completed -> {
                    updateTaskList(listOf(TaskStatus.COMPLETED))
                }
            }
            true
        }
    }


    private fun updateTaskList(statusFilter: List<TaskStatus>) {
        currentStatusFilter = statusFilter
        taskViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
            val filteredTasks = tasks.filter { task -> statusFilter.contains(task.status) }
            taskListAdapter.submitList(filteredTasks)
        }
        binding.recyclerView.apply {
            adapter = taskListAdapter
            layoutManager = LinearLayoutManager(requireContext())
            taskListAdapter.onTaskDeleted = { deletedTask ->
                taskViewModel.deleteTask(deletedTask)
                Toast.makeText(
                    requireContext(),
                    getString(R.string.task_deleted),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
