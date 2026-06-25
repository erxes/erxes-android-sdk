package com.erxes.messenger.util

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.LinkAnnotation
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLinkStyles
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withLink
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
    private val aTag = Regex(
        "<a\\b[^>]*?href=[\"']([^\"']*)[\"'][^>]*>(.*?)</a>",
        setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL),
    )
    private val urlRegex = Regex("https?://[^\\s<>\"')]+", RegexOption.IGNORE_CASE)

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

    /**
     * Like [toPlainText] but preserves hyperlinks as clickable [LinkAnnotation.Url] spans,
     * styled with [linkColor] + underline. Tapping a link opens it via the platform URI
     * handler (Compose wires `LocalUriHandler` for `Url` annotations). Mirrors iOS, which
     * renders detected links in the messenger tint and opens them on tap.
     */
    fun toAnnotatedString(raw: String, linkColor: Color): AnnotatedString {
        val s = raw.trim()
        if (s.isEmpty()) return AnnotatedString("")
        return when {
            s.startsWith("[") -> parseBlockNoteAnnotated(s, linkColor)
                ?: linkify(toPlainText(s), linkColor)
            s.contains("<") -> htmlToAnnotated(s, linkColor)
            else -> linkify(s, linkColor)
        }
    }

    /** Strips remaining tags and decodes entities from an HTML fragment. */
    private fun String.stripTags(): String = replace(anyTag, "").decodeHtmlEntities()

    private fun htmlToAnnotated(html: String, linkColor: Color): AnnotatedString {
        // Collapse block boundaries to newlines first (keeping <a> tags intact), then
        // splice anchor tags into clickable spans and linkify any bare URLs around them.
        val normalized = html.replace(blockEnd, "\n").replace(br, "\n")
        return buildAnnotatedString {
            var last = 0
            for (m in aTag.findAll(normalized)) {
                appendLinkified(normalized.substring(last, m.range.first).stripTags(), linkColor)
                val href = m.groupValues[1].decodeHtmlEntities()
                val label = m.groupValues[2].stripTags().ifBlank { href }
                appendLink(href, label, linkColor)
                last = m.range.last + 1
            }
            appendLinkified(normalized.substring(last).stripTags(), linkColor)
        }
    }

    /** Renders plain text, turning bare `http(s)://…` URLs into clickable links. */
    private fun linkify(text: String, linkColor: Color): AnnotatedString =
        buildAnnotatedString { appendLinkified(text, linkColor) }

    private fun AnnotatedString.Builder.appendLinkified(text: String, linkColor: Color) {
        if (text.isEmpty()) return
        var last = 0
        for (m in urlRegex.findAll(text)) {
            append(text.substring(last, m.range.first))
            appendLink(m.value, m.value, linkColor)
            last = m.range.last + 1
        }
        append(text.substring(last))
    }

    private fun AnnotatedString.Builder.appendLink(url: String, label: String, linkColor: Color) {
        if (url.isBlank()) {
            append(label)
            return
        }
        val style = SpanStyle(color = linkColor, textDecoration = TextDecoration.Underline)
        withLink(LinkAnnotation.Url(url, TextLinkStyles(style))) { append(label) }
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

    private fun parseBlockNoteAnnotated(s: String, linkColor: Color): AnnotatedString? = try {
        val arr = JSONArray(s)
        val blocks = ArrayList<AnnotatedString>(arr.length())
        for (i in 0 until arr.length()) {
            arr.optJSONObject(i)?.let { block ->
                extractBlockAnnotated(block, linkColor).takeIf { it.isNotEmpty() }?.let(blocks::add)
            }
        }
        if (blocks.isEmpty()) null
        else buildAnnotatedString {
            blocks.forEachIndexed { idx, b ->
                if (idx > 0) append("\n")
                append(b)
            }
        }
    } catch (_: Exception) {
        null
    }

    private fun extractBlockAnnotated(block: JSONObject, linkColor: Color): AnnotatedString {
        val parts = mutableListOf<AnnotatedString>()
        block.optJSONArray("content")?.let { inlines ->
            val line = buildAnnotatedString {
                for (i in 0 until inlines.length()) {
                    inlines.optJSONObject(i)?.let { append(inlineAnnotated(it, linkColor)) }
                }
            }
            if (line.isNotEmpty()) parts.add(line)
        }
        block.optJSONArray("children")?.let { children ->
            for (i in 0 until children.length()) {
                children.optJSONObject(i)?.let {
                    extractBlockAnnotated(it, linkColor).takeIf { sub -> sub.isNotEmpty() }?.let(parts::add)
                }
            }
        }
        return buildAnnotatedString {
            parts.forEachIndexed { idx, p ->
                if (idx > 0) append("\n")
                append(p)
            }
        }
    }

    private fun inlineAnnotated(inline: JSONObject, linkColor: Color): AnnotatedString =
        when (inline.optString("type")) {
            "text" -> linkify(inline.optString("text", ""), linkColor)
            "link" -> {
                val href = inline.optString("href", "")
                val label = joinContent(inline).ifEmpty { href }
                buildAnnotatedString { appendLink(href, label, linkColor) }
            }
            else -> linkify(joinContent(inline).ifEmpty { inline.optString("text", "") }, linkColor)
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
