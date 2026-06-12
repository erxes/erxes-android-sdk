package com.erxes.messenger.data.model

/**
 * Ticket pipeline configuration returned (top-level) by `widgetsMessengerConnect`.
 * Drives whether the Tickets feature is shown and which fields the create form exposes.
 * Mirrors the iOS `TicketConfig`.
 */
data class TicketConfig(
    val id: String,
    val name: String,
    val pipelineId: String,
    val channelId: String?,
    val selectedStatusId: String,
    val parentId: String?,
    val formFields: TicketFormFields = TicketFormFields(),
)

data class TicketFormFields(
    val name: TicketFieldConfig? = null,
    val description: TicketFieldConfig? = null,
    val attachment: TicketFieldConfig? = null,
    val tags: TicketFieldConfig? = null,
)

data class TicketFieldConfig(
    val isShow: Boolean,
    val label: String? = null,
    val placeholder: String? = null,
    val order: Int = 99,
)
