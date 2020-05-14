package com.jhonghawan.wordpigeon;
import android.bluetooth.BluetoothAdapter;
import android.util.Log;

import com.brother.ptouch.sdk.BLEPrinter;
import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;

import java.util.List;

public class Print {

    public void printResults() {
        final Printer printer = new Printer();
        final PrinterInfo settings = printer.getPrinterInfo();
        settings.printerModel = PrinterInfo.Model.QL_1110NWB;
        settings.port = PrinterInfo.Port.NET;
        settings.ipAddress = "your-printer-ip-address";

        new Thread(new Runnable() {
            @Override
            public void run() {
                if (printer.startCommunication()) {
                    // Put any code to use printer
                    printer.endCommunication();
                }
            }
        }).start();

         // For Bluetooth:
         printer.setBluetooth(BluetoothAdapter.getDefaultAdapter());
         settings.port = PrinterInfo.Port.BLUETOOTH;
         settings.macAddress = "your-printer-bluetooth-address";

        // For USB:
        settings.port = PrinterInfo.Port.USB;
    }

    public void searchWiFiPrinter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Printer printer = new Printer();
                NetPrinter[] printerList = printer.getNetPrinters("QL-1110NWB");
                for (NetPrinter currPrinter: printerList) {
                    Log.d("TAG", String.format("Model: %s, IP Address: %s", currPrinter.modelName, currPrinter.ipAddress));
                }
            }
        }).start();
    }

    public void searchBLEPrinter() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                Printer printer = new Printer();
                List<BLEPrinter> printerList = printer.getBLEPrinters(BluetoothAdapter.getDefaultAdapter(), 30);
                for (BLEPrinter currPrinter: printerList) {
                    Log.d("TAG", "Local Name: " + currPrinter.localName);
                }
            }
        }).start();
    }
}
