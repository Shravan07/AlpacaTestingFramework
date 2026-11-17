package api.utilities;

import java.io.IOException;
import org.testng.annotations.DataProvider;

public class DataProviders {

    @DataProvider(name = "AlpacaOrderData")
    public String[][] getOrderData() throws IOException {
        String path = System.getProperty("user.dir") + "/testdata/AlpacaOrderData.xlsx";
        XLUtility xl = new XLUtility(path);

        int rownum = xl.getRowCount("Sheet1");
        int colcount = xl.getCellCount("Sheet1", 1);

        String apiData[][] = new String[rownum][colcount];

        for (int i = 1; i <= rownum; i++) {
            for (int j = 0; j < colcount; j++) {
                apiData[i - 1][j] = xl.getCellData("Sheet1", i, j);
            }
        }
        return apiData;
    }
    
    
    @DataProvider(name = "AlpacaOrderReplaceData")
    public String[][] getOrderReplaceData() throws IOException {
        String path = System.getProperty("user.dir") + "/testdata/AlpacaOrderData.xlsx";
        XLUtility xl = new XLUtility(path);

        int rownum = xl.getRowCount("Sheet2");
        int colcount = xl.getCellCount("Sheet2", 1);

        String apiData[][] = new String[rownum][colcount];

        for (int i = 1; i <= rownum; i++) {
            for (int j = 0; j < colcount; j++) {
                apiData[i - 1][j] = xl.getCellData("Sheet2", i, j);
            }
        }
        return apiData;
    }
    
    
}