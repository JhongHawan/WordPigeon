package com.jhonghawan.wordpigeon;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.PrinterStatus;

import java.util.List;

public class BrotherPrint {

    private Printer printer;
    private Bitmap ImageToPrint;

    BrotherPrint() {
        // initialization for print
        printer = new Printer();
    }

    /**
     * Prints the given image to the QL-1100 Printer.
     * @param image The image that should be printed to the card.
     * */
    public void print(Bitmap image) {
        // Specify printer
        PrinterInfo settings = printer.getPrinterInfo();
        settings.printerModel = PrinterInfo.Model.QL_1110NWB;
        settings.port = PrinterInfo.Port.NET;
        settings.ipAddress = "192.168.1.172";

        // Print Settings
        settings.labelNameIndex = LabelInfo.QL1100.W62.ordinal();
        settings.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
        settings.valign = PrinterInfo.VAlign.MIDDLE;
        settings.align = PrinterInfo.Align.CENTER;
        settings.isAutoCut = true;
        settings.numberOfCopies = 1;
        printer.setPrinterInfo(settings);

        // For Bluetooth:
//         printer.setBluetooth(BluetoothAdapter.getDefaultAdapter());
//         settings.port = PrinterInfo.Port.BLUETOOTH;
//         settings.macAddress = "your-printer-bluetooth-address";

        // For USB:
//         settings.port = PrinterInfo.Port.USB;

        ImageToPrint = image;

        // Connect, then print
        new Thread(new Runnable() {
            @Override
            public void run() {
                if (printer.startCommunication()) {
                    PrinterStatus result = printer.printImage(ImageToPrint);
                    if (result.errorCode != PrinterInfo.ErrorCode.ERROR_NONE) {
                        Log.d("TAG", "ERROR - " + result.errorCode);
                    }
                    printer.endCommunication();
                }
            }
        }).start();
    }

    /**
     * Given the list of lines and text settings it converts the strings into a Bitmap image
     * ready to print.
     * @param lines The lines of text to be converted to a Bitmap image.
     * @param textSize Font size for the text on the Bitmap image.
     * @param textColor Text color for the text on the Bitmap image.
     * */
    public Bitmap textAsBitmap(List<String> lines, float textSize, int textColor) {
        Paint paint = new Paint();
        paint.setTextSize(textSize);
        paint.setColor(Color.WHITE);
        paint.setTextAlign(Paint.Align.LEFT);
        float baseline = -paint.ascent();
        // Hard coded to the first line item assuming the text is all the same.
        int width = (int) (paint.measureText(lines.get(0)) + 0.5f);
        int height = (int) (baseline + paint.descent() + 0.5f);
        Bitmap image = Bitmap.createBitmap(width + 500, height + 350, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(image);
        canvas.drawRect(0, 0, width + 500, height + 350, paint);
        paint.setColor(textColor);
        int increment = 0;
        // For every line of input draw it on the canvas and increment the next line.
        for (String line : lines) {
            canvas.drawText(line, 0, baseline + increment, paint);
            increment += 100;
        }
        return image;
    }

}
