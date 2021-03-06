package com.jhonghawan.wordpigeon;

import android.annotation.TargetApi;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.os.Build;
import com.brother.ptouch.sdk.BLEPrinter;
import com.brother.ptouch.sdk.CustomPaperInfo;
import com.brother.ptouch.sdk.LabelInfo;
import com.brother.ptouch.sdk.NetPrinter;
import com.brother.ptouch.sdk.Printer;
import com.brother.ptouch.sdk.PrinterInfo;
import com.brother.ptouch.sdk.Unit;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class PrinterManager {
public static enum CONNECTION { BLUETOOTH, WIFI, USB };
private static String[] PRINTERS = new String[] { "QL-820NWB",
        "QL-1110NWB",
        "RJ-4250WB",
        "PJ-763",
        "PJ-763MFi",
        "PJ-773" };

private static String[] ROLLS = new String[] {
        "DK-2251",
        "DK-2205",
        "RD-M01E5",
        "A4", "A4", "A4" };

private static String[] LABELS = new String[] {
        "DK-1201",
        "DK-1247",
        "RD-M03E1",
        "LETTER","LETTER", "LETTER" };

private static PrinterInfo.Model model;
private static PrinterInfo info;
private static Printer printer;

private static String printerModel;
private static String printerMode;
private static CONNECTION printerConn;


private static boolean done = true;
private static boolean toast = true;

private PrinterManager() {
    // No constructor
}

public static Printer getPrinter() {
    return printer;
}

public static String getModel() {
    return printerModel;
}

public static void setModel(String m) {
    printerModel = m;
}

public static String[] getSupportedModels () {
    return PRINTERS;
}

public static CONNECTION getConnection() {
    return printerConn;
}

public static void setConnection(CONNECTION c) {
    printerConn = c;
}

public static CONNECTION[] getSupportedConnections() {
    return CONNECTION.values();
}

public static String[] getLabelRoll() {
    if (printerModel != null) {
        for (int i = 0; i < PRINTERS.length; i++)
            if (PRINTERS[i].equals(printerModel))
                return new String[] { LABELS[i], ROLLS[i]};
    }
    return new String[]{};
}

private static void setRJCustomPaper(boolean hasHeight) {
    info.paperSize = PrinterInfo.PaperSize.CUSTOM;
    info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
    float width = 102.0f;
    float margins = 0f;
    CustomPaperInfo customPaperInfo;
    if (hasHeight) {
        float height = 152.0f;
        customPaperInfo = CustomPaperInfo.newCustomDiaCutPaper(info.printerModel,
                Unit.Mm, width, height, margins, margins, margins, margins, 0);
    } else {
        customPaperInfo = CustomPaperInfo.newCustomRollPaper(info.printerModel,
                Unit.Mm, width, margins, margins, margins);
    }
    info.setCustomPaperInfo(customPaperInfo);

}

public static void loadLabel() {
    printerMode = "label";
    switch (printerModel) {
        case "QL-820NWB":
        case "QL_820NWB":
            info.labelNameIndex = LabelInfo.QL700.W29H90.ordinal();
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            info.isAutoCut = true;
            break;
        case "QL-1110NWB":
        case "QL_1110NWB":
            info.labelNameIndex = LabelInfo.QL1100.W103H164.ordinal();
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            info.isAutoCut = true;
            break;
        case "RJ-4250WB":
        case "RJ_4250WB":
            setRJCustomPaper(true);
            break;
        case "PJ-763":
        case "PJ_763":
        case "PJ-763MFi":
        case "PJ_763MFi":
        case "PJ-773":
        case "PJ_773":
            info.paperSize = PrinterInfo.PaperSize.LETTER;
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            break;

    }
    printer.setPrinterInfo(info);
}

public static void loadRoll() {
    printerMode = "roll";

    switch (printerModel) {
        case "QL-820NWB":
        case "QL_820NWB":
            info.labelNameIndex = LabelInfo.QL700.W62RB.ordinal();
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            info.isAutoCut = true;
            break;
        case "QL-1110NWB":
        case "QL_1110NWB":
            info.labelNameIndex = LabelInfo.QL1100.W62.ordinal();
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            info.isAutoCut = true;
            break;
        case "RJ-4250WB":
        case "RJ_4250WB":
            setRJCustomPaper(false);
            break;
        case "PJ-763":
        case "PJ_763":
        case "PJ-763MFi":
        case "PJ_763MFi":
        case "PJ-773":
        case "PJ_773":
            info.paperSize = PrinterInfo.PaperSize.A4;
            info.printMode = PrinterInfo.PrintMode.FIT_TO_PAGE;
            break;
    }
    printer.setPrinterInfo(info);
}

public static String getMode() {
    return printerMode;
}

public static void setWorkingDirectory(Context context) {
    info.workPath = context.getFilesDir().getAbsolutePath() + "/";
}

public static String dashToLower(String val) {
    return val.replace("-","_");
}
public static String lowerToDash(String val) {
    return val.replace("_","-");
}

public static void findPrinter(String printer, CONNECTION conn) {
    printerModel = printer;
    printerConn = conn;

    model = PrinterInfo.Model.valueOf(dashToLower(printer));
    findPrinter(conn);
}

private static void findPrinter(CONNECTION conn) {
    done = false;
    printer = new Printer();
    info = printer.getPrinterInfo();

    switch(conn) {
        case BLUETOOTH:
            BluetoothAdapter bluetoothAdapter = BluetoothAdapter
                    .getDefaultAdapter();
            if (bluetoothAdapter != null) {
                if (!bluetoothAdapter.isEnabled()) {
                    printer = null; // Not enabled.
                    done = true;
                    return;
                }
            }

            List<BluetoothDevice> pairedDevices = getPairedBluetoothDevice(bluetoothAdapter);
            for (BluetoothDevice device : pairedDevices) {
                toastIt("Direct Bluetooth: " + printerModel + " " + device.getName());
                if (device.getName().contains(printerModel)) {
                    model = PrinterInfo.Model.valueOf(dashToLower(printerModel));
                    printer.setBluetooth(BluetoothAdapter.getDefaultAdapter());
                    info.printerModel = model;
                    info.port = PrinterInfo.Port.BLUETOOTH;
                    info.macAddress = device.getAddress();
                    done = true;
                    return;
                }
            }

            List<BLEPrinter> bleList = printer.getBLEPrinters(BluetoothAdapter.getDefaultAdapter(), 30);
            for (BLEPrinter printer: bleList) {
                toastIt("Direct BLE: " + printerModel + " " + printer.localName);
                if (printer.localName.contains(printerModel)) {
                    model = PrinterInfo.Model.valueOf(dashToLower(printerModel));
                    info.port = PrinterInfo.Port.BLE;
                    info.setLocalName(printer.localName); // Probably wrong.
                    done = true;
                    return;
                }
            }

            for (BluetoothDevice device : pairedDevices) {
                for (int i = 0; i < PRINTERS.length; i++) {
                    toastIt("Fallback Bluetooth: " + PRINTERS[i] + " " + device.getName());
                    if (device.getName().contains((PRINTERS[i]))) {
                        model = PrinterInfo.Model.valueOf(dashToLower(PRINTERS[i]));
                        printer.setBluetooth(BluetoothAdapter.getDefaultAdapter());
                        printerModel = lowerToDash(model.toString());
                        info.printerModel = model;
                        info.port = PrinterInfo.Port.BLUETOOTH;
                        info.macAddress = device.getAddress();
                        done = true;
                        return;
                    }
                }
            }

            for (BLEPrinter printer: bleList) {
                for (int i = 0; i < PRINTERS.length; i++) {
                    toastIt("Fallback BLE: " + PRINTERS[i] + " " + printer.localName);
                    if (printer.localName.contains(PRINTERS[i])) {
                        model = PrinterInfo.Model.valueOf(dashToLower(PRINTERS[i]));
                        printerModel = lowerToDash(model.toString());
                        info.port = PrinterInfo.Port.BLE;
                        info.setLocalName(printer.localName); // Probably wrong.
                        done = true;
                        return;
                    }
                } // Assume the BLE is good enough.
            }

            printer = null; // No BL-based printers.
            done = true;
            return;
        case WIFI:
            String name = "Brother " + printerModel;
            toastIt("Direct WiFi: " + name);
            NetPrinter[] printerList = printer.getNetPrinters(name);
            for (NetPrinter printer: printerList) {
                model = PrinterInfo.Model.valueOf(dashToLower(printer.modelName).split("Brother ")[1]);
                printerModel = lowerToDash(model.toString());
                info.printerModel = model;
                info.port = PrinterInfo.Port.NET;
                info.ipAddress = printer.ipAddress;
                done = true;
                return;
            }
            for (int i = 0; i < PRINTERS.length; i++) {
                name = "Brother " + PRINTERS[i];
                toastIt("Fallback WiFi: " + name);
                printerList = printer.getNetPrinters(name);
                for (NetPrinter printer: printerList) {
                    model = PrinterInfo.Model.valueOf(dashToLower(printer.modelName).split("Brother ")[1]);
                    printerModel = lowerToDash(model.toString());
                    info.printerModel = model;
                    info.port = PrinterInfo.Port.NET;
                    info.ipAddress = printer.ipAddress;
                    done = true;
                    return;
                }
            }
            printer = null; // No Net-based printers.
            done = true;
            return;
        case USB:
            toastIt("USB: YOLO");
            info.port = PrinterInfo.Port.USB; // YOLO. USB-printers?
            done = true;
            return;
        default:
            toastIt("Default Case");
            printer = null; // Error, add nothing.
            done = true;
            return;
    }
}

private static void toastIt(String s) {
    if (toast) {
        System.out.println(s);
    }
}

@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR2)
private static List<BluetoothDevice> getPairedBluetoothDevice(BluetoothAdapter bluetoothAdapter) {
    Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
    if (pairedDevices == null || pairedDevices.size() == 0) {
        return new ArrayList<>();
    }
    ArrayList<BluetoothDevice> devices = new ArrayList<>();
    for (BluetoothDevice device : pairedDevices) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            devices.add(device);
        }
        else {
            if (device.getType() != BluetoothDevice.DEVICE_TYPE_LE) {
                devices.add(device);
            }
        }
    }

    return devices;
}
}
