import java.util.Arrays;

public class CustomHashMap<K, V> {

    //Внутренний класс для хранения пар ключ-значение
    private static class Entry<K, V> {
        final K key;
        V value;
        Entry<K, V> next;

        Entry(K key, V value, Entry<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    // Начальный размер массива
    private static final int INITIAL_CAPACITY = 16;
    // Коэффициент загрузки
    private static final float LOAD_FACTOR = 0.75f;

    // Массив корзин
    private Entry<K, V>[] buckets;
    // Количество элементов в HashMap
    private int size;

    //Конструктор по умолчанию
    @SuppressWarnings("unchecked")
    public CustomHashMap() {
        buckets = (Entry<K, V>[]) new Entry[INITIAL_CAPACITY];
        size = 0;
    }

    private int getBucketIndex(K key, int bucketLength) {
        if (key == null) {
            return 0; // null ключ всегда идет в первую корзину
        }
        // Используем встроенный hashCode() и применяем маску для получения индекса
        int hash = key.hashCode();
        // Дополнительное перемешивание битов для лучшего распределения
        hash = hash ^ (hash >>> 16);
        return hash & (bucketLength - 1);
    }


    public void put(K key, V value) {
        // Если достигнут коэффициент загрузки, увеличиваем размер
        if (size >= buckets.length * LOAD_FACTOR) {
            resize();
        }

        int bucketIndex = getBucketIndex(key, buckets.length);
        Entry<K, V> current = buckets[bucketIndex];

        // Проверяем, существует ли уже такой ключ в цепочке
        while (current != null) {
            if (keysEqual(key, current.key)) {
                // Ключ найден - обновляем значение
                current.value = value;
                return;
            }
            current = current.next;
        }

        // Ключ не найден - добавляем новую запись в начало цепочки
        Entry<K, V> newEntry = new Entry<>(key, value, buckets[bucketIndex]);
        buckets[bucketIndex] = newEntry;
        size++;
    }

    private boolean keysEqual(K key1, K key2) {
        if (key1 == null && key2 == null) return true;
        if (key1 == null || key2 == null) return false;
        return key1.equals(key2);
    }


    public V get(K key) {
        int bucketIndex = getBucketIndex(key, buckets.length);
        Entry<K, V> current = buckets[bucketIndex];

        // Ищем ключ в цепочке
        while (current != null) {
            if (keysEqual(key, current.key)) {
                return current.value; // Ключ найден
            }
            current = current.next;
        }

        return null; // Ключ не найден
    }


    public V remove(K key) {
        int bucketIndex = getBucketIndex(key, buckets.length);
        Entry<K, V> current = buckets[bucketIndex];
        Entry<K, V> previous = null;

        // Ищем ключ в цепочке
        while (current != null) {
            if (keysEqual(key, current.key)) {
                // Ключ найден - удаляем его из цепочки
                if (previous == null) {
                    // Удаляем первый элемент цепочки
                    buckets[bucketIndex] = current.next;
                } else {
                    // Удаляем элемент из середины/конца цепочки
                    previous.next = current.next;
                }
                size--;
                return current.value;
            }
            previous = current;
            current = current.next;
        }

        return null; // Ключ не найден
    }


    @SuppressWarnings("unchecked")
    private void resize() {
        Entry<K, V>[] oldBuckets = buckets;
        int newCapacity = oldBuckets.length * 2;
        buckets = (Entry<K, V>[]) new Entry[newCapacity];

        // Сохраняем старый размер и сбрасываем счетчик
        int oldSize = size;
        size = 0;

        // Перебираем все старые корзины и перераспределяем элементы
        for (Entry<K, V> oldEntry : oldBuckets) {
            Entry<K, V> current = oldEntry;
            while (current != null) {
                // Вычисляем новый индекс для текущего элемента
                int newBucketIndex = getBucketIndex(current.key, newCapacity);

                // Сохраняем ссылку на следующий элемент перед изменением
                Entry<K, V> next = current.next;

                // Вставляем текущий элемент в начало новой цепочки
                current.next = buckets[newBucketIndex];
                buckets[newBucketIndex] = current;
                size++;

                current = next;
            }
        }

        // Восстанавливаем правильный размер (на случай если были дубликаты)
        size = oldSize;
    }


    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }


    public boolean containsKey(K key) {
        return get(key) != null;
    }


    @SuppressWarnings("unchecked")
    public void clear() {
        Arrays.fill(buckets, null);
        size = 0;
    }

    // Демо
    public static void main(String[] args) {
        CustomHashMap<String, Integer> map = new CustomHashMap<>();

        // Тест put и get
        map.put("one", 1);
        map.put("two", 2);
        map.put("three", 3);

        System.out.println("Get 'one': " + map.get("one")); // 1
        System.out.println("Get 'two': " + map.get("two")); // 2

        // Тест перезапись
        map.put("two", 22);
        System.out.println("Get 'two' after update: " + map.get("two")); // 22

        // Тест remove
        System.out.println("Remove 'three': " + map.remove("three")); // 3
        System.out.println("Get 'three' after remove: " + map.get("three")); // null

        // Тест null ключ
        map.put(null, 0);
        System.out.println("Get null: " + map.get(null)); // 0
        map.put(null, 999);
        System.out.println("Get null after update: " + map.get(null)); // 999

        // Тест коллизии и ресайз
        for (int i = 0; i < 20; i++) {
            map.put("key" + i, i);
        }

        System.out.println("Size after adding 20 elements: " + map.size());
        System.out.println("Get key15: " + map.get("key15")); // 15

        // Тест containsKey
        System.out.println("Contains key 'one': " + map.containsKey("one")); // true
        System.out.println("Contains key 'nonexistent': " + map.containsKey("nonexistent")); // false

        // Тест clear
        map.clear();
        System.out.println("Size after clear: " + map.size()); // 0
        System.out.println("Is empty: " + map.isEmpty()); // true
    }
}