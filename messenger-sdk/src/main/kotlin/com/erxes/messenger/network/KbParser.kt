package com.erxes.messenger.network

import com.erxes.messenger.data.model.KbArticle
import com.erxes.messenger.data.model.KbCategory
import com.erxes.messenger.data.model.KbTopic
import kotlinx.serialization.json.JsonObject

/** Pure parsing for knowledge-base topics. Mirrors `HelpViewModel.parseTopic` (iOS). */
object KbParser {

    fun parseTopic(json: JsonObject): KbTopic = KbTopic(
        id = json.str("_id").orEmpty(),
        title = json.str("title")?.takeIf { it.isNotBlank() } ?: "Help center",
        description = json.str("description"),
        categories = (json.arr("categories")).orEmpty().mapNotNull { (it as? JsonObject)?.let(::parseCategory) },
        parentCategories = (json.arr("parentCategories")).orEmpty()
            .mapNotNull { (it as? JsonObject)?.let(::parseCategory) },
    )

    private fun parseCategory(json: JsonObject): KbCategory? {
        val id = json.str("_id") ?: return null
        val articles = (json.arr("articles")).orEmpty().mapNotNull { (it as? JsonObject)?.let(::parseArticle) }
        val childrenIds = (json.arr("childrens")).orEmpty()
            .mapNotNull { (it as? JsonObject)?.str("_id") }
        return KbCategory(
            id = id,
            title = json.str("title").orEmpty(),
            description = json.str("description"),
            numOfArticles = json.int("numOfArticles") ?: articles.size,
            parentCategoryId = json.str("parentCategoryId"),
            icon = json.str("icon"),
            childrenIds = childrenIds,
            articles = articles,
        )
    }

    private fun parseArticle(json: JsonObject): KbArticle? {
        val id = json.str("_id") ?: return null
        return KbArticle(
            id = id,
            title = json.str("title").orEmpty(),
            summary = json.str("summary"),
            content = json.str("content").orEmpty(),
            viewCount = json.int("viewCount") ?: 0,
        )
    }

    /**
     * Articles to show for a selected [category]: its own plus those of its child
     * categories (which live flat in [allCategories], keyed by parentCategoryId),
     * de-duplicated by id. Mirrors `HelpViewModel.articles(for:)`.
     */
    fun articlesFor(category: KbCategory, allCategories: List<KbCategory>): List<KbArticle> {
        val result = category.articles.toMutableList()
        allCategories.filter { it.parentCategoryId == category.id && it.articles.isNotEmpty() }
            .forEach { result.addAll(it.articles) }
        val seen = HashSet<String>()
        return result.filter { seen.add(it.id) }
    }
}
