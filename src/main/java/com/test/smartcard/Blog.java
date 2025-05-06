package com.test.smartcard;

import com.test.smartcard.exception.CardDataException;

import javax.smartcardio.*;
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
            String aa = bytesToHex(card.getATR().getBytes());
            System.out.printf("  Card ATR: %s%n", aa);

            var channel = card.getBasicChannel();
            int[] command = {0xFF, 0xCA, 0x00, 0x00, 0x00}; // Получение UID команда APDU
            ResponseAPDU answer = channel.transmit(new CommandAPDU(intArrayToByteArray(command)));
            int returnCode = answer.getSW(); // Код возврата APDU команды (0x9000 - нормальное завершение)
            byte[] uid = answer.getData(); // Данные, возвращаемые по APDU команде (без кода возврата)
            System.out.printf("Card UID: %s%n", bytesToHex(uid));

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
