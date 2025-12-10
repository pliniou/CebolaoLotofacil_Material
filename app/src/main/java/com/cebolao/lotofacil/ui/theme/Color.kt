package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color

// --- BRAND IDENTITY (Cores Chapadas e Vivas) ---
// Cores inspiradas em Neon/Cyberpunk mas com legibilidade Material Design
val BrandVerde = Color(0xFF00E676)  // Verde Neon (Primary Action)
val BrandRosa = Color(0xFFFF4081)   // Rosa Neon (Secondary/Stats)
val BrandAzul = Color(0xFF42A5F5)   // Azul (Info)
val BrandAmarelo = Color(0xFFFFC107) // Amarelo (Warning)
val BrandVermelho = Color(0xFFFF5252) // Vermelho (Error)
val BrandRoxo = Color(0xFFD500F9)   // Roxo (Alternative)
val BrandLaranja = Color(0xFFFF9100) // Laranja (Alternative)

// --- BACKGROUNDS & SURFACES (Dark Mode Only) ---
val DarkBackground = Color(0xFF050608) // Fundo Principal (Deep Dark)
val DarkSurface = Color(0xFF111318)    // Cards / Superfícies
val DarkSurfaceElevated = Color(0xFF1E2127) // Divisores / Bordas / Inativos
val DarkSurfaceHighlight = Color(0xFF2A2D35) // Destaques sutis

// --- CONTENT COLORS ---
val WhiteHighEmphasis = Color(0xFFFFFFFF)
val WhiteMediumEmphasis = Color(0xFF9BA0B0) // Texto secundário (Blue-ish Grey)
val WhiteDisabled = Color(0xFF1E2127)       // Inativo / Bordas

// --- STATE COLORS ---
// --- STATE COLORS ---
val ErrorColor = BrandVermelho
val SuccessColor = BrandVerde
val WarningColor = BrandAmarelo
val InfoColor = BrandAzul

// --- LOTOFÁCIL SPECIFIC ---
val CaixaBlue = Color(0xFF005CA9)    // Mantendo identidade oficial onde necessário
val CaixaOrange = Color(0xFFF39200)

// --- GRADIENTS STUB (Caso precise no futuro) ---
val GradientPrimaryStart = BrandRoxo
val GradientPrimaryEnd = BrandAzul