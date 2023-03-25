package fr.unilim.stodolist.ui

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import fr.unilim.stodolist.R
import fr.unilim.stodolist.databinding.FragmentTaskListBinding
import androidx.navigation.fragment.findNavController
import fr.unilim.stodolist.db.TaskDatabase
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.repository.TaskRepository
import fr.unilim.stodolist.viewmodel.TaskViewModel
import fr.unilim.stodolist.viewmodel.TaskViewModelFactory

class TaskListFragment : Fragment() {

    private var _binding: FragmentTaskListBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel
    private lateinit var taskListAdapter: TaskListAdapter

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
        }

        val taskListAdapter = TaskListAdapter()
        binding.recyclerView.adapter = taskListAdapter

        taskListAdapter.onTaskUpdated = { updatedTask ->
            if (updatedTask.status == TaskStatus.COMPLETED) {
                showCompletedTaskDialog()
            }
            taskViewModel.updateTask(updatedTask)
        }


        taskViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
            taskListAdapter.submitList(tasks)
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.fabAddTask.setOnClickListener {
            findNavController().navigate(R.id.action_taskListFragment_to_addTaskFragment)
        }

        taskViewModel.getAllTasks().observe(viewLifecycleOwner) { tasks ->
            tasks?.let {
                taskListAdapter.submitList(it)
            }
        }
    }

    private fun showCompletedTaskDialog() {
        val dialog = Dialog(requireContext())
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.emoji_dialog)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        dialog.show()

        // Ferme le Dialog après 2 secondes
        Handler(Looper.getMainLooper()).postDelayed({
            dialog.dismiss()
        }, 2000)
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
