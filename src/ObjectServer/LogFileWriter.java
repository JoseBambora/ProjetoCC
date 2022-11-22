package ObjectServer;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author Miguel Cidade Silva
 * Classe cuja função é criar um objeto que assegura a concorrência no processo de escrita de entradas nos ficheiros de log
 * DNSPacket.Data de criação 19/11/2022
 * DNSPacket.Data de edição 19/11/2022
 */
public class LogFileWriter {
    private static final Map<String,ReentrantLock> logLocks = new HashMap<>();
    private static final Lock lockMap = new ReentrantLock();

    /**
     * Construtor de objetos da classe ObjectServer.LogFileWriter
     */
    public LogFileWriter() {}

    /**
     * Getter do lock associado ao ficheiro de log
     * @param ficheiro nome do ficheiro de log
     * @return o lock do ficheiro de log
     */
    public static Lock getLockMap(String ficheiro) {
        lockMap.lock();
        if (!logLocks.containsKey(ficheiro))
            logLocks.put(ficheiro, new ReentrantLock());
        ReentrantLock res = logLocks.get(ficheiro);
        lockMap.unlock();
        return res;
    }

    /**
     * Função encarregada de escrever uma linha num ficheiro de log garantindo concorrência
     * @param ficheiro O ficheiro em que se pretende escrever
     * @param line A linha a escrever
     * @throws IOException Exceção para caso o ficheiro não exista
     */
    public static void writeLineInLogFile(String ficheiro, String line) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ficheiro));
        Lock l = getLockMap(ficheiro);
        l.lock();
        try {
            writer.write(line);
            writer.close();

        } finally {
            l.unlock();
        }
    }

    /**
     * Função encarregada de escrever várias linhas num ficheiro de log garantindo concorrência
     * @param ficheiro O ficheiro em que se pretende escrever
     * @param lines As linhas a escrever sobre a forma de lista
     * @throws IOException Exceção para caso o ficheiro não exista
     */
    public static void writeInLogFile(String ficheiro, List<String> lines) throws IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(ficheiro));
        Lock l = getLockMap(ficheiro);
        l.lock();
        try {
            for(String line : lines){
                writer.write(line);
            }
            writer.close();
        }finally {
            l.unlock();
        }
    }
}
