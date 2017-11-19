# Transit is package for controlling distribution of messages between java programs.
 The user can choose to use it asynchronously or synchronously as Transit ansynchrounsly sends
 and recieves messages into a queue.

 In a typical hello world from a client to the server, the user will bind the server to an IP. The user then
 has the client connect to the IP and send a message. The Transit context on the client will add onto the message
 information about itself so the server can reply to it at anytime (or never). To facilitate this the client
 of course needs to listen on its own port.

 Question: should the client's listener context ignore any messages from anyone but the servers its sent messages to?
 or just ones it hasn't gotten replies from yet?

 The server acts like a UDP server, listening for clients, spinning off a thread to deal with them,
and storing the clients reply-to information for later reply. This is all in the context though, the user
will simply be iterating over a queue of expected messages.

All this is over TCP so we know whether or not a message is actually received. We just don't know if we'll get a reply.
To facilitate this, the context will always reply with an ack to any message.

Question: and then close it? Who closes this stuff?
