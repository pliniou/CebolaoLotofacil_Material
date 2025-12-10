package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color

// --- FLAT MODERN PALETTE ---
// A sophisticated, clean, and professional palette.
// Base: Slate/Gray for neutral surfaces.
// Accents: Vibrant but flat Indigo/Emerald for actions.

val FlatWhite = Color(0xFFFFFFFF)
val FlatBlack = Color(0xFF000000)

// Grays / Slates
val Slate50 = Color(0xFFF8FAFC)
val Slate100 = Color(0xFFF1F5F9)
val Slate200 = Color(0xFFE2E8F0)
val Slate300 = Color(0xFFCBD5E1) // Borders / Disabled
val Slate400 = Color(0xFF94A3B8) // Secondary Text
val Slate500 = Color(0xFF64748B)
val Slate700 = Color(0xFF334155) // Primary Text (on light)
val Slate800 = Color(0xFF1E293B) // Surfaces (Dark)
val Slate900 = Color(0xFF0F172A) // Background (Dark)
val Slate950 = Color(0xFF020617) // Deep Background

// Accents (Primary - Violet/Indigo)
val Violet500 = Color(0xFF6366F1) // Primary Action
val Violet600 = Color(0xFF4F46E5) // Primary Pressed
val Violet400 = Color(0xFF818CF8) // Primary Lighter

// Secondary (Emerald/Green)
val Emerald500 = Color(0xFF10B981) // Success / Secondary
val Emerald600 = Color(0xFF059669)

// Semantic Colors
val ErrorRed = Color(0xFFEF4444)
val WarningAmber = Color(0xFFF59E0B)
val InfoSky = Color(0xFF0EA5E9)

// --- SEMANTIC MAPPING (Dark Theme Default) ---

// Backgrounds
val AppBackground = Slate950
val SurfaceColor = Slate900
val SurfaceHighlight = Slate800

// Content
val TextPrimary = FlatWhite
val TextSecondary = Slate400
val TextDisabled = Slate500

// Actions
val PrimaryAction = Violet500
val SecondaryAction = Emerald500

// Lotofacil Identity (Generic Mappings)
val BrandVerde = Emerald500
val BrandRosa = Color(0xFFEC4899) // Pink-500 equivalent
val BrandAzul = InfoSky
val BrandAmarelo = WarningAmber
val BrandVermelho = ErrorRed
val BrandRoxo = Violet500
val BrandLaranja = Color(0xFFF97316)

// Compatibility Mappings for Existing Code
val DarkBackground = AppBackground
val DarkSurface = SurfaceColor
val DarkSurfaceElevated = SurfaceHighlight
val DarkSurfaceHighlight = Slate700

val WhiteHighEmphasis = TextPrimary
val WhiteMediumEmphasis = TextSecondary
val WhiteDisabled = TextDisabled

// State Colors
val ErrorColor = ErrorRed
val SuccessColor = Emerald500
val WarningColor = WarningAmber
val InfoColor = InfoSky

// Legacy (Can be removed later if unused)
val CaixaBlue = Color(0xFF005CA9)
val CaixaOrange = Color(0xFFF39200)

val GradientPrimaryStart = Violet600
val GradientPrimaryEnd = Violet400