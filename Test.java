import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.*;
public class Test {
    public static void main(String argv[]){
        Scanner sc = new Scanner(System.in);
        System.out.print("Enter: ");
        String n = sc.next();
        updateURL(24, n);
        sc.close();
    }

    public static int updateURL(int id, String path){
        try{
            System.out.println(path);
            File f = new File(path);
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = "";  
            String splitBy = ",";
            String newURL = "";
            String[] url;
            while ((line = br.readLine()) != null){  
                url = line.split(splitBy);
                if(url[0].equals(Integer.toString(id))){
                    newURL = url[1];
                    break;
                }
                url = null;
            }
            System.out.println(newURL);
            return 1;
        }catch(Exception e){
            return 0;
        }
    }
}
