# disassembler
Решение лабораторной работы №4 "ISA" (Архитектура ЭВМ, 1 курс, КТ ИТМО)
## Описание условия
* Дан elf-файл 
* Используется ISA RISC-V, а именно наборы RV32I, RV32M
* Нужно написать транслятор этого машинного кода в текст программы на ассемблере (дизассемблер)
* Обрабатывать нужно только две секции: text и symtab

С полным условием можно ознакомиться в файле [assignment.pdf](/assignment.pdf)
## Особенности решения
Код был специально написан, чтобы делать минимальные изменения при изменении формата файла
* В пакете [disassembler.elf](src/main/java/disassembler/elf) находятся классы позволяющие гибко парсить байтовые таблицы. Таким образом добавление парсинга какой-нибудь новой секции очень просто. Например таблица целых чисел
  | Поле | Длина в байтах |
  | ---- | -------------- |
  | first | 10 |
  | second | 20 |
  | third | 30 |
  
  может быть спарсена таким кодом
  ```java
  Table<Integer> table = new TableStructure<>(bytesToInt)
        .entry(10, "first")
        .entry(20, "second")
        .entry(30, "third")
        .build(iterator);
  ```
  где `bytesToInt` реализует `Function<List<Byte>, Integer>` &mdash; функция перевода списка байт, соответствующих полю, в это поле (`Integer`), а `iterator` &mdash; итератор массива байт таблицы, реализающий `disasembler.util.ByteIterator`

Таким же образом можно парсить не только таблицы чисел, главное, задать функцию, которая будет генерировать нужный тип из списка байт 

* Добавление новой ISA или других наборов команд также просто &mdash; достаточно написать парсер, реализующий интерфейс `disassembler.isa.InstructionParser`, и классы инструкций, являющихся наследниками `disassembler.isa.Instruction`



## Тесты
К большинству компонентов программы написаны [тесты](src/test/java/disassembler). Также есть workflow в Github Actions запускающий их при каждом push-e. 

***

В репозитории лежит решение с улучшениями. С оригинальным решением, сданным на курсе, можно ознакомиться в ветке [ugly](https://github.com/danilkolotov/disassembler/tree/ugly)