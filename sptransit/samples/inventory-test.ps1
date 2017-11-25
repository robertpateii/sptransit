start cmd "/k java -cp ..\out\production\samples inventory.Server 3003 inventory.txt"
pause
start cmd "/k java -cp ..\out\production\samples inventory.Client localhost 3003"
start cmd "/k java -cp ..\out\production\samples inventory.Client localhost 3003"

