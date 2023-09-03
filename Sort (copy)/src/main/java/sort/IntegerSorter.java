package sort;

import terminalReader.TerminalReader;

import java.io.*;
import java.util.*;

public class IntegerSorter {
    private BufferedReader[] readers; // Массив для хранения BufferedReader каждого файла
    private BufferedWriter bufferedWriter; // Для записи отсортированных чисел в выходной файл
    private boolean[] listIsOpenStream; // Массив флагов, указывающих, открыт ли каждый файл для чтения
    private ArrayList<Integer> currentItemsFromFiles; // Список для хранения чисел, считанных из файлов
    private ArrayList<Integer> previousItemsFromFiles; // Список для временного хранения чисел для проверки сортировки
    private int compareSort = 1; // Переменная, определяющая направление сортировки (по умолчанию - по возрастанию)

    private TerminalReader terminalReader; // Объект для чтения входных параметров (файлов, флагов, и т.д.)

    public IntegerSorter(TerminalReader terminalReader) {
        this.terminalReader = terminalReader;
        readers = new BufferedReader[terminalReader.getFiles().size()]; // Создание массива BufferedReader для хранения BufferedReader каждого файла
        if (terminalReader.getFlags()[1].equals("d")) {
            compareSort = -1; // Если флаг 'd' указан, меняем направление сортировки на убывание
        }
    }

    // Метод для сортировки чисел из файлов
    public void sort() {
        ArrayList<File> files = terminalReader.getFiles(); // Получение списка входных файлов из TerminalReader
        int numberFiles = files.size(); // Количество файлов
        currentItemsFromFiles = new ArrayList<>(numberFiles); // Инициализация списка для чисел из файлов
        previousItemsFromFiles = new ArrayList<>(numberFiles); // Инициализация списка для временного хранения чисел

        listIsOpenStream = new boolean[numberFiles]; // Инициализация массива флагов для открытия файлов

        try {
            bufferedWriter = new BufferedWriter(new FileWriter(terminalReader.getOut())); // Инициализация BufferedWriter для записи отсортированных чисел в выходной файл
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        try {
            for (int i = 0; i < numberFiles; i++) {
                BufferedReader reader = new BufferedReader(new FileReader(files.get(i))); // Открытие каждого входного файла для чтения
                readers[i] = reader; // Сохранение BufferedReader в соответствующий элемент массива
                currentItemsFromFiles.add(null); // Добавление null в список чисел для каждого файла (пока числа не считаны)
                previousItemsFromFiles.add(null); // Добавление null в список чисел для проверки сортировки (пока числа не считаны)
                listIsOpenStream[i] = true; // Установка флага, что файл открыт
            }

            int countIter = 0;
            // Цикл для сортировки чисел из файлов
            while (!isAllClose()) {
                for (int i = 0; i < numberFiles; i++) {
                    // Если текущее число для файла еще не считано и файл открыт
                    if (currentItemsFromFiles.get(i) == null && listIsOpenStream[i]) {
                        String line = readers[i].readLine(); // Чтение строки из текущего файла
                        // Если файл закончился, закрыть его и продолжить считывание из других файлов
                        if (line == null) {
                            closeFile(i);
                            continue;
                        }
                        int numberLine = 0;
                        try {
                            numberLine = Integer.parseInt(line); // Попытка преобразовать строку в число
                        } catch (NumberFormatException e) {
                            closeFile(i); // Если не удалось преобразовать, закрыть файл и продолжить считывание из других файлов
                        }
                        currentItemsFromFiles.set(i, numberLine);
                        // Проверка сортировки и закрытие файла, если число не удовлетворяет условиям сортировки
                        if (isError(i, numberLine)) {
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
                // Запись минимального/максимального числа в выходной файл и его удаление из списка чисел для каждого файла
                write(currentItemsFromFiles);
            }
            closeBufferedWriter(); // Закрытие BufferedWriter после окончания сортировки
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    // Метод для проверки, удовлетворяет ли число условиям сортировки
    public boolean isError(int ind, int numberLine) {
        if (previousItemsFromFiles.get(ind) != null) {
            // Проверка, удовлетворяет ли число условиям сортировки:
            // 1) При сортировке по возрастанию: текущее число должно быть больше предыдущего.
            // 2) При сортировке по убыванию: текущее число должно быть меньше предыдущего.
            if ((currentItemsFromFiles.get(ind) < previousItemsFromFiles.get(ind) && compareSort == 1) ||
                    (currentItemsFromFiles.get(ind) > previousItemsFromFiles.get(ind) && compareSort == -1)) {
                try {
                    String line = readers[ind].readLine(); // Чтение строки из текущего файла
                    while (line != null) {
                        numberLine = 0;
                        try {
                            numberLine = Integer.parseInt(line); // Попытка преобразовать строку в число
                        } catch (NumberFormatException e) {
                            // Пропустить обработку строки, если не удалось преобразовать в число
                        }
                        if (compareSort == 1) {
                            // Если число из файла удовлетворяет условиям сортировки, обновить текущее число и вернуть false
                            if (numberLine >= previousItemsFromFiles.get(ind)) {
                                currentItemsFromFiles.set(ind, numberLine);
                                previousItemsFromFiles.set(ind, numberLine);
                                return false;
                            }
                        } else {
                            // Если число из файла удовлетворяет условиям сортировки, обновить текущее число и вернуть false
                            if (numberLine <= previousItemsFromFiles.get(ind)) {
                                currentItemsFromFiles.set(ind, numberLine);
                                previousItemsFromFiles.set(ind, numberLine);
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
        previousItemsFromFiles.set(ind, numberLine);
        return false; // Если число удовлетворяет условиям сортировки, возвращается false
    }

    // Метод для записи минимального/максимального числа в выходной файл
    public void write(ArrayList<Integer> arr) {
        int count = (compareSort == 1) ? findMin(arr) : findMax(arr);

        int ind = arr.indexOf(count); // Получение индекса найденного числа
        try {
            bufferedWriter.write(String.valueOf(count)); // Запись числа в выходной файл
            bufferedWriter.newLine(); // Переход на новую строку
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        arr.set(ind, null); // Удаление записанного числа из списка чисел для каждого файла
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

    // Метод для поиска минимального числа в списке чисел
    public int findMin(ArrayList<Integer> arr) {
        Integer min = null;
        for (int i = 0; i < arr.size(); i++) {
            Integer number = arr.get(i);
            if (number != null && (min == null || min > number)) {
                min = number;
            }
        }
        return min; // Возвращается минимальное число
    }

    // Метод для поиска максимального числа в списке чисел
    public int findMax(ArrayList<Integer> arr) {
        Integer max = null;
        for (int i = 0; i < arr.size(); i++) {
            Integer number = arr.get(i);
            if (number != null && (max == null || max < number)) {
                max = number;
            }
        }
        return max; // Возвращается максимальное число
    }

    // Метод для закрытия файла
    public void closeFile(int i) {
        try {
            readers[i].close(); // Закрытие BufferedReader
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        listIsOpenStream[i] = false; // Установка флага, что файл закрыт
        currentItemsFromFiles.set(i, null); // Обнуление списка чисел для текущего файла
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
