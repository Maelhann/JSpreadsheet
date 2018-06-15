package spreadsheet;

import common.gui.SpreadsheetGUI;

import java.util.Scanner;

public class MainTest {

    private static final int DEFAULT_NUM_ROWS = 5000;
    private static final int DEFAULT_NUM_COLUMNS = 5000;

    public static void main(String[] args) {
        Spreadsheet sheet = new Spreadsheet();
        SpreadsheetGUI guiSheet = new SpreadsheetGUI(sheet, DEFAULT_NUM_ROWS,
                DEFAULT_NUM_COLUMNS);
        Scanner scanner = new Scanner(System.in);
        if (args.length == 2) {
            guiSheet = new SpreadsheetGUI(sheet, scanner.nextInt(), scanner.nextInt());
        }
        guiSheet.start();
    }

}

