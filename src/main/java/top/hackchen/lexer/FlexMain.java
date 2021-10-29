package top.hackchen.lexer;

import top.hackchen.lexer.generator.LexerGenerator;

import java.io.*;
import java.util.Arrays;

/**
 * @author 陈浩天
 * @date 2021/10/16 11:16 星期六
 */
public class FlexMain {
    public static void main(String[] args) {
        if (args.length == 0) {
            System.out.println("Usage : java -jar c-flex.jar <input file> [<output file>]");
        } else {
            try {
                LexerGenerator generator = new LexerGenerator(args[0]);
                String outfile = args.length >= 2 ? args[1] : new File(args[0]).getAbsoluteFile().getParent() + "\\" + generator.getOptions().className + ".java";
                generator.printToFile(outfile);
                System.out.println("Output: " + outfile);
            } catch (FileNotFoundException e) {
                System.out.println("File not found: " + args[0]);
            } catch (IOException e) {
                System.out.println("IO Error occurred while scanning file: " + e);
            }
        }
    }
}
