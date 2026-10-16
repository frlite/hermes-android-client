package com.hermes.android.data

import android.content.Context
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File

data class Message(
    val role: String, // "user" | "assistant"
    val content: String,
    val timestamp: Long = System.currentTimeMillis()
)

data class Conversation(
    val id: String = java.util.UUID.randomUUID().toString(),
    val title: String = "新对话",
    val messages: MutableList<Message> = mutableListOf(),
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

class ConversationStore(context: Context) {
    private val gson = Gson()
    private val file = File(context.filesDir, "conversations.json")
    private var conversations: MutableList<Conversation> = mutableListOf()

    init {
        load()
    }

    private fun load() {
        if (file.exists()) {
            val type = object : TypeToken<MutableList<Conversation>>() {}.type
            conversations = gson.fromJson(file.readText(), type) ?: mutableListOf()
        }
    }

    private fun save() {
        file.writeText(gson.toJson(conversations))
    }

    fun getAll(): List<Conversation> = conversations.sortedByDescending { it.updatedAt }

    fun getById(id: String): Conversation? = conversations.find { it.id == id }

    fun create(): Conversation {
        val conv = Conversation()
        conversations.add(conv)
        save()
        return conv
    }

    fun addMessage(conversationId: String, message: Message) {
        val conv = conversations.find { it.id == conversationId } ?: return
        conv.messages.add(message)
        if (conv.title == "新对话" && message.role == "user") {
            conv.title = message.content.take(20)
        }
        conv.updatedAt = System.currentTimeMillis()
        save()
    }

    fun delete(id: String) {
        conversations.removeAll { it.id == id }
        save()
    }

    fun rename(id: String, newTitle: String) {
        val conv = conversations.find { it.id == id } ?: return
        conv.title = newTitle
        save()
    }
}
