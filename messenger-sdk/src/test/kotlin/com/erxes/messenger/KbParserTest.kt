package com.erxes.messenger

import com.erxes.messenger.network.KbParser
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import org.junit.Assert.assertEquals
import org.junit.Test

class KbParserTest {

    private fun obj(text: String) = Json.parseToJsonElement(text) as JsonObject

    @Test
    fun `parses topic with categories and articles`() {
        val topic = KbParser.parseTopic(
            obj(
                """
                {
                  "_id": "t1", "title": "Help",
                  "parentCategories": [
                    { "_id": "p1", "title": "Getting started",
                      "childrens": [{ "_id": "c1" }],
                      "articles": [{ "_id": "a1", "title": "Welcome", "content": "<p>hi</p>" }] }
                  ],
                  "categories": [
                    { "_id": "p1", "title": "Getting started",
                      "articles": [{ "_id": "a1", "title": "Welcome", "content": "<p>hi</p>" }] },
                    { "_id": "c1", "title": "Child", "parentCategoryId": "p1",
                      "articles": [{ "_id": "a2", "title": "Deep dive", "content": "<p>x</p>" }] }
                  ]
                }
                """.trimIndent()
            )
        )
        assertEquals("Help", topic.title)
        assertEquals(1, topic.parentCategories.size)
        assertEquals(listOf("c1"), topic.parentCategories[0].childrenIds)
    }

    @Test
    fun `articlesFor merges child category articles and dedupes`() {
        val topic = KbParser.parseTopic(
            obj(
                """
                {
                  "_id": "t1", "title": "Help",
                  "parentCategories": [
                    { "_id": "p1", "title": "Parent",
                      "articles": [{ "_id": "a1", "title": "One", "content": "" }] }
                  ],
                  "categories": [
                    { "_id": "p1", "title": "Parent",
                      "articles": [{ "_id": "a1", "title": "One", "content": "" }] },
                    { "_id": "c1", "title": "Child", "parentCategoryId": "p1",
                      "articles": [
                        { "_id": "a1", "title": "One", "content": "" },
                        { "_id": "a2", "title": "Two", "content": "" }
                      ] }
                  ]
                }
                """.trimIndent()
            )
        )
        val parent = topic.parentCategories.first()
        val articles = KbParser.articlesFor(parent, topic.categories)
        // a1 (parent + child duplicate) deduped, plus a2 from the child.
        assertEquals(listOf("a1", "a2"), articles.map { it.id })
    }
}
