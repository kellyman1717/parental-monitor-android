package com.parentalmonitor.ui.theme

import androidx.compose.ui.graphics.Color

/**
 * Calm, muted color palette — Sage Green & Cream.
 * Maps directly from palettes.jsx → PALETTE_SAGE.
 */
object DesignColors {
    // Backgrounds
    val Bg           = Color(0xFFF5F2EC)   // warm cream
    val Surface      = Color(0xFFFFFFFF)   // cards
    val SurfaceAlt   = Color(0xFFEAE6DC)   // elevated surface
    val Border       = Color(0xFFD9D3C4)   // soft border

    // Primary — sage green
    val Primary      = Color(0xFF7C9885)
    val PrimarySoft  = Color(0xFFA8BFA8)
    val PrimaryDark  = Color(0xFF5A7560)
    val OnPrimary    = Color(0xFFFFFFFF)

    // Secondary / Accent
    val Secondary    = Color(0xFFC2A878)   // warm sand
    val Accent       = Color(0xFF9C8AA5)   // dusty mauve

    // Text
    val Text         = Color(0xFF2F3A33)   // deep forest
    val TextMuted    = Color(0xFF6B7568)   // warm gray
    val TextSubtle   = Color(0xFF9A9F8E)   // light gray

    // Semantic
    val Success      = Color(0xFF8FAD8B)   // muted green
    val Warning      = Color(0xFFD4A574)   // muted amber
    val Danger       = Color(0xFFC17B6E)   // muted terracotta
    val Info         = Color(0xFF8AA1B5)   // dusty blue

    // Shadow
    val Shadow       = Color(0x14556450)   // rgba(85,100,80,0.08)
}
