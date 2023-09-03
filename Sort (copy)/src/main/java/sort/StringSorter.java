package sort;

import terminalReader.TerminalReader;

import java.io.*;
import java.util.*;

public class StringSorter {
    private BufferedReader[] readers; // Массив для хранения BufferedReader для каждого входного файла
    private BufferedWriter bufferedWriter; // Писатель для записи отсортированных строк в выходной файл
    private boolean[] listIsOpenStream; // Массив флагов, указывающих, открыт ли каждый файл
    private ArrayList<String> currentItemsFromFiles; // Список для хранения строк, считанных из файлов
    private ArrayList<String> previousItemsFromFiles; // Список для временного хранения строк для проверки сортировки
    private int compareSort = 1; // Переменная, определяющая направление сортировки (по умолчанию - по возрастанию)

    private TerminalReader terminalReader; // Объект для чтения входных параметров (файлов, флагов и т.д.)

    public StringSorter(TerminalReader terminalReader) {
        this.terminalReader = terminalReader;
        readers = new BufferedReader[terminalReader.getFiles().size()]; // Создание массива BufferedReader
        if (terminalReader.getFlags()[1].equals("d")) {
            compareSort = -1; // Если флаг 'd' указан, меняем направление сортировки на убывание
        }
    }

    // Метод для сортировки строк из файлов
    public void sort() {
        ArrayList<File> files = terminalReader.getFiles(); // Получение списка входных файлов из TerminalReader
        int numberFiles = files.size();
        currentItemsFromFiles = new ArrayList<>(numberFiles); // Инициализация списка для строк из файлов
        previousItemsFromFiles = new ArrayList<>(numberFiles); // Инициализация списка для временного хранения строк
        listIsOpenStream = new boolean[numberFiles]; // Инициализация массива флагов для открытия файлов

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(terminalReader.getOut())); // Инициализация BufferedWriter для записи отсортированных строк в выходной файл
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            // Открытие каждого входного файла для чтения и сохранение BufferedReader в соответствующий элемент массива
            for (int i = 0; i < numberFiles; i++) {
                BufferedReader reader = new BufferedReader(new FileReader(files.get(i)));
                readers[i] = reader;
                currentItemsFromFiles.add(null); // Добавление null в список строк для каждого файла (пока строки не считаны)
                previousItemsFromFiles.add(null); // Добавление null в список строк для проверки сортировки (пока строки не считаны)
                listIsOpenStream[i] = true; // Установка флага, что файл открыт
            }

            int countIter = 0;
            // Цикл для сортировки строк из файлов
            while (!isAllClose()) {
                for (int i = 0; i < numberFiles; i++) {
                    // Если текущая строка для файла еще не считана и файл открыт
                    if (currentItemsFromFiles.get(i) == null && listIsOpenStream[i]) {
                        String line = readers[i].readLine(); // Чтение строки из текущего файла
                        // Если файл закончился, закрыть его и продолжить считывание из других файлов
                        if (line == null) {
                            closeFile(i);
                            continue;
                        }
                        currentItemsFromFiles.set(i, line); // Сохранение строки в список для текущего файла
                        // Проверка сортировки и закрытие файла, если строка не удовлетворяет условиям сортировки
                        if (isError(i, line)) {
                            closeFile(i);
                        }
                        // Пропустить обработку текущего файла в первой итерации, если он еще не считан
                        if (countIter == 0 && listIsOpenStream[i]) {
                            continue;
                        } else if (listIsOpenStream[i]) {
                            break;
                        }
                    }
                }
                countIter++;
                // Если все файлы закрыты, выход из цикла
                if (isAllClose()) {
                    break;
                }
                // Запись минимальной/максимальной строки в выходной файл и ее удаление из списка строк для каждого файла
                write(currentItemsFromFiles);
            }
            closeBufferedWriter(); // Закрытие BufferedWriter после окончания сортировки
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для проверки, удовлетворяет ли строка условиям сортировки
    public boolean isError(int ind, String string) {
        if (previousItemsFromFiles.get(ind) != null) {
            // Проверка, удовлетворяет ли строка условиям сортировки:
            // 1) При сортировке по возрастанию: текущая строка должна быть больше предыдущей и не содержать пробелы.
            // 2) При сортировке по убыванию: текущая строка должна быть меньше предыдущей и не содержать пробелы.
            if ((compareSort == 1 && string.compareTo(previousItemsFromFiles.get(ind)) < 0) ||
                    (compareSort == -1 && string.compareTo(previousItemsFromFiles.get(ind)) > 0) ||
                    string.contains(" ")) {
                try {
                    String line = readers[ind].readLine(); // Чтение строки из текущего файла
                    // Проверка остальных строк текущего файла, чтобы удостовериться, что они также не удовлетворяют условиям сортировки
                    while (line != null) {
                        if (compareSort == 1) {
                            if (line.compareTo(previousItemsFromFiles.get(ind)) >= 0 && !line.contains(" ")) {
                                currentItemsFromFiles.set(ind, line);
                                previousItemsFromFiles.set(ind, line);
                                return false;
                            }
                        } else {
                            if (line.compareTo(previousItemsFromFiles.get(ind)) <= 0 && !line.contains(" ")) {
                                currentItemsFromFiles.set(ind, line);
                                previousItemsFromFiles.set(ind, line);
                                return false;
                            }
                        }
                        line = readers[ind].readLine(); // Чтение следующей строки для следующей итерации
                    }
                    return true; // Если текущее число меньше числа для проверки, это ошибка для сортировки по возрастанию
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
            }
        }
        previousItemsFromFiles.set(ind, string);
        return false; // Если строка удовлетворяет условиям сортировки, возвращается false
    }

    // Метод для записи минимальной/максимальной строки в выходной файл
    public void write(ArrayList<String> arr) {
        String count;
        if (compareSort == 1) {
            count = findMin(arr); // Поиск минимальной строки для сортировки по возрастанию
        } else {
            count = findMax(arr); // Поиск максимальной строки для сортировки по убыванию
        }
        int ind = arr.indexOf(count); // Получение индекса найденной строки
        try {
            bufferedWriter.write(count); // Запись строки в выходной файл
            bufferedWriter.newLine(); // Переход на новую строку
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        arr.set(ind, null); // Удаление записанной строки из списка строк для каждого файла
    }

    // Метод для проверки, закрыты ли все входные файлы
    public boolean isAllClose() {
        for (int i = 0; i < listIsOpenStream.length; i++) {
            if (listIsOpenStream[i]) {
                return false; // Если хотя бы один файл открыт, возвращается false
            }
        }
        return true; // Если все файлы закрыты, возвращается true
    }

    // Метод для поиска минимальной строки в списке строк
    public String findMin(ArrayList<String> arr) {
        String min = null;
        for (int i = 0; i < arr.size(); i++) {
            String str = arr.get(i);
            if (str != null) {
                if (min == null || str.compareTo(min) < 0) {
                    min = str;
                }
            }
        }
        return min; // Возвращается минимальная строка
    }

    // Метод для поиска максимальной строки в списке строк
    public String findMax(ArrayList<String> arr) {
        String max = null;
        for (int i = 0; i < arr.size(); i++) {
            String str = arr.get(i);
            if (str != null) {
                if (max == null || str.compareTo(max) > 0) {
                    max = str;
                }
            }
        }
        return max; // Возвращается максимальная строка
    }

    // Метод для закрытия файла
    public void closeFile(int i) {
        try {
            readers[i].close(); // Закрытие BufferedReader
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listIsOpenStream[i] = false; // Установка флага, что файл закрыт
        currentItemsFromFiles.set(i, null); // Обнуление списка строк для текущего файла
    }

    // Метод для закрытия BufferedWriter
    public void closeBufferedWriter() {
        try {
            bufferedWriter.close(); // Закрытие BufferedWriter
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
