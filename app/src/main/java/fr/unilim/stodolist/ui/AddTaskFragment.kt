package fr.unilim.stodolist.ui

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import fr.unilim.stodolist.R
import fr.unilim.stodolist.databinding.FragmentAddTaskBinding
import fr.unilim.stodolist.db.TaskDatabase
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.repository.TaskRepository
import fr.unilim.stodolist.viewmodel.TaskViewModel
import fr.unilim.stodolist.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class AddTaskFragment : Fragment() {

    private var _binding: FragmentAddTaskBinding? = null
    private val binding get() = _binding!!

    private lateinit var taskViewModel: TaskViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        // Ajouter le bouton retour dans la barre d'outils
        val actionBar = (requireActivity() as AppCompatActivity).supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(true)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> {
                // Si le bouton retour est cliqué, revenir en arrière
                activity?.onBackPressed()
                return true
            }
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddTaskBinding.inflate(inflater, container, false)

        val taskDatabase = TaskDatabase.getInstance(requireContext())
        val taskDao = taskDatabase.taskDao()
        val taskRepository = TaskRepository(taskDao)
        val viewModelFactory = TaskViewModelFactory(taskRepository)
        taskViewModel = ViewModelProvider(this, viewModelFactory).get(TaskViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.btnSaveTask.setOnClickListener {
            saveTask()
        }

        binding.btnPickDate.setOnClickListener {
            showDatePickerDialog()
        }
    }

    private fun saveTask() {
        val taskTitle = binding.etTaskTitle.text.toString().trim()

        if (taskTitle.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.enter_task_title),
                Toast.LENGTH_SHORT
            ).show()
            return
        }

        // Get the due date from the button text
        val dueDateString = binding.btnPickDate.text.toString()
        val dueDate: Date? = if (dueDateString != getString(R.string.selected_due_date)) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            dateFormat.parse(dueDateString)
        } else {
            null
        }

        val task = Task(title = taskTitle, status = TaskStatus.TODO, dueDate = dueDate)

        taskViewModel.insertTask(task)
        Toast.makeText(requireContext(), getString(R.string.task_added), Toast.LENGTH_SHORT).show()
        activity?.onBackPressed()
    }

    private fun showDatePickerDialog() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDayOfMonth)
                val taskDueDate = selectedDate.time
                binding.btnPickDate.text =
                    SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(taskDueDate)
            },
            year,
            month,
            day
        )
        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
