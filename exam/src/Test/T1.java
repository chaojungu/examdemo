package Test;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class T1 {
	public static void main(String[] args) {
		try {
			FileInputStream in=new FileInputStream("C:/Users/Administrator/Desktop/s2笔试/唐伟剑_9.txt");
			InputStreamReader reader=new InputStreamReader(in, "gbk");
			BufferedReader breader=new BufferedReader(reader);
			
			String line="";
			while((line =breader.readLine())!= null){
				System.out.println(line);
			}
			
			breader.close();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
