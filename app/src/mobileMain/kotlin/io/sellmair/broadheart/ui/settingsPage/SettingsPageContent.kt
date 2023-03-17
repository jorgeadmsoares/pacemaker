package io.sellmair.broadheart.ui.settingsPage

import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import io.sellmair.broadheart.model.User
import io.sellmair.broadheart.GroupMember
import io.sellmair.broadheart.Group
import io.sellmair.broadheart.viewModel.ApplicationIntent

@Composable
internal fun SettingsPageContent(
    me: User,
    groupState: Group? = null,
    onIntent: (ApplicationIntent.SettingsPageIntent) -> Unit = {},
    onCloseSettingsPage: () -> Unit
) {
    Column(Modifier.fillMaxSize()) {
        SettingsPageHeader(
            me = me,
            onIntent = onIntent,
            onCloseSettingsPage = onCloseSettingsPage
        )

        Spacer(Modifier.height(24.dp))

        Box(Modifier.padding(horizontal = 24.dp)) {
            SettingsPageDevicesList(
                me = me,
                groupState = groupState,
                onIntent = onIntent
            )
        }
    }
}

@Composable
internal fun SettingsPageDevicesList(
    me: User,
    groupState: Group?,
    onIntent: (ApplicationIntent.SettingsPageIntent) -> Unit
) {
    Column(Modifier.fillMaxWidth()) {
        Text(
            text = "Nearby Devices",
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        val memberStates = groupState?.members.orEmpty()
            .sortedWith(
                compareBy<GroupMember> { it.user?.isMe?.let { 0 } ?: 1 }
                    .then(compareBy { it.sensorInfo?.id?.value.orEmpty() })
            )
        LazyColumn {
            items(memberStates) { member ->
                Box(
                    modifier = Modifier
                        .animateContentSize()
                ) {
                    NearbyDeviceCard(
                        me = me,
                        state = member,
                        onEvent = onIntent
                    )
                }
            }
        }
    }
}
