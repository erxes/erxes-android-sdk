package com.erxes.messenger.data.model

/** A knowledge-base topic (`cpKnowledgeBaseTopicDetail`). Mirrors the iOS `KBTopic`. */
data class KbTopic(
    val id: String,
    val title: String,
    val description: String?,
    /** Flat list of all categories (child categories are keyed by [KbCategory.parentCategoryId]). */
    val categories: List<KbCategory>,
    /** Top-level categories shown first. */
    val parentCategories: List<KbCategory>,
)

data class KbCategory(
    val id: String,
    val title: String,
    val description: String?,
    val numOfArticles: Int,
    val parentCategoryId: String?,
    val icon: String?,
    val childrenIds: List<String>,
    val articles: List<KbArticle>,
)

data class KbArticle(
    val id: String,
    val title: String,
    val summary: String?,
    /** HTML content. */
    val content: String,
    val viewCount: Int,
)
