@file:Suppress("DEPRECATION")

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
import fr.unilim.stodolist.viewmodel.AndroidTask
import fr.unilim.stodolist.viewmodel.TaskStatus
import fr.unilim.stodolist.viewmodel.TaskViewModel
import fr.unilim.stodolist.viewmodel.TaskViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

/**
 * @deprecated This Fragment is deprecated. The app now uses Compose UI from the shared module.
 * See [MainActivity] which uses the shared [fr.unilim.stodolist.ui.App] composable.
 * This file is kept for reference only.
 */
@Deprecated(
    message = "Use Compose UI from shared module instead. See MainActivity.",
    level = DeprecationLevel.WARNING
)
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

        // Use the new factory that creates the shared module dependencies
        val viewModelFactory = TaskViewModelFactory(requireActivity().application)
        taskViewModel = ViewModelProvider(this, viewModelFactory)[TaskViewModel::class.java]

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
        binding.btnSaveTask.isEnabled = false
        binding.btnPickDate.isEnabled = false
        binding.etTaskTitle.isEnabled = false

        val taskTitle = binding.etTaskTitle.text.toString().trim()

        if (taskTitle.isEmpty()) {
            Toast.makeText(
                requireContext(),
                getString(R.string.enter_task_title),
                Toast.LENGTH_SHORT
            ).show()
            binding.btnSaveTask.isEnabled = true
            binding.btnPickDate.isEnabled = true
            binding.etTaskTitle.isEnabled = true
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

        val taskDescription = binding.description.text.toString().trim()

        val task = AndroidTask(
            title = taskTitle,
            status = TaskStatus.TODO,
            description = taskDescription.ifEmpty { null },
            dueDate = dueDate
        )

        taskViewModel.insertTask(task)
        activity?.runOnUiThread {
            Toast.makeText(
                requireContext(),
                getString(R.string.task_added),
                Toast.LENGTH_SHORT
            ).show()
            taskViewModel.scheduleTaskNotification(requireContext(), task)
            activity?.onBackPressed()
        }
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

        // Empêcher la sélection d'une date antérieure à aujourd'hui
        datePickerDialog.datePicker.minDate = calendar.timeInMillis

        datePickerDialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
