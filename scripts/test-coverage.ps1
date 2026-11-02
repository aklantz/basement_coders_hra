param([switch]$PersistenceOnly)

$ErrorActionPreference = 'Stop'
Push-Location (Join-Path $PSScriptRoot '..')
try {
    $output = 'target/coverage'
    New-Item -ItemType Directory -Force "$output/main", "$output/test", "$output/test/data", 'target/coverage-tools' | Out-Null
    foreach ($artifact in @(@('agent', 'org.jacoco.agent', 'runtime'), @('cli', 'org.jacoco.cli', 'nodeps'))) {
        $jar = "target/coverage-tools/$($artifact[0]).jar"
        if (!(Test-Path $jar)) {
            Invoke-WebRequest -UseBasicParsing "https://repo.maven.apache.org/maven2/org/jacoco/$($artifact[1])/0.8.12/$($artifact[1])-0.8.12-$($artifact[2]).jar" -OutFile $jar
        }
    }
    $repository = Join-Path $env:USERPROFILE '.m2/repository'
    $jars = Get-ChildItem $repository -Recurse -Filter *.jar | Where-Object { $_.Name -notmatch '-(sources|javadoc)\.jar$' }
    $classpath = (Join-Path $repository 'junit/junit/4.13.2/junit-4.13.2.jar') + ';' + ($jars.FullName -join ';')
    $sources = Get-ChildItem src/main/java/com/model,src/main/java/com/location -Filter *.java
    javac -g --release 11 -encoding UTF-8 -cp $classpath -d "$output/main" $sources.FullName
    if ($LASTEXITCODE -ne 0) { throw 'Production compilation failed.' }
    $tests = Get-ChildItem src/test/java -Recurse -Filter *.java
    javac -g --release 11 -encoding UTF-8 -cp "$output/main;$classpath" -d "$output/test" $tests.FullName
    if ($LASTEXITCODE -ne 0) { throw 'Test compilation failed.' }
    Copy-Item src/test/resources/data/*.json "$output/test/data"
    $hashes = @{}
    Get-ChildItem json,src/test/resources -Recurse -Filter *.json | ForEach-Object { $hashes[$_.FullName] = (Get-FileHash $_.FullName).Hash }
    $names = $tests | ForEach-Object { 'com.' + $_.Directory.Name + '.' + $_.BaseName }
    if ($PersistenceOnly) { $names = @('com.model.DataLoaderTest', 'com.model.DataWriterTest') }
    # JUnit failures still produce coverage; retain their exit status and full log.
    $ErrorActionPreference = 'Continue'
    java "-javaagent:target/coverage-tools/agent.jar=destfile=$output/jacoco.exec,append=false" -cp "$output/test;$output/main;$classpath" org.junit.runner.JUnitCore $names *> "$output/tests.log"
    $testExit = $LASTEXITCODE
    $ErrorActionPreference = 'Stop'
    $classArgs = @()
    Get-ChildItem "$output/main" -Recurse -Filter *.class | Where-Object { $_.BaseName -notin @('UI', 'ReliefUI', 'LoginTest') } | ForEach-Object { $classArgs += @('--classfiles', $_.FullName) }
    java -jar target/coverage-tools/cli.jar report "$output/jacoco.exec" @classArgs --sourcefiles src/main/java --html "$output/html" --xml "$output/report.xml" --csv "$output/report.csv"
    if ($LASTEXITCODE -ne 0) { throw 'Coverage reporting failed.' }
    foreach ($path in $hashes.Keys) {
        if ((Get-FileHash $path).Hash -ne $hashes[$path]) { throw "JSON changed: $path" }
    }
    Get-Content "$output/tests.log" -Tail 4
    Import-Csv "$output/report.csv" | Where-Object { $_.CLASS -in @('DataLoader','DataWriter') } | ForEach-Object {
        $covered = [int]$_.LINE_COVERED
        $total = $covered + [int]$_.LINE_MISSED
        Write-Output "$($_.CLASS): $covered/$total lines ($([math]::Round(100*$covered/$total, 1))%)"
    }
    Write-Output "JUnit exit status: $testExit. Report: $output/html/index.html. Application/source JSON hashes unchanged."
} finally {
    Pop-Location
}
exit $testExit
