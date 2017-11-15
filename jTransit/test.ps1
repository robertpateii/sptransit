echo "need to run servers in background and be able to kill them"
start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server1.txt"
sleep 5
start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server2.txt"
sleep 5
start cmd "/k java -cp target\classes Samples.Reservations.Server < inputs\server3.txt"
sleep 5
Get-Content inputs\client1.txt | java -cp target\classes Samples.Reservations.Client
# https://stackoverflow.com/questions/16098366/can-i-send-some-text-to-the-stdin-of-an-active-process-under-windows