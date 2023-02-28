package fr.unilim.stodolist

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.room.Room
import fr.unilim.stodolist.dao.TaskDao
import fr.unilim.stodolist.database.AppDatabase
import fr.unilim.stodolist.models.Task
import fr.unilim.stodolist.models.TaskStatus
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Créer une instance de la base de données
        val db = Room.databaseBuilder(
            applicationContext,
            AppDatabase::class.java, "task_database"
        ).build()

        // Créer une coroutine scope
        val coroutineScope = CoroutineScope(Dispatchers.IO)

        // Lancer une coroutine pour insérer une nouvelle tâche
        coroutineScope.launch {
            val newTask = Task(
                title = "Acheter du lait",
                description = "Acheter du lait pour le petit-déjeuner",
                dueDate = Date(),
                status = TaskStatus.TODO
            )

            db.taskDao().insertTask(newTask)

            // Récupérer toutes les tâches et les afficher dans la console
            val allTasks = db.taskDao().getAllTasks()
            for (task in allTasks) {
                Log.d("MainActivity", "Task: ${task.title}, ${task.description}")
            }

            // Supprimer la tâche ajoutée
            db.taskDao().deleteTask(newTask)
        }
    }
}

