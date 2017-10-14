start cmd /k "java -cp build Server < inputs\server1.txt"
TIMEOUT 10
start cmd /k "java -cp build Server < inputs\server2.txt"
TIMEOUT 10
start cmd /k "java -cp build Client < inputs\client1.txt"
start cmd /k "java -cp build Client < inputs\client2.txt"