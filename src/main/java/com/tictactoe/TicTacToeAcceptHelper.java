package com.tictactoe;

import java.net.ServerSocket;
import java.net.Socket;
import java.util.List;

/**
 * Данный класс содержит логику ожидания подключения через сокет.
 * Выполнен в потоках для того, что бы играки могли асинзронно подключиться к прослушивающим портам.
 * После установления подключения, поток завершает саою работу.
 *
 * Конструктор получает номер порта, который будет слушать и потокобезопасный список содержащий обьекты
 * подключенных клиентов.
 */
public class TicTacToeAcceptHelper implements Runnable {
    int port;
    List<TicTacToeClientHelper> clientHelpers;

    TicTacToeAcceptHelper(int port, List<TicTacToeClientHelper> clientHelpers) {
        this.port = port;
        this.clientHelpers = clientHelpers;
    }

    public void run() {
        System.out.println("Waiting connection to the port " + port);
        try {
            synchronized (this) {
                ServerSocket serverSocket = new ServerSocket(port);
                System.out.println("Accepting socket on port " + port);
                Socket connectedSocket = serverSocket.accept();
                System.out.println("Client connected to the port " + port);

                synchronized (clientHelpers) {
                    clientHelpers.add(new TicTacToeClientHelper(connectedSocket, port));
                }
            }
        }
        catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
