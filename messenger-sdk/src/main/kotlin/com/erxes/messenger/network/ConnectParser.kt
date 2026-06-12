package com.erxes.messenger.network

import com.erxes.messenger.data.model.ConnectResponse
import com.erxes.messenger.data.model.GreetingMessages
import com.erxes.messenger.data.model.MessengerData
import com.erxes.messenger.data.model.OnlineHour
import com.erxes.messenger.data.model.SocialLinks
import com.erxes.messenger.data.model.TicketConfig
import com.erxes.messenger.data.model.TicketFieldConfig
import com.erxes.messenger.data.model.TicketFormFields
import com.erxes.messenger.data.model.UiOptions
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.contentOrNull

/**
 * Parses the `widgetsMessengerConnect` payload into a [ConnectResponse].
 * Pure function (no I/O) so it can be unit-tested against captured JSON.
 * Mirrors `AppViewModel.parseConnectResponse` (iOS).
 *
 * Real API shape notes:
 *  - `uiOptions.primary.DEFAULT` carries the hex color (not `uiOptions.color`).
 *  - `messages.greetings.{title,message}` feed the hero header.
 */
object ConnectParser {

    fun parse(data: JsonObject?, fallbackIntegrationId: String): ConnectResponse {
        val ui = data?.obj("uiOptions")
        val md = data?.obj("messengerData")
        val msgs = md?.obj("messages")

        val primary = ui?.obj("primary")
        val primaryHex = primary?.str("DEFAULT")
            ?: primary?.str("default")
            ?: ui?.str("color")

        val uiOptions = UiOptions(
            color = primaryHex,
            textColor = ui?.str("textColor"),
            backgroundColor = ui?.str("backgroundColor"),
            wallpaper = ui?.str("wallpaper"),
            logo = ui?.str("logo"),
        )

        val greetings = msgs?.obj("greetings")
        val onlineHours = (md?.arr("onlineHours")).orEmpty().mapNotNull { el ->
            val h = el as? JsonObject ?: return@mapNotNull null
            val day = h.str("day") ?: return@mapNotNull null
            val from = h.str("from") ?: return@mapNotNull null
            val to = h.str("to") ?: return@mapNotNull null
            OnlineHour(day, from, to)
        }

        val links = md?.obj("links")
        val socialLinks = SocialLinks(
            facebook = links?.str("facebook"),
            instagram = links?.str("instagram"),
            twitter = links?.str("twitter") ?: links?.str("x"),
            youtube = links?.str("youtube"),
            linkedin = links?.str("linkedin"),
            discord = links?.str("discord"),
            github = links?.str("github"),
        )

        val messengerData = MessengerData(
            supporterIds = (md?.arr("supporterIds")).orEmpty().mapNotNull { (it as? JsonPrimitive)?.contentOrNull },
            notifyCustomer = md?.bool("notifyCustomer") ?: false,
            availabilityMethod = md?.str("availabilityMethod"),
            isOnline = md?.bool("isOnline") ?: false,
            onlineHours = onlineHours,
            timezone = md?.str("timezone"),
            messages = GreetingMessages(
                greetTitle = greetings?.str("title"),
                greet = greetings?.str("message"),
                away = msgs?.str("away"),
                thank = msgs?.str("thank"),
                welcome = msgs?.str("welcome"),
            ),
            links = socialLinks,
            knowledgeBaseTopicId = md?.str("knowledgeBaseTopicId")?.takeIf { it.isNotEmpty() },
            responseRate = md?.str("responseRate"),
            // ticketConfig lives at the TOP LEVEL of widgetsMessengerConnect, not in messengerData.
            ticketConfig = parseTicketConfig(data?.obj("ticketConfig")),
            requireAuth = md?.bool("requireAuth") ?: false,
            showChat = md?.bool("showChat") ?: true,
            showLauncher = md?.bool("showLauncher") ?: true,
            forceLogoutWhenResolve = md?.bool("forceLogoutWhenResolve") ?: false,
            showVideoCallRequest = md?.bool("showVideoCallRequest") ?: false,
        )

        return ConnectResponse(
            integrationId = data?.str("integrationId") ?: fallbackIntegrationId,
            customerId = data?.str("customerId"),
            visitorId = data?.str("visitorId"),
            languageCode = data?.str("languageCode"),
            uiOptions = uiOptions,
            messengerData = messengerData,
        )
    }

    /** Parses the top-level `ticketConfig`. Null unless the required ids are present. */
    private fun parseTicketConfig(tc: JsonObject?): TicketConfig? {
        if (tc == null) return null
        val id = tc.str("_id") ?: return null
        val pipelineId = tc.str("pipelineId") ?: return null
        val statusId = tc.str("selectedStatusId") ?: return null
        val ff = tc.obj("formFields")
        fun field(key: String): TicketFieldConfig? {
            val f = ff?.obj(key) ?: return null
            return TicketFieldConfig(
                isShow = f.bool("isShow") ?: false,
                label = f.str("label"),
                placeholder = f.str("placeholder"),
                order = f.int("order") ?: 99,
            )
        }
        return TicketConfig(
            id = id,
            name = tc.str("name").orEmpty(),
            pipelineId = pipelineId,
            channelId = tc.str("channelId"),
            selectedStatusId = statusId,
            parentId = tc.str("parentId"),
            formFields = TicketFormFields(
                name = field("name"),
                description = field("description"),
                attachment = field("attachment"),
                tags = field("tags"),
            ),
        )
    }
}
