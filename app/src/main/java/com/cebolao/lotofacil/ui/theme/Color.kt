package com.cebolao.lotofacil.ui.theme

import androidx.compose.ui.graphics.Color

// --- BRAND IDENTITY (Cores Chapadas e Vivas) ---
// Cores inspiradas em Neon/Cyberpunk mas com legibilidade Material Design
val BrandAzul = Color(0xFF2979FF)   // Azul Elétrico
val BrandRoxo = Color(0xFFD500F9)   // Roxo Neon
val BrandVerde = Color(0xFF00E676)  // Verde Matrix
val BrandAmarelo = Color(0xFFFFEA00) // Amarelo Laser
val BrandRosa = Color(0xFFFF4081)   // Rosa Choque
val BrandLaranja = Color(0xFFFF9100) // Laranja Vivo

// --- BACKGROUNDS & SURFACES (Dark Mode Only) ---
// Tons de cinza azulado muito escuro para contraste alto com as cores vivas
val DarkBackground = Color(0xFF121212) // Quase preto absoluto
val DarkSurface = Color(0xFF1E1E1E)    // Cinza escuro padrão Material
val DarkSurfaceElevated = Color(0xFF2D2D2D) // Um pouco mais claro para cards
val DarkSurfaceHighlight = Color(0xFF383838) // Destaques

// --- CONTENT COLORS ---
val WhiteHighEmphasis = Color(0xFFFFFFFF)
val WhiteMediumEmphasis = Color(0xB3FFFFFF) // 70%
val WhiteDisabled = Color(0x61FFFFFF)       // 38%

// --- STATE COLORS ---
val ErrorColor = Color(0xFFFF5252)   // Vermelho Alerta
val SuccessColor = BrandVerde        // O mesmo verde da marca
val WarningColor = BrandAmarelo      // O mesmo amarelo da marca
val InfoColor = BrandAzul            // O mesmo azul da marca

// --- LOTOFÁCIL SPECIFIC ---
val CaixaBlue = Color(0xFF005CA9)    // Mantendo identidade oficial onde necessário
val CaixaOrange = Color(0xFFF39200)

// --- GRADIENTS STUB (Caso precise no futuro) ---
val GradientPrimaryStart = BrandRoxo
val GradientPrimaryEnd = BrandAzul