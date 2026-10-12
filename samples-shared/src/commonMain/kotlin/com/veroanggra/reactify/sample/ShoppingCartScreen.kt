package com.veroanggra.reactify.sample

import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.animation.core.spring
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.offset
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.graphicsLayer
import com.veroanggra.reactify.list.GooeyNotchShape
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
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

private data class Product(val id: Int, val name: String, val price: Int, val emoji: String, val tint: Color)

private val products = listOf(
    Product(1, "Apel Fuji", 32_000, "🍎", Color(0xFFF6C6CC)),
    Product(2, "Susu UHT", 18_500, "🥛", Color(0xFFC4E4F7)),
    Product(3, "Roti Tawar", 15_000, "🍞", Color(0xFFFFF1B8)),
    Product(4, "Telur Ayam 1kg", 28_000, "🥚", Color(0xFFD8CCF8)),
    Product(5, "Kopi Bubuk", 24_000, "☕", Color(0xFFC9F5C4)),
    Product(6, "Mie Instan", 3_500, "🍜", Color(0xFFFAD2B4)),
    Product(7, "Keju Slice", 21_000, "🧀", Color(0xFFE3C8F2)),
    Product(8, "Pisang Cavendish", 12_000, "🍌", Color(0xFFBDEFE3)),
    Product(9, "Air Mineral 1.5L", 6_000, "💧", Color(0xFFF7C1DA)),
    Product(10, "Cokelat Batang", 17_000, "🍫", Color(0xFFD4E8B0)),
    Product(11, "Yogurt Cup", 9_500, "🍦", Color(0xFFFFE0A8)),
    Product(12, "Sayur Bayam", 7_000, "🥬", Color(0xFFC9D6FA)),
)

private val CardYellow = Color(0xFFFCF3BD)
private val SheetColor = Color(0xFF1C1C1C)
private val RowColor = Color(0xFF2A2A2A)
private val CardRadius = 28.dp
private val RowRadius = 16.dp
private val Mint = Color(0xFFC8F5D4)
private val NotchRadius = 30.dp
private val AddButtonSize = 44.dp
private val StepperButtonSize = 26.dp

private fun formatRupiah(amount: Int): String {
    val digits = amount.toString()
    val grouped = digits.reversed().chunked(3).joinToString(".").reversed()
    return "Rp$grouped"
}

@Composable
fun ShoppingCartScreen(
    modifier: Modifier = Modifier,
    renderer: GooeyRenderer = remember { GooeyRenderers.auto() },
) {
    val state = rememberGooeyTransferState()
    val productsById = remember { products.associateBy { it.id } }

    // Quantity per product, separate from GooeyTransferState: the gooey token only tracks
    // whether a product is in the cart at all (fires once on the first add); the stepper
    // below just adjusts this count without replaying the fly-in animation.
    val quantities = remember { mutableStateMapOf<Int, Int>() }
    val itemCount by remember { derivedStateOf { quantities.values.sum() } }
    val total by remember {
        derivedStateOf { quantities.entries.sumOf { (id, qty) -> productsById.getValue(id).price * qty } }
    }

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
                        Text("Keranjang", color = Color.Black, fontSize = 16.sp, fontWeight = FontWeight.Medium)
                        Spacer(Modifier.weight(1f))
                        TransferredTokens()
                        Spacer(Modifier.weight(1f))
                        Text(
                            if (itemCount > 0) "$itemCount item • ${formatRupiah(total)}" else "Keranjang kosong",
                            color = Color.Black,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold,
                        )
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
                    "Belanjaan",
                    color = Color.White,
                    fontSize = 15.sp,
                    fontWeight = FontWeight.Medium,
                    modifier = Modifier.align(Alignment.CenterHorizontally).padding(vertical = 20.dp),
                )
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(products, key = { it.id }) { product ->
                        GooeySource(
                            state = state,
                            key = product.id,
                            color = product.tint,
                            cornerRadius = RowRadius,
                            // Reads `quantities` live, so the badge updates on the resting
                            // token in the cart without needing another transfer.
                            token = { ProductToken(product, quantities[product.id] ?: 1) },
                        ) {
                            // `isTransferred` flips true the moment the fly-in animation starts
                            // and stays true until a return-to-source flight finishes, so this
                            // mirrors the quantity map into/out of existence at the right time.
                            LaunchedEffect(isTransferred) {
                                if (isTransferred) {
                                    if (quantities[product.id] == null) quantities[product.id] = 1
                                } else {
                                    quantities.remove(product.id)
                                }
                            }
                            ProductRow(
                                product = product,
                                iconModifier = Modifier.tokenAnchor(),
                                quantity = quantities[product.id] ?: 0,
                                onIncrement = { quantities[product.id] = (quantities[product.id] ?: 0) + 1 },
                                onDecrement = {
                                    val current = quantities[product.id] ?: 0
                                    if (current <= 1) {
                                        state.returnToSource(product.id)
                                    } else {
                                        quantities[product.id] = current - 1
                                    }
                                },
                            )
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
            .clickable(onClickLabel = "Toggle cart", onClick = onClick)
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
            .clickable(onClickLabel = "Expand cart", onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text("+", color = Color.Black, fontSize = 22.sp)
    }
}

/**
 * Row for one product in the sheet. While [quantity] is 0 the whole row is the tap/drag target
 * that [GooeySource] wires up to start a transfer; once it's at least 1 the row instead shows a
 * +/- stepper that adjusts [quantity] directly, without flying another token.
 */
@Composable
private fun ProductRow(
    product: Product,
    iconModifier: Modifier,
    quantity: Int,
    onIncrement: () -> Unit,
    onDecrement: () -> Unit,
) {
    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(RowRadius))
            .background(RowColor)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ProductIcon(product, iconModifier.size(40.dp))
        Spacer(Modifier.width(14.dp))
        Column(Modifier.weight(1f)) {
            Text(product.name, color = Color.White, fontSize = 15.sp)
            Text(formatRupiah(product.price), color = Color.White.copy(alpha = 0.6f), fontSize = 12.sp)
        }
        if (quantity > 0) {
            QuantityStepper(quantity, onIncrement, onDecrement)
        }
    }
}

@Composable
private fun QuantityStepper(quantity: Int, onIncrement: () -> Unit, onDecrement: () -> Unit) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        StepperButton("−", onClick = onDecrement)
        Text(
            "$quantity",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.width(24.dp),
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
        )
        StepperButton("+", onClick = onIncrement)
    }
}

@Composable
private fun StepperButton(label: String, onClick: () -> Unit) {
    Box(
        Modifier
            .size(StepperButtonSize)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.12f))
            .clickable(onClickLabel = label, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = Color.White, fontSize = 15.sp, fontWeight = FontWeight.Medium)
    }
}

@Composable
private fun ProductIcon(product: Product, modifier: Modifier = Modifier) {
    Box(modifier.clip(CircleShape).background(product.tint), contentAlignment = Alignment.Center) {
        Text(product.emoji, fontSize = 18.sp)
    }
}

/** Token drawn both in flight and resting in the cart; shows a quantity badge once above 1. */
@Composable
private fun ProductToken(product: Product, quantity: Int) {
    Box(Modifier.fillMaxSize()) {
        ProductIcon(product, Modifier.fillMaxSize())
        if (quantity > 1) {
            QuantityBadge(quantity, Modifier.align(Alignment.TopEnd))
        }
    }
}

@Composable
private fun BoxScope.QuantityBadge(quantity: Int, modifier: Modifier = Modifier) {
    Box(
        modifier
            .size(16.dp)
            .clip(CircleShape)
            .background(Mint),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            "$quantity",
            color = Color.Black,
            fontSize = 9.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}
