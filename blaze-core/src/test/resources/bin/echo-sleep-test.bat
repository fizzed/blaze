@echo OFF
for /l %%x in (1, 1, 5000) do (
  echo Iteration %%x [will now sleep for 1 sec]
  ping /n 1 /w 1000 localhost >nul
)