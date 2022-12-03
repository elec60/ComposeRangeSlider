package com.example.composerangeslider

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.example.composerangeslider.ui.theme.ComposeRangeSliderTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            ComposeRangeSliderTheme {
                Box(
                    modifier = Modifier
                        .fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    RangeSlider(
                        modifier = Modifier
                            .padding(horizontal = 48.dp)
                            .fillMaxWidth(),
                        rangeColor = Color(73, 147, 236),
                        backColor = Color(203, 225, 246),
                        barHeight = 8.dp,
                        circleRadius = 10.dp,
                        progress1InitialValue = 0.3f,
                        progress2InitialValue = 0.8f,
                        tooltipSpacing = 10.dp,
                        tooltipWidth = 40.dp,
                        tooltipHeight = 30.dp,
                        cornerRadius = CornerRadius(32f, 32f),
                        tooltipTriangleSize = 8.dp,
                    ) { progress1, progress2 ->

                    }
                }
            }
        }
    }
}
