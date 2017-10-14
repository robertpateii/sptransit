start cmd /k "java -cp build Server < inputs\server1.txt"
pause
start cmd /k "java -cp build Server < inputs\server2.txt"
pause
start cmd /k "java -cp build Client < inputs\client1.txt"
start cmd /k "java -cp build Client < inputs\client2.txt"
