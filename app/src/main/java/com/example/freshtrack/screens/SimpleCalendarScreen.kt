package com.example.freshtrack.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.*

@Composable
fun SimpleCalendarScreen(
    yearMonth: YearMonth = YearMonth.now(),
    mealsByDate: Map<LocalDate, List<String>> = emptyMap(),
    onDateSelected: (LocalDate) -> Unit = {},
    textColor: androidx.compose.ui.graphics.Color = MaterialTheme.colorScheme.onBackground
) {
    var selectedDate by remember { mutableStateOf<LocalDate?>(null) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .wrapContentHeight()
            .padding(16.dp)
    ) {
        Text(
            text = "${yearMonth.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${yearMonth.year}",
            style = MaterialTheme.typography.headlineMedium,
            color = textColor
        )
        Spacer(Modifier.height(8.dp))
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            DayOfWeek.values().forEach { dow ->
                Text(
                    text = dow.getDisplayName(TextStyle.SHORT, Locale.getDefault()),
                    modifier = Modifier.weight(1f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = androidx.compose.ui.text.style.TextAlign.Center,
                    color = textColor
                )
            }
        }
        Spacer(Modifier.height(4.dp))

        val firstDay = yearMonth.atDay(1).dayOfWeek.value % 7
        val totalDays = yearMonth.lengthOfMonth()
        val blanksBefore = List(firstDay) { null as LocalDate? }
        val dates = (1..totalDays).map { LocalDate.of(yearMonth.year, yearMonth.month, it) }
        val blanksAfter = List((7 - (firstDay + totalDays) % 7) % 7) { null }
        val cells = blanksBefore + dates + blanksAfter

        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier
                .fillMaxWidth()
                .height(240.dp)
        ) {
            items(cells) { date ->
                Box(
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                        .clickable(enabled = date != null) {
                            date?.let {
                                selectedDate = it
                                onDateSelected(it)
                            }
                        },
                    contentAlignment = Alignment.TopCenter
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.fillMaxSize()
                    ) {
                        Text(
                            text = date?.dayOfMonth?.toString() ?: "",
                            color = if (date == selectedDate) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface
                        )
                        Spacer(Modifier.weight(1f))
                        if (date != null && mealsByDate[date]?.isNotEmpty() == true) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .clip(CircleShape)
                                    .background(MaterialTheme.colorScheme.primary)
                            )
                        }
                        Spacer(Modifier.height(4.dp))
                    }
                }
            }
        }

        Spacer(Modifier.height(16.dp))

        selectedDate?.let { date ->
            Text(
                text = "Selected: ${date.month.getDisplayName(TextStyle.FULL, Locale.getDefault())} ${date.dayOfMonth}, ${date.year}",
                style = MaterialTheme.typography.bodyLarge,
                color = textColor
            )
        }
    }
}

