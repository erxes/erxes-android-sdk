package com.erxes.messenger.ui.screens

import android.widget.TextView
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.text.HtmlCompat
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.erxes.messenger.data.model.KbArticle
import com.erxes.messenger.data.model.KbCategory

/** Knowledge-base browser: categories → articles → article detail. */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
internal fun HelpScreen(
    onBack: () -> Unit,
    viewModel: HelpViewModel = viewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    var category by remember { mutableStateOf<KbCategory?>(null) }
    var article by remember { mutableStateOf<KbArticle?>(null) }

    val title = when {
        article != null -> article!!.title
        category != null -> category!!.title
        else -> state.topic?.title ?: "Help center"
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(title, maxLines = 1) },
                navigationIcon = {
                    IconButton(onClick = {
                        when {
                            article != null -> article = null
                            category != null -> category = null
                            else -> onBack()
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary,
                ),
            )
        },
    ) { inner ->
        Box(modifier = Modifier.fillMaxSize().padding(inner)) {
            when {
                state.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))
                state.topic == null -> Text(
                    text = state.error ?: "No articles available",
                    modifier = Modifier.align(Alignment.Center).padding(24.dp),
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                article != null -> ArticleDetail(article!!)
                category != null -> ArticleList(
                    articles = viewModel.articlesFor(category!!),
                    onOpen = { article = it },
                )
                else -> CategoryList(
                    categories = state.topic!!.parentCategories.ifEmpty { state.topic!!.categories },
                    onOpen = { category = it },
                )
            }
        }
    }
}

@Composable
private fun CategoryList(categories: List<KbCategory>, onOpen: (KbCategory) -> Unit) {
    LazyColumn(Modifier.fillMaxSize()) {
        items(categories, key = { it.id }) { cat ->
            Column(
                Modifier.fillMaxWidth().clickable { onOpen(cat) }.padding(16.dp),
            ) {
                Text(cat.title, style = MaterialTheme.typography.bodyLarge, fontWeight = FontWeight.Medium)
                cat.description?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ArticleList(articles: List<KbArticle>, onOpen: (KbArticle) -> Unit) {
    if (articles.isEmpty()) {
        Box(Modifier.fillMaxSize()) {
            Text(
                "No articles in this category",
                Modifier.align(Alignment.Center).padding(24.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        return
    }
    LazyColumn(Modifier.fillMaxSize()) {
        items(articles, key = { it.id }) { art ->
            Column(
                Modifier.fillMaxWidth().clickable { onOpen(art) }.padding(16.dp),
            ) {
                Text(art.title, style = MaterialTheme.typography.bodyLarge)
                art.summary?.takeIf { it.isNotBlank() }?.let {
                    Text(it, style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
            HorizontalDivider()
        }
    }
}

@Composable
private fun ArticleDetail(article: KbArticle) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp),
    ) {
        Text(article.title, style = MaterialTheme.typography.headlineSmall, fontWeight = FontWeight.Bold)
        // Render the article's HTML content via a native TextView.
        AndroidView(
            modifier = Modifier.fillMaxWidth().padding(top = 12.dp),
            factory = { context -> TextView(context) },
            update = { tv ->
                tv.text = HtmlCompat.fromHtml(article.content, HtmlCompat.FROM_HTML_MODE_COMPACT)
            },
        )
    }
}
