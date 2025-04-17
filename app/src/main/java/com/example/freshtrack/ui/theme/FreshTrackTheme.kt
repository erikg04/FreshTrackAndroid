package com.example.freshtrack.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

// ── Icon‑derived colors ───────────────────────────────────────────────
private val IconTeal   = Color(0xFF3CA795)  // clipboard background
private val IconBrown  = Color(0xFF795548)  // clipboard border
private val IconYellow = Color(0xFFFDD835)  // clip
private val IconCream  = Color(0xFFFFF3E0)  // paper
private val IconRed    = Color(0xFFD32F2F)  // apple
private val IconGreen  = Color(0xFF66BB6A)  // leaf
private val IconDark   = Color(0xFF212121)  // checkmarks/text

// ── Light scheme ────────────────────────────────────────────────────
private val LightColorScheme = lightColorScheme(
    primary       = IconTeal,
    onPrimary     = IconCream,
    secondary     = IconGreen,
    onSecondary   = IconDark,
    tertiary      = IconRed,
    onTertiary    = IconCream,
    background    = IconCream,
    onBackground  = IconDark,
    surface       = IconCream,
    onSurface     = IconDark,
    error         = IconRed,
    onError       = IconCream
)

// ── Dark scheme ─────────────────────────────────────────────────────
private val DarkColorScheme = darkColorScheme(
    primary       = IconTeal,
    onPrimary     = IconCream,
    secondary     = IconGreen,
    onSecondary   = IconDark,
    tertiary      = IconRed,
    onTertiary    = IconCream,
    background    = IconTeal,
    onBackground  = IconCream,
    surface       = IconBrown,
    onSurface     = IconCream,
    error         = IconRed,
    onError       = IconCream
)

// ── Theme wrapper ───────────────────────────────────────────────────
@Composable
fun FreshTrackTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colors = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colors,
        typography = Typography,
        content = content
    )
}