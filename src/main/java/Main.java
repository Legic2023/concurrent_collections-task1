import java.util.Objects;
import java.util.Optional;
import java.util.Random;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;

public class Main {
    final static int TEXTS_LENGTH = 100_000;
    final static int NUMBER_OF_TEXTS = 10_000;
    final static int MAX_QUEUE_SIZE = 100;
    final static String TEXTS_LETTERS = "abc";
    static BlockingQueue<String> texts1 = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    static BlockingQueue<String> texts2 = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);
    static BlockingQueue<String> texts3 = new ArrayBlockingQueue<>(MAX_QUEUE_SIZE);

    public static void main(String[] args) {
        // поток для заполнения очередей
        new Thread(() -> {
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                try {
                    texts1.put(generateText(TEXTS_LETTERS, TEXTS_LENGTH));
                    texts2.put(generateText(TEXTS_LETTERS, TEXTS_LENGTH));
                    texts3.put(generateText(TEXTS_LETTERS, TEXTS_LENGTH));
                } catch (InterruptedException e) {
                    return;
                }
            }
        }).start();

        // поток для подсчета строк с наиболее повторяющимся символом "a"
        threadStarter(texts1, 'a');

        // поток для подсчета строк с наиболее повторяющимся символом "b"
        threadStarter(texts2, 'b');

        // поток для подсчета строк с наиболее повторяющимся символом "c"
        threadStarter(texts3, 'c');

    }

    // функция-генератор текста
    public static String generateText(String letters, int length) {
        Random random = new Random();
        StringBuilder text = new StringBuilder();
        for (int i = 0; i < length; i++) {
            text.append(letters.charAt(random.nextInt(letters.length())));
        }
        return text.toString();
    }

    // функция-анализатор текста на наиболее повторяющийся символ
    public static char charMaxRepeat(String text, String letter) {

        // Map для хранения значений вычислений (кол. символов "a", "b", "c" в анализируемой строке)
        ConcurrentHashMap<Character, Integer> map = new ConcurrentHashMap<>();
        for (int i = 0; i < text.length(); i++) {
            for (int j = 0; j < letter.length(); j++)
                if (text.charAt(i) == letter.charAt(j)) {
                    if (map.get(letter.charAt(j)) == null) {
                        map.put(letter.charAt(j), 1);
                    } else {
                        map.put(letter.charAt(j), map.get(letter.charAt(j)) + 1);
                    }
                }
        }
        // находим max значение
        Optional<Integer> maxValue = map.values().stream()
                .max(Integer::compare);

        // находим ключ для max значения
        char result = 0;
        for (char key : map.keySet()) {
            if (Objects.equals(map.get(key), maxValue.get())) {
                result = key;
            }
        }
        return result;
    }

    // метод для запуска потоков
    public static void threadStarter(BlockingQueue<String> texts, char ch) {
        new Thread(() -> {
            int countA = 0;
            for (int i = 0; i < NUMBER_OF_TEXTS; i++) {
                try {
                    if (charMaxRepeat(texts.take(), TEXTS_LETTERS) == ch) {
                        countA++;
                    }
                } catch (InterruptedException e) {
                    return;
                }
            }
            System.out.printf("Число строк с макс. кол-вом \"%c\": %s%n", ch, countA);
        }).start();
    }

}
