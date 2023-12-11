package hexaworld.net;

import java.net.Socket;

public interface TCPReceiver {
  void receiveTCP(Socket tcpClientSocket);
}