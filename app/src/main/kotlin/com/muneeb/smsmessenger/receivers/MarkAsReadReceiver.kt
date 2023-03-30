package com.muneeb.smsmessenger.receivers

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.simplemobiletools.commons.extensions.notificationManager
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.muneeb.smsmessenger.extensions.conversationsDB
import com.muneeb.smsmessenger.extensions.markThreadMessagesRead
import com.muneeb.smsmessenger.extensions.updateUnreadCountBadge
import com.muneeb.smsmessenger.helpers.MARK_AS_READ
import com.muneeb.smsmessenger.helpers.THREAD_ID
import com.muneeb.smsmessenger.helpers.refreshMessages

class MarkAsReadReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        when (intent.action) {
            MARK_AS_READ -> {
                val threadId = intent.getLongExtra(THREAD_ID, 0L)
                context.notificationManager.cancel(threadId.hashCode())
                ensureBackgroundThread {
                    context.markThreadMessagesRead(threadId)
                    context.conversationsDB.markRead(threadId)
                    context.updateUnreadCountBadge(context.conversationsDB.getUnreadConversations())
                    refreshMessages()
                }
            }
        }
    }
}
