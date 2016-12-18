package dz1.utils;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ruav on 16.12.16.
 */
public class Validator {
    static public boolean validate(String str){
        Pattern pattern = Pattern.compile("[a-zA-Z]+");
        Matcher matcher = pattern.matcher(str);
        return matcher.matches();
    }
}
