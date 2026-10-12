package com.veroanggra.reactify.sample

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import com.veroanggra.reactify.list.GooeyNotchShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.veroanggra.reactify.gooey.GooeyRenderer
import com.veroanggra.reactify.gooey.GooeyRenderers
import com.veroanggra.reactify.list.GooeySource
import com.veroanggra.reactify.list.GooeyTarget
import com.veroanggra.reactify.list.GooeyTransferHost
import com.veroanggra.reactify.list.rememberGooeyTransferState

private data class Friend(val id: Int, val name: String, val tint: Color)

private val friends = listOf(
    Friend(1, "Lessie Brody", Color(0xFFF6C6CC)),
    Friend(2, "Moreen Rosalynne", Color(0xFFD8CCF8)),
    Friend(3, "Roy Melantha", Color(0xFFC9F5C4)),
    Friend(4, "Terell Osbourne", Color(0xFFFFF1B8)),
    Friend(5, "Nadia Pranata", Color(0xFFC4E4F7)),
    Friend(6, "Bima Santoso", Color(0xFFFAD2B4)),
    Friend(7, "Clara Wijaya", Color(0xFFE3C8F2)),
    Friend(8, "Dimas Hartono", Color(0xFFBDEFE3)),
    Friend(9, "Eka Putri", Color(0xFFF7C1DA)),
    Friend(10, "Farhan Akbar", Color(0xFFD4E8B0)),
    Friend(11, "Gita Maharani", Color(0xFFFFE0A8)),
    Friend(12, "Hana Lestari", Color(0xFFC9D6FA)),
)

private val CardYellow = Color(0xFFFCF3BD)
private val SheetColor = Color(0xFF1C1C1C)
private val RowColor = Color(0xFF2A2A2A)
private val CardRadius = 28.dp
private val RowRadius = 16.dp
private val Mint = Color(0xFFC8F5D4)
private val NotchRadius = 30.dp
private val AddButtonSize = 44.dp

@Composable
fun InviteFriendsScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyTransferState()

    GooeyTransferHost(
        state = state,
        renderer = renderer,
        modifier = modifier
            .fillMaxSize()
            .background(Color.Black)
            .systemBarsPadding(),
    ) {
        Column(Modifier.fillMaxSize().padding(horizontal = 12.dp)) {
            var collapsed by remember { mutableStateOf(false) }
            val notchRadius by animateDpAsState(
                targetValue = if (collapsed) NotchRadius else 0.dp,
                animationSpec = spring(dampingRatio = 0.55f, stiffness = Spring.StiffnessLow),
            )
            val cardHeight by animateDpAsState(if (collapsed) 140.dp else 180.dp)

            Box(Modifier.padding(top = 12.dp).fillMaxWidth()) {
                GooeyTarget(
                    state = state,
                    cornerRadius = CardRadius,
                    notchRadius = notchRadius,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(cardHeight)
                        .background(CardYellow, GooeyNotchShape(CardRadius, notchRadius)),
                ) {
                    Column(
                        Modifier.fillMaxSize().padding(24.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                    ) {
                        Text("Invite Friends", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        TransferredTokens()
                        Spacer(Modifier.weight(1f))
                    }
                }
                AddButton(
                    progress = notchRadius / NotchRadius,
                    onClick = { collapsed = false },
                    modifier = Modifier.align(Alignment.BottomCenter).offset(y = AddButtonSize / 2),
                )
            }

            SheetHandle(onClick = { collapsed = !collapsed })

            Column(
                Modifier
                    .fillMaxSize()
                    .clip(RoundedCornerShape(topStart = CardRadius, topEnd = CardRadius))
                    .background(SheetColor)
                    .padding(horizontal = 12.dp),
            ) {
                Text(
                    "Friends nearby",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 20.dp),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(friends, key = { it.id }) { friend ->
                        GooeySource(
                            state = state,
                            key = friend.id,
                            color = friend.tint,
                            cornerRadius = RowRadius,
                            token = { Avatar(friend, Modifier.fillMaxSize()) },
                        ) {
                            FriendRow(friend, avatarModifier = Modifier.tokenAnchor())
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SheetHandle(onClick: () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .clickable(onClickLabel = "Toggle invite card", onClick = onClick)
            .padding(vertical = 14.dp),
        contentAlignment = Alignment.Center,
    ) {
        Box(Modifier.size(width = 36.dp, height = 3.dp).clip(CircleShape).background(Color.White))
    }
}

@Composable
private fun AddButton(progress: Float, onClick: () -> Unit, modifier: Modifier = Modifier) {
    if (progress <= 0.01f) return
    Box(
        modifier
            .size(AddButtonSize)
            .graphicsLayer {
                scaleX = progress
                scaleY = progress
            }
            .clip(CircleShape)
            .background(Mint)
            .clickable(onClickLabel = "Expand invite card", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("+", color = Color.Black, fontSize = 22.sp)
    }
}

@Composable
private fun FriendRow(friend: Friend, avatarModifier: Modifier) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RowRadius))
            .background(RowColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Avatar(friend, avatarModifier.size(40.dp))
        Spacer(Modifier.width(14.dp))
        Text(friend.name, color = Color.White, fontSize = 15.sp)
    }
}

@Composable
private fun Avatar(friend: Friend, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(friend.tint), contentAlignment = Alignment.Center) {
        Text(
            friend.name.split(" ").joinToString("") { it.take(1) },
            color = Color.Black.copy(alpha = 0.7f),
            fontSize = 13.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}
