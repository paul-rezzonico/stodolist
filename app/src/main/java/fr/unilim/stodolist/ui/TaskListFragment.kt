package fr.unilim.stodolist.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import fr.unilim.stodolist.R
import fr.unilim.stodolist.databinding.FragmentTaskListBinding
import androidx.navigation.fragment.findNavController
import fr.unilim.stodolist.db.TaskDatabase
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
