package com.tictactoe;

import java.io.*;
import java.net.Socket;

/**
 * Класс содержил логику чтения данных от клиента и отправки данных клиенту.
 * Реализован ввиде потока. Получает подключенный сокет и порт подключения.
 * Далее поток садится на ожидания получения данных от клиента через открытый сокет.
 */
public class TicTacToeClientHelper implements Runnable {
    private final Socket connectedSocket;
    byte[] cbuf = new byte[14];
    boolean dataReady = false;
    int port;
    boolean running = false;
    int numberOfReadData;
    DataInputStream input;
    DataOutputStream output;

    public TicTacToeClientHelper(Socket connectedSocket, int port) {
        this.connectedSocket = connectedSocket;
        this.port = port;

        // создаются потоки чтения записи из сокета.
        try {
            input = new DataInputStream(connectedSocket.getInputStream());
            output = new DataOutputStream(connectedSocket.getOutputStream());
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public boolean isDataReady() {
        return dataReady;
    }

    public void dataNotReady() {
        dataReady = false;
    }

    public int getPort() {
        return port;
    }

    public byte[] getData() {
        dataNotReady();
        return cbuf;
    }

    public boolean isRunning() {
        return running;
    }

    // функция отправки данных клиенту
    public void sendToClient(byte[] sbuf) {
        try {
            System.out.print("Port = " + port + " sent     data: ");
            for(int i = 0; i < numberOfReadData; i++) {
                System.out.print(String.format("0x%02X ", sbuf[i]));
            }
            System.out.println();

            output.write(sbuf);
            dataNotReady();
        } catch (Exception e){
            System.out.println(e.getMessage());
        }
    }

    public void setRunning(boolean val) {
        running = val;
    }

    // основная логика чтения данных от клиента
    public void run() {
        running = true;
        try {
            while (((numberOfReadData = input.read(cbuf))  > -1) && running) {
                dataReady = true;
                System.out.print(String.format("Port = " + port + " received data: "));
                for(int i = 0; i < numberOfReadData; i++) {
                    System.out.print(String.format("0x%02X ", cbuf[i]));
                }
                System.out.println();
            }
            running = false;
            connectedSocket.close();
            System.out.println("Socket closed. Port = " + port);
        } catch (Exception e) {
            running = false;
            System.out.println(e.getMessage());
        }
    }
}
