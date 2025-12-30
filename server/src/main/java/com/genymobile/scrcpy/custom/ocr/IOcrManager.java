package com.genymobile.scrcpy.custom.ocr;

public interface IOcrManager {

    void extractSoFiles(String baseCacheDir);

    void extractAssetsFiles(String baseCacheDir);

    void loadSoFiles(String baseCacheDir);

    void startOcr();
}
