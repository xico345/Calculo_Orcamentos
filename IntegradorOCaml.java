// IntegradorOCaml.java
import java.io.*;
import java.util.*;
import java.util.stream.*;

public class IntegradorOCaml {
    private final String ocamlExe;

    public IntegradorOCaml(String ocamlExe) {
        this.ocamlExe = ocamlExe;
    }

    /**
     * Executa um comando OCaml com um conjunto de IDs e retorna as linhas de saída.
     * Cada linha de saída é dividida por ';' e retornada como um array de strings.
     * @param cmd O comando OCaml a ser executado (ex: "orcamento_final", "orcamento_items").
     * @param ids Uma string de IDs separados por vírgulas (ex: "1,2,3").
     * @return Uma lista de arrays de strings, onde cada array representa uma linha de saída.
     * @throws IOException Se ocorrer um erro de I/O.
     * @throws InterruptedException Se a execução do processo for interrompida.
     * @throws RuntimeException Se o OCaml retornar um código de erro.
     */
    private List<String[]> run(String cmd, String ids) throws IOException, InterruptedException {
        ProcessBuilder pb = new ProcessBuilder(ocamlExe, cmd, ids);
        pb.redirectErrorStream(true); // Redireciona o erro padrão para o output padrão
        Process p = pb.start(); // Inicia o processo OCaml

        List<String[]> out = new ArrayList<>();
        try (BufferedReader r = new BufferedReader(new InputStreamReader(p.getInputStream()))) {
            String line;
            while ((line = r.readLine()) != null && !line.isBlank()) {
                out.add(Arrays.stream(line.split(";")) // Divide a linha por ';'
                              .map(String::trim)      // Remove espaços em branco
                              .toArray(String[]::new)); // Converte para array de strings
            }
        }

        int exitCode = p.waitFor(); // Espera o processo terminar
        if (exitCode != 0) {
            throw new RuntimeException("OCaml " + cmd + " falhou com código de saída " + exitCode);
        }
        return out;
    }

    /**
     * Obtém o orçamento final calculado pelo OCaml.
     * @param ids Uma string de IDs de serviços e/ou itens.
     * @return Um array de strings contendo [Peças, Mão de Obra, Custos Fixos, Descontos, TOTAL].
     * @throws IOException Se ocorrer um erro de I/O.
     * @throws InterruptedException Se a execução do processo for interrompida.
     * @throws RuntimeException Se o OCaml não retornar dados de orçamento final.
     */
    public String[] getOrcamentoFinal(String ids) throws IOException, InterruptedException {
        List<String[]> res = run("orcamento_final", ids);
        if (res.isEmpty()) {
            throw new RuntimeException("Sem dados de orçamento final");
        }
        return res.get(0); // Retorna a primeira (e única) linha de resultados
    }

    /**
     * Obtém a lista de itens relacionados aos IDs fornecidos, com detalhes.
     * @param ids Uma string de IDs de serviços e/ou itens.
     * @return Uma lista de objetos Item.
     * @throws IOException Se ocorrer um erro de I/O.
     * @throws InterruptedException Se a execução do processo for interrompida.
     */
    public List<Item> getOrcamentoItems(String ids) throws IOException, InterruptedException {
        List<String[]> res = run("orcamento_items", ids);
        return res.stream()
            .map(parts -> new Item(
                Integer.parseInt(parts[0]), // id
                parts[1],                   // nome
                parts[2],                   // marca
                parts[3],                   // tipo
                Double.parseDouble(parts[4]), // custo
                Double.parseDouble(parts[5]), // preco (já com desconto aplicado no OCaml)
                Integer.parseInt(parts[6])  // quantidade
            ))
            .collect(Collectors.toList());
    }

    /**
     * Obtém a lista de serviços mecânicos relacionados aos IDs fornecidos, com detalhes de custo.
     * @param ids Uma string de IDs de serviços.
     * @return Uma lista de objetos ServicoMecanico.
     * @throws IOException Se ocorrer um erro de I/O.
     * @throws InterruptedException Se a execução do processo for interrompida.
     */
    public List<ServicoMecanico> getOrcamentoMecanico(String ids) throws IOException, InterruptedException {
        List<String[]> res = run("orcamento_mecanico", ids);
        return res.stream()
            .map(parts -> new ServicoMecanico(
                Integer.parseInt(parts[0]),   // id
                Double.parseDouble(parts[1]), // horas (agora é o segundo campo)
                Double.parseDouble(parts[2]), // custo_min_mecanico (agora é o terceiro campo)
                Double.parseDouble(parts[3]), // custo_sem_desconto (agora é o quarto campo)
                Double.parseDouble(parts[4]), // desconto_aplicado (agora é o quinto campo)
                Double.parseDouble(parts[5])  // custo_com_desconto (agora é o sexto campo)
            ))
            .collect(Collectors.toList());
    }
}
