package io.sellmair.pacemaker.ui.settingsPage

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import io.sellmair.pacemaker.ApplicationIntent
import io.sellmair.pacemaker.model.User
import io.sellmair.pacemaker.model.nameAbbreviation
import io.sellmair.pacemaker.ui.displayColor
import io.sellmair.pacemaker.ui.toColor
import io.sellmair.pacemaker.ui.widget.Headline
import io.sellmair.pacemaker.ui.widget.UserHead

@Composable
internal fun SettingsPageHeader(
    me: User,
    onIntent: (ApplicationIntent.SettingsPageIntent) -> Unit = {},
) {
    var userName by remember { mutableStateOf(me.name) }

    Row(
        Modifier
            .fillMaxWidth()
            .padding(12.dp)
    ) {
        BasicTextField(
            modifier = Modifier
                .weight(1f)
                .align(Alignment.CenterVertically),
            value = userName,
            textStyle = TextStyle.Headline,
            singleLine = true,
            onValueChange = { newName ->
                userName = newName
                onIntent(ApplicationIntent.SettingsPageIntent.UpdateMe(me.copy(name = newName)))
            })

        UserHead(
            abbreviation = me.nameAbbreviation,
            color = me.displayColor.toColor(),
            size = 32.dp,
            modifier = Modifier
                .padding(4.dp)
        )
    }
}