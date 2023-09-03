package pakage;

import sort.IntegerSorter;
import sort.StringSorter;
import terminalReader.TerminalReader;

public class Main {
    public static void main(String[] args) {

        if (args.length < 3) {
            System.out.println("Вы ввели мало аргументов. Корректная запись ./build/install/Sort/bin/Sort [-a or -d] (по умолчанию -a) [-s or -i] out.txt file1.txt file2.txt ...");
        } else {
            TerminalReader terminalReader = new TerminalReader(args); // Хранит всю информацию переданную командой в терминале
            if (terminalReader.isCorret()) { // Если все файлы в порядке, то можно приступить к выполнению
                long before = System.currentTimeMillis();
                if (terminalReader.getFlags()[0].equals("i")) {
                    IntegerSorter integerSort = new IntegerSorter(terminalReader); // Сортирует числа
                    integerSort.sort();
                } else {
                    StringSorter stringSort = new StringSorter(terminalReader); // Сортирует строки
                    stringSort.sort();
                }
                long after = System.currentTimeMillis();
                System.out.println("Сортировка вышла за " + (double) ((after - before) / 1000) + " cекунд");
            }
        }

    }
}