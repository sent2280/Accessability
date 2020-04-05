package accpoc.sample;

import java.io.File;
import com.google.common.io.CharStreams;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Date;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.util.IOUtils;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

public class App 
{
	 public static Process process = null;
    public static void main( String[] args ) throws IOException, InterruptedException
    {
    	String pa11yBasePath = "";
    	String outputPath =""; 
    	String excelPath ="";
    	String pa11yPath="";
    	 
    	
    	String pathArr[] = System.getenv("Path").split(";");
    	
    	for (String path : pathArr) {
    		if(path.endsWith("AppData\\Roaming\\npm"))
    		{
    			pa11yBasePath  = path;
    			break;
    		}
    		
		}
    	
    	if(pa11yBasePath.equals("")) {
    		System.out.println("Inside pa11y path null");
    		
    		try {
    		 pa11yPath = args[2];
    		}
    		catch(Exception e) {
    			System.out.println("Please provide 3rd argument");
    		}
    	}
    	else {
    		 pa11yPath = pa11yBasePath + "\\" + "pa11y.cmd";	
    	}
    	
    	
    	if((args.length < 2 || args.length > 3) ) {
    		System.err.println("Please provide valid arguments like excel path, report output directory and optional pa11y path ..\n");
    		System.out.println("3rd argument is required only if pa11y is installed other than default path \n");
    		System.out.println("For Example: \n" +
    		"java -jar <jarfile> <Excel path> <Output directory> <pa11y.cmd (Optional) >\n" +
    		"java -jar Test.jar D:/TestFile/TestData.xlsx C:/Desktop/Output D:/Sample/Path/npm/pally.cmd \n");
    		System.exit(2);
    	}
    	else {
    		excelPath = args[0];
    		outputPath = args[1];
    	}
    	
    	
    	File excelFile = new File(excelPath);
    	FileInputStream fis = new FileInputStream(excelFile);
    	
    	XSSFWorkbook workbook = new XSSFWorkbook(fis);
    	XSSFSheet sheet = workbook.getSheetAt(0);
    	
    	for (int rowIndex=0; rowIndex <= sheet.getLastRowNum(); rowIndex++) {
    		String cmd;
    		String secondCell="";
    		Row row = sheet.getRow(rowIndex);
    		//Skip the Headers
    		if(rowIndex == 0) {
    			System.out.println("SKIPPING THE HEADERS...");
    		}
    		else {
    			String firstCell = row.getCell(0).getStringCellValue();
    		
    		try {
    			secondCell = row.getCell(1).getStringCellValue();
    		}
    		catch(Exception e) {
    			//System.out.println("Report name configure in excel is blank");
    		}
    		
    		File directory = new File(outputPath);
    		if(!directory.exists()) {
    			System.err.println("The Output directory does not exist, Please provide the valid path and run again");
    			System.exit(2);
    			//directory.mkdir();
    		}
    		
    		if(row.getCell(1) == null || row.getCell(1).getCellType() == row.getCell(1).CELL_TYPE_BLANK){
    			String defaultName = defaultReportName();
    			System.out.println("Report name is not provided so storing report with default name...");
    			cmd = pa11yPath + " " + "--reporter html" + " " + firstCell + " " + ">" + " " + outputPath + "/" + defaultName;
    		}
    		else if( !secondCell.contains(".html")) {
    			cmd =  pa11yPath + " " + "--reporter html" + " " + firstCell + " " + ">" + " " + outputPath + "/" + secondCell + ".html";
    		}
    		else {
    			cmd = pa11yPath + " " + "--reporter html" + " " + firstCell + " " + ">" + " " + outputPath + "/" + secondCell;
    		}
    		
    		System.out.println("Started accessability for URL " + firstCell);
    		
    		int actualStatus = executeCMD(cmd);
    		//System.out.println("cmd = " + cmd);
    		// System.out.println("actualStatus = " + actualStatus);
    		if(actualStatus == 2) {
    			System.out.printf("Accessability report generated successfully, for %s !!!... Please checkout the output directory for reports \n ", 
    					firstCell);
    		}
    		else {
    			System.err.printf("Accessability report generation failed, for %s !!!... Please check the below error message \n ", 
    					firstCell);
    			System.err.println(" Error msg = " + CharStreams.toString(new InputStreamReader(process.getErrorStream())));
    		}
    			
    		}
    		
    	}
    	
    	workbook.close();
    	fis.close();
    }
    
    private static int executeCMD(String command) throws InterruptedException {
    
    	int exitValue =1;
    //	boolean processStatus = false;
    	try {
    		 process = Runtime.getRuntime().exec(command);
    		 process.waitFor(60000,TimeUnit.SECONDS);
    		 exitValue = process.exitValue();
    	//	 System.out.println("exitValue = " + exitValue);
    	}catch(IOException e) {
    		e.printStackTrace();
    	}
    	
    	return exitValue;
    }
    
    private static String defaultReportName() {
    	Date now =  new Date();
    	return now.getTime() + "report.html";
    }
}
