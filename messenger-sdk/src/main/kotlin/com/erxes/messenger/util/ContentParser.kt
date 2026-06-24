package com.erxes.messenger.util

import org.json.JSONArray
import org.json.JSONObject

/**
 * Converts message content — BlockNote JSON, HTML, or plain text — into display text.
 * Mirrors iOS `ContentParser`: flattens BlockNote inline nodes, strips HTML tags and
 * decodes entities. Keeps newlines at block boundaries; no inline formatting.
 */
object ContentParser {

    private val blockEnd = Regex("</(p|div|li|blockquote|h[1-6]|tr|td|th)>", RegexOption.IGNORE_CASE)
    private val br = Regex("<br\\s*/?>", RegexOption.IGNORE_CASE)
    private val anyTag = Regex("<[^>]+>")
    private val blankLines = Regex("\\n{3,}")

    /** Best-effort plain-text rendering of [raw]. Safe on any thread. */
    fun toPlainText(raw: String): String {
        val s = raw.trim()
        if (s.isEmpty()) return ""
        return when {
            s.startsWith("[") -> parseBlockNote(s) ?: if (s.contains("<")) stripHtml(s) else s
            s.contains("<") -> stripHtml(s)
            else -> s
        }
    }

    private fun stripHtml(html: String): String =
        html
            .replace(blockEnd, "\n")
            .replace(br, "\n")
            .replace(anyTag, "")
            .decodeHtmlEntities()
            .replace(blankLines, "\n\n")
            .trim()

    private fun String.decodeHtmlEntities(): String =
        replace("&amp;", "&")
            .replace("&nbsp;", " ")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&#39;", "'")
            .replace("&apos;", "'")

    // ── BlockNote JSON ──────────────────────────────────────────────────────────

    private fun parseBlockNote(s: String): String? = try {
        val arr = JSONArray(s)
        val parts = ArrayList<String>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { block ->
                extractBlockText(block).takeIf { it.isNotEmpty() }?.let(parts::add)
            }
        }
        parts.joinToString("\n").trim().ifEmpty { null }
    } catch (_: Exception) {
        null
    }

    private fun extractBlockText(block: JSONObject): String {
        val parts = mutableListOf<String>()
        block.optJSONArray("content")?.let { inlines ->
            val line = buildString {
                for (i in 0 until inlines.length()) {
                    inlines.optJSONObject(i)?.let { append(inlineText(it)) }
                }
            }
            if (line.isNotEmpty()) parts.add(line)
        }
        block.optJSONArray("children")?.let { children ->
            for (i in 0 until children.length()) {
                children.optJSONObject(i)?.let {
                    extractBlockText(it).takeIf { sub -> sub.isNotEmpty() }?.let(parts::add)
                }
            }
        }
        return parts.joinToString("\n")
    }

    private fun inlineText(inline: JSONObject): String = when (inline.optString("type")) {
        "text" -> inline.optString("text", "")
        "link" -> joinContent(inline).ifEmpty { inline.optString("href", "") }
        else -> joinContent(inline).ifEmpty { inline.optString("text", "") }
    }

    private fun joinContent(inline: JSONObject): String {
        val content = inline.optJSONArray("content") ?: return ""
        return buildString {
            for (i in 0 until content.length()) {
                content.optJSONObject(i)?.let { append(inlineText(it)) }
            }
        }
    }
}
