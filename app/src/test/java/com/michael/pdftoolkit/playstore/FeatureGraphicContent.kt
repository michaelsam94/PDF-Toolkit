package com.michael.pdftoolkit.playstore

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Merge
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.michael.pdftoolkit.ui.theme.MyApplicationTheme
import com.michael.pdftoolkit.ui.theme.OnPrimaryLight
import com.michael.pdftoolkit.ui.theme.PrimaryContainerLight
import com.michael.pdftoolkit.ui.theme.PrimaryLight
import com.michael.pdftoolkit.ui.theme.TertiaryLight

@Composable
fun FeatureGraphicContent() {
  MyApplicationTheme(dynamicColor = false) {
    Box(
      modifier =
        Modifier
          .fillMaxSize()
          .background(
            Brush.horizontalGradient(
              colors = listOf(PrimaryLight, TertiaryLight, PrimaryContainerLight),
            ),
          ),
    ) {
      Row(
        modifier =
          Modifier
            .fillMaxSize()
            .padding(horizontal = 48.dp, vertical = 40.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween,
      ) {
        Column(
          modifier = Modifier.weight(1f),
          verticalArrangement = Arrangement.Center,
        ) {
          Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
              imageVector = Icons.Default.PictureAsPdf,
              contentDescription = null,
              tint = OnPrimaryLight,
              modifier = Modifier.size(40.dp),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
              text = "PDF Toolkit",
              color = OnPrimaryLight,
              fontSize = 42.sp,
              fontWeight = FontWeight.Bold,
            )
          }
          Spacer(modifier = Modifier.height(16.dp))
          Text(
            text = "Merge, split, sign, and convert PDFs — fully offline on your device.",
            color = OnPrimaryLight.copy(alpha = 0.92f),
            fontSize = 20.sp,
            lineHeight = 28.sp,
            modifier = Modifier.width(420.dp),
          )
        }

        Card(
          modifier =
            Modifier
              .width(220.dp)
              .fillMaxHeight()
              .clip(RoundedCornerShape(24.dp)),
          shape = RoundedCornerShape(24.dp),
          colors = CardDefaults.cardColors(containerColor = Color.White),
          elevation = CardDefaults.cardElevation(defaultElevation = 12.dp),
        ) {
          Column(
            modifier =
              Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp),
          ) {
            Text(
              text = "Quick actions",
              style = MaterialTheme.typography.titleSmall,
              fontWeight = FontWeight.Bold,
            )
            FeatureGraphicActionChip("Merge PDFs", Icons.Default.Merge)
            FeatureGraphicActionChip("Sign documents", Icons.Default.PictureAsPdf)
            Spacer(modifier = Modifier.weight(1f))
            Text(
              text = "Private & offline",
              style = MaterialTheme.typography.labelMedium,
              color = PrimaryLight,
            )
          }
        }
      }
    }
  }
}

@Composable
private fun FeatureGraphicActionChip(
  label: String,
  icon: androidx.compose.ui.graphics.vector.ImageVector,
) {
  Row(
    modifier =
      Modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(PrimaryContainerLight.copy(alpha = 0.55f))
        .padding(horizontal = 12.dp, vertical = 10.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Icon(
      imageVector = icon,
      contentDescription = null,
      tint = PrimaryLight,
      modifier = Modifier.size(20.dp),
    )
    Spacer(modifier = Modifier.width(8.dp))
    Text(text = label, style = MaterialTheme.typography.bodyMedium)
  }
}
