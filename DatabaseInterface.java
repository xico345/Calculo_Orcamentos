// DatabaseInterface.java
import java.io.*;
import java.util.*;

public class DatabaseInterface {

    /** Executa o binário OCaml e converte cada linha em um Item */
    public List<Item> listarItems() throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder("./main.exe", "listar_items");
        pb.redirectErrorStream(true);
        Process proc = pb.start();

        List<Item> items = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(
                 new InputStreamReader(proc.getInputStream()))) {

            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split(";");
                if (parts.length == 7) {
                    int id        = Integer.parseInt(parts[0].trim());
                    String nome   = parts[1].trim();
                    String marca  = parts[2].trim();
                    String tipo   = parts[3].trim();
                    double custo  = Double.parseDouble(parts[4].trim());
                    double preco  = Double.parseDouble(parts[5].trim());
                    int qtd       = Integer.parseInt(parts[6].trim());
                    items.add(new Item(id, nome, marca, tipo, custo, preco, qtd));
                }
            }
        }

        int exitCode = proc.waitFor();
        if (exitCode != 0) {
            throw new RuntimeException("OCaml retornou código de erro " + exitCode);
        }
        return items;
    }

    public static void main(String[] args) {
        DatabaseInterface di = new DatabaseInterface();
        try {
            List<Item> items = di.listarItems();
            // Aqui podemos exibir num GUI, ou simplesmente no console:
            items.forEach(System.out::println);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}