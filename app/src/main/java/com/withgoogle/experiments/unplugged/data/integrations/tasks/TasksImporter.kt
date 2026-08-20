package com.withgoogle.experiments.unplugged.data.integrations.tasks

import com.google.api.client.googleapis.auth.oauth2.GoogleCredential
import com.google.api.client.http.javanet.NetHttpTransport
import com.google.api.client.json.gson.GsonFactory
import com.google.api.services.tasks.Tasks
import com.withgoogle.experiments.unplugged.model.TaskItem
import com.withgoogle.experiments.unplugged.model.TaskList
import timber.log.Timber
import java.time.Instant

class TasksImporter(val token: String) {
    val service by lazy {
        Timber.d("Tasks token: $token")

        val credential = GoogleCredential().setAccessToken(token)
        Tasks.Builder(NetHttpTransport(), GsonFactory.getDefaultInstance(), credential)
            .setApplicationName("Paper phone")
            .build()
    }

    fun taskItems(taskListId: String): List<TaskItem> {
        val tasksModel = service.tasks().list(taskListId).execute()

        val tasks = tasksModel.items?.map { task ->
            // task.due is a String now in the newer tasks API
            TaskItem(task.title, task.due?.let { Instant.parse(it) })
        } ?: emptyList()

        Timber.d(tasks.toString())

        return tasks
    }

    fun taskList(): List<TaskList> {
        val taskListModel = service.tasklists().list().execute()

        val taskLists = taskListModel.items.map { taskList ->
            TaskList(taskList.id, taskList.title, taskItems(taskList.id))
        }

        Timber.d(taskLists.toString())

        return taskLists
    }
}

