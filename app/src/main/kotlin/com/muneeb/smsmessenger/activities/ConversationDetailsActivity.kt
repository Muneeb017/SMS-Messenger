package com.muneeb.smsmessenger.activities

import android.os.Bundle
import androidx.core.content.res.ResourcesCompat
import com.muneeb.smsmessenger.adapters.ContactsAdapter
import com.muneeb.smsmessenger.dialogs.RenameConversationDialog
import com.muneeb.smsmessenger.extensions.*
import com.muneeb.smsmessenger.helpers.THREAD_ID
import com.muneeb.smsmessenger.models.Conversation
import com.simplemobiletools.commons.extensions.applyColorFilter
import com.simplemobiletools.commons.extensions.getProperPrimaryColor
import com.simplemobiletools.commons.extensions.getProperTextColor
import com.simplemobiletools.commons.extensions.updateTextColors
import com.simplemobiletools.commons.helpers.NavigationIcon
import com.simplemobiletools.commons.helpers.ensureBackgroundThread
import com.simplemobiletools.commons.models.SimpleContact
import com.muneeb.smsmessenger.R
import kotlinx.android.synthetic.main.activity_conversation_details.*

class ConversationDetailsActivity : com.muneeb.smsmessenger.activities.SimpleActivity() {

    private var threadId: Long = 0L
    private var conversation: Conversation? = null
    private lateinit var participants: ArrayList<SimpleContact>

    override fun onCreate(savedInstanceState: Bundle?) {
        isMaterialActivity = true
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_conversation_details)

        updateMaterialActivityViews(conversation_details_coordinator, participants_recyclerview, useTransparentNavigation = true, useTopSearchMenu = false)
        setupMaterialScrollListener(participants_recyclerview, conversation_details_toolbar)

        threadId = intent.getLongExtra(THREAD_ID, 0L)
        ensureBackgroundThread {
            conversation = conversationsDB.getConversationWithThreadId(threadId)
            participants = if (conversation != null && conversation!!.isScheduled) {
                val message = messagesDB.getThreadMessages(conversation!!.threadId).firstOrNull()
                message?.participants ?: arrayListOf()
            } else {
                getThreadParticipants(threadId, null)
            }
            runOnUiThread {
                setupTextViews()
                setupParticipants()
            }
        }
    }

    override fun onResume() {
        super.onResume()
        setupToolbar(conversation_details_toolbar, NavigationIcon.Arrow)
        updateTextColors(conversation_details_holder)

        val primaryColor = getProperPrimaryColor()
        conversation_name_heading.setTextColor(primaryColor)
        members_heading.setTextColor(primaryColor)
    }

    private fun setupTextViews() {
        conversation_name.apply {
            ResourcesCompat.getDrawable(resources, R.drawable.ic_edit_vector, theme)?.apply {
                applyColorFilter(getProperTextColor())
                setCompoundDrawablesWithIntrinsicBounds(null, null, this, null)
            }

            text = conversation?.title
            setOnClickListener {
                RenameConversationDialog(this@ConversationDetailsActivity, conversation!!) { title ->
                    text = title
                    ensureBackgroundThread {
                        conversation = renameConversation(conversation!!, newTitle = title)
                    }
                }
            }
        }
    }

    private fun setupParticipants() {
        val adapter = ContactsAdapter(this, participants, participants_recyclerview) {
            val contact = it as SimpleContact
            val address = contact.phoneNumbers.first().normalizedNumber
            getContactFromAddress(address) { simpleContact ->
                if (simpleContact != null) {
                    startContactDetailsIntent(simpleContact)
                }
            }
        }
        participants_recyclerview.adapter = adapter
    }
}
