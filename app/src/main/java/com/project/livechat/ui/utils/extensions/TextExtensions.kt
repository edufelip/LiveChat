package com.project.livechat.ui.utils.extensions

import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.material3.LocalTextStyle
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextLayoutResult
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle

fun buildLinkText(textIterable: List<AnnotatedStrStruct>, primary: Color): AnnotatedString {
    return buildAnnotatedString {
        for (element in textIterable) {
            when (element.type) {
                is AnnotatedStructType.LINK -> {
                    pushStringAnnotation(tag = element.type.tag, annotation = "")
                    withStyle(style = SpanStyle(color = primary)) {
                        append(element.text)
                    }
                    pop()
                }
                AnnotatedStructType.REGULAR -> {
                    append(element.text)
                }
            }
        }
    }
}

class AnnotatedStrStruct(
    val text: String,
    val type: AnnotatedStructType
)

sealed class AnnotatedStructType {
    object REGULAR : AnnotatedStructType()
    data class LINK(val tag: String) : AnnotatedStructType()
}

@Composable
fun LinkText(
    text: AnnotatedString,
    modifier: Modifier = Modifier,
    style: TextStyle = LocalTextStyle.current,
    onClick: (String) -> Unit
) {
    var textLayoutResult by remember { mutableStateOf<TextLayoutResult?>(null) }
    Text(
        text = text,
        style = style,
        modifier = modifier.pointerInput(text) {
            detectTapGestures { position ->
                val layoutResult = textLayoutResult ?: return@detectTapGestures
                val offset = layoutResult.getOffsetForPosition(position)
                text.getStringAnnotations(start = offset, end = offset)
                    .firstOrNull()
                    ?.let { onClick(it.tag) }
            }
        },
        onTextLayout = { textLayoutResult = it }
    )
}
