package com.agus.kiwariandroidtest

import android.app.Activity
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.item_message.view.*

class MessageAdapter(private val context: Context): RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val listMessage: MutableList<FriendlyMessage> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_message, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int {
        return listMessage.size
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if(holder is ViewHolder){
            holder.bindTo(listMessage[position])
        }
    }

    fun add(message: FriendlyMessage) {
        if (!this.listMessage.contains(message)) {
            this.listMessage.add(message)
            notifyItemInserted(listMessage.size - 1)
        }
    }


    fun removeAll() {
        for (i in this.listMessage.indices) {
            this.listMessage.removeAt(0)
            notifyItemRemoved(0)
        }
    }

    class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        fun bindTo(message: FriendlyMessage){
            with(itemView){
                messageTextView.text = message.text
                nameTextView.text = message.name
                dateTextView.text = message.date
            }
        }

    }


}