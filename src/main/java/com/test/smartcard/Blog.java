package com.test.smartcard;

import com.test.smartcard.exception.CardDataException;

import javax.smartcardio.*;
import java.nio.ByteBuffer;
import java.util.List;
public class Blog {
    public static void main(String[] args) {
        try {
            var factory = TerminalFactory.getDefault();
            var terminals = factory.terminals().list();

            if (terminals.size() == 0) {
                throw new CardDataException("Не найден считыватель");
            }
// get first terminal
            var terminal = terminals.get(0);

            terminal.waitForCardPresent(0);
            Card card = terminal.connect("*");

            System.out.printf("  Card protocol: %s%n", card.getProtocol());
//            System.out.printf("  Card ATR: %s%n", main(card.getATR().getBytes()));
            System.out.printf("  Card ATR: %s%n", bytesToHex(card.getATR().getBytes()));

            var channel = card.getBasicChannel();
            int[] command = {0xFF, 0xCA, 0x00, 0x00, 0x00}; // Получение UID команда APDU
            ResponseAPDU answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            int returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            byte[] uid = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Card UID: %s%n", bytesToHex(uid));
//// Блок экспериментов с Mifare Classic 1K
            // 1.Загрузка ключа 0xFF, 0x82, 0x00, 0x00, 0x06 + 6 байт ключ
            //command = new int[]{0xFF, 0x82, 0x00, 0x00, 0x06, 0xA0, 0xA1, 0xA2, 0xA3, 0xA4, 0xA5}; // Ключ NDEF по умольчанию для блока 0
            command = new int[]{0xFF, 0x82, 0x00, 0x00, 0x06, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF, 0xFF}; // Ключ NDEF по умольчанию для блока 0
            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            byte[] read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Ответ загрузки ключа : %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() )  ) ;
            //   System.out.printf("  Данные загрузки ключа: %s%n", bytesToHex(read16Byte));
            //   System.out.println("");

            // 2. Аутентификация сектора 0
            // APDU команды выглядит так: FF 86 00 00 05 01 00 00 60 00
            // CLA = FF
            // INS = 86, команда General Authenticate
            // P1 = 00, должен стоять ноль
            // P2 = 00, должен стоять ноль
            // Lc = 05, длина данных для команды, 5 байтов
            // собственно данные, они имеют следующую структуру
            // 01 — версия, на данный момент допускается только 01
            // 00 — старший байт номера блока
            // 00 — младший байт номера блока, в нашем случае мы хотим прочитать самый первый блок, он с номером 00
            // 60 — тип ключа, который мы хотим использовать для аутентификации, 60 означает Ключ A, 61 — Ключ B.
            // 00 — номер ячейки, в которую был записан ключ в предыдущем вызове Load Keys
            command = new int[]{0xFF, 0x86, 0x00, 0x00, 0x05, 0x01, 0x00, 0x00, 0x61, 0x00}; // Аутентификация блока 0
            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Ответ аутентификация ключа A блок 00 : %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() )  ) ;
            System.out.printf("  Данные аутентификации ключа: %s%n", bytesToHex(read16Byte));
            System.out.println("");

            // 3. Чтение блока 01
            command = new int[]{0xFF, 0xB0, 0x00, 0x03, 0x10}; // Чтение блока 01
            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Ответ чтения блока 01: %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() )  ) ;
            System.out.printf("  Данные чтения блока 01: %s%n", bytesToHex(read16Byte));
            System.out.println("");


            command = new int[]{0xFF, 0x86, 0x00, 0x00, 0x05, 0x01, 0x00, 0x04, 0x61, 0x00}; // Аутентификация блока 4
            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Ответ аутентификация ключа A блок 00 : %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() )  ) ;
            System.out.printf("  Данные аутентификации ключа: %s%n", bytesToHex(read16Byte));
            System.out.println("");

            // 3. Чтение блока 01
            command = new int[]{0xFF, 0xB0, 0x00, 0x07, 0x10}; // Чтение блока 01
            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("  Ответ чтения блока 07: %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() )  ) ;
            System.out.printf("  Данные чтения блока 07: %s%n", bytesToHex(read16Byte));
            System.out.println("");

//// КОНЕЦ: Блок экспериментов с Mifare Classic 1K
//// Блок экспериментов с NTag213
//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x02, 0x30, 0x00}; // Чтение 16 байт начиная с нулевого блока
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            byte[] read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Card Read Block 0 - 3: %s%n", bytesToHex(read16Byte));
//            System.out.println("");
//
//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x02, 0x30, 0x12}; // Чтение 16 байт начиная с 12-го блока
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("Card Read Block 12 - 15: %s%n", bytesToHex(read16Byte));
//
//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x05, 0x1B, 0xAA, 0xBB, 0xCC, 0xDD}; // ввод пароля
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            byte[] pack = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Return code: %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() ) );
//            System.out.printf("  Pack auth: %s%n", bytesToHex(pack));

//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x02, 0x30, 0x12}; // Чтение 16 байт начиная с 12-го блока
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Return code: %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() ) );
//            System.out.printf("  Card Read Block 12 - 15: %s%n", bytesToHex(read16Byte));
//
//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x02, 0x30, 0x12}; // Чтение 16 байт начиная с 12-го блока
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Return code: %s%n", bytesToHex(ByteBuffer.allocate(4).putInt(returnCode).array() ) );
//            System.out.printf("  Card Read Block 12 - 15: %s%n", bytesToHex(read16Byte));
//
//            command = new int[]{0xFF, 0x00, 0x00, 0x00, 0x02, 0x30, 0x00}; // Чтение 16 байт начиная с нулевого блока
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            read16Byte = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Card Read Block 0 - 3: %s%n", bytesToHex(read16Byte));
//
//            command = new int[]{0xFF, 0xCA, 0x00, 0x00, 0x00}; // Получение UID команда APDU
//            answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
//            returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
//            uid = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
//            System.out.printf("  Card UID: %s%n", bytesToHex(uid));
////КОНЕЦ: Блок экспериментов с NTag213
            card.disconnect(true);

        } catch(CardException e)
        {
            System.out.println("Problem: " + e.toString());
        }
    }
    //  Card ATR: 3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 36 00 00 00 00 5D - карта тройка
    //  Card ATR: 3B 8B 80 01 00 31 C0 64 08 44 03 77 00 90 00 37 - банковская карта
    //  Card ATR: 3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 01 00 00 00 00 6A - "пустая" карта Mifare classic 1 K
    //  Card ATR: 3B 8F 80 01 80 4F 0C A0 00 00 03 06 03 00 03 00 00 00 00 68 - карта на NXP NTag213
    private static final char[] HEX_ARRAY = "0123456789ABCDEF".toCharArray();
    public static String bytesToHex(byte[] bytes) {
        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = HEX_ARRAY[v >>> 4];
            hexChars[j * 2 + 1] = HEX_ARRAY[v & 0x0F];
        }
        String s = new String(hexChars);
        s = s.replaceAll("..", "$0 ");
        return s;
    }
    public static byte[] intArrayToByteArray(int[] intArray) {
        byte[] byteArray = new byte[intArray.length];
        for (int j = 0; j < intArray.length; j++) {
            byteArray[j] = (byte) intArray[j];
        }
        return byteArray;
    }
}
