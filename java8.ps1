$env:JAVA_HOME = "C:\Program Files\Java\jdk-1.8"
$env:M2_HOME = "C:\ambiente\ambiente_tjce\others\apache-maven-3.6.3"
$env:Path = "$env:JAVA_HOME\bin;" + "$env:M2_HOME\bin;" + $env:Path
Write-Host "Java 8 ativado no PowerShell." -ForegroundColor Green
java -version
mvn --version
