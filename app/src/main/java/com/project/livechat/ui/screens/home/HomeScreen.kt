package com.project.livechat.ui.screens.home

import androidx.activity.OnBackPressedDispatcher
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.PagerState
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import com.project.livechat.ui.navigation.builder.ContactsScreen
import com.project.livechat.ui.screens.home.behavior.homeTabBehaviorFactory
import com.project.livechat.ui.screens.home.models.TabItemModel
import com.project.livechat.ui.screens.home.models.chatCardList
import com.project.livechat.ui.screens.home.models.tabItemList
import com.project.livechat.ui.screens.home.widgets.ChatCard
import com.project.livechat.ui.screens.home.widgets.TabItem
import com.project.livechat.ui.theme.LiveChatTheme
import kotlinx.coroutines.launch

@Composable
fun HomeScreen(
    navHostController: NavHostController,
    backPressedDispatcher: OnBackPressedDispatcher
) {
    val pagerState = rememberPagerState {
        tabItemList.size
    }
    HomeContent(
        itemList = tabItemList,
        pagerState = pagerState,
        navHostController = navHostController
    )

}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeContent(
    itemList: List<TabItemModel>,
    pagerState: PagerState,
    navHostController: NavHostController
) {

    Scaffold(
        topBar = {
            TopAppBar(
                title = {},
                actions = {
                    IconButton(onClick = { /*TODO*/ }) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = null
                        )
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = {
                navHostController.navigate(ContactsScreen)
            }) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = null
                )
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.padding(paddingValues = paddingValues)
        ) {
            Tabs(itemList = itemList, pagerState = pagerState)
            TabsContent(itemList = itemList, pagerState = pagerState)
        }
    }
}

@Composable
fun Tabs(itemList: List<TabItemModel>, pagerState: PagerState) {
    val scope = rememberCoroutineScope()
    Column {
        LazyRow(
            modifier = Modifier
                .padding(start = 8.dp)
                .padding(bottom = 8.dp)
        ) {
            itemsIndexed(itemList) { index, item ->
                TabItem(pagerState = pagerState, index = index, item = item) { _ ->
                    scope.launch {
                        homeTabBehaviorFactory(
                            itemList[index].behavior,
                            pagerState,
                            scope,
                            index
                        ).execute()
                    }
                }
            }
        }
        Divider(
            Modifier
                .height(1.dp)
                .fillMaxWidth()
        )
    }
}

@Composable
fun TabsContent(itemList: List<TabItemModel>, pagerState: PagerState) {
    HorizontalPager(
        state = pagerState,
        userScrollEnabled = false
    ) {
        TabContent()
    }
}

@Composable
fun TabContent() {
    chatCardList.takeIf { it.isNotEmpty() }?.let {
        LazyColumn(
            modifier = Modifier
                .fillMaxHeight()
                .padding(top = 8.dp, start = 8.dp, end = 8.dp)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            itemsIndexed(it) { _, item ->
                ChatCard(chatCardModel = item)
            }
        }
    } ?: run {
        Column(
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = "")
        }
    }
}

@Preview
@Composable
fun HomePreview() {
    val pagerState = rememberPagerState {
        tabItemList.size
    }
    val navHostController = rememberNavController()
    LiveChatTheme {
        HomeContent(
            itemList = tabItemList,
            pagerState = pagerState,
            navHostController = navHostController
        )
    }
}