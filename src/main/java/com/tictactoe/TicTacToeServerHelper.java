package com.tictactoe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class TicTacToeServerHelper implements Runnable {
    private final static int SERVER_PORT1 = 5000;
    private final static int SERVER_PORT2 = 5001;
    private final static int OFFSET = 5;
    boolean running = true;
    private List<TicTacToeClientHelper> clientHelpers = Collections.synchronizedList(new ArrayList<>());

    private void startClients() {
        System.out.println("startClients()");
        for(TicTacToeClientHelper c : clientHelpers) {
            System.out.println("Client connected to the port = "  + c.getPort());
            Thread thread = new Thread(c);
            thread.start();
        }
    }

    private void stopClients() {
        for(TicTacToeClientHelper c : clientHelpers) {
            System.out.println("Stop client with port = " + c.getPort());
            c.setRunning(false);
            try {
                c.wait();
            } catch (Exception e) {
                System.out.println();
            }
        }
        clientHelpers.clear();
    }

    private void startGame() {
        byte[] sbuf = new byte[14];
        sbuf[0] = '0';
        sbuf[1] = 'A';
        clientHelpers.get(0).sendToClient(sbuf);
        clientHelpers.get(1).sendToClient(sbuf);
    }

    byte getModifiedValue(byte value) {
        if (value == 1) {
            return  11;
        } else {
           return 22;
        }
    }

    // функция проверки выиграшных комбинаций
    private byte[] checkForWinCombination(TicTacToeClientHelper client) {
        byte[] buff = client.getData();
        for(int i = 0; i < 3; i++) {
            if (buff[i + OFFSET] != 0 && (buff[i + OFFSET] == buff[i + 3 + OFFSET] && buff[i + 3 + OFFSET] == buff[i + 6 + OFFSET])) {
                buff[i + OFFSET] = buff[i + 3 + OFFSET] = buff[i + 6 + OFFSET] = getModifiedValue(buff[i + OFFSET]);
                buff[1] = 'D';
                break;
            } else if(i == 0) {
                // проверка диагонали 0 4 8
                if (buff[i + OFFSET] != 0 && (buff[i + OFFSET] == buff[4 + OFFSET] && buff[4 + OFFSET] == buff[8 + OFFSET])) {
                    buff[i + OFFSET] = buff[4 + OFFSET] = buff[8 + OFFSET] = getModifiedValue(buff[i + OFFSET]);
                    buff[1] = 'D';
                    break;
                }
            } else if(i == 2) { // проверка диагонали 2 4 6
                if (buff[i + OFFSET] != 0 && (buff[i + OFFSET] == buff[4 + OFFSET] && buff[4 + OFFSET] == buff[6 + OFFSET])) {
                    buff[i + OFFSET] = buff[4 + OFFSET] = buff[6 + OFFSET] = getModifiedValue(buff[i + OFFSET]);
                    buff[1] = 'D';
                    break;
                }
            }
        }

        for(int i = 0; i < 9; i=i+3) {
            if (buff[i + OFFSET] != 0 && (buff[i + OFFSET] == buff[i + 1 + OFFSET] && buff[i + 1 + OFFSET] == buff[i + 2 + OFFSET])) {
                buff[i + OFFSET] = buff[i + 1 + OFFSET] = buff[i + 2 + OFFSET] = getModifiedValue(buff[i + OFFSET]);
                buff[1] = 'D';
                break;
            }
        }

        // устанавливает команду завершение игры, выиигрышная комбинация наденна.
        if(buff[1] == 'D') {
            client.sendToClient(buff);
        }
        return buff;
    }

    public void setRunning(boolean val) {
        running = val;
    }

    @Override
    public void run() {
        while (running) {
            try {
                // ожидает подключение клиентов
                System.out.println("Starting TicTacToeAcceptHelpers");
                Thread thread1 = new Thread(new TicTacToeAcceptHelper(SERVER_PORT1, clientHelpers));
                thread1.start();
                Thread thread2 = new Thread(new TicTacToeAcceptHelper(SERVER_PORT2, clientHelpers));
                thread2.start();

                System.out.println("Waiting all clients connected.");

                // облокируется до тех пор пока все клиенты не подключатся
                thread1.join();
                thread2.join();

                // запускаются потоки работающие с подключенными клиентами
                startClients();

                // основной цикл работы сервера
                if (clientHelpers.size() == 2 && clientHelpers.get(0).isRunning() && clientHelpers.get(1).isRunning()) {
                    startGame();
                    while(clientHelpers.get(0).isRunning() && clientHelpers.get(1).isRunning()) {
                       if(clientHelpers.get(0).isDataReady()) {
                           byte[] buff = checkForWinCombination(clientHelpers.get(0));
                           clientHelpers.get(1).sendToClient(buff);
                       } else if(clientHelpers.get(1).isDataReady()) {
                           byte[] buff = checkForWinCombination(clientHelpers.get(1));
                           clientHelpers.get(0).sendToClient(buff);
                       }
                        Thread.sleep(100);
                    }
                    stopClients();
                }
                Thread.sleep(1000);
            } catch (Exception e) {
                System.out.println(e.getMessage());
            }
        }
    }
}
