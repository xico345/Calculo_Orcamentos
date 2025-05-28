// Main.java
public class Main {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.err.println("Uso: java Main <caminho_main.exe> <IDs Serviços>");
            System.exit(1);
        }

        String ocamlExe = args[0]; // Ex: ./main.exe
        String idsServicos = args[1]; // Ex: 1,5,14,16

        IntegradorOCaml integ = new IntegradorOCaml(ocamlExe);
        try {
            String[] orcamento = integ.getOrcamentoFinal(idsServicos);
            
            System.out.println("--- Orçamento Final ---");
            System.out.printf("Peças: €%.2f\n", Double.parseDouble(orcamento[0]));
            System.out.printf("Mão de Obra: €%.2f\n", Double.parseDouble(orcamento[1]));
            System.out.printf("Custos Fixos: €%.2f\n", Double.parseDouble(orcamento[2]));
            System.out.printf("Descontos: €%.2f\n", Double.parseDouble(orcamento[3]));
            System.out.printf("TOTAL: €%.2f\n", Double.parseDouble(orcamento[4]));
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}