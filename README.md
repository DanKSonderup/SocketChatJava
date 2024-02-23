# SocketChatJava
 
SocketChatJava is a side project developed to showcase my knowledge of Socket programming, Thread management, Event handling using GUI and structure of programming.

The program consists of 2 seperate App classes. Both act as both client and Server as my wish was to recreate a true P2P experience.

The program uses a ClientController and ServerController depending on who initiates the contact.

When a user launches the App their ServerSocket info is automatically subscribed to a NameServer which other Apps can then send a request to using a name instead of IP and port.

I've used Threads to handle all the streams between the client and server so that both users can type simultaneously and the GUI will not be blocked while it waits for an IO call. 

Using an Observer Pattern I've tried to keep the 3-layer clean architecthure by letting the lower layers notify the GUI once new data has been received over the socket.

The code has not been optimized and will contain room for improvement, regardless please feel free to take a look to get an insight to some of my programming abilities regarding Sockets, Threads and Observer Pattern

![Screenshot1](https://github.com/DanKSonderup/SocketChatJava/blob/main/SocketChatsh1.png)
