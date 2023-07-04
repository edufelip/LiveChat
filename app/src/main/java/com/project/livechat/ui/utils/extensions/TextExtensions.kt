package com.project.livechat.ui.utils.extensions

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
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