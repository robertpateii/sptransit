# Robert to pull server socket out of runnable
# Robert to slim down runnable to be just a new thread on recieving client
# Someone to start refactoring inventory and reservation samples to use the package
# Someone to update the report with the current status of our package, public methods, hello world, internal methods, decisions, and why
# Samuel to make a diagram showing what it looks like when our package is used to control a message
# Someone to figure out how to add causal ordering as an option to our package
# closing sockets and contexts at some point
# fix to request reply socket that has reply, causal socket won't have reply

causal order:

new public send method that's one way, on context?  doesn't need anything from bind



assume each process has the IP/Port of the other processes and itself

p1:
new context
connect to p3 socket
send message1
close connection
wait
connect to p1 socket
send message2
close connection

p2:
new context
bind to p2 socket
loop on recieving from p2 socket
  receives message2
  print and sleep
  one-way-send message3 to p3
end loop

p3:
listening, looping on listener accept
  gets message3 from p2
  print it and sleep
  goes back to listener until next message
  gets message1 from p1
  print it and sleep
  gets message4 from p2


differentiate one-way-send from a send when you expect a reply





