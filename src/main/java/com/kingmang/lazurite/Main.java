package com.kingmang.lazurite;

import com.kingmang.lazurite.base.Stop;
import com.kingmang.lazurite.parser.ast.Statement;
import com.kingmang.lazurite.parser.pars.*;
import com.kingmang.lazurite.parser.pars.FunctionAdder;
import com.kingmang.lazurite.runtime.Time;

import java.io.BufferedReader;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.List;
import java.util.concurrent.TimeUnit;

import java.util.Scanner;



public class Main  {

    public static String VERSION(){
        return "2.6";
    }
    public static String link = "";
    public static int check = 0;
    static Settings setting = new Settings();

    public static void main(String[] args) throws IOException{
        while(true)Start();



    }

    public static void Start() throws IOException {
        System.out.println("\n");
        BufferedReader sc = new BufferedReader(new InputStreamReader(System.in));
        String cmd = sc.readLine();
        if (cmd.contains("lazurite") || cmd.contains("lzr")) {
            String[] objs = cmd.split(" ");
            if (objs[1].equals("--run")||objs[1].equals("-r")) {
                System.out.print("\n\nEnter path to your file: ");
                Scanner scan = new Scanner(System.in);
                String in = scan.nextLine();
                check = 0;
                RUN(in);
            }else if(objs[1].equals("--help")||objs[1].equals("-h")){
                Help();
                check = 0;
            }else if(objs[1].equals("--version")||objs[1].equals("-v")){
                ver();
                check = 0;
            }
         }else if(cmd.contains("cls")|| cmd.contains("clear")){
            check = 0;
            clear();
        }else{
            ver();
        }

    }

    public  static void Help(){
        System.out.println(
                "\n--run / -r - asks for the path to the file and runs it\n" +
                        "--version / -v - returns the version of Lazurite\n" +
                        "--help / -h - show help commands\n" +
                        "--timetest/ -tt - shows how long it took to run the program\n"+
                        "cls - clears the command line\n"
        );

    }
    public static void ver(){
        System.out.println("---------------------------------");
        System.out.println("Lazurite version: " + VERSION());
        System.out.println("---------------------------------");
    }

    public static void clear(){
        System.out.print("\033[H\033[2J");
        System.out.flush();
        check = 0;
    }

    public static void RUN(String input) throws IOException {
        Settings setting = new Settings();
        if(check==1)setting.showMeasurements = true;
        RunProgram(SourceLoader.readSource(input), setting);

    }



    private static void RunProgram(String input, Settings options) throws IOException {
        options.validate();
        final Time measurement = new Time();
        measurement.start("Tokenize time");
        final List<Token> tokens = Lexer.tokenize(input);
        measurement.stop("Tokenize time");
        if (options.showTokens) {
            final int tokensCount = tokens.size();
            for (int i = 0; i < tokensCount; i++) {
                System.out.println(i + " " + tokens.get(i));
            }
        }

        measurement.start("Parse time");
        final Parser parser = new Parser(tokens);
        final Statement parsedProgram = parser.parse();
        measurement.stop("Parse time");
        if (options.showAst) {
            System.out.println(parsedProgram.toString());
        }
        if (parser.getParseErrors().hasErrors()) {
            System.out.println(parser.getParseErrors());
            return;
        }
        if (options.lintMode) {
            Linter.lint(parsedProgram);
            return;
        }
        final Statement program;
        if (options.optimizationLevel > 0) {
            measurement.start("Optimization time");
            program = Optimizer.optimize(parsedProgram, options.optimizationLevel, options.showAst);
            measurement.stop("Optimization time");
            if (options.showAst) {
                System.out.println(program.toString());
            }
        } else {
            program = parsedProgram;
        }
        program.accept(new FunctionAdder());
        try {
            measurement.start("Execution time");
            program.execute();
        } catch (Stop ex) {
            /**/
        } catch (Exception ex) {
            Console.handleException(Thread.currentThread(), ex);
        } finally {
            if (options.showMeasurements) {
                measurement.stop("Execution time");
                System.out.println("\n\n======================");
                System.out.println(measurement.summary(TimeUnit.MILLISECONDS, true));
            }
        }

    }

    private static class Settings {
        boolean showTokens, showAst, showMeasurements;
        boolean lintMode;
        boolean beautifyMode;
        int optimizationLevel;

        Settings() {
            showTokens = false;
            showAst = false;
            showMeasurements = false;
            lintMode = false;
            beautifyMode = false;
            optimizationLevel = 0;
        }

        void validate() {
            if (lintMode) {
                showTokens = false;
                showAst = false;
                showMeasurements = false;
                optimizationLevel = 0;
            }
        }
    }

}
