# this doesn't seem to work but would be nice:
# $server1 = Start-Job -ScriptBlock {Get-Content inputs\server1.txt | java -cp target\classes Samples.Reservations.Server}

start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server1.txt"
sleep 5

start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server2.txt"
sleep 5

start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server3.txt"
sleep 5

java -cp target\classes Samples.Reservations.Client 3 127.0.0.1:8025 127.0.0.1:8030 127.0.0.1:8035