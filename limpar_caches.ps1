Write-Host "Iniciando limpeza de caches..." -ForegroundColor Cyan

# 1. Parar Daemons do Gradle
Write-Host "Parando Gradle Daemons..." -ForegroundColor Yellow
if (Test-Path ".\gradlew.bat") {
    .\gradlew.bat --stop
} else {
    Write-Host "Gradlew não encontrado no diretório atual, tentando parar globalmente..."
    gradle --stop 2>$null
}

# 2. Limpar Caches do Projeto Local
Write-Host "Limpando caches locais do projeto..." -ForegroundColor Yellow
$projectPaths = @(
    ".gradle",
    "build",
    "app\build"
)

foreach ($path in $projectPaths) {
    if (Test-Path $path) {
        Write-Host "Removendo $path..."
        Remove-Item -Recurse -Force $path -ErrorAction SilentlyContinue
    }
}

# 3. Limpar Cache Global do Gradle
Write-Host "Limpando caches globais do Gradle..." -ForegroundColor Yellow
$globalGradleCache = "$env:USERPROFILE\.gradle\caches"
if (Test-Path $globalGradleCache) {
    Write-Host "Removendo conteúdo de $globalGradleCache..."
    # Remover apenas o conteúdo para manter a pasta raiz se desejar, ou remover tudo. Removemos tudo para garantir.
    Remove-Item -Recurse -Force $globalGradleCache -ErrorAction SilentlyContinue
} else {
    Write-Host "Cache global do Gradle não encontrado em $globalGradleCache"
}

# 4. Limpar Caches do Android Studio
Write-Host "Procurando caches do Android Studio..." -ForegroundColor Yellow
$googleDir = "$env:LOCALAPPDATA\Google"

if (Test-Path $googleDir) {
    $studioDirs = Get-ChildItem -Path $googleDir -Filter "AndroidStudio*" -Directory
    
    if ($studioDirs) {
        foreach ($dir in $studioDirs) {
            $cachePath = Join-Path -Path $dir.FullName -ChildPath "caches"
            if (Test-Path $cachePath) {
                Write-Host "Removendo cache de $($dir.Name)..."
                Remove-Item -Recurse -Force $cachePath -ErrorAction SilentlyContinue
            } else {
                Write-Host "Pasta 'caches' não encontrada para $($dir.Name)"
            }
        }
    } else {
        Write-Host "Nenhuma instalação do Android Studio encontrada em $googleDir"
    }
}

Write-Host "Limpeza concluída! Recomenda-se reiniciar o Android Studio." -ForegroundColor Green
Write-Host "Pressione qualquer tecla para sair..."
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown")
