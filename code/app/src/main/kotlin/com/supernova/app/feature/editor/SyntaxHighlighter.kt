package com.supernova.app.feature.editor

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation

class CodeHighlighter : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val build = AnnotatedString.Builder(text.text)
        
        // Keywords (Kotlin, Python, JS, Bash, Java)
        val keywords = Regex("\\b(fun|val|var|class|if|else|import|package|return|for|while|def|print|from|const|let|function|public|private|interface|object|when|try|catch|finally|throw|type|async|await|static|final|break|continue|in|is|as|this|super|new|void|null|true|false|export|default|echo|if|then|fi|elif|case|esac|do|done)\\b")
        keywords.findAll(text.text).forEach {
            build.addStyle(SpanStyle(color = Color(0xFFBD93F9), fontWeight = FontWeight.Bold), it.range.first, it.range.last + 1)
        }
        
        // Strings
        val strings = Regex("\".*?\"|'.*?'|`.*?`")
        strings.findAll(text.text).forEach {
            build.addStyle(SpanStyle(color = Color(0xFFF1FA8C)), it.range.first, it.range.last + 1)
        }

        // Comments
        val comments = Regex("//.*|#.*|/\\*.*?\\*/")
        comments.findAll(text.text).forEach {
            build.addStyle(SpanStyle(color = Color(0xFF6272A4)), it.range.first, it.range.last + 1)
        }

        // Numbers
        val numbers = Regex("\\b\\d+(\\.\\d+)?\\b")
        numbers.findAll(text.text).forEach {
            build.addStyle(SpanStyle(color = Color(0xFFFFB86C)), it.range.first, it.range.last + 1)
        }
        
        // Function calls
        val functions = Regex("\\b[a-zA-Z_][a-zA-Z0-9_]*(?=\\s*\\()")
        functions.findAll(text.text).forEach {
            build.addStyle(SpanStyle(color = Color(0xFF50FA7B)), it.range.first, it.range.last + 1)
        }

        return TransformedText(build.toAnnotatedString(), OffsetMapping.Identity)
    }
}
