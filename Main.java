import java.io.*;


public class Main {
    public static void main(String[] args) {

        if (args.length != 2){
            System.err.println("Wrong argument number");
            System.err.println("Usage:");
            System.err.println("Main <input_elf_file> <output_file>");
            System.exit(1);
        }
        byte[] input = new byte[0];
        try (InputStream in = new FileInputStream(args[0])) {
            input = in.readAllBytes();
        } catch (IOException e) {
            System.err.println("An I/O error occurred while reading input file.");
            System.exit(1);
        }

        String result = new Parser(input).parse();
        try (OutputStreamWriter out = new OutputStreamWriter(new FileOutputStream(args[1]))){
            out.write(result);
        } catch (IOException e){
            System.err.println("An I/O error occurred while writing to output file.");
            System.exit(1);
        }
    }
}
