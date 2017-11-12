start cmd /k "java -cp build Server < inputs\server1.txt"
pause
start cmd /k "java -cp build Server < inputs\server2.txt"
start cmd /k "java -cp build Server < inputs\server3.txt"
start cmd /k "java -cp build Server < inputs\server4.txt"
start cmd /k "java -cp build Server < inputs\server5.txt"
start cmd /k "java -cp build Server < inputs\server6.txt"
pause
start cmd /k "java -cp build Client"
