$newArgs = @("-jar", "blaze.jar")
for ($i = 0; $i -lt $args.count; $i++) {
  $newArgs += $args[$i]
}

$process = Start-Process -FilePath "java.exe" -NoNewWindow -Wait -ArgumentList $newArgs