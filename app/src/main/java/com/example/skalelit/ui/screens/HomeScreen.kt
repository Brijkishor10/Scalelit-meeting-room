package com.example.skalelit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Groups
import androidx.compose.material.icons.rounded.Wifi
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.example.skalelit.data.entity.RoomEntity
import com.example.skalelit.ui.components.SkaleCard
import com.example.skalelit.ui.components.SkaleChip
import com.example.skalelit.ui.theme.SkalePrimary
import com.example.skalelit.ui.theme.SkaleTextBody
import com.example.skalelit.ui.theme.SkaleTextHead

@Composable
fun HomeScreen(rooms: List<RoomEntity>, onClick: (RoomEntity) -> Unit) {
    LazyColumn(
        contentPadding = PaddingValues(20.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        item {
            Column {
                Text("Find your space", fontSize = 24.sp, fontWeight = FontWeight.Bold, color = SkaleTextHead)
                Text("Book meeting rooms and events.", color = SkaleTextBody)
            }
        }

        item {
            Text("Recommended", fontSize = 18.sp, fontWeight = FontWeight.SemiBold, color = SkaleTextHead)
        }

        items(rooms) { room ->
            // FIX: RoomCard definition provided below
            RoomCard(room, onClick = { onClick(room) })
        }
    }
}

// --- MISSING COMPONENT ADDED HERE ---
@Composable
fun RoomCard(room: RoomEntity, onClick: () -> Unit) {
    SkaleCard(Modifier.fillMaxWidth(), onClick = onClick) {
        Column {
            Box(Modifier.height(160.dp).fillMaxWidth()) {
                AsyncImage(
                    model = room.img,
                    contentDescription = null,
                    modifier = Modifier.fillMaxSize(),
                    contentScale = ContentScale.Crop
                )
                Box(Modifier.padding(12.dp).align(Alignment.TopEnd)) {
                    SkaleChip("Available")
                }
            }
            Column(Modifier.padding(16.dp)) {
                Text(room.name, fontSize = 18.sp, fontWeight = FontWeight.Bold, color = SkaleTextHead)
                Text(room.loc, color = SkaleTextBody, fontSize = 14.sp)
                Spacer(Modifier.height(12.dp))
                Row {
                    Icon(Icons.Rounded.Groups, null, modifier = Modifier.size(16.dp), tint = SkalePrimary)
                    Text(" ${room.cap} Seats", fontSize = 12.sp, color = SkaleTextBody)
                    Spacer(Modifier.width(16.dp))
                    Icon(Icons.Rounded.Wifi, null, modifier = Modifier.size(16.dp), tint = SkalePrimary)
                    Text(" Fast Wifi", fontSize = 12.sp, color = SkaleTextBody)
                }
            }
        }
    }
}