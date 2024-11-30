package com.example.contactapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ChatAdapter(
    private val messagesList: List<Message>,
    private val userId: Int
) : RecyclerView.Adapter<ChatAdapter.MessageViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = messagesList[position]

        // Si el mensaje fue enviado por el usuario actual (sender_id = userId)
        if (message.senderId == userId) {
            // Mensaje enviado, mostrar en el lado derecho
            holder.sentMessage.visibility = View.VISIBLE
            holder.receivedMessage.visibility = View.GONE
            holder.sentMessage.text = message.messageText
        } else {
            // Mensaje recibido, mostrar en el lado izquierdo
            holder.receivedMessage.visibility = View.VISIBLE
            holder.sentMessage.visibility = View.GONE
            holder.receivedMessage.text = message.messageText
        }
    }

    override fun getItemCount(): Int {
        return messagesList.size
    }

    class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val receivedMessage: TextView = itemView.findViewById(R.id.receivedMessage)
        val sentMessage: TextView = itemView.findViewById(R.id.sentMessage)
    }
}



