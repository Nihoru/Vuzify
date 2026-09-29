package com.nihoru.vuzify.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.nihoru.vuzify.ui.theme.RoomBadgeBackground
import com.nihoru.vuzify.ui.theme.RoomBadgeText
import com.nihoru.vuzify.ui.theme.TextTertiary
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun DateBadge(
    date: Date,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val dayNum = SimpleDateFormat("dd", Locale("ru")).format(date)
    val month = SimpleDateFormat("LLL", Locale("ru")).format(date).uppercase().replace(".", "")
    val weekday = SimpleDateFormat("EE", Locale("ru")).format(date).lowercase()

    Box(
        modifier = modifier
            .height(52.dp)
            .clip(RoundedCornerShape(26.dp))
            .background(RoomBadgeBackground)
            .clickable { onClick() }
            .padding(horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dayNum,
                fontSize = 26.sp,
                fontWeight = FontWeight.Black,
                color = RoomBadgeText,
                lineHeight = 26.sp
            )

            Spacer(modifier = Modifier.width(6.dp))

            Column {
                Text(
                    text = month,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Black,
                    color = RoomBadgeText,
                    lineHeight = 12.sp
                )
                Text(
                    text = weekday,
                    fontSize = 10.sp,
                    fontWeight = FontWeight.Medium,
                    color = TextTertiary,
                    lineHeight = 11.sp
                )
            }
        }
    }
}
