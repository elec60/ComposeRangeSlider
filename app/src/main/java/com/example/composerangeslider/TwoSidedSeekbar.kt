package com.example.composerangeslider

import android.view.MotionEvent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.*
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.*
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.RoundRect
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.input.pointer.*
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.*
import androidx.compose.ui.unit.Dp
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

@OptIn(ExperimentalComposeUiApi::class, ExperimentalTextApi::class)
@Composable
fun RangeSlider(
    modifier: Modifier,
    rangeColor: Color,
    backColor: Color,
    barHeight: Dp,
    circleRadius: Dp,
    cornerRadius: CornerRadius,
    progress1InitialValue: Float,
    progress2InitialValue: Float,
    tooltipSpacing: Dp,
    tooltipWidth: Dp,
    tooltipHeight: Dp,
    tooltipTriangleSize: Dp,
    onProgressChanged: (progress1: Float, progress2: Float) -> Unit
) {

    val circleRadiusInPx = with(LocalDensity.current) { circleRadius.toPx() }

    var progress1 by remember {
        mutableStateOf(progress1InitialValue)
    }
    var progress2 by remember {
        mutableStateOf(progress2InitialValue)
    }

    var width by remember {
        mutableStateOf(0f)
    }

    var height by remember {
        mutableStateOf(0f)
    }

    var leftCircleDragging by remember {
        mutableStateOf(false)
    }

    var rightCircleDragging by remember {
        mutableStateOf(false)
    }

    val leftTooltipOverlapping by remember {
        derivedStateOf { mutableStateOf(false) }
    }

    var leftCircleOffset by remember {
        mutableStateOf(Offset.Zero)
    }
    var rightCircleOffset by remember {
        mutableStateOf(Offset.Zero)
    }

    val scaleAnim1 by animateFloatAsState(
        targetValue = if (leftCircleDragging) 2f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    val scaleAnim2 by animateFloatAsState(
        targetValue = if (rightCircleDragging) 2f else 1f,
        animationSpec = tween(durationMillis = 300)
    )

    val tooltipAnim1 by animateFloatAsState(
        targetValue = if (leftTooltipOverlapping.value) -180f else 0f,
        animationSpec = tween(durationMillis = 300)
    )

    val path = remember {
        Path()
    }

    val textMeasurer = rememberTextMeasurer()


    Canvas(
        modifier = modifier
            .height(barHeight)
            .pointerInteropFilter(
                onTouchEvent = { motionEvent ->

                    when (motionEvent.action) {
                        MotionEvent.ACTION_DOWN -> {
                            val x = motionEvent.x
                            val y = motionEvent.y
                            val dis1 = sqrt(
                                (x - leftCircleOffset.x).pow(2) + (y - leftCircleOffset.y).pow(2)
                            )
                            val dis2 = sqrt(
                                (x - rightCircleOffset.x).pow(2) + (y - rightCircleOffset.y).pow(2)
                            )

                            if (dis1 < circleRadiusInPx) { // left circle clicked
                                leftCircleDragging = true
                            } else if (dis2 < circleRadiusInPx) { // right circle clicked
                                rightCircleDragging = true
                            }
                        }
                        MotionEvent.ACTION_MOVE -> {
                            val x = motionEvent.x

                            if (leftCircleDragging) {
                                progress1 = if (x <= 0) {
                                    0f
                                } else if (x >= width * progress2) {
                                    progress2
                                } else {
                                    x / width
                                }
                                leftCircleOffset = leftCircleOffset.copy(x = width * progress1)
                            } else if (rightCircleDragging) {
                                progress2 = if (x >= width) {
                                    1f
                                } else if (x <= width * progress1) {
                                    progress1
                                } else {
                                    x / width
                                }
                                rightCircleOffset = rightCircleOffset.copy(x = width * progress2)
                            }
                        }
                        MotionEvent.ACTION_UP -> {
                            leftCircleDragging = false
                            rightCircleDragging = false
                            onProgressChanged(progress1, progress2)
                        }
                    }
                    true
                }
            )
            .onGloballyPositioned {
                leftCircleOffset = Offset(x = it.size.width * progress1, y = it.size.height / 2f)
                rightCircleOffset = Offset(x = it.size.width * progress2, y = it.size.height / 2f)
            }
    ) {
        width = this.size.width
        height = this.size.height

        drawRoundRect(
            color = backColor,
            cornerRadius = cornerRadius,
            topLeft = Offset(x = 0f, y = barHeight.toPx() / 4f),
            size = Size(width = width, height = barHeight.toPx() / 2f)
        )

        //draw inner rect (between two circles)
        drawRect(
            color = rangeColor,
            topLeft = Offset(x = width * progress1, y = 0f),
            size = Size(width = width * (progress2 - progress1), height = height)
        )

        //draw left circle
        scale(scaleAnim1, pivot = leftCircleOffset) {
            drawCircle(
                color = rangeColor.copy(alpha = 0.2f),
                radius = circleRadius.toPx(),
                center = leftCircleOffset
            )
        }
        drawCircle(
            color = rangeColor,
            radius = circleRadius.toPx(),
            center = leftCircleOffset
        )

        //draw right circle
        scale(scaleAnim2, pivot = rightCircleOffset) {
            drawCircle(
                color = rangeColor.copy(alpha = 0.2f),
                radius = circleRadius.toPx(),
                center = rightCircleOffset
            )
        }
        drawCircle(
            color = rangeColor,
            radius = circleRadius.toPx(),
            center = rightCircleOffset,
        )

        //draw left Tooltip
        val leftL = leftCircleOffset.x - tooltipWidth.toPx() / 2f
        val topL =
            leftCircleOffset.y - tooltipSpacing.toPx() - circleRadiusInPx - tooltipHeight.toPx()

        val leftR = rightCircleOffset.x - tooltipWidth.toPx() / 2f
        val topR =
            rightCircleOffset.y - tooltipSpacing.toPx() - circleRadiusInPx - tooltipHeight.toPx()

        if (leftCircleDragging || rightCircleDragging) {
            leftTooltipOverlapping.value = (leftL + tooltipWidth.toPx()) >= leftR
        }
        rotate(tooltipAnim1, pivot = leftCircleOffset) {
            drawPath(
                path.apply {
                    reset()
                    addRoundRect(
                        RoundRect(
                            left = leftL,
                            top = topL,
                            right = leftL + tooltipWidth.toPx(),
                            bottom = topL + tooltipHeight.toPx(),
                            cornerRadius = CornerRadius(x = 15f, y = 15f)
                        )
                    )
                    moveTo(
                        x = leftCircleOffset.x - tooltipTriangleSize.toPx(),
                        y = leftCircleOffset.y - circleRadiusInPx - tooltipSpacing.toPx()
                    )
                    relativeLineTo(tooltipTriangleSize.toPx(), tooltipTriangleSize.toPx())
                    relativeLineTo(tooltipTriangleSize.toPx(), -tooltipTriangleSize.toPx())
                    close()
                },
                color = Color(191, 0, 0)
            )
        }

        //draw right Tooltip
        drawPath(
            path.apply {
                reset()
                addRoundRect(
                    RoundRect(
                        left = leftR,
                        top = topR,
                        right = leftR + tooltipWidth.toPx(),
                        bottom = topR + tooltipHeight.toPx(),
                        cornerRadius = CornerRadius(x = 15f, y = 15f)
                    )
                )
                moveTo(
                    x = rightCircleOffset.x - tooltipTriangleSize.toPx(),
                    y = rightCircleOffset.y - circleRadiusInPx - tooltipSpacing.toPx()
                )
                relativeLineTo(tooltipTriangleSize.toPx(), tooltipTriangleSize.toPx())
                relativeLineTo(tooltipTriangleSize.toPx(), -tooltipTriangleSize.toPx())
                close()
            },
            color = Color.Red
        )

        val textLeft = (progress1 * 100).roundToInt().toString()
        var textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(textLeft),
            style = TextStyle(color = Color.White)
        )
        var textSize = textLayoutResult.size

        rotate(tooltipAnim1, pivot = leftCircleOffset) {
            drawText(
                textLayoutResult = textLayoutResult,
                topLeft = Offset(
                    x = leftL + tooltipWidth.toPx() / 2 - textSize.width / 2,
                    y = topL + tooltipHeight.toPx() / 2 - textSize.height / 2
                )
            )
        }

        val textRight = (progress2 * 100).roundToInt().toString()
        textLayoutResult = textMeasurer.measure(
            text = AnnotatedString(textRight),
            style = TextStyle(color = Color.White)
        )
        textSize = textLayoutResult.size

        drawText(
            textLayoutResult = textLayoutResult,
            topLeft = Offset(
                x = leftR + tooltipWidth.toPx() / 2 - textSize.width / 2,
                y = topR + tooltipHeight.toPx() / 2 - textSize.height / 2
            ),
        )
    }
}