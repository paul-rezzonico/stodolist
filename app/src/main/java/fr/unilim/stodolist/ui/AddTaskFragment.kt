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
import fr.unilim.stodolist.database.TaskDatabase
import fr.unilim.stodolist.databinding.FragmentAddTaskBinding
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import fr.unilim.stodolist.repository.TaskRepository
import fr.unilim.stodolist.viewmodel.TaskViewModel
import fr.unilim.stodolist.viewmodel.TaskViewModelFactory
import io.ktor.client.*
import io.ktor.client.engine.cio.*
import io.ktor.client.features.json.*
import io.ktor.client.features.json.serializer.*
import io.ktor.client.request.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.serialization.json.*
import java.text.SimpleDateFormat
import java.util.*


private const val OPENAI_API_KEY = "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"
// sera supprimée dans quelques semaines

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
        binding.btnSaveTask.isEnabled = false
        binding.IADescriptionToggle.isEnabled = false
        binding.btnPickDate.isEnabled = false
        binding.etTaskTitle.isEnabled = false

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

        var resultText: String? = null

        if (binding.IADescriptionToggle.isChecked) {
            CoroutineScope(Dispatchers.IO).launch {
                val httpClient = HttpClient(CIO) {
                    install(JsonFeature) {
                        serializer = KotlinxSerializer(kotlinx.serialization.json.Json {
                            ignoreUnknownKeys = true
                        })
                    }
                }

                val data = buildJsonObject {
                    put("model", "text-davinci-003")
                    put(
                        "prompt",
                        "Title of the task: --- $taskTitle --- // 3 very shorts bullet points dividing the tasks to be done, in french, without numbering (use •): "
                    )
                    put("max_tokens", 200)
                }



                try {
                    val response: JsonObject =
                        httpClient.post("https://api.openai.com/v1/completions") {
                            headers {
                                append("Content-Type", "application/json")
                                append("Authorization", "Bearer $OPENAI_API_KEY")
                            }
                            body = data
                        }

                    resultText = response["choices"]
                        ?.jsonArray
                        ?.get(0)
                        ?.jsonObject
                        ?.get("text")
                        ?.jsonPrimitive
                        ?.content
                        ?.replace("\"", "")

                    println(resultText)
                } catch (e: Exception) {
                    e.printStackTrace()
                    resultText = "Indisponible"
                } finally {
                    httpClient.close()
                }

                val task = Task(
                    title = taskTitle,
                    status = TaskStatus.TODO,
                    description = resultText,
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
        } else {
            val task = Task(
                title = taskTitle,
                status = TaskStatus.TODO,
                description = resultText,
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
