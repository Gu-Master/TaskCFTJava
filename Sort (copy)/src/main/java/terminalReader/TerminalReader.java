package terminalReader;

import java.io.File;
import java.util.ArrayList;

public class TerminalReader {
    private String[] args; // Хранит все аргументы которые переданны в терминал
    private String[] flags = new String[2]; // Хранит флаги
    private ArrayList<File> files = new ArrayList<>(); // Хранит все имена переданных нам файлов для сортировки
    private File out; // Хранит файл для вывода
    private String dir = "files";
    private boolean isCorret = true;
    private int ind = 0; // Так как команды последовательны, то лучше сделать одну переменную для учета

    public boolean isCorret() {
        return isCorret;
    }

    public TerminalReader(String[] args) {
        this.args = args;
        this.flags = setFlags();
        if (isCorret) {
            this.out = setOut();
            this.files = setFiles();
        }
    }

    public String[] getFlags() {
        return flags;
    }

    public ArrayList<File> getFiles() {
        return files;
    }

    public File getOut() {
        return out;
    }

    public String[] setFlags() {
        String kindOfSorting = "a"; // По умолчанию вид сортировки по возрастанию
        String dataType = "i"; // Для удобства по умолчанию вид i и дальше я это уточню

        for (int i = 0; i < args.length; i++) {
            if (args[i].equals("-a") || args[i].equals("-d")) {
                if (args[i].equals("-d")) {
                    kindOfSorting = "d";
                }
                ind++;
            }
            if (args[i].equals("-s") || args[i].equals("-i")) {  // Проверка типа данных
                if (args[i].equals("-s")) {
                    dataType = "s";
                }
                ind++;
            }
        }
        if(!dataType.equals("i") && !dataType.equals("s")){
            System.out.println("Не предоставлен ключевой тип данных");
            isCorret = false;
        }
        flags[0] = dataType;
        flags[1] = kindOfSorting;

        return flags;
    }


    // Метод для создания файла out.txt
    // Метод для создания файла out.txt !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    public File setOut() {
        out = new File(dir, args[ind]);
        ind++;
        fileCheck(out);
        if (!out.canWrite()) {
            System.out.println("В файл нельзя ничего записать =(");
            isCorret = false;
        }
        return out;
    }

    public ArrayList<File> setFiles() { // Открытие переданных файлов
        for (; ind < args.length; ind++) {
            File file = new File(dir, args[ind]);
            if (file.isFile()) {
                files.add(file);
            } else {
                System.out.println("Файл по имени " + file.getName() + " не найден");
                System.out.println("Убедитесь что он создан в директории Sort в папке files ");
            }
        }
        if (files.size() < 1) {
            System.out.println("Ни один переданный файл не найден и программе нечего сортировать =)");
        }
        return files;
    }

    public void fileCheck(File file) {
        if (!file.isFile()) {
            System.out.println("Не найден файл - " + file.getName());
            System.out.println("Убедитесь что он создан в директории Sort в папке files ");
            isCorret = false;
        }
    }
}
